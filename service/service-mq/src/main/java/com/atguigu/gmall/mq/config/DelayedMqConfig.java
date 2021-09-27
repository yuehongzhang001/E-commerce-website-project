package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @author Yuehong Zhang
 */
@Configuration
public class DelayedMqConfig {

    //  定义变量：
    public static final String exchange_delay = "exchange.delay";
    public static final String routing_delay = "routing.delay";
    public static final String queue_delay_1 = "queue.delay.1";


    //  创建队列
    @Bean
    public Queue delayQeue(){
        //  消息的过期时间，以及交换机，路由键！
        //        HashMap<String, Object> map = new HashMap<>();
        //        map.put("x-dead-letter-exchange",exchange_dead);
        //        map.put("x-dead-letter-routing-key",routing_dead_2);
        //        //  设置消息的TTL。
        //        map.put("x-message-ttl",10000);
        //  使用插件的时候：不需要在队列中进行设置！ 这个延迟时间在哪设置?
        return new Queue(queue_delay_1,true,false,false);

    }
    //  创建交换机
    @Bean
    public CustomExchange delayExchange(){
        //  需要在这个交换机上进行配置！
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-delayed-type","direct");
        return new CustomExchange(exchange_delay,"x-delayed-message",true,false,map);
    }
    //  配置绑定关系！
    @Bean
    public Binding delaybinding(){
        //   return BindingBuilder.bind(queue1()).to(exchange()).with(routing_dead_1);
        return BindingBuilder.bind(delayQeue()).to(delayExchange()).with(routing_delay).noargs();
    }



}
