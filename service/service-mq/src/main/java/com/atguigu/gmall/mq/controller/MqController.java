package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Yuehong Zhang
 */
@RestController
@RequestMapping("/mq")
public class MqController {

    //  localhost:8282/mq/sendConfirm
    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //  发送消息的映射路径
    @GetMapping("sendConfirm")
    public Result sendConfirm(){
        //  发送消息
        rabbitService.sendMessage("exchange.confirm","routing.confirm888","您来晚了，没有服务了！");

        return Result.ok();
    }

    //  发送消息
    @GetMapping("sendDeadLettle")
    public Result sendDeadLettle(){
        //  需要知道当前的系统时间！
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("发送时间：\t"+simpleDateFormat.format(new Date()));
        //  设置发送的消息！
        rabbitService.sendMessage(DeadLetterMqConfig.exchange_dead,DeadLetterMqConfig.routing_dead_1,"来晚了，没有服务了！");
        return Result.ok();
    }

    @GetMapping("sendDelay")
    public Result sendDelay(){
        //  需要知道当前的系统时间！
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //  设置发送的消息！ 使用插件的时候：不需要在队列中进行设置！ 这个延迟时间在哪设置?
        //  rabbitService.sendMessage(DeadLetterMqConfig.exchange_dead,DeadLetterMqConfig.routing_dead_1,"来晚了，没有服务了！");
        rabbitTemplate.convertAndSend(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, "来的真早，提前享受服务！", new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //  这个延迟时间在哪设置?
                message.getMessageProperties().setDelay(10000);
                System.out.println("发送时间：\t"+simpleDateFormat.format(new Date()));
                return message;
            }
        });
        return Result.ok();
    }
}
