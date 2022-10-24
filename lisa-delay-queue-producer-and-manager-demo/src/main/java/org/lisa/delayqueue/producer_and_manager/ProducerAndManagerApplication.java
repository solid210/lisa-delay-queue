package org.lisa.delayqueue.producer_and_manager;


import org.lisa.delayqueue.base.util.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * @author solid
 */
@SpringBootApplication
@Import(SpringContextUtil.class)
public class ProducerAndManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProducerAndManagerApplication.class, args);
    }

}
