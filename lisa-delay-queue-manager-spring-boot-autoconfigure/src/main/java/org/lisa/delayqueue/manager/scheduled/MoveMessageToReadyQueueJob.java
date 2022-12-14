package org.lisa.delayqueue.manager.scheduled;

import org.lisa.delayqueue.base.config.DelayQueueConfigProperties;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

import java.util.List;

import static org.lisa.delayqueue.base.constant.Constant.*;
import static org.lisa.delayqueue.base.util.RedisScriptUtils.getRedisScript;

/**
 * @description: 将消息从zset移动到stream定时任务
 * @author: wuxu
 * @createDate: 2022/10/8
 */
@Slf4j
public class MoveMessageToReadyQueueJob {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DelayQueueConfigProperties delayQueueConfigProperties;

    public static final String RESOURCE_NAME = "script/lua/move_msg_to_ready_queue.lua";
    public static final String LUA_NAME = "move_msg_to_ready_queue.lua";

    /**
     * 每隔1秒钟，扫描一下有没有到时间的定时任务
     */
//    @Scheduled(cron = "0/1 * * * * ?")
    @Scheduled(cron = "${lisa-delay-queue.manager-server.crontab-move-to-ready-queue}")
    public void execute() {
        long now = System.currentTimeMillis();
        log.info("[{}] move message to ready queue. now -> {}", SERVER_NAME_MANAGER, now);
        delayQueueConfigProperties.getGroups().forEach(delayQueueConfig -> {
            moveMessageFromWaitingQueueToReadyQueue(delayQueueConfig, now);
            moveMessageFromRetryQueueToReadyQueue(delayQueueConfig, now);
        });
    }

    private void moveMessageFromWaitingQueueToReadyQueue(DelayQueueConfigProperties.DelayQueueConfig delayQueueConfig, long now) {
        String waitingQueueKey = ZSET_WAITING_QUEUE + delayQueueConfig.getTopic();
        String readyQueueKey = STREAM_READY_QUEUE + delayQueueConfig.getTopic();
        long startScore = 0;
        List<String> result = moveMessage(waitingQueueKey, readyQueueKey, startScore, now, delayQueueConfig.getMovingSize());
        log.info("[{}] moveMessageFromWaitingQueueToReadyQueue result:{}", SERVER_NAME_MANAGER, result);
    }

    private void moveMessageFromRetryQueueToReadyQueue(DelayQueueConfigProperties.DelayQueueConfig delayQueueConfig, long now) {
        String retryQueueKey = ZSET_RETRY_QUEUE + delayQueueConfig.getTopic();
        String readyQueueKey = STREAM_READY_QUEUE + delayQueueConfig.getTopic();
        long startScore = 0;
        List<String> result = moveMessage(retryQueueKey, readyQueueKey, startScore, now, delayQueueConfig.getMovingSize());
        log.info("[{}] moveMessageFromRetryQueueToReadyQueue result:{}", SERVER_NAME_MANAGER, result);
    }

    private List<String> moveMessage(String redisKeyZset, String readyQueueKey, long startScore, long endScore, int count) {
        log.info("[{}] processMessageQueue, redisKeyZset:{}, readyQueueKey:{}, startScore:{}, endScore:{}, count:{}", SERVER_NAME_MANAGER, redisKeyZset, readyQueueKey, startScore, endScore, count);
        try {
            return stringRedisTemplate.execute(getRedisScript(RESOURCE_NAME, LUA_NAME, List.class), Lists.newArrayList(redisKeyZset, readyQueueKey), String.valueOf(startScore), String.valueOf(endScore), String.valueOf(count));
        } catch (Exception e) {
            return null;
        }
    }
}
