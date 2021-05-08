package com.atguigu.gmall.list.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.list.service.SearchService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mqx
 */
@Component
public class ListReceiver {

    @Autowired
    private SearchService searchService;

    //  监听消息 实现商品上架 发送的消息主体：skuId
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = {MqConst.ROUTING_GOODS_UPPER}
    ))
    public void upperGoods(Long skuId, Message message, Channel channel){
        //  有消息
        if (skuId!=null){
            //  实现上架
            searchService.upperGoods(skuId);
        }
        //  消息确认：
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    //  监听消息 实现商品上架 发送的消息主体：skuId
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = {MqConst.ROUTING_GOODS_LOWER}
    ))
    public void lowerGoods(Long skuId, Message message, Channel channel){
        //  有消息
        if (skuId!=null){
            //  实现上架
            searchService.lowerGoods(skuId);
        }
        //  消息确认：
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
