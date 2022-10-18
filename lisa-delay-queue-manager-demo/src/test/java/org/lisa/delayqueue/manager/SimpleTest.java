package org.lisa.delayqueue.manager;

import org.lisa.delayqueue.manager.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/10/3
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class SimpleTest {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DemoService demoService;

    @Test
    public void testGet(){
        demoService.sayHello();
    }

    @Test
    public void testAdd2ZSet() throws InterruptedException {
        for(int i = 0; i < 10; i++){
            stringRedisTemplate.opsForZSet().add("demo-key", "value_" + i, System.currentTimeMillis());
            TimeUnit.SECONDS.sleep(1);
        }
        Set<String> sets = stringRedisTemplate.opsForZSet().reverseRangeByScore("demo-key", 0, System.currentTimeMillis());
        log.info("sets -> {}", sets);
    }

    @Test
    public void addAndDelZSet(){
        stringRedisTemplate.opsForZSet().add("addAndDelZSet", "value1", -1);
    }

    @Test
    public void replaceTest(){
        String url = "http://www.163.com";
        url = url.replace("163", "h");
        log.info("url -> {}", url);
    }
}
