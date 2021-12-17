package ink.kangaroo.pixivhub.model.result;

import ink.kangaroo.pixiv.sdk.model.illust.PixivIllustDetailUrls;
import lombok.Data;

/**
 * @Classname DyOpenService
 * @Description TODO
 * @Date 2021/11/29 5:39
 * @Created by Kangaroo
 */
@Data
public class LikeIllustResult {

    private String id;

    private PixivIllustDetailUrls url;
}
