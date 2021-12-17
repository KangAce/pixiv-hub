package ink.kangaroo.pixivhub.repository;

import ink.kangaroo.pixivhub.model.User;
import ink.kangaroo.pixivhub.model.UserAuth;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * User repository.
 *
 * @author kang
 */
public interface UserRepository extends BaseRepository<User, Long> {

    /**
     * Gets user by username.
     * 通过email获取用户
     *
     * @param username username must not be blank
     * @return an optional user
     */
    @NonNull
    Optional<User> findByUsername(@NonNull String username);

    /**
     * Gets user by email.
     * 通过用户名获取用户
     *
     * @param email email must not be blank
     * @return an optional user
     */
    @NonNull
    Optional<User> findByEmail(@NonNull String email);

    /**
     * 根据用户名、邮箱、手机号查询用户
     *
     * @param username 用户名
     * @param email    邮箱
     * @param phone    手机号
     * @return 用户信息
     */
    Optional<User> findByUsernameOrEmailOrPhone(String username, String email, String phone);

    /**
     * 根据用户名列表查询用户列表
     *
     * @param usernameList 用户名列表
     * @return 用户列表
     */
    List<User> findByUsernameIn(List<String> usernameList);

    Optional<User> findByPhone(@NonNull String phone);
    Optional<User> findByUserId(@NonNull String userId);

    User findByUserAuths(@NonNull UserAuth userAuth);
}
