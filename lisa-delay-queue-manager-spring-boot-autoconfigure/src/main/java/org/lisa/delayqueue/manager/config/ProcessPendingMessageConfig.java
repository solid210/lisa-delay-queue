package org.lisa.delayqueue.manager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/10/17
 */
@Data
@ConfigurationProperties(prefix = ProcessPendingMessageConfig.LISA_DELAY_QUEUE_MANAGER_SERVER_CONFIG_PREFIX)
public class ProcessPendingMessageConfig {

    public static final String LISA_DELAY_QUEUE_MANAGER_SERVER_CONFIG_PREFIX = "lisa-delay-queue.manager-server";

    /**
     * 命令举例：：xpending stream:ready_queue:mystream group-1 - + 20
     */
    private String rangeStart = "-";

    private String rangeEnd = "+";

    private int count = 20;

    /**
     * pending多久后算超时，开始进行超时处理
     */
    private int timeout = 10000;

    /**
     * 推迟多久后运行
     */
    private int delayTime = 20000;

    /**
     * 最大重试次数
     */
    private int maxRetryCount = 10;
}
