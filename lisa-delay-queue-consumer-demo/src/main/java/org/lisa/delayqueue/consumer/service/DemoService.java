package org.lisa.delayqueue.consumer.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.lisa.delayqueue.base.entity.Message;
import org.lisa.delayqueue.producer.dto.OrderInfo;
import org.lisa.delayqueue.producer.dto.User;
import org.lisa.delayqueue.consumer.listener.MessageEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/9/25
 */
@Slf4j
@Service
public class DemoService implements ApplicationListener<MessageEvent> {

    @SneakyThrows
    @Override
    public void onApplicationEvent(MessageEvent event) {
        log.info("[DemoService#onApplicationEvent], event -> {}", event);
        Object source = event.getSource();
        log.info("source -> {}", source);
        Message<String> message = JSONObject.parseObject(String.valueOf(source), new TypeReference<Message<String>>(){

        });
        log.info("message -> {}", message);
        Class<?> clazz = message.getClazz();
        if(User.class.equals(clazz)){
            User user = JSONObject.parseObject(message.getBody(), User.class);
            log.info("user -> {}", user);
        }
        if(OrderInfo.class.equals(clazz)){
            OrderInfo orderInfo = JSONObject.parseObject(message.getBody(), OrderInfo.class);
            log.info("orderInfo -> {}", orderInfo);
        }
    }
}
