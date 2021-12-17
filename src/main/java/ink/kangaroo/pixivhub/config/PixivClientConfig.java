package ink.kangaroo.pixivhub.config;

import ink.kangaroo.pixiv.sdk.PixivClient;
import ink.kangaroo.pixiv.sdk.config.PixivProperties;
import ink.kangaroo.pixivhub.cache.AbstractStringCacheStore;
import ink.kangaroo.pixivhub.cache.InMemoryCacheStore;
import ink.kangaroo.pixivhub.cache.LevelCacheStore;
import ink.kangaroo.pixivhub.cache.RedisCacheStore;
import ink.kangaroo.pixivhub.utils.JDBCUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
public class PixivClientConfig {
    @Autowired
    TribeProperties tribeProperties;

    @Bean("pixivClient")
    PixivClient getPixivClient() {
        PixivClient pixivClient = null;
        PixivProperties pixivProperties = new PixivProperties();
//        JDBCUtils.update()
        pixivProperties.setCookie(tribeProperties.getCookie());
        log.info("tribeProperties.getCookie() -> {}", tribeProperties.getCookie());
//        pixivProperties.setProxyIp("127.0.0.1");
//        pixivProperties.setProxyPort(1080);
        try {
            pixivClient = PixivClient.init(pixivProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pixivClient;
    }

    /**
     * 设置缓存机制
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public AbstractStringCacheStore stringCacheStore() {
        AbstractStringCacheStore stringCacheStore;
        switch (tribeProperties.getCache()) {
            case "level":
                stringCacheStore = new LevelCacheStore();
                break;
            case "redis":
                stringCacheStore = new RedisCacheStore(this.tribeProperties);
                break;
            case "memory":
            default:
                //memory or default
                stringCacheStore = new InMemoryCacheStore();
                break;

        }
        log.info("Tribe cache store load impl : [{}]", stringCacheStore.getClass());
        return stringCacheStore;

    }
}
