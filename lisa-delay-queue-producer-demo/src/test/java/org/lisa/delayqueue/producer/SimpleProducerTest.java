package org.lisa.delayqueue.producer;

import com.alibaba.fastjson.JSONObject;
import org.lisa.delayqueue.base.util.RedisScriptUtils;
import org.lisa.delayqueue.producer.dto.OrderInfo;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/10/3
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class SimpleProducerTest {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testSetRedis() {
        String msgId = "msg:id:666888";
        String msgValue = String.valueOf(System.currentTimeMillis());
        stringRedisTemplate.opsForValue().set(msgId, msgValue);
        String valueFromRedis = stringRedisTemplate.opsForValue().get(msgId);
        log.info("valueFromRedis -> {}", valueFromRedis);
    }

//    @Test
//    public void testExecuteByConnection(){
//        String resourceName = "script/lua/test_xpending.lua";
//        String luaName = "xpending.lua";
//
//        DefaultRedisScript<List> redisScript = getRedisScript(resourceName, luaName, List.class);
//        List<String> list = Lists.newArrayList();
//        list.toArray(new String[]);
//        Object result = stringRedisTemplate.getConnectionFactory().getConnection()
//                .evalSha(redisScript.getSha1(), ReturnType.VALUE, 1, "mystream", "group-1");
//        log.info("result：{}", result);
//    }

    @Test
    public void testHGet() {
        String resourceName = "script/lua/test_hget.lua";
        String luaName = "hget.lua";
        String result = stringRedisTemplate.execute(getRedisScript(resourceName, luaName, String.class), Lists.newArrayList("test_hash"), "test_key_1");
        log.info("Result from lua. result -> {}", result);
    }

    @Test
    public void testPending() {
        String resourceName = "script/lua/test_xpending.lua";
        String luaName = "xpending.lua";
        Object result = stringRedisTemplate.execute(getRedisScript(resourceName, luaName, List.class), Lists.newArrayList("mystream"), "group-1", "-", "+", "20");
        log.info("Result from lua. result -> {}", result);
    }

    @Test
    public void testListResult(){
        String resourceName = "script/lua/test_list_result.lua";
        String luaName = "test_list_result.lua";
        Object result = stringRedisTemplate.execute(getRedisScript(resourceName, luaName, List.class), Lists.newArrayList("mystream"));
        log.info("Result from lua. result -> {}", result);
    }

    @Test
    public void testZrangebyscoreV1() {
        zrangeByScoreAndRemove();
    }

    @Test
    public void testZrangeByScore(){
        String zsetKey = "zset:waiting_queue:mystream";
        zrangeByScore(zsetKey);
    }

    private void zrangeByScore() {
        zrangeByScore("demo-key");
    }

    private void zrangeByScore(String zsetKey) {
        log.info("----------------------------zrangeByScore----------------------------");
        String resourceName = "script/lua/test_zrangebyscore_1.lua";
        String luaName = "test_zrangebyscore_1.lua";
        String now = String.valueOf(System.currentTimeMillis());
        List<String> result = stringRedisTemplate.execute(getRedisScript(resourceName, luaName, List.class), Lists.newArrayList(zsetKey), now, "15");
        log.info("Result from lua. result -> {}", result);
    }

    private void zrangeByScoreAndRemove() {
        log.info("----------------------------zrangeByScoreAndRemove----------------------------");
        String resourceName = "script/lua/test_zrangebyscore_3.lua";
        String luaName = "test_zrangebyscore_3.lua";
        String now = String.valueOf(System.currentTimeMillis());
        List<List<String>> results = stringRedisTemplate.execute(getRedisScript(resourceName, luaName, List.class), Lists.newArrayList("demo-key"), now, "15");
        log.info("Result from lua. results -> {}", results);
        results.forEach(msg -> {
            log.info("msg[0] -> {}, msg[1] -> {}", msg.get(0), msg.get(1));
        });
    }

    @Test
    public void testAddAndRemove() {
        pushMessage();
        zrangeByScoreAndRemove();
        zrangeByScore();
    }

    @Test
    public void testXrange(){
        String resourceName = "script/lua/test_xrange.lua";
        String luaName = "test_xrange.lua";
        String recordId = "1665489021127-0";
        String result = stringRedisTemplate.execute(getRedisScript(resourceName, luaName, String.class), Lists.newArrayList("stream:ready_queue:mystream"), recordId);
        log.info("Result from lua. result -> {}", result);
    }

    private void pushMessage() {
        log.info("----------------------------pushMessage----------------------------");
        String resourceName = "script/lua/push_msg_to_waiting_queue.lua";
        String luaName = "push_msg_to_waiting_queue.lua";
        String now = String.valueOf(System.currentTimeMillis() - 10000);
        for (int i = 0; i < 3; i++) {
            String msgId = String.valueOf(UUID.randomUUID());
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setOrderNo((long) i);
            orderInfo.setUserId((long) i);
            Boolean result = stringRedisTemplate.execute(getRedisScript(resourceName, luaName, Boolean.class), Lists.newArrayList("demo-key"), msgId, now, JSONObject.toJSONString(orderInfo));
            log.info("result -> {}", result);
        }
    }

    @Test
    public void testLoad() {
        // 在方法里调用执行
        // 第一个参数 对 lua 脚本对象的调用
        // 第二个参数 是 lua 脚本中 Keys 参数
        // 第三个参数 是 lua 脚本中 ARGV 参数
        String value = String.valueOf(System.currentTimeMillis());
        String resourceName = "script/lua/test.lua";
        String luaName = "test.lua";
        String result = stringRedisTemplate.execute(getRedisScript(resourceName, luaName), Lists.newArrayList("lua-demo-key"), value);
        Assert.assertEquals("当前值与从redis取出的值相同", value, result);
        log.info("From lua. result -> {}", result);
    }

    private DefaultRedisScript<String> getRedisScript(String resourceName, String luaName) {
        return RedisScriptUtils.getRedisScript(resourceName, luaName);
    }

    private <T> DefaultRedisScript<T> getRedisScript(String resourceName, String luaName, Class<T> clazz) {
        return RedisScriptUtils.getRedisScript(resourceName, luaName, clazz);
    }
}