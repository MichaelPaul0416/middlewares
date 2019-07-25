package com.wq.middleware.springboot.redis.limit;

import java.lang.annotation.*;

/**
 * @Author: wangqiang20995
 * @Date:2019/7/25
 * @Description:这一个注解的含义就是在expire指定的时间内，只能操作limit次，操作的key是limitModel
 * @Resource:
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    //需要被限流的模块名称
    String limitModel() default "rate:limiter";

    //指定时间内需要被限流的次数
    int limit() default 1;

    //过期时间
    long expire() default 30;



}
