package org.lisa.delayqueue.consumer.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lisa.delayqueue.base.config.DelayQueueConfigProperties;
import org.lisa.delayqueue.base.util.SpringContextUtil;
import org.lisa.delayqueue.consumer.listener.DefaultStreamListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import static org.lisa.delayqueue.base.constant.Constant.STREAM_READY_QUEUE;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/9/24
 */
@Slf4j
@ConditionalOnProperty(prefix = DelayQueueConfigProperties.LISA_DELAY_QUEUE_CONFIG_PREFIX, name = "enabled", havingValue = "true")
@EnableConfigurationProperties({DelayQueueConfigProperties.class, DelayQueueConsumerServerConfig.class})
@Configuration
@EnableScheduling
@Import(SpringContextUtil.class)
public class DelayQueueConsumerAutoConfiguration {

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private DelayQueueConfigProperties delayQueueConfigProperties;

    @Resource
    private DelayQueueConsumerServerConfig delayQueueConsumerServerConfig;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 主要做的是将OrderStreamListener监听绑定消费者，用于接收消息
     *
     * @param connectionFactory
     * @param streamListener
     * @return
     */
    @Bean
    public Map<String, StreamMessageListenerContainer<String, ObjectRecord<String, String>>> consumerListenerMap(
            RedisConnectionFactory connectionFactory,
            DefaultStreamListener streamListener) {
        log.info("delayQueueConsumerServerConfiguration -> {}", delayQueueConsumerServerConfig);
        return delayQueueConfigProperties.getGroups().stream()
                .collect(
                        Collectors.toMap(
                                DelayQueueConfigProperties.DelayQueueConfig::getTopic,
                                streamConfig -> {
                                    StreamMessageListenerContainer<String, ObjectRecord<String, String>> container = streamContainer(streamConfig, connectionFactory, streamListener);
                                    container.start();
                                    return container;
                                }
                        )
                );
    }

    /**
     * @param delayQueueConfig  redisStream流的配置项
     * @param connectionFactory
     * @param streamListener    绑定的监听类
     * @return
     */
    private StreamMessageListenerContainer<String, ObjectRecord<String, String>> streamContainer(DelayQueueConfigProperties.DelayQueueConfig delayQueueConfig, RedisConnectionFactory connectionFactory, org.springframework.data.redis.stream.StreamListener streamListener) {

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofMillis(delayQueueConsumerServerConfig.getPollTimeoutMillis())) // 拉取消息超时时间
                        .batchSize(delayQueueConsumerServerConfig.getPollBatchSize()) // 批量抓取消息
                        .targetType(String.class) // 传递的数据类型
                        .executor(threadPoolTaskExecutor)
                        .build();
        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container = StreamMessageListenerContainer
                .create(connectionFactory, options);
        //指定消费最新的消息
        StreamOffset<String> offset = StreamOffset.create(STREAM_READY_QUEUE + delayQueueConfig.getTopic(), ReadOffset.lastConsumed());
        //创建消费者
        Consumer consumer = Consumer.from(delayQueueConfig.getGroup(), delayQueueConfig.getConsumer());
        StreamMessageListenerContainer.StreamReadRequest<String> streamReadRequest = StreamMessageListenerContainer.StreamReadRequest.builder(offset)
                .errorHandler((error) -> {
                })
                .cancelOnError(e -> false)
                .consumer(consumer)
                //关闭自动ack确认
                .autoAcknowledge(false)
                .build();
        //指定消费者对象
        container.register(streamReadRequest, streamListener);
        return container;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public DefaultStreamListener defaultStreamListener() {
        return new DefaultStreamListener();
    }
}
