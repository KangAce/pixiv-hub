package ink.kangaroo.pixivhub.service;

import ink.kangaroo.pixiv.sdk.PixivClient;
import ink.kangaroo.pixiv.sdk.model.artist.GetPixivArtistParam;
import ink.kangaroo.pixiv.sdk.model.artist.PixivArtistResult;
import ink.kangaroo.pixiv.sdk.model.discovery.DiscoveryPixivParam;
import ink.kangaroo.pixiv.sdk.model.discovery.DiscoveryResult;
import ink.kangaroo.pixiv.sdk.model.discovery.Illust;
import ink.kangaroo.pixiv.sdk.model.discovery.Thumbnails;
import ink.kangaroo.pixiv.sdk.model.illust.*;
import ink.kangaroo.pixiv.sdk.model.rank.PixivRankContent;
import ink.kangaroo.pixiv.sdk.model.rank.PixivRankMode;
import ink.kangaroo.pixiv.sdk.model.rank.param.GetPixivRankParam;
import ink.kangaroo.pixiv.sdk.model.rank.result.PixivRankContentResult;
import ink.kangaroo.pixiv.sdk.model.rank.result.PixivRankResult;
import ink.kangaroo.pixivhub.cache.CacheStore;
import ink.kangaroo.pixivhub.cache.lock.CacheLock;
import ink.kangaroo.pixivhub.cache.lock.CacheParam;
import ink.kangaroo.pixivhub.model.LikeIllust;
import ink.kangaroo.pixivhub.model.User;
import ink.kangaroo.pixivhub.repository.LikeIllustRepository;
import ink.kangaroo.pixivhub.utils.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * https://www.pixiv.net/ajax/top/manga?mode=all&lang=zh 推荐作品
 * https://www.pixiv.net/ajax/top/illust?mode=all&lang=zh
 */
@Slf4j
@Component
public class PixivServiceImpl implements PixivService {

    private final PixivClient pixivClient;
    private final CacheStore cacheStore;
    private final LikeIllustRepository likeIllustRepository;
    String pixivTargetUrl = "https://i.pximg.net/";
    String pixivForwardUrl = "https://pixiv.imore.fun/";

    public PixivServiceImpl(PixivClient pixivClient, CacheStore cacheStore, LikeIllustRepository likeIllustRepository) {
        this.pixivClient = pixivClient;
        this.cacheStore = cacheStore;
        this.likeIllustRepository = likeIllustRepository;
    }

    @Override
    @CacheLock(prefix = "getPixivRankResultRealTime", expired = 60 * 60)
    public PixivRankResult getPixivRankResultRealTime(@CacheParam Pageable pageable, @CacheParam String date, @CacheParam String mode, @CacheParam String content) {
        GetPixivRankParam getPixivRankParam = new GetPixivRankParam();
        getPixivRankParam.setMode(PixivRankMode.getByValue(mode));
        getPixivRankParam.setContent(PixivRankContent.getByValue(content));
        getPixivRankParam.setPageNum(pageable.getPageNumber() + 1);
        getPixivRankParam.setDate(date);
        PixivRankResult pixivRank = pixivClient.getPixivRank(getPixivRankParam);
        PixivService bean = ApplicationContextUtils.getBean(PixivService.class);
        if (pixivRank != null && !CollectionUtils.isEmpty(pixivRank.getContents())) {
            List<PixivRankContentResult> pixivRankContentResults = pixivRank.getContents().stream().peek(e -> {
                String url = e.getUrl();
                e.setUrl(url.replace(pixivTargetUrl, pixivForwardUrl));
                e.setProfileImg(e.getProfileImg().replace(pixivTargetUrl, pixivForwardUrl));
            }).filter(e -> {

                if (e.getIllustPageCount() > 2) {
//                if (pixivIllustDetail.getSanityLevel() <= 2) {
                    log.info("IllustId -> {} ,IllustPageCount -> {} ", e.getIllustId(), e.getIllustPageCount());
                    return false;
                }
                PixivIllustDetailResult pixivIllustDetail = bean.getPixivIllustDetail(e.getIllustId());
                if (pixivIllustDetail.getSanityLevel() > 2) {
//                if (pixivIllustDetail.getSanityLevel() <= 2) {
                    log.info("SanityLevel -> {} ,restrict -> {} ,xRestrict -> {} ,", pixivIllustDetail.getSanityLevel(), pixivIllustDetail.getRestrict(), pixivIllustDetail.getXRestrict());
                    return false;
                }
                return true;
            }).collect(Collectors.toList());
            pixivRank.setContents(pixivRankContentResults);
        } else {
            log.info(" PixivRankResult -> Empty Result List");
        }
        return pixivRank;
    }

