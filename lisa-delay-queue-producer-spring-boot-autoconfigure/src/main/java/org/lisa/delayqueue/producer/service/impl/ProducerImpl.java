package org.lisa.delayqueue.producer.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lisa.delayqueue.base.config.DelayQueueConfigProperties;
import org.lisa.delayqueue.base.entity.Message;
import org.lisa.delayqueue.producer.service.Producer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.lisa.delayqueue.base.constant.Constant.*;
import static org.lisa.delayqueue.base.util.RedisScriptUtils.getRedisScript;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/9/24
 */
@Slf4j
@Setter
public class ProducerImpl implements Producer, InitializingBean {

    private DelayQueueConfigProperties.DelayQueueConfig delayQueueConfig;

    private StringRedisTemplate stringRedisTemplate;

    public static final String RESOURCE_NAME = "script/lua/push_msg.lua";
    public static final String LUA_NAME = "push_msg.lua";

    @Override
    public <T> void send(Message<T> msg) {
        LocalDateTime nowDateTime = LocalDateTime.now();
        log.info("[{}] send msg. msg -> {}, nowDateTime -> {}", SERVER_NAME_PRODUCER, msg, nowDateTime);
        long now = nowDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        send(msg, now);
    }

    @Override
    public <T> void send(Message<T> msg, long expectAt) {
        String topic = delayQueueConfig.getTopic();
        String waitingQueueKey = ZSET_WAITING_QUEUE + topic;
        String readyQueueKey = STREAM_READY_QUEUE + topic;
        String msgId = UUID.randomUUID().toString();
        String score = String.valueOf(expectAt);
        String msgBodyKey = MESSAGE_BODY + topic + ":" + msgId;
        String msgBodyValue = JSONObject.toJSONString(msg);
        String now = String.valueOf(System.currentTimeMillis());
        log.info("[{}] [开始]调用lua脚本添加延时消息。waitingQueueKey:{}, readyQueueKey:{}, msgId:{}, score:{}, msgBodyKey:{}, msgBodyValue:{}, now:{}", SERVER_NAME_PRODUCER, waitingQueueKey, readyQueueKey, msgId, score, msgBodyKey, msgBodyValue, now);
        Boolean result = stringRedisTemplate.execute(getRedisScript(RESOURCE_NAME, LUA_NAME, Boolean.class), Lists.newArrayList(waitingQueueKey, readyQueueKey), msgId, score, msgBodyKey, msgBodyValue, now);
        log.info("[{}] [结束]调用lua脚本添加延时消息。msgId:{}, result:{}", SERVER_NAME_PRODUCER, msgId, result);
    }

    @Override
    public <T> void send(Message<T> msg, LocalDateTime expectAt) {
        log.info("[{}] send msg with expectAt. msg -> {}, expectAt(LocalDateTime) -> {}", SERVER_NAME_PRODUCER, msg, expectAt);
        send(msg, expectAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    @Override
    public <T> void send(Message<T> msg, LocalDateTime expectAt, ZoneOffset zoneOffset) {
        log.info("[{}] send msg with expectAt and zoneOffset. msg -> {}, expectAt(LocalDateTime) -> {}, zoneOffset -> {}", SERVER_NAME_PRODUCER, msg, expectAt, zoneOffset);
        send(msg, expectAt.toInstant(zoneOffset).toEpochMilli());
    }

    @Override
    public void afterPropertiesSet() {
        String topic = delayQueueConfig.getTopic();
        STREAM_MAP.put(topic, this);
    }
}
