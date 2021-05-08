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
 * @author mqx
 */
@Configuration
public class OrderCanelMqConfig {

    //  创建队列
    @Bean
    public Queue delayQeue(){
        //  使用插件的时候：不需要在队列中进行设置！ 这个延迟时间在哪设置?
        return new Queue(MqConst.QUEUE_ORDER_CANCEL,true,false,false);

    }
    //  创建交换机
    @Bean
    public CustomExchange delayExchange(){
        //  需要在这个交换机上进行配置！
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-delayed-type","direct");
        return new CustomExchange(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,"x-delayed-message",true,false,map);
    }
    //  配置绑定关系！
    @Bean
    public Binding delaybinding(){
        //   return BindingBuilder.bind(queue1()).to(exchange()).with(routing_dead_1);
        return BindingBuilder.bind(delayQeue()).to(delayExchange()).with(MqConst.ROUTING_ORDER_CANCEL).noargs();
    }
}
