package org.lisa.delayqueue.producer_and_manager.controller;


import io.lettuce.core.dynamic.annotation.Param;
import org.lisa.delayqueue.producer_and_manager.service.DemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author solid
 */
@RestController
public class RedisStreamController {

    @Resource
    private DemoService demoService;

    @GetMapping("produceMsg")
    public void produceMsg(@Param("msg") String msg) {
        demoService.test(msg);
    }
}
