package com.atguigu.gmall.common.service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Yuehong Zhang
 */
@Service
public class RabbitService {

    // Write a method to send a message
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // Indicates that the message was successfully sent
    public boolean sendMessage(String exchange, String routingKey, Object message){
        //  send messages!
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
        return true;
    }

    // send delayed message
    public boolean sendDelayMessage(String exchange, String routingKey, Object message,int delayTime){
        //  send messages!
        rabbitTemplate.convertAndSend(exchange, routingKey, message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                // Set the delay time
                message.getMessageProperties().setDelay(delayTime*1000);
                return message;
            }
        });
        return true;
    }
}