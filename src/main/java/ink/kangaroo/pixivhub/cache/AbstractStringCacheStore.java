package ink.kangaroo.pixivhub.cache;

import cn.hutool.db.PageResult;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * String cache store.
 *
 * @author kang
 */
@Slf4j
public abstract class AbstractStringCacheStore extends AbstractCacheStore<String, String> {
    protected Optional<CacheWrapper<String>> jsonToCacheWrapper(String json) {
        Assert.hasText(json, "json value must not be null");
        CacheWrapper<String> cacheWrapper = null;
        cacheWrapper = JSON.parseObject(json, CacheWrapper.class);
        return Optional.ofNullable(cacheWrapper);
    }

    public <T> void putAny(String key, T value) {
        put(key, JSON.toJSONString(value));
    }

    public <T> void putAny(@NonNull String key, @NonNull T value, long timeout, @NonNull TimeUnit timeUnit) {
        put(key, JSON.toJSONString(value), timeout, timeUnit);
    }

    public <T> Optional<T> getAny(String key, Class<T> type) {
        Assert.notNull(type, "Type must not be null");

        return get(key).map(value -> {
            return JSON.parseObject(value, type);
        });
    }

    @Override
    public PageResult<String> findKeysForPage(String patternKey, Pageable pageable) {
        return null;
    }
}
