package ink.kangaroo.pixivhub.model.enums;

/**
 * @author Kang
 * @date 2020/10/30 17:34
 */
public enum Enable implements ValueEnum<Integer> {

    /**
     * Disable MFA auth.
     */
    DISABLE(0),

    /**
     * Time-based One-time Password (rfc6238).
     * see: https://tools.ietf.org/html/rfc6238
     */
    ENABLE(1);

    private final Integer value;

    Enable(Integer value) {
        this.value = value;
    }

    public static boolean isEnable(Enable enable) {
        return Enable.ENABLE == enable;
    }

    @Override
    public Integer getValue() {
        return value;
    }

}
