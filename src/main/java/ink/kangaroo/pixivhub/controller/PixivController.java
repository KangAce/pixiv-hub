package ink.kangaroo.pixivhub.controller;


import ink.kangaroo.pixiv.sdk.model.artist.PixivArtistResult;
import ink.kangaroo.pixiv.sdk.model.discovery.DiscoveryPixivParam;
import ink.kangaroo.pixiv.sdk.model.discovery.DiscoveryResult;
import ink.kangaroo.pixiv.sdk.model.discovery.PixivDiscoveryCategory;
import ink.kangaroo.pixiv.sdk.model.illust.PixivIllustDetailResult;
import ink.kangaroo.pixiv.sdk.model.rank.PixivRankContent;
import ink.kangaroo.pixiv.sdk.model.rank.PixivRankMode;
import ink.kangaroo.pixiv.sdk.model.rank.result.PixivRankResult;
import ink.kangaroo.pixivhub.cache.lock.CacheParam;
import ink.kangaroo.pixivhub.model.LikeIllust;
import ink.kangaroo.pixivhub.model.User;
import ink.kangaroo.pixivhub.model.result.LikeIllustResult;
import ink.kangaroo.pixivhub.service.PixivService;
import ink.kangaroo.pixivhub.service.UserService;
import ink.kangaroo.pixivhub.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Slf4j
@RestController
@RequestMapping("/api/pixiv")
public class PixivController {
    private final PixivService pixivService;
    private final UserService userService;

    public PixivController(PixivService pixivService, UserService userService) {
        this.pixivService = pixivService;
        this.userService = userService;
    }

    /**
     * 分页获取排行榜
     *
     * @param pageable
     * @param date
     * @param mode
     * @param content
     * @return
     */
    @GetMapping("/wpList")
//    @CacheLock(prefix = "wpList", expired = 60 * 1000)
    public PixivRankResult wpListPage(
            @PageableDefault(sort = "rankNumber", direction = ASC) @CacheParam Pageable pageable,
            @CacheParam String date,
            @NotNull @CacheParam String mode,
            @NotNull @CacheParam String content
    ) {
        if (StringUtils.isBlank(date)) {
//            date = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, DateUtils.addMinutes(DateUtils.getNowDate(), -(35 * 60 + 25)));

            date = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, DateUtils.addMinutes(DateUtils.getNowDate(), -(36 * 60)));
        }
        if (StringUtils.isBlank(content)) {
            content = PixivRankContent.PIXIV_RANK_ALL.getValue();
        }
        if (StringUtils.isBlank(mode)) {
            mode = PixivRankMode.PIXIV_RANK_DAILY.getValue();
        }
//        log.info("pageable -> {}, date -> {}, mode -> {}, content -> {}", pageable, date, mode, content);
        PixivRankResult pixivRankResultRealTime = pixivService.getPixivRankResultRealTime(pageable, date, mode, content);
        pixivRankResultRealTime.setNext(null);
        return pixivRankResultRealTime;
    }

    /**
     * 插画详情
     *
     * @param illustId
     * @return
     */
    @GetMapping("/illust_detail")
    public PixivIllustDetailResult pixivIllustDetailResult(@RequestHeader("PIXIV-TOKEN") String token, @CacheParam String illustId) {
        User byToken = userService.findByToken(token);
        return pixivService.getPixivIllustDetailWithUser(byToken, illustId);
    }

    /**
     * 获取用户详情
     *
     * @param artistId
     * @return
     */
    @GetMapping("/artist_detail")
    public PixivArtistResult getPixivArtist(@CacheParam String artistId) {
        PixivArtistResult pixivArtist = pixivService.getPixivArtist(artistId);
        log.info("pixivArtist -> {}", pixivArtist);
        return pixivArtist;
    }

    /**
     * 发现
     *
     * @param mode     all
     * @param category discoveryCategory
     * @param limit    20
     * @param lang     zh
     * @return
     */
    @GetMapping("/discovery")
    public DiscoveryResult discovery(@CacheParam String mode, @CacheParam String category, @CacheParam String limit, @CacheParam String lang) {
        DiscoveryPixivParam discoveryPixivParam = new DiscoveryPixivParam();
        discoveryPixivParam.setCategory(PixivDiscoveryCategory.getByValue(category));
        discoveryPixivParam.setLang(lang);
        discoveryPixivParam.setLimit(limit);
        discoveryPixivParam.setMode(mode);
        return pixivService.discovery(discoveryPixivParam);
    }

    /**
     * 喜欢
     *
     * @param id all
     * @return
     */
    @GetMapping("/like")
    public Boolean like(@RequestHeader("PIXIV-TOKEN") String token, String id) {
        User user = userService.findByToken(token);
        return pixivService.likePixiv(user, id);
    }

    /**
     * 发现
     *
     * @param token
     * @return
     */
    @GetMapping("/likeIllustList")
    public List<LikeIllustResult> likeList(@RequestHeader("PIXIV-TOKEN") String token) {
        User user = userService.findByToken(token);
        List<LikeIllust> likeIllusts = pixivService.likeIllustList(user);

        return likeIllusts.stream().map(e -> {
            LikeIllustResult likeIllustResult = new LikeIllustResult();
            likeIllustResult.setId(e.getIllustId());
            PixivIllustDetailResult pixivIllustDetail = pixivService.getPixivIllustDetail(e.getIllustId());
            likeIllustResult.setUrl(pixivIllustDetail.getUrls());
            return likeIllustResult;
        }).collect(Collectors.toList());
    }

}