    @Override
    @CacheLock(prefix = "getPixivIllustDetail", expired = 60 * 60 * 24 * 7)
    public PixivIllustDetailResult getPixivIllustDetail(@CacheParam String illustId) {
        if (illustId == null) {
            return null;
        }
        GetPixivIllustDetailParam getPixivIllustDetailParam = new GetPixivIllustDetailParam();
        getPixivIllustDetailParam.setIllustId(illustId);

        PixivIllustDetailResult pixivIllustDetail = pixivClient.getPixivIllustDetail(getPixivIllustDetailParam);
        PixivIllustDetailUrls urls = pixivIllustDetail.getUrls();
        if (urls != null) {
            urls.setMini(urls.getMini().replace(pixivTargetUrl, pixivForwardUrl));
            urls.setOriginal(urls.getOriginal().replace(pixivTargetUrl, pixivForwardUrl));
            urls.setRegular(urls.getRegular().replace(pixivTargetUrl, pixivForwardUrl));
            urls.setSmall(urls.getSmall().replace(pixivTargetUrl, pixivForwardUrl));
            urls.setThumb(urls.getThumb().replace(pixivTargetUrl, pixivForwardUrl));
            PixivIllustPageResult pixivIllustPageResult = new PixivIllustPageResult();
            pixivIllustPageResult.setWidth(pixivIllustDetail.getWidth());
            pixivIllustPageResult.setHeight(pixivIllustDetail.getWidth());
            pixivIllustPageResult.setUrls(urls);
            pixivIllustDetail.setPageUrls(List.of(pixivIllustPageResult));
        }
        if (pixivIllustDetail.getPageCount() > 1) {
            PixivService bean = ApplicationContextUtils.getBean(PixivService.class);
            List<PixivIllustPageResult> pixivIllustPage = bean.getPixivIllustPage(illustId);
            pixivIllustDetail.setPageUrls(pixivIllustPage);
        }
//        log.info("urls -> {}", urls);
//        log.info("pixivIllustDetail -> {}", pixivIllustDetail);
        return pixivIllustDetail;
    }

    @Override
    public PixivIllustDetailResult getPixivIllustDetailWithUser(User user,String illustId) {
        PixivService bean = ApplicationContextUtils.getBean(PixivService.class);
        PixivIllustDetailResult pixivIllustDetail = bean.getPixivIllustDetail(illustId);
        Optional<LikeIllust> byUserAndAndIllustId = likeIllustRepository.getByUserAndAndIllustId(user, illustId);
        if (byUserAndAndIllustId.isPresent()){
            pixivIllustDetail.setLiked(true);
        }
        return pixivIllustDetail;
    }

    @Override
    @CacheLock(prefix = "getPixivIllustPage", expired = 60 * 60 * 24 * 7)
    public List<PixivIllustPageResult> getPixivIllustPage(@CacheParam String illustId) {
        if (illustId == null) {
            return null;
        }
        GetPixivIllustPageParam getPixivIllustPageParam = new GetPixivIllustPageParam();
        getPixivIllustPageParam.setIllustId(illustId);
        List<PixivIllustPageResult> pixivIllustPageResults = pixivClient.getPixivIllustPage(getPixivIllustPageParam);
        return pixivIllustPageResults.stream().peek(e -> {
            PixivIllustDetailUrls urls = e.getUrls();
            if (urls != null) {
                String original = urls.getOriginal();
                if (original != null) {
                    urls.setOriginal(original.replace(pixivTargetUrl, pixivForwardUrl));
                }
                String thumb = urls.getThumb();
                if (thumb != null) {
                    urls.setThumb(thumb.replace(pixivTargetUrl, pixivForwardUrl));
                }
                String small = urls.getSmall();
                if (small != null) {
                    urls.setSmall(small.replace(pixivTargetUrl, pixivForwardUrl));
                }
                String regular = urls.getRegular();
                if (regular != null) {
                    urls.setRegular(regular.replace(pixivTargetUrl, pixivForwardUrl));
                }
                String mini = urls.getMini();
                if (mini != null) {
                    urls.setMini(mini.replace(pixivTargetUrl, pixivForwardUrl));
                }
                e.setUrls(urls);
            }
        }).collect(Collectors.toList());
    }

