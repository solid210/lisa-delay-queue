package org.lisa.delayqueue.manager.scheduled;

import org.lisa.delayqueue.base.config.DelayQueueConfigProperties;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.lisa.delayqueue.manager.config.ProcessPendingMessageConfig;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

import static org.lisa.delayqueue.base.constant.Constant.*;
import static org.lisa.delayqueue.base.util.RedisScriptUtils.getRedisScript;

/**
 * @description: 扫描并处理pending的消息
 * @author: wuxu
 * @createDate: 2022/10/10
 */
@Slf4j
public class ProcessPendingMessageJob {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DelayQueueConfigProperties delayQueueConfigProperties;

    @Resource
    private ProcessPendingMessageConfig processPendingMessageConfig;

    public static final String RESOURCE_NAME = "script/lua/pending_msg_to_retry_queue.lua";
    public static final String LUA_NAME = "pending_msg_to_retry_queue.lua";


//    @Scheduled(cron = "0/5 * * * * ?")
    @Scheduled(cron = "${lisa-delay-queue.manager-server.crontab-process-pending-message}")
    public void execute() {
        log.info("[{}] 定时扫描阻塞的消息。processPendingMessageConfiguration -> {}", SERVER_NAME_MANAGER, processPendingMessageConfig);
        delayQueueConfigProperties
                .getGroups()
                .forEach(this::processPendingMessage);
    }

    private void processPendingMessage(DelayQueueConfigProperties.DelayQueueConfig delayQueueConfig){
        String streamKey = STREAM_READY_QUEUE + delayQueueConfig.getTopic();
        String retryQueueKey = ZSET_RETRY_QUEUE + delayQueueConfig.getTopic();
        String retryCountKey = HASH_RETRY_COUNT + delayQueueConfig.getTopic();
        String garbageKey = SET_GARBAGE_KEY + delayQueueConfig.getTopic();

        stringRedisTemplate.execute(getRedisScript(RESOURCE_NAME, LUA_NAME, String.class),
            Lists.newArrayList(streamKey, retryQueueKey, retryCountKey, garbageKey),
            delayQueueConfig.getGroup(),
            processPendingMessageConfig.getRangeStart(),
            processPendingMessageConfig.getRangeEnd(),
            String.valueOf(processPendingMessageConfig.getCount()),
            String.valueOf(processPendingMessageConfig.getTimeout()),
            String.valueOf(processPendingMessageConfig.getDelayTime()),
            String.valueOf(processPendingMessageConfig.getMaxRetryCount())
        );
    }
}
