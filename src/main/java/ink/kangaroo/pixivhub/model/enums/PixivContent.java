package ink.kangaroo.pixivhub.model.enums;

public enum PixivContent {

    ALL("综合","all"),
    ILLUST("插画","illust"),

    ;

    private String name;
    private String content;
    PixivContent(String name, String content) {
        this.name = name;
        this.content = content;
    }
}
