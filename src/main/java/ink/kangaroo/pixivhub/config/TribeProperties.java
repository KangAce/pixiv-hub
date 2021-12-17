package ink.kangaroo.pixivhub.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.ArrayList;

import static ink.kangaroo.pixivhub.model.support.TribeConst.FILE_SEPARATOR;
import static ink.kangaroo.pixivhub.model.support.TribeConst.USER_HOME;


/**
 * @author Kang  Re-run Spring Boot Configuration Annotation Processor to update generated metadata
 * @date 2020/10/30 8:25
 */
@Data(staticConstructor = "of")
@ConfigurationProperties("tribe")
public class TribeProperties {

    /**
     * cache store impl
     * memory
     * level
     * redis
     */
    private String cache = "";

    private ArrayList<String> cacheRedisNodes = new ArrayList<>();

    private String cacheRedisPassword = "";
    private String cookie = "";

    /**
     * Work directory.
     */
    private String workDir = ensureSuffix("/root/", FILE_SEPARATOR) + ".pixiv" + FILE_SEPARATOR;

    public static String ensureSuffix(@NonNull String string, @NonNull String suffix) {
        Assert.hasText(string, "String must not be blank");
        Assert.hasText(suffix, "Suffix must not be blank");

        return StringUtils.removeEnd(string, suffix) + suffix;
    }
}
