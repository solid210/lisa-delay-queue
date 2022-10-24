package org.lisa.delayqueue.manager.config;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lisa.delayqueue.base.config.DelayQueueConfigProperties;
import org.lisa.delayqueue.base.entity.Message;
import org.lisa.delayqueue.base.util.SpringContextUtil;
import org.lisa.delayqueue.manager.scheduled.CleanStreamJob;
import org.lisa.delayqueue.manager.scheduled.MoveMessageToReadyQueueJob;
import org.lisa.delayqueue.manager.scheduled.ProcessPendingMessageJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.Resource;
import java.util.Collections;

import static org.lisa.delayqueue.base.constant.Constant.*;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/9/24
 */
@Slf4j
@ConditionalOnProperty(prefix = DelayQueueConfigProperties.LISA_DELAY_QUEUE_CONFIG_PREFIX, name = "enabled", havingValue = "true")
@EnableConfigurationProperties({DelayQueueConfigProperties.class, ProcessPendingMessageConfig.class})
@Configuration
@Import(SpringContextUtil.class)
@EnableScheduling
public class DelayQueueManagerAutoConfiguration implements InitializingBean {

    @Resource
    private DelayQueueConfigProperties delayQueueConfigProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
    public MoveMessageToReadyQueueJob moveMessageToReadyQueueJob() {
        log.info("[{}] New MoveMessageToReadyQueueJob", SERVER_NAME_MANAGER);
        return new MoveMessageToReadyQueueJob();
    }

    @Bean
    public CleanStreamJob cleanStreamJob() {
        log.info("[{}] New CleanStreamJob", SERVER_NAME_MANAGER);
        return new CleanStreamJob();
    }

    @Bean
    public ProcessPendingMessageJob processPendingMessageJob() {
        log.info("[{}] New ProcessPendingMessageJob", SERVER_NAME_MANAGER);
        return new ProcessPendingMessageJob();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("[{}] Init message queue data of topic", SERVER_NAME_MANAGER);
        delayQueueConfigProperties.getGroups()
                .forEach(delayQueueConfig -> {
                    /**
                     * 1. 初始化waiting queue (zset)
                     * key = 前缀+topic
                     * value = msgId
                     * score = 消息触发时间戳
                     */
                    initWaitingQueueZset(delayQueueConfig);

                    /**
                     * 2. 初始化ready queue (stream)
                     * key = 前缀+topic
                     * value = msgId|score
                     */
                    initReadyQueueStream(delayQueueConfig);

                    /**
                     * 3. 初始化retry queue (zset)
                     * key = 前缀+topic
                     * value = msgId
                     * score = 下次重试触发时间戳
                     */
                    initRetryQueueZset(delayQueueConfig);

                    /**
                     * 4. 初始化retry count（hash）
                     * key = 前缀+topic
                     * field = msgId
                     * value = 可以重试的次数
                     */
                    initRetryCountHash(delayQueueConfig);

                    /**
                     * 5. 初始化garbage key(set)
                     * key = 前缀+topic
                     * value = msgId
                     * score = 放入时间
                     * 当超过重试次数一直无法消费掉的msgId，放入这里
                     * 为了防止该垃圾回收膨胀，会定期清除一些数据（根据数量和时间清除）
                     */
                    initGarbageKeySet(delayQueueConfig);
                });

    }

    private void initWaitingQueueZset(DelayQueueConfigProperties.DelayQueueConfig delayQueueConfig) {
        initZset(ZSET_WAITING_QUEUE + delayQueueConfig.getTopic());
    }

    private void initReadyQueueStream(DelayQueueConfigProperties.DelayQueueConfig delayQueueConfig) {
        String streamKey = STREAM_READY_QUEUE + delayQueueConfig.getTopic();
        StringRecord stringRecord = StreamRecords.string(Collections.singletonMap("name", JSONObject.toJSONString(new Message<>(INIT_MESSAGE)))).withStreamKey(streamKey);
        stringRedisTemplate.opsForStream().add(stringRecord);
        StreamInfo.XInfoGroups xInfoGroups = stringRedisTemplate.opsForStream().groups(streamKey);
        if (xInfoGroups.isEmpty()) {
            stringRedisTemplate.opsForStream().createGroup(streamKey, delayQueueConfig.getGroup());
        }
    }

    private void initRetryQueueZset(DelayQueueConfigProperties.DelayQueueConfig delayQueueConfig) {
        initZset(ZSET_RETRY_QUEUE + delayQueueConfig.getTopic());
    }

    private void initRetryCountHash(DelayQueueConfigProperties.DelayQueueConfig delayQueueConfig) {
        // 无需初始化，使用时会自动初始化
    }

    private void initGarbageKeySet(DelayQueueConfigProperties.DelayQueueConfig delayQueueConfig) {
        // 无需初始化，使用时会自动初始化
    }

    private void initZset(String zsetKey){
        stringRedisTemplate.opsForZSet().remove(zsetKey, INIT_MESSAGE);
        stringRedisTemplate.opsForZSet().add(zsetKey, INIT_MESSAGE, -1);
    }
}
