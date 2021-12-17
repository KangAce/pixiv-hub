package ink.kangaroo.pixivhub.cache.lock;

import com.alibaba.fastjson.JSON;
import ink.kangaroo.pixivhub.cache.AbstractStringCacheStore;
import ink.kangaroo.pixivhub.exception.BaseException;
import ink.kangaroo.pixivhub.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Interceptor for cache lock annotation.
 *
 * @author kangaroo
 * @date 3/28/19
 */
@Slf4j
@Aspect
@Configuration
public class CacheLockInterceptor {
    //TODO
    private final static String CACHE_LOCK_PREFOX = "cache_lock_";

    private final static String CACHE_LOCK_VALUE = "locked";

    private final AbstractStringCacheStore cacheStore;

    public CacheLockInterceptor(AbstractStringCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Around(value = "@annotation(ink.kangaroo.pixivhub.cache.lock.CacheLock)")
    public Object interceptCacheLock(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get method signature
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        log.debug("Starting locking: [{}]", methodSignature.toString());

        // Get cache lock
        CacheLock cacheLock = methodSignature.getMethod().getAnnotation(CacheLock.class);

        // Build cache lock key
        String cacheLockKey = buildCacheLockKey(cacheLock, joinPoint);

        log.debug("Built lock key: [{}]", cacheLockKey);


        try {
            // Get from cache
            Boolean cacheResult = cacheStore.putIfAbsent(cacheLockKey, CACHE_LOCK_VALUE, cacheLock.expired(), cacheLock.timeUnit());

            // Get from cache
            Optional<String> os = cacheStore.get("cache" + ":" + cacheLockKey);
//            log.info("get cache key -> {}", cacheLockKey);
            if (os.isPresent()) {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                //获取method对象
                Method method = signature.getMethod();
                //获取方法的返回值的类型
                Class returnType = method.getReturnType();

                log.info("get cache key success -> {}", cacheLockKey);
                return JSON.parseObject(os.get(), returnType);
            }

            if (cacheResult == null) {
                throw new BaseException("Unknown reason of cache " + cacheLockKey);
            }

            if (!cacheResult) {
                throw new BaseException("访问过于频繁，请稍后再试！");
            }
            Object[] args = joinPoint.getArgs();
            // Proceed the method
            Object proceed = joinPoint.proceed();
            // Get from cache
            cacheStore.putIfAbsent("cache" + ":" + cacheLockKey, JSON.toJSONString(proceed), cacheLock.expired(), cacheLock.timeUnit());
//            log.info("put cache key-> {}", aBoolean);
            return proceed;
        } finally {
            // Delete the cache
            if (cacheLock.autoDelete()) {
                cacheStore.delete(cacheLockKey);
                log.debug("Deleted the cache lock: [{}]", cacheLock);
            }
        }
    }

    private String buildCacheLockKey(@NonNull CacheLock cacheLock, @NonNull ProceedingJoinPoint joinPoint) {
        Assert.notNull(cacheLock, "Cache lock must not be null");
        Assert.notNull(joinPoint, "Proceeding join point must not be null");

        // Get the method
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        // Build the cache lock key
        StringBuilder cacheKeyBuilder = new StringBuilder(CACHE_LOCK_PREFOX);

        String delimiter = cacheLock.delimiter();

        if (StringUtils.isNotBlank(cacheLock.prefix())) {
            cacheKeyBuilder.append(cacheLock.prefix());
        } else {
            cacheKeyBuilder.append(methodSignature.getMethod().toString());
        }

        // Handle cache lock key building
        Annotation[][] parameterAnnotations = methodSignature.getMethod().getParameterAnnotations();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            log.debug("Parameter annotation[{}] = {}", i, parameterAnnotations[i]);

            for (int j = 0; j < parameterAnnotations[i].length; j++) {
                Annotation annotation = parameterAnnotations[i][j];
                log.debug("Parameter annotation[{}][{}]: {}", i, j, annotation);
                if (annotation instanceof CacheParam) {
                    // Get current argument
                    Object arg = joinPoint.getArgs()[i];
                    log.debug("Cache param args: [{}]", arg);

                    // Append to the cache key
                    cacheKeyBuilder.append(delimiter).append(arg);
                }
            }
        }

        if (cacheLock.traceRequest()) {
            // Append http request info
            cacheKeyBuilder.append(delimiter).append(ServletUtils.getRequestIp());
        }

        return cacheKeyBuilder.toString();
    }
}
