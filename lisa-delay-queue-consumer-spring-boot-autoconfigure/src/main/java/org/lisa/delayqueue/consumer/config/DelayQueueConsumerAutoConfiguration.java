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

import static org.lisa.delayqueue.base.constant.Constant.SERVER_NAME_CONSUMER;
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
     * ??????????????????OrderStreamListener??????????????????????????????????????????
     *
     * @param connectionFactory
     * @param streamListener
     * @return
     */
    @Bean
    public Map<String, StreamMessageListenerContainer<String, ObjectRecord<String, String>>> consumerListenerMap(
            RedisConnectionFactory connectionFactory,
            DefaultStreamListener streamListener) {
        log.info("[{}] delayQueueConsumerServerConfiguration -> {}", SERVER_NAME_CONSUMER, delayQueueConsumerServerConfig);
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
     * @param delayQueueConfig  redisStream???????????????
     * @param connectionFactory
     * @param streamListener    ??????????????????
     * @return
     */
    private StreamMessageListenerContainer<String, ObjectRecord<String, String>> streamContainer(DelayQueueConfigProperties.DelayQueueConfig delayQueueConfig, RedisConnectionFactory connectionFactory, org.springframework.data.redis.stream.StreamListener streamListener) {

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofMillis(delayQueueConsumerServerConfig.getPollTimeoutMillis())) // ????????????????????????
                        .batchSize(delayQueueConsumerServerConfig.getPollBatchSize()) // ??????????????????
                        .targetType(String.class) // ?????????????????????
                        .executor(threadPoolTaskExecutor)
                        .build();
        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container = StreamMessageListenerContainer
                .create(connectionFactory, options);
        //???????????????????????????
        StreamOffset<String> offset = StreamOffset.create(STREAM_READY_QUEUE + delayQueueConfig.getTopic(), ReadOffset.lastConsumed());
        //???????????????
        Consumer consumer = Consumer.from(delayQueueConfig.getGroup(), delayQueueConfig.getConsumer());
        StreamMessageListenerContainer.StreamReadRequest<String> streamReadRequest = StreamMessageListenerContainer.StreamReadRequest.builder(offset)
                .errorHandler((error) -> {
                })
                .cancelOnError(e -> false)
                .consumer(consumer)
                //????????????ack??????
                .autoAcknowledge(false)
                .build();
        //?????????????????????
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
        // key??????String??????????????????
        template.setKeySerializer(stringRedisSerializer);
        // hash???key?????????String??????????????????
        template.setHashKeySerializer(stringRedisSerializer);
        // value?????????????????????jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash???value?????????????????????jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public DefaultStreamListener defaultStreamListener() {
        return new DefaultStreamListener();
    }
}
