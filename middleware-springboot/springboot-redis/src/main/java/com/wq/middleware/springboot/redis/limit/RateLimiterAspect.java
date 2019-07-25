package com.wq.middleware.springboot.redis.limit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wangqiang20995
 * @Date:2019/7/25
 * @Description:
 * @Resource:
 */
@Aspect
@Component
public class RateLimiterAspect {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisTemplate redisTemplate;

    private DefaultRedisScript<Long> redisScript;

    @PostConstruct
    public void init() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("rateLimiter.lua")));
        logger.info("redis 分布式限流处理器加载完成");
    }

//    @Pointcut("@annotation(com.wq.middleware.springboot.redis.limit.RateLimiter)")
//    public void rateLimiter() {
//    }


    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint point, RateLimiter rateLimiter) throws Throwable {

        String model = rateLimiter.limitModel();
        if (StringUtils.isEmpty(model)) {
            throw new NullPointerException("empty limit model...set limitModel for [" + RateLimiter.class.getName() + "]");
        }

        int times = rateLimiter.limit();
        long expire = rateLimiter.expire();// s

        logger.info("rateLimiter{limitModel:[{}],limit:[{}],expire:[{} s]}", model, times, expire);
        List<String> keys = new ArrayList<>();
        keys.add(model);

        //调用lua脚本执行
        Long result = (Long) redisTemplate.execute(redisScript, keys, expire, times);
        if (result == 0) {
            String msg = String.format("在指定的时间内[%ss]内，调用次数超过[%s]次，当前请求[%s]被限流"
                    , expire, times, Thread.currentThread().getName());
            logger.error(msg);
            return "false";
        }

        logger.info("规定时间内[{}],限流次数未到阀值[{}],当前请求[{}]不被限流，调用成功",
                expire, times, Thread.currentThread().getName());

        return point.proceed();

    }


}
