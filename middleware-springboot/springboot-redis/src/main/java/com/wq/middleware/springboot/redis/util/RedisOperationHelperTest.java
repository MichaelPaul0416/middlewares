package com.wq.middleware.springboot.redis.util;

import com.wq.middleware.springboot.redis.RedisApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = {RedisApplication.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class RedisOperationHelperTest {

    @Autowired
    private RedisOperationHelper redisOperation;

    @Test
    public void cache(){
        redisOperation.plainCache("hello","world");
    }

}
