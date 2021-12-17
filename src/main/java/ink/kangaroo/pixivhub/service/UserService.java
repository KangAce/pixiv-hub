package ink.kangaroo.pixivhub.service;

import ink.kangaroo.pixivhub.controller.AuthToken;
import ink.kangaroo.pixivhub.model.User;

public interface UserService {


    AuthToken loginMaByOpenId(String appid, String openid, String unionid, String sessionKey, String nickName, String avatarUrl);

    User findByToken(String token);

}
