package org.lisa.delayqueue.base.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author solid
 */
@Data
@ConfigurationProperties(prefix = DelayQueueConfigProperties.LISA_DELAY_QUEUE_CONFIG_PREFIX)
public class DelayQueueConfigProperties {

    public static final String LISA_DELAY_QUEUE_CONFIG_PREFIX = "lisa-delay-queue";
    public static final String PROPERTY_NAME_GROUPS = "groups";

    private List<DelayQueueConfig> groups;

    @Data
    public static class DelayQueueConfig {
        /**
         * 延迟队列的topic，必填
         */
        private String topic;

        /**
         * 延迟队列分组，必填
         */
        private String group;

        /**
         * 消费者名称，作为consumer使用时必填
         */
        private String consumer;

        /**
         * stream队列长度（默认1000），如果队列长度超过该值，则会进行修剪
         */
        private int maxLength = 1000;

        /**
         * 单次移动的消息数量
         */
        private int movingSize = 100;
    }

}
