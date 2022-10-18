package org.lisa.delayqueue.producer.service;

import org.lisa.delayqueue.base.entity.Message;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @description: 发送消息工具类
 * @author: wuxu
 * @createDate: 2022/10/1
 */
public class PublishMessageService {

    /**
     * @param topic Message Topic
     * @param msg 消息体
     * @param <T> 消息体中的对象类型
     */
    public static <T> void sendMessage(String topic, Message<T> msg) {
        sendMessage(topic, msg, System.currentTimeMillis());
    }

    public static <T> void sendMessage(String topic, Message<T> msg, long expectAt) {
        Producer.STREAM_MAP.get(topic).send(msg);
    }

    public static <T> void sendMessage(String topic, Message<T> msg, LocalDateTime expectAt) {
        sendMessage(topic, msg, expectAt.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
    }

    public static <T> void sendMessage(String topic, Message<T> msg, LocalDateTime expectAt, ZoneOffset zoneOffset) {
        sendMessage(topic, msg, expectAt.toInstant(zoneOffset).toEpochMilli());
    }
}
