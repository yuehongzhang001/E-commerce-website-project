package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @author Yuehong Zhang
 */
@Configuration // 配置类！
public class DeadLetterMqConfig {

    //  定义变量：
    public static final String exchange_dead = "exchange.dead";
    public static final String routing_dead_1 = "routing.dead.1";
    public static final String routing_dead_2 = "routing.dead.2";
    public static final String queue_dead_1 = "queue.dead.1";
    public static final String queue_dead_2 = "queue.dead.2";


    //  创建一个队列
    @Bean
    public Queue queue1(){
        //  创建一个map
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange",exchange_dead);
        map.put("x-dead-letter-routing-key",routing_dead_2);
        //  设置消息的TTL。
        map.put("x-message-ttl",10000);
        // 队列名称，是否持久化，是否独享、排外的【true:只可以在本次连接中访问】，是否自动删除，队列的其他属性参数
        return  new Queue(queue_dead_1,true,false,false,map);
    }

    //  声明一个交换机
    @Bean
    public DirectExchange exchange(){
        return new DirectExchange(exchange_dead,true,false);
    }

    //  设置绑定关系！
    @Bean
    public Binding binding1(){
        //  返回绑定关系！
        return BindingBuilder.bind(queue1()).to(exchange()).with(routing_dead_1);
    }

    //  设置队列2
    @Bean
    public Queue queue2(){
        //  返回！
        return new Queue(queue_dead_2,true,false,false,null);
    }

    //  设置绑定关系！
    @Bean
    public Binding binding2(){
        //  返回绑定关系！
        return BindingBuilder.bind(queue2()).to(exchange()).with(routing_dead_2);
    }
}
