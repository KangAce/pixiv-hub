package ink.kangaroo.pixivhub.repository;

import ink.kangaroo.pixivhub.model.UserAuth;

import java.util.Optional;

/**
 * User repository.
 *
 * @author kang
 */
public interface UserAuthRepository extends BaseRepository<UserAuth, Long> {
    public Optional<UserAuth> findByOauthId(String oathId);
    public Optional<UserAuth> findByUnionId(String uionId);
}
