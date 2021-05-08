package com.atguigu.gmall.task.scheduled;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author mqx
 */
@Component
@EnableScheduling
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    //  定义每天凌晨1点钟！0 0 1 * * ?
    //  编写一个定时任务！
    @Scheduled(cron = "0/10 * * * * ?")
    public void sendMsg(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_1,"寂寞.....");
        //System.out.println("寂寞.....");
    }

    //  编写一个定时任务！
    @Scheduled(cron = "0 0 18 * * ?")
    public void clearReids(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_18,"delete....");
        //System.out.println("寂寞.....");
    }
}
