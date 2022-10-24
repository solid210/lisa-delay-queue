package org.lisa.delayqueue.base.constant;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/10/3
 */
public class Constant {

    public static final String ZSET_WAITING_QUEUE = "zset:waiting_queue:";

    public static final String STREAM_READY_QUEUE = "stream:ready_queue:";

    public static final String ZSET_RETRY_QUEUE = "zset:retry_queue:";

    public static final String HASH_RETRY_COUNT = "hash:retry_count:";

    public static final String SET_GARBAGE_KEY = "set:garbage_key:";

    public static final String MESSAGE_BODY = "message:body:";

    public static final String INIT_MESSAGE = "init message...remember lisa, cqy, stone, miles, liam by solid";

    public static final String BEAN_NAME_SUFFIX_MESSAGE_PRODUCER = "_MessageProducer";

    public static final String SERVER_NAME_PRODUCER = "lisa-delay-queue-producer";

    public static final String SERVER_NAME_MANAGER = "lisa-delay-queue-manager";

    public static final String SERVER_NAME_CONSUMER = "lisa-delay-queue-consumer";

    /**
     * consumer服务配置项前缀
     */
    public static final String LISA_DELAY_QUEUE_CONSUMER_SERVER_CONFIG_PREFIX = "lisa-delay-queue.consumer-server";

    /**
     * producer服务配置项前缀
     */
    public static final String LISA_DELAY_QUEUE_PRODUCER_SERVER_CONFIG_PREFIX = "lisa-delay-queue.producer-server";

    /**
     * manager服务配置项前缀
     */
    public static final String LISA_DELAY_QUEUE_MANAGER_SERVER_CONFIG_PREFIX = "lisa-delay-queue.manager-server";
}
