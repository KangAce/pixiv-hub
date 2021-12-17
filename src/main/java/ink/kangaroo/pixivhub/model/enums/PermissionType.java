package ink.kangaroo.pixivhub.model.enums;

/**
 * @author Kang
 * @date 2020/10/30 18:05
 */
public enum PermissionType implements ValueEnum<Integer> {

    DEFAULT(1),
    PAGE(1),
    BUTTON(2),
    ;
    private final Integer value;

    PermissionType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
