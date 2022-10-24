package org.lisa.delayqueue.manager.scheduled;

import org.lisa.delayqueue.base.config.DelayQueueConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

import static org.lisa.delayqueue.base.constant.Constant.SERVER_NAME_MANAGER;
import static org.lisa.delayqueue.base.constant.Constant.STREAM_READY_QUEUE;

/**
 * @description: 定期清理消息，释放内存空间
 * @author: wuxu
 * @createDate: 2022/10/10
 */
@Slf4j
public class CleanStreamJob {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DelayQueueConfigProperties delayQueueConfigProperties;

//    @Scheduled(cron = "0 0 3 * * ?")
    @Scheduled(cron = "${lisa-delay-queue.manager-server.crontab-clean-stream}")
    public void execute(){
        log.info("[{}] CleanStreamJob", SERVER_NAME_MANAGER);
        delayQueueConfigProperties.getGroups()
            .stream()
            .forEach(delayQueueConfig -> {
                String streamKey = STREAM_READY_QUEUE + delayQueueConfig.getTopic();
                // 获取group中的pending消息信息，本质上就是执行XPENDING指令
                PendingMessagesSummary pendingMessagesSummary = stringRedisTemplate.opsForStream().pending(streamKey, delayQueueConfig.getGroup());
                // 所有pending消息的数量
                long totalPendingMessages = pendingMessagesSummary.getTotalPendingMessages();
                log.info("[{}] totalPendingMessages:{}", SERVER_NAME_MANAGER, totalPendingMessages);
                stringRedisTemplate.opsForStream().trim(streamKey, totalPendingMessages + delayQueueConfig.getMaxLength());
            });
    }
}
