package org.lisa.delayqueue.producer.config;

import org.lisa.delayqueue.base.config.DelayQueueConfigProperties;
import org.lisa.delayqueue.base.util.SpringContextUtil;
import org.lisa.delayqueue.producer.service.impl.ProducerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

import static org.lisa.delayqueue.base.config.DelayQueueConfigProperties.LISA_DELAY_QUEUE_CONFIG_PREFIX;
import static org.lisa.delayqueue.base.constant.Constant.BEAN_NAME_SUFFIX_MESSAGE_PRODUCER;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/9/24
 */
@ConditionalOnProperty(prefix = LISA_DELAY_QUEUE_CONFIG_PREFIX, name = "enabled", havingValue = "true")
//检查依赖的lisa-delay-queue配置是否存在
@Configuration
@EnableConfigurationProperties(DelayQueueConfigProperties.class)
@Import(SpringContextUtil.class)
@Slf4j
public class DelayQueueProducerAutoConfiguration implements EnvironmentAware, BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Resource
    private DelayQueueConfigProperties delayQueueConfigProperties;

    private static final String PROPERTY_NAME_DELAY_QUEUE_CONFIG = "delayQueueConfig";

    private static final String PROPERTY_NAME_STRING_REDIS_TEMPLATE = "stringRedisTemplate";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        delayQueueConfigProperties.getGroups()
                .forEach(delayQueueConfig -> {
                    ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
                    DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ProducerImpl.class);
                    beanDefinitionBuilder.addPropertyValue(PROPERTY_NAME_DELAY_QUEUE_CONFIG, delayQueueConfig);
                    beanDefinitionBuilder.addPropertyReference(PROPERTY_NAME_STRING_REDIS_TEMPLATE, PROPERTY_NAME_STRING_REDIS_TEMPLATE);
                    defaultListableBeanFactory.registerBeanDefinition(delayQueueConfig.getTopic() + BEAN_NAME_SUFFIX_MESSAGE_PRODUCER, beanDefinitionBuilder.getRawBeanDefinition());
                });
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void setEnvironment(Environment environment) {
        BindResult<DelayQueueConfigProperties> bindResult = Binder.get(environment).bind(LISA_DELAY_QUEUE_CONFIG_PREFIX, DelayQueueConfigProperties.class);
        delayQueueConfigProperties = bindResult.get();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
