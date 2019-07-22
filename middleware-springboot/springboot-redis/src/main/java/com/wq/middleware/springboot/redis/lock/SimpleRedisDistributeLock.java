package com.wq.middleware.springboot.redis.lock;

import com.wq.middleware.springboot.redis.util.RedisOperationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 简化版的redis分布式锁，不涉及可重入，过期续费，加锁解锁同一个调用者这些特性
 * 其中隐藏的问题，在下面这篇博客中有所体现：https://www.cnblogs.com/linjiqin/p/8003838.html
 * 两个常见的错误在这片博客中都提及了
 */
@Component
public class SimpleRedisDistributeLock {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRedisDistributeLock.class);

    private static final int finalDefaultTTLWithKey = 24 * 3600;

    private static final long defaultExpireTime = 20 * 1000;

    private static final boolean Success = true;

    @Autowired
    private RedisOperationHelper redisOperationHelper;

    public boolean lock(String resource) {
        return this.lock(resource,defaultExpireTime);
    }

    public boolean lock(String key, long expireTime) {
        logger.debug("redis lock start:key[{}]/value[{}]", key, expireTime);
        if (expireTime < 0) {
            logger.warn("negative expire time set and choose default expire time:{}", defaultExpireTime);
            expireTime = defaultExpireTime;
        }

        long now = Instant.now().toEpochMilli();
        long lockExpireTime = now + expireTime;

        boolean result = redisOperationHelper.plainCacheIfAbsent(key, String.valueOf(lockExpireTime));//设置key的过期时间deadline
        if (result) {
            redisOperationHelper.plainCacheSetExpire(key, finalDefaultTTLWithKey);
            logger.info("get distribute lock for key[{}] successfully", key);
            return true;
        }

        Object valueFromRedis = getKeyWithRetry(key, 3);//旧锁的过期时间
        if (valueFromRedis == null) {
            //retry之后获取的还是null，说明可能是redis挂了或者已经被释放（key被删除）
            throw new IllegalStateException("3 times retry and value of key[" + key + "] is still failed," +
                    "and the redis server may be unAvailable or the lock has been released");
        }

        long oldExpireTime = Long.parseLong((String) valueFromRedis);
        logger.info("old expire time[{}] for [{}]",valueFromRedis,key);

        if(oldExpireTime <= now){
            //锁已经过期
            logger.warn("redis distribute lock[{}] has been expired...",key);

            /**
             * 这里之所以要用getAndSet方法而不是Set方法，原因如下
             * 1.oldExpireTime是上一个持有该锁的进程设置的过期时间，进入到这个分支代表锁已经过期，此时的话，其他进程是可以竞争锁的
             * 2.假设现在有两个进程A，B同时竞争锁，B先获取到，B通过getAndSet，将key的新value设置为新的超时时间T1，返回上一个持有者的超时时间T0
             *   那么此时T0=oldExpireTime
             *   然后后面进程A去getAndSet了，此时返回的其实是进程B设置的value值T1，此时T1!=T0，所以代表进程A竞争锁失败，退出
             */
            String value = this.redisOperationHelper.plainGetAndSet(key,String.valueOf(lockExpireTime));

            if(Long.valueOf(value) == oldExpireTime){
                logger.info("get redis distribute lock[{}] successfully",key);
                //设置超时时间
                this.redisOperationHelper.plainCacheSetExpire(key, finalDefaultTTLWithKey);
                return true;
            }else {
                return false;
            }
        }

        return false;
    }

    public boolean unlock(String key){
        logger.info("unlock redis distribute lock[{}]",key);
        this.redisOperationHelper.plainRemoveByKey(key);
        return Success;
    }

    private Object getKeyWithRetry(String key, int retryTimes) {
        int fail = 0;
        do {
            try {
                return this.redisOperationHelper.getPlainCache(key);
            } catch (Exception e) {
                logger.error("get value of key[{}] failed and will retry", key, e);
                fail++;
                if (fail == retryTimes) {
                    return null;
                }
            }
        } while (fail < retryTimes);

        return null;
    }
}
