package org.lisa.delayqueue.producer.service;


import org.lisa.delayqueue.base.entity.Message;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 发送消息
 *
 * @author solid
 */
public interface Producer {

    static Map<String, Producer> STREAM_MAP = new ConcurrentHashMap<>();

    /**
     * 发送消息（立即发送，基本上会延迟一秒）
     *
     * @param msg 消息体对象
     */
    public <T> void send(Message<T> msg);

    /**
     * 发送消息（定时发送）
     *
     * @param msg      消息体对象
     * @param expectAt 期望在何时发送（时间戳）
     */
    public <T> void send(Message<T> msg, long expectAt);

    /**
     * 发送消息（定时发送）
     *
     * @param msg      消息体对象
     * @param expectAt 期望在何时发送（时间戳）
     */
    public <T> void send(Message<T> msg, LocalDateTime expectAt);

    /**
     * 发送消息（定时发送）
     *
     * @param msg        消息体对象
     * @param expectAt   期望在何时发送（时间戳）
     * @param zoneOffset 拾取
     */
    public <T> void send(Message<T> msg, LocalDateTime expectAt, ZoneOffset zoneOffset);
}
