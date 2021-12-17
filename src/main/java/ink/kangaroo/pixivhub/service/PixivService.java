package ink.kangaroo.pixivhub.service;

import ink.kangaroo.pixiv.sdk.model.artist.PixivArtistResult;
import ink.kangaroo.pixiv.sdk.model.discovery.DiscoveryPixivParam;
import ink.kangaroo.pixiv.sdk.model.discovery.DiscoveryResult;
import ink.kangaroo.pixiv.sdk.model.illust.PixivIllustDetailResult;
import ink.kangaroo.pixiv.sdk.model.illust.PixivIllustPageResult;
import ink.kangaroo.pixiv.sdk.model.rank.result.PixivRankResult;
import ink.kangaroo.pixivhub.cache.lock.CacheLock;
import ink.kangaroo.pixivhub.cache.lock.CacheParam;
import ink.kangaroo.pixivhub.model.LikeIllust;
import ink.kangaroo.pixivhub.model.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PixivService {

    PixivRankResult getPixivRankResultRealTime(Pageable pageable, String date, String mode, String content);

    PixivIllustDetailResult getPixivIllustDetail(String illustId);

    PixivIllustDetailResult getPixivIllustDetailWithUser(User user, String illustId);

    @CacheLock(prefix = "getPixivIllustPage", expired = 60 * 60 * 24 * 7)
    List<PixivIllustPageResult> getPixivIllustPage(@CacheParam String illustId);

    PixivArtistResult getPixivArtist(String artistId);

    DiscoveryResult discovery(DiscoveryPixivParam discoveryPixivParam);

    Boolean likePixiv(User user, String id);

    List<LikeIllust> likeIllustList(User user);
}
