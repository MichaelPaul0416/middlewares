package com.wq.middleware.springboot.redis.lock;

import com.wq.middleware.springboot.redis.util.RedisOperationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RedisDistributeLock {

    private static final Logger logger = LoggerFactory.getLogger(RedisDistributeLock.class);

    private static final int finalDefaultTTLWithKety = 24 * 3600;

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

        boolean result = redisOperationHelper.plainCacheIfAbsent(key, String.valueOf(expireTime));
        if (result) {
            redisOperationHelper.plainCacheSetExpire(key, finalDefaultTTLWithKety);
            logger.info("get distribute lock for key[{}] successfully", key);
            return true;
        }

        Object valueFromRedis = getKeyWithRetry(key, 3);
        if (valueFromRedis == null) {
            //retry之后获取的还是null，说明可能是redis挂了
            throw new IllegalStateException("3 times retry and value of key[" + key + "] is still failed,and the redis server may be unAvailable");
        }

        long oldExpireTime = Long.parseLong((String) valueFromRedis);
        logger.info("old expire time[{}] for [{}]",valueFromRedis,key);

        if(oldExpireTime <= now){
            //锁已经过期
            logger.warn("redis distribute lock[{}] has been expired...",key);

            String value = this.redisOperationHelper.plainGetAndSet(key,String.valueOf(lockExpireTime));
            if(Long.valueOf(value) == oldExpireTime){
                logger.info("get redis distribute lock[{}] successfully",key);
                //设置超时时间
                this.redisOperationHelper.plainCacheSetExpire(key,finalDefaultTTLWithKety);
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
        return true;
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
