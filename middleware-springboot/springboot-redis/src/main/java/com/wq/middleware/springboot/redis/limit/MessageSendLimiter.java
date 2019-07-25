package com.wq.middleware.springboot.redis.limit;

import com.wq.common.redis.CountDown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Author: wangqiang20995
 * @Date:2019/7/25
 * @Description:
 * @Resource:
 */
@Component
public class MessageSendLimiter implements CountDown {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @RateLimiter(limitModel = "message:limiter")
    public void sendMessage(String phone,String message){
        logger.info("send message[{}] to phone[{}]",message,phone);
    }

    @Override
    public void decrease() {

    }
}
