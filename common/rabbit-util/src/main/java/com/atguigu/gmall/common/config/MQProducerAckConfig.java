package com.atguigu.gmall.common.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author mqx
 */
@Component
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {


    //  发送消息： RabbitTemplate
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //  写一个方法
    //  修饰一个非静态的void（）方法,在服务器加载Servlet的时候运行，并且只会被服务器执行一次在构造函数之后执行，init（）方法之前执行。
    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    /**
     * 消息成功发送到交换机上
     * @param correlationData 数据载体带有Id标识的！
     * @param ack   消息是否发送成功
     * @param cause 消息发送失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if(ack){
            //  消息成功发送到了交换机
            System.out.println("消息发送成功！");
        }else {
            System.out.println("消息发送异常");
        }
    }

    /**
     * 表示消息如果没有成功发送到队列则会执行当前这个方法！
     * @param message
     * @param replyCode
     * @param replyText
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("消息主体: " + new String(message.getBody()));
        System.out.println("应答码: " + replyCode);
        System.out.println("描述：" + replyText);
        System.out.println("消息使用的交换器 exchange : " + exchange);
        System.out.println("消息使用的路由键 routing : " + routingKey);
    }
}
