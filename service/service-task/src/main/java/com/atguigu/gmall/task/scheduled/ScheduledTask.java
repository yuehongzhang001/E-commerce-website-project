package com.atguigu.gmall.task.scheduled;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Yuehong Zhang
 */
@Component
@EnableScheduling
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    // Define 1 o'clock in the morning every day! 0 0 1 * *?
    // Write a timed task!
    @Scheduled(cron = "0/10 * * * * ?")
    public void sendMsg(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_1,"Lonely...");
        //System.out.println("Lonely.....");
    }

    // Write a timed task!
    @Scheduled(cron = "0 0 18 * * ?")
    public void clearReids(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_18,"delete....");
        //System.out.println("Lonely.....");
    }
}