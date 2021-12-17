package ink.kangaroo.pixivhub.model.enums;

/**
 * Option Type.
 *
 * @date 2019-12-02
 */
public enum OptionType implements ValueEnum<Integer> {

    /**
     * internal option
     */
    INTERNAL(0),

    /**
     * custom option
     */
    CUSTOM(1);

    private final Integer value;

    OptionType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
