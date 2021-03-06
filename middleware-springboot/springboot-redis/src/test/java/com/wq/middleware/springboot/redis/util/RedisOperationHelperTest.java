package com.wq.middleware.springboot.redis.util;

import com.wq.middleware.springboot.redis.RedisApplication;
import com.wq.middleware.springboot.redis.lock.SimpleRedisDistributeLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: wangqiang20995
 * @Date:2019/7/22
 * @Description:
 * @Resource:
 */
@SpringBootTest(classes = {RedisApplication.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class RedisOperationHelperTest {

    @Autowired
    private RedisOperationHelper redisOperation;

    @Resource
    private ThreadPoolExecutor serviceWorkerPool;

    @Autowired
    private SimpleRedisDistributeLock simpleRedisDistributeLock;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void cache() {
        redisOperation.plainCache("hello", "world");
    }

    @Test
    public void redisGetAndSet(){
        String s = redisOperation.plainGetAndSet("1","one");
        logger.info("get and set-1:{}",s);//null
        s = redisOperation.plainGetAndSet("1","two");
        logger.info("get and set-2:{}",s);//one
    }

    @Test
    public void distributeLock() {
        String key = "paul";
        CountDownLatch monitor = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {

            serviceWorkerPool.execute(() -> {
                //获取锁的过程发放在外面，获取锁时失败[发生异常，不是返回false]的话，就不需要unlock
                boolean ok = simpleRedisDistributeLock.lock(key,5);
                try {
                    if (!ok) {
                        logger.info("redis distribute obtain failed[" + Thread.currentThread().getName() + "]");
                        return;
                    }

                    logger.info("redis distribute lock obtain successfully[{}]", Thread.currentThread().getName());
                    redisOperation.plainCache("thread", Thread.currentThread().getName());

//                    Thread.sleep(1000 *3);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if(ok) {
                        simpleRedisDistributeLock.unlock(key);
                    }
                    monitor.countDown();
                }
            });
        }

        try {
            monitor.await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }
    }
}
