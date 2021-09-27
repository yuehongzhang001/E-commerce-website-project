package com.atguigu.gmall.common.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Yuehong Zhang
 */
@Component
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {


    // Send message: RabbitTemplate
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // write a method
    // Decorate a non-static void() method, which runs when the server loads the Servlet, and will only be executed by the server once after the constructor, and before the init() method.
    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    /**
     * The message was successfully sent to the switch
     * @param correlationData data carrier with Id mark!
     * @param ack whether the message was sent successfully
     * @param cause The reason why the message failed to be sent
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if(ack){
            // The message was successfully sent to the switch
            System.out.println("Message sent successfully!");
        }else {
            System.out.println("Message sending exception");
        }
    }

    /**
     * Indicates that the current method will be executed if the message is not successfully sent to the queue!
     * @param message
     * @param replyCode
     * @param replyText
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("Message body: "+ new String(message.getBody()));
        System.out.println("Reply Code: "+ replyCode);
        System.out.println("Description:" + replyText);
        System.out.println("The exchange used by the message exchange: "+ exchange);
        System.out.println("The routing key used by the message routing: "+ routingKey);
    }
}