    @Override
//    @CacheLock(prefix = "PixivArtist", expired = 60 * 60 * 24 * 7)
    public PixivArtistResult getPixivArtist(@CacheParam String artistId) {
        if (artistId == null) {
            return null;
        }
        GetPixivArtistParam getPixivArtistParam = new GetPixivArtistParam();
        getPixivArtistParam.setArtistId(artistId);
        PixivArtistResult pixivArtist = pixivClient.getPixivArtist(getPixivArtistParam);
        String image = pixivArtist.getImage();
        String imageBig = pixivArtist.getImageBig();
        if (image != null) {
            pixivArtist.setImage(image.replace(pixivTargetUrl, pixivForwardUrl));
        }
        if (imageBig != null) {
            pixivArtist.setImageBig(imageBig.replace(pixivTargetUrl, pixivForwardUrl));
        }
        return pixivArtist;
    }

    @Override
    public DiscoveryResult discovery(DiscoveryPixivParam discoveryPixivParam) {
        DiscoveryResult discovery = pixivClient.discovery(discoveryPixivParam);
        if (discovery != null) {
            Thumbnails thumbnails = discovery.getThumbnails();

            PixivService bean = ApplicationContextUtils.getBean(PixivService.class);
            List<Illust> collect = thumbnails.getIllust().stream().peek(e -> {
                PixivIllustDetailResult pixivIllustDetail = bean.getPixivIllustDetail(e.getId());
                e.setUrl(pixivIllustDetail.getUrls().getSmall());
                e.setProfileImageUrl(e.getProfileImageUrl().replace(pixivTargetUrl, pixivForwardUrl));
            }).collect(Collectors.toList());
            thumbnails.setIllust(collect);
            List<Object> illustSeries = discovery.getIllustSeries();
            if (!CollectionUtils.isEmpty(illustSeries)) {
//                try {
//                    EmailUtils.sendEmil("26599114@qq.com", "illustSeries 出现数据，请根据数据进行解析。", JSON.toJSONString(illustSeries));
//                } catch (UnsupportedEncodingException | MessagingException e) {
//                    e.printStackTrace();
//                }
            }
            List<Object> requests = discovery.getRequests();
            if (!CollectionUtils.isEmpty(requests)) {
//                try {
//                    EmailUtils.sendEmil("26599114@qq.com", "requests 出现数据，请根据数据进行解析。", JSON.toJSONString(requests));
//                } catch (UnsupportedEncodingException | MessagingException e) {
//                    e.printStackTrace();
//                }
            }
            Map tagTranslation = discovery.getTagTranslation();
            if (!CollectionUtils.isEmpty(tagTranslation)) {
//                try {
//                    log.info("");
//                    EmailUtils.sendEmil("26599114@qq.com", "tagTranslation 出现数据，请根据数据进行解析。", JSON.toJSONString(tagTranslation));
//                } catch (UnsupportedEncodingException | MessagingException e) {
//                    e.printStackTrace();
//                }
            }
        } else {
            log.info(" PixivRankResult -> Empty Result List");
        }
        return discovery;
    }

    @Override
    @Modifying
    @Transactional
    public Boolean likePixiv(User user, String id) {
        Optional<LikeIllust> byUserAndAndIllustId = likeIllustRepository.getByUserAndAndIllustId(user, id);
        if (byUserAndAndIllustId.isPresent()) {
            Integer integer = likeIllustRepository.deleteByUserAndIllustId(user, id);
            if (integer > 0) {
                return false;
            }
        }else {
            LikeIllust likeIllust = new LikeIllust();
            likeIllust.setIllustId(id);
            likeIllust.setUser(user);
            likeIllustRepository.save(likeIllust);
            return true;
        }
        return null;
    }

    @Override
    public List<LikeIllust> likeIllustList(User user) {
        return likeIllustRepository.findByUser(user);
    }

}
