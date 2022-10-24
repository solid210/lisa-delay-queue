package org.lisa.delayqueue.producer_and_manager.service;

import org.lisa.delayqueue.base.entity.Message;
import org.lisa.delayqueue.base.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.lisa.delayqueue.producer.service.Producer;
import org.lisa.delayqueue.producer.util.PublishMessageUtil;
import org.lisa.delayqueue.producer_and_manager.dto.OrderInfo;
import org.lisa.delayqueue.producer_and_manager.dto.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.*;
import java.util.Random;

import static org.lisa.delayqueue.base.constant.Constant.BEAN_NAME_SUFFIX_MESSAGE_PRODUCER;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/9/24
 */
@Slf4j
@Service
public class DemoService {

    @Resource(name = "mystream" + BEAN_NAME_SUFFIX_MESSAGE_PRODUCER)
    private Producer producer;

//    @Resource(name = "mystream2_Producer")
//    private Producer producer2;

    public void test(String msg) {
        // 创建消息记录, 以及指定stream
        producer.send(new Message<>(msg));
//        producer2.send(new Message<>(msg));

        User user = new User();
        long now = System.currentTimeMillis();
        long userId = now;
        user.setId(userId);
        user.setName("solid");
        producer.send(new Message<>(user));

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(userId);
        orderInfo.setOrderNo(new Random().nextLong());
        orderInfo.setCreateTime(LocalDateTime.now());
        producer.send(new Message<>(orderInfo));
//        producer2.send(new Message<>(orderInfo));

        String topic = "mystream";
        PublishMessageUtil.sendMessage(topic, new Message<>(user));

        Producer producer3 = SpringContextUtil.getBean(topic + BEAN_NAME_SUFFIX_MESSAGE_PRODUCER, Producer.class);
        user.setName("producer3");
        producer3.send(new Message<>(user));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault());
        LocalDateTime after10Seconds = localDateTime.plusSeconds(10);
        log.info("localDateTime -> {}, after10Seconds -> {}", localDateTime, after10Seconds);
        user.setId(after10Seconds.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        user.setName("after10Seconds");
        producer3.send(new Message<>(user), after10Seconds);
    }
}
