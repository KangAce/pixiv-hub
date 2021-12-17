package ink.kangaroo.pixivhub.cache;

import cn.hutool.db.PageResult;
import ink.kangaroo.pixivhub.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * In-memory cache store.
 *
 * @author kang
 */
@Slf4j
public class InMemoryCacheStore extends AbstractStringCacheStore {
    /**
     * Cleaner schedule period. (ms)
     */
    private final static long PERIOD = 60 * 1000;

    /**
     * Cache container.
     */
    private final static ConcurrentHashMap<String, CacheWrapper<String>> CACHE_CONTAINER = new ConcurrentHashMap<>();

    private final Timer timer;

    /**
     * Lock.
     */
    private final Lock lock = new ReentrantLock();

    public InMemoryCacheStore() {
        // Run a cache store cleaner
        timer = new Timer();
        timer.scheduleAtFixedRate(new CacheExpiryCleaner(), 0, PERIOD);
    }

    @Override
    Optional<CacheWrapper<String>> getInternal(String key) {
        Assert.hasText(key, "Cache key must not be blank");

        return Optional.ofNullable(CACHE_CONTAINER.get(key));
    }

    @Override
    void putInternal(String key, CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");

        // Put the cache wrapper
        CacheWrapper<String> putCacheWrapper = CACHE_CONTAINER.put(key, cacheWrapper);

        log.debug("Put [{}] cache result: [{}], original cache wrapper: [{}]", key, putCacheWrapper, cacheWrapper);
    }

    @Override
    Boolean putInternalIfAbsent(String key, CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");

        log.debug("Preparing to put key: [{}], value: [{}]", key, cacheWrapper);

        lock.lock();
        try {
            // Get the value before
            Optional<String> valueOptional = get(key);

            if (valueOptional.isPresent()) {
                log.warn("Failed to put the cache, because the key: [{}] has been present already", key);
                return false;
            }

            // Put the cache wrapper
            putInternal(key, cacheWrapper);
            log.debug("Put successfully");
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void delete(String key) {
        Assert.hasText(key, "Cache key must not be blank");

        CACHE_CONTAINER.remove(key);
        log.debug("Removed key: [{}]", key);
    }

    @PreDestroy
    public void preDestroy() {
        log.debug("Cancelling all timer tasks");
        timer.cancel();
        clear();
    }

    private void clear() {
        CACHE_CONTAINER.clear();
    }

    /**
     * Cache cleaner.
     *
     * @author kang
     * @date 03/28/19
     */
    private class CacheExpiryCleaner extends TimerTask {
        //TODO
        @Override
        public void run() {
            CACHE_CONTAINER.keySet().forEach(key -> {
                if (!InMemoryCacheStore.this.get(key).isPresent()) {
                    log.debug("Deleted the cache: [{}] for expiration", key);
                }
            });
        }
    }

    @Override
    public PageResult<String> findKeysForPage(String patternKey, Pageable pageable) {
        Enumeration<String> keys = CACHE_CONTAINER.keys();
        List<String> result = new PageResult<>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (Pattern.matches(patternKey, key)) {
                result.add(key);
            }
        }
        PageResult<String> pageResult = ListUtil.pagination(result, pageable.getPageNumber(), pageable.getPageSize());

        return pageResult;
    }
}
