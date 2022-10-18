package org.lisa.delayqueue.comsumer;

import org.lisa.delayqueue.consumer.ConsumerApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisa.delayqueue.consumer.config.DelayQueueConsumerServerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/10/15
 */
@SpringBootTest(classes = ConsumerApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class SimpleTest {

    @Resource
    private DelayQueueConsumerServerConfig delayQueueConsumerServerConfig;

    @Test
    public void testDateTransfer() {
        long now = System.currentTimeMillis();
        LocalDateTime ldt1a = new Date(now).toInstant().atOffset(ZoneOffset.of("+8")).toLocalDateTime();
        log.info("LocalDateTime_1a -> {}", ldt1a);

        LocalDateTime ldt1b = new Date(now).toInstant().atZone(ZoneOffset.of("+8")).toLocalDateTime();
        log.info("LocalDateTime_1b -> {}", ldt1b);

        LocalDateTime ldt2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneOffset.of("+8"));
        log.info("LocalDateTime_2 -> {}", ldt2);

        LocalDateTime ldt3 = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneOffset.ofHours(8));
        log.info("LocalDateTime_3 -> {}", ldt3);

        LocalDateTime ldt4 = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneOffset.ofHours(0));
        log.info("LocalDateTime_4 -> {}", ldt4);

        LocalDateTime ldt5 = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault());
        log.info("LocalDateTime_5 -> {}", ldt5);
    }

    @Test
    public void testConfig(){
        log.info("Default zoneId -> {}", ZoneId.systemDefault());
        log.info("pollTimeoutMillis -> {}", delayQueueConsumerServerConfig.getPollTimeoutMillis());
        log.info("pollBatchSize -> {}", delayQueueConsumerServerConfig.getPollBatchSize());
    }
}
