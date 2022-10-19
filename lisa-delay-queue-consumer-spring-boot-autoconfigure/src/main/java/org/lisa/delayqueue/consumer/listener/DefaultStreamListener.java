package org.lisa.delayqueue.consumer.listener;


import org.lisa.delayqueue.base.config.DelayQueueConfigProperties;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;

import javax.annotation.Resource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.lisa.delayqueue.base.constant.Constant.*;
import static org.lisa.delayqueue.base.util.RedisScriptUtils.getRedisScript;


/**
 * @author solid
 */
@Slf4j
public class DefaultStreamListener implements StreamListener<String, ObjectRecord<String, String>>, ApplicationEventPublisherAware {

    @Resource
    private DelayQueueConfigProperties delayQueueConfigProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private ApplicationEventPublisher applicationEventPublisher;

    public static final String RESOURCE_NAME = "script/lua/ack_message.lua";
    public static final String LUA_NAME = "ack_message.lua";

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        try {
            // 消息ID
            RecordId messageId = message.getId();
            String value = message.getValue();
            String stream = message.getStream();
            // 通过stream，反推出topic
            String topic = StringUtils.replaceOnce(stream, STREAM_READY_QUEUE, "");
            String group = delayQueueConfigProperties.getGroups()
                    .stream()
                    .filter(delayQueueConfig -> delayQueueConfig.getTopic().equals(topic))
                    .map(DelayQueueConfigProperties.DelayQueueConfig::getGroup)
                    .findFirst()
                    .get();
            log.info("StreamMessageListener stream message。stream -> {}, messageId -> {}, topic -> {}, value -> {}", message.getStream(), messageId, topic, value);

            if(StringUtils.contains(value, INIT_MESSAGE)){
                // 初始化的message，直接ack掉
                stringRedisTemplate.opsForStream().acknowledge(group, message);
                return;
            }
            // consumer消费的message包含msgId和预期执行时间
            String[] arrays = value.split("\\|");
            String msgId = arrays[0];
            long score = Long.parseLong(arrays[1]);
            long now = System.currentTimeMillis();
            LocalDateTime scoreDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(score), ZoneId.systemDefault());
            LocalDateTime nowDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault());
            log.info("msgId -> {}, score -> {}, now -> {}, scoreDateTime -> {}, nowDateTime -> {}", msgId, score, now, scoreDateTime, nowDateTime);

            // 从redis中再取出msgId对应的msgValue
            String msgBodyKey = MESSAGE_BODY + topic + ":" + msgId;
            String body = stringRedisTemplate.opsForValue().get(msgBodyKey);
            applicationEventPublisher.publishEvent(new MessageEvent(topic, body));

            // 手动ACK
            ackMessage(topic, group, msgId, msgBodyKey, messageId);
        } catch (Exception e) {
            // 处理异常
            e.printStackTrace();
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private void ackMessage(String topic, String group, String msgId, String msgBodyKey, RecordId recordId){
        log.info("Ack message start. topic -> {}, group -> {}, msgId -> {}, msgBodyKey -> {}, recordId -> {}", topic, group, msgId, msgBodyKey, recordId);
        String streamKey = STREAM_READY_QUEUE + topic;
        String retryCountKey = HASH_RETRY_COUNT + topic;
        String garbageKey = SET_GARBAGE_KEY + topic;
        Long count = stringRedisTemplate.execute(getRedisScript(RESOURCE_NAME, LUA_NAME, Long.class), Lists.newArrayList(streamKey, retryCountKey, garbageKey), group, msgId, msgBodyKey, recordId.getValue());
        log.info("Ack message end. msgId -> {}, count -> {}", msgId, count);
    }
}