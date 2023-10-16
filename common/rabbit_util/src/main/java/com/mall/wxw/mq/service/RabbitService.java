package com.mall.wxw.mq.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: wxw24633
 * @Time: 2023/10/16  14:49
 */
@Service
public class RabbitService {
    //  引入操作rabbitmq 的模板
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param exchange  交换机
     * @param routingKey    路由键
     * @param message   消息
     */
    public boolean sendMessage(String exchange,String routingKey, Object message){
        //  调用发送数据的方法
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
        return true;
    }


}
