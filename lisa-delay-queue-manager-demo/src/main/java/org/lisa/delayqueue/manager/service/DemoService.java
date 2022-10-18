package org.lisa.delayqueue.manager.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/9/25
 */
@Slf4j
@Service
public class DemoService {

    public void sayHello() {
        log.info("hello world");
    }
}
