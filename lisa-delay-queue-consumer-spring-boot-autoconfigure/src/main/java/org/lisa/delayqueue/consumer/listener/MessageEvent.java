package org.lisa.delayqueue.consumer.listener;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/9/25
 */
@ToString(callSuper = true)
@Getter
public class MessageEvent extends ApplicationEvent {

    private static final long serialVersionUID = -5546589030626479941L;

    private final String topic;

    public MessageEvent(String topic, Object source) {
        super(source);
        this.topic = topic;
    }
}
