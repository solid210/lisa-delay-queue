package org.lisa.delayqueue.consumer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/10/17
 */
@Data
@ConfigurationProperties(prefix = DelayQueueConsumerServerConfig.LISA_DELAY_QUEUE_CONSUMER_SERVER_CONFIG_PREFIX)
public class DelayQueueConsumerServerConfig {

    public static final String LISA_DELAY_QUEUE_CONSUMER_SERVER_CONFIG_PREFIX = "lisa-delay-queue.consumer-server";

    /**
     * 消息拉取超时时间
     */
    private int pollTimeoutMillis = 5000;

    /**
     * 批量抓取消息数量
     */
    private int pollBatchSize = 10;
}
