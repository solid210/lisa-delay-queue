package org.lisa.delayqueue.producer.util;

import org.lisa.delayqueue.base.entity.Message;
import org.lisa.delayqueue.producer.service.Producer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @description: 发送消息工具类
 * @author: wuxu
 * @createDate: 2022/10/1
 */
public class PublishMessageUtil {

    /** 发送消息（相当于立刻发送）
     * @param topic Message Topic
     * @param msg Message Body
     * @param <T> 消息体中的对象类型
     */
    public static <T> void sendMessage(String topic, Message<T> msg) {
        sendMessage(topic, msg, System.currentTimeMillis());
    }

    /**
     * 在指定时间发送消息
     * @param topic Message Topic
     * @param msg Message Body
     * @param expectAt 期望发送时间（毫秒数时间戳，默认当前系统时区）
     * @param <T> 消息体中的对象类型
     */
    public static <T> void sendMessage(String topic, Message<T> msg, long expectAt) {
        Producer.STREAM_MAP.get(topic).send(msg);
    }

    /**
     * 在指定时间发送消息
     * @param topic Message Topic
     * @param msg Message Body
     * @param expectAt 期望发送时间（默认当前系统时区）
     * @param <T> 消息体中的对象类型
     */
    public static <T> void sendMessage(String topic, Message<T> msg, LocalDateTime expectAt) {
        sendMessage(topic, msg, expectAt.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
    }

    /**
     * 在指定时间发送消息
     * @param topic Message Topic
     * @param msg Message Body
     * @param expectAt 期望发送时间
     * @param zoneOffset 时区
     * @param <T> 消息体中的对象类型
     */
    public static <T> void sendMessage(String topic, Message<T> msg, LocalDateTime expectAt, ZoneOffset zoneOffset) {
        sendMessage(topic, msg, expectAt.toInstant(zoneOffset).toEpochMilli());
    }
}
