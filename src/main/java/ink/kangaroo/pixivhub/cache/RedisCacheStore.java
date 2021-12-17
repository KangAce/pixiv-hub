package ink.kangaroo.pixivhub.cache;

import cn.hutool.db.PageResult;
import com.alibaba.fastjson.JSON;
import ink.kangaroo.pixivhub.config.TribeProperties;
import ink.kangaroo.pixivhub.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.*;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Redis cache store.
 *
 * @author chaos
 */
@Slf4j
public class RedisCacheStore extends AbstractStringCacheStore {

    private volatile static JedisCluster REDIS;

    /**
     * Lock.
     */
    private final Lock lock = new ReentrantLock();

    protected TribeProperties tribeProperties;

    public RedisCacheStore(TribeProperties tribeProperties) {
        this.tribeProperties = tribeProperties;
        initRedis();
    }

    private void initRedis() {
        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxIdle(2);
        cfg.setMaxTotal(30);
        cfg.setMaxWaitMillis(5000);
        Set<HostAndPort> nodes = new HashSet<>();
        for (String hostPort : this.tribeProperties.getCacheRedisNodes()) {
            String[] temp = hostPort.split(":");
            if (temp.length > 0) {
                String host = temp[0];
                int port = 6379;
                if (temp.length > 1) {
                    try {
                        port = Integer.parseInt(temp[1]);
                    } catch (Exception ex) {

                    }
                }
                nodes.add(new HostAndPort(host, port));
            }
        }
        if (nodes.isEmpty()) {
            nodes.add(new HostAndPort("127.0.0.1", 6379));
        }
        REDIS = new JedisCluster(nodes, 5, 20, 3, this.tribeProperties.getCacheRedisPassword(), "clientName", cfg);
        log.info("Initialized cache redis cluster: {}", REDIS.getClusterNodes());
    }

    protected JedisCluster redis() {
        if (REDIS == null) {
            synchronized (RedisCacheStore.class) {
                if (REDIS != null) {
                    return REDIS;
                }
                initRedis();
                return REDIS;
            }
        }
        return REDIS;
    }

    @NotNull
    @Override
    Optional<CacheWrapper<String>> getInternal(@NotNull String key) {
        Assert.hasText(key, "Cache key must not be blank");
        String v = REDIS.get(key);
        return StringUtils.isEmpty(v) ? Optional.empty() : jsonToCacheWrapper(v);
    }

    @Override
    void putInternal(@NotNull String key, @NotNull CacheWrapper<String> cacheWrapper) {
        putInternalIfAbsent(key, cacheWrapper);
        try {
            REDIS.set(key, JSON.toJSONString(cacheWrapper));
            Date ttl = cacheWrapper.getExpireAt();
            if (ttl != null) {
                REDIS.pexpireAt(key, ttl.getTime());
            }
        } catch (Exception e) {
            log.warn("Put cache fail json2object key: [{}] value:[{}]", key, cacheWrapper);
        }
    }

    @Override
    Boolean putInternalIfAbsent(@NotNull String key, @NotNull CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");

        if (REDIS.setnx(key, JSON.toJSONString(cacheWrapper)) <= 0) {
            log.warn("Failed to put the cache, because the key: [{}] has been present already", key);
            return false;
        }
        Date ttl = cacheWrapper.getExpireAt();
        if (ttl != null) {
            REDIS.pexpireAt(key, ttl.getTime());
        }
        return true;
    }

    @Override
    public void delete(@NotNull String key) {
        Assert.hasText(key, "Cache key must not be blank");
        REDIS.del(key);
        log.debug("Removed key: [{}]", key);
    }

    @PreDestroy
    public void preDestroy() {
    }

    @Override
    public PageResult<String> findKeysForPage(String patternKey, Pageable pageable) {
        ScanParams scanParams = new ScanParams();
        scanParams.match(patternKey);
        ScanResult<String> scan = REDIS.scan(patternKey, scanParams);
        List<String> result = scan.getResult();
        return ListUtil.pagination(result, pageable.getPageNumber(), pageable.getPageSize());
    }
}
