package com.atguigu.gmall.order.config;

import com.atguigu.gmall.common.constant.MqConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @author Yuehong Zhang
 */
@Configuration
public class OrderCanelMqConfig {

    // Create a queue
    @Bean
    public Queue delayQeue(){
        // When using the plug-in: no need to set in the queue! Where is this delay time set?
        return new Queue(MqConst.QUEUE_ORDER_CANCEL,true,false,false);

    }
    // Create a switch
    @Bean
    public CustomExchange delayExchange(){
        // Need to be configured on this switch!
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-delayed-type","direct");
        return new CustomExchange(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,"x-delayed-message",true,false,map);
    }
    // Configure the binding relationship!
    @Bean
    public Binding delaybinding(){
        // return BindingBuilder.bind(queue1()).to(exchange()).with(routing_dead_1);
        return BindingBuilder.bind(delayQeue()).to(delayExchange()).with(MqConst.ROUTING_ORDER_CANCEL).noargs();
    }
}