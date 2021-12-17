package ink.kangaroo.pixivhub.model.enums;

/**
 * Post Permalink type enum.
 *
 * @date 2020-01-07
 */
public enum PostPermalinkType implements ValueEnum<Integer> {

    /**
     * /archives/${slug}
     */
    DEFAULT(0),

    /**
     * /1970/01/01/${slug}
     */
    DATE(1),

    /**
     * /1970/01/${slug}
     */
    DAY(2),

    /**
     * /?p=${id}
     */
    ID(3),

    /**
     * /1970/${slug}
     */
    YEAR(4);

    private final Integer value;

    PostPermalinkType(Integer value) {
        this.value = value;
    }


    @Override
    public Integer getValue() {
        return value;
    }
}
