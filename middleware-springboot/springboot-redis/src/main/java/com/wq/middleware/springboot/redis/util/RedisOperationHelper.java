package com.wq.middleware.springboot.redis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisOperationHelper {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisTemplate redisTemplate;

    public void plainCache(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
        logger.info("[key{}/value{}] 加入缓存成功", key, value);
    }
}
