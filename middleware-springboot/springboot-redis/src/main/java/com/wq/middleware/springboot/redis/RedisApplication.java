package com.wq.middleware.springboot.redis;

import com.wq.middleware.springboot.redis.lock.SimpleRedisDistributeLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * @Author: wangqiang20995
 * @Date:2019/7/18
 * @Description:
 * @Resource:
 */
@SpringBootApplication
@EnableAutoConfiguration
public class RedisApplication {


    private static final Logger logger = LoggerFactory.getLogger(RedisApplication.class);

    public static void main(String[] args){
        ApplicationContext applicationContext = SpringApplication.run(RedisApplication.class,args);
        if (applicationContext != null){
            logger.info("spring boot application started...");
        }

        SimpleRedisDistributeLock lock = applicationContext.getBean(SimpleRedisDistributeLock.class);
        lock.lock("hello");

        logger.info("hello{}","world");

        lock.unlock("hello");


    }
}
