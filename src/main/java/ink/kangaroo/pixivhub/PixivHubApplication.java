package ink.kangaroo.pixivhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableAsync
@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan
public class PixivHubApplication {

    public static void main(String[] args) {
        //java.lang.IllegalArgumentException:Comparison method violates its general contract!
        //jdk6->jdk7 之后Collections.sort()会出现异常，下边的设定会导致排序效率降低，但是稳定；
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        SpringApplication.run(PixivHubApplication.class, args);
    }

}
