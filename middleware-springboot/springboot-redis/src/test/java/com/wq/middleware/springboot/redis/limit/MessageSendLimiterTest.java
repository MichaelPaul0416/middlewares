package com.wq.middleware.springboot.redis.limit;

import com.wq.middleware.springboot.redis.RedisApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * @Author: wangqiang20995
 * @Date:2019/7/25
 * @Description:
 * @Resource:
 */
@SpringBootTest(classes = RedisApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class MessageSendLimiterTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MessageSendLimiter sendLimiter;

    @Resource
    private ExecutorService serviceWorkerPool;

    @Test
    public void aop() {
        sendLimiter.sendMessage("15878993321", "welcome");
    }

    @Test
    public void concurrentCallSendMessage() {
        CountDownLatch countDownLatch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            this.serviceWorkerPool.execute(() -> {
                sendLimiter.sendMessage("15878993321", "welcome-" + finalI);
                countDownLatch.countDown();
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }

        logger.info("all task done");

        this.serviceWorkerPool.shutdown();
    }
}