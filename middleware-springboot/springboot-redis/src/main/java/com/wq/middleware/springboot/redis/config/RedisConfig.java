package com.wq.middleware.springboot.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisConfig {

    @Bean
    RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL,JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(objectMapper);

        template.setValueSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public ThreadPoolExecutor serviceWorkerPool(){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                10,
                100,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(100),
                new ServiceWorkerPool());

        return threadPoolExecutor;
    }

    private static class ServiceWorkerPool implements ThreadFactory{
        private static final AtomicInteger threadNumber = new AtomicInteger(0);
        private final ThreadGroup threadGroup;
        private final String prefixThread = "serviceWorkerPool-";

        public ServiceWorkerPool(){
            SecurityManager securityManager = System.getSecurityManager();
            threadGroup = (securityManager != null) ? securityManager.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();

        }
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(threadGroup,r,prefixThread + "-" + threadNumber.getAndAdd(1),0);
            if (t.isDaemon()){
                t.setDaemon(false);
            }

            if(t.getPriority() != Thread.NORM_PRIORITY){
                t.setPriority(Thread.NORM_PRIORITY);
            }

            return t;
        }
    }
}
