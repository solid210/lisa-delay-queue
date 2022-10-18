package org.lisa.delayqueue.producer;


import org.lisa.delayqueue.base.util.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * @author solid
 */
@SpringBootApplication
@Import(SpringContextUtil.class)
public class ProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

}
