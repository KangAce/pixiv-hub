package ink.kangaroo.pixivhub.service;

import ink.kangaroo.pixivhub.controller.AuthToken;
import ink.kangaroo.pixivhub.model.User;
import ink.kangaroo.pixivhub.model.UserAuth;
import ink.kangaroo.pixivhub.model.enums.MFAType;
import ink.kangaroo.pixivhub.repository.UserAuthRepository;
import ink.kangaroo.pixivhub.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserAuthRepository authRepository;
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthToken loginMaByOpenId(String appid, String openid, String unionid, String sessionKey, String nickName, String avatarUrl) {
        Optional<UserAuth> byOauthId = authRepository.findByOauthId(openid);
        AuthToken authToken = null;
        if (byOauthId.isPresent()) {
            UserAuth userAuth = byOauthId.get();
            String userId = userAuth.getUser().getUserId();
            authToken = new AuthToken();
            authToken.setAccessToken(userId);
            authToken.setRefreshToken(userId);
            authToken.setExpiredIn(0L);
            return authToken;
        } else {
            User user = new User();
            user.setUserId(StringUtils.remove(UUID.randomUUID().toString(), '-'));
            user.setNickname(nickName);
            user.setUsername(UUID.randomUUID().toString());
            user.setAvatar(avatarUrl);
            user.setPassword(UUID.randomUUID().toString());
            user.setMfaType(MFAType.NONE);
            User save = userRepository.save(user);
            UserAuth userAuth = new UserAuth();
            userAuth.setUser(save);
            userAuth.setOauthName(nickName);
            userAuth.setOauthId(openid);
            userAuth.setUnionId(unionid);
            userAuth.setOAuthAccessToken(unionid);
            userAuth.setOAuthRefreshToken(unionid);
            userAuth.setOAuthExpires("0");
            authRepository.save(userAuth);
            authToken = new AuthToken();
            authToken.setAccessToken(save.getUserId());
            authToken.setRefreshToken(save.getUserId());
            authToken.setExpiredIn(0L);
        }

        return authToken;
    }

    @Override
    public User findByToken(String token) {
        Optional<User> byUserId = userRepository.findByUserId(token);
        return byUserId.orElse(null);
    }

}
