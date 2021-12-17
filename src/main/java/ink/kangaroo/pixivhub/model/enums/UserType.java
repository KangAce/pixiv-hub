package ink.kangaroo.pixivhub.model.enums;

/**
 * MFA type.
 *
 * @author xun404
 */
public enum UserType implements ValueEnum<Integer> {

    /**
     * 普通用户
     */
    COMMON(0),

    /**
     * 管理员
     */
    ADMIN(1);

    private final Integer value;

    UserType(Integer value) {
        this.value = value;
    }

    public static boolean isAdmin(UserType userType) {
        return userType != null && UserType.COMMON != userType;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
