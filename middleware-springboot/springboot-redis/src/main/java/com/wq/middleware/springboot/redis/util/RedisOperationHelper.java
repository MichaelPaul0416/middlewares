package com.wq.middleware.springboot.redis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisOperationHelper {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisTemplate redisTemplate;

    public void plainCache(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
        logger.info("[key{}/value{}] 加入缓存成功", key, value);
    }

    public boolean plainCacheIfAbsent(String key,String value){
        boolean result = this.redisTemplate.opsForValue().setIfAbsent(key,value);
        logger.info("redis lock setnx:key[{}]/value[{}] and result[{}]",key,value,result);
        return result;
    }

    public void plainCacheSetExpire(String key,long ttl){
        this.redisTemplate.expire(key,ttl,TimeUnit.SECONDS);
    }

    public <T> T getPlainCache(String key){
        return (T) this.redisTemplate.opsForValue().get(key);
    }

    public <T> T plainGetAndSet(String key,String value){
        return (T) this.redisTemplate.opsForValue().getAndSet(key,value);
    }

    public void plainRemoveByKey(String key){
        this.redisTemplate.delete(key);
    }
}
