package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mqx
 */
@Component
public class ConfirmReceiver {

    //  监听获取消息！ 需要设置绑定关系！
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm",durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm"),
            key = {"routing.confirm"}
    ))
    public void getMsg(String msg , Message message, Channel channel){
        try {
            System.out.println("接收到的消息：\t"+new String(message.getBody()));
            System.out.println("接收到的消息：\t"+msg);
            //  手动确认：ack
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            //  false: 表示一个一个的确认，true 表示批量确认消息！
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            e.printStackTrace();
            //  log.info("......")
            //  第三个参数：是否重回队列！
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
        }
    }
    //  监听消息！
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void getMsg1(String msg,Message message,Channel channel) throws IOException {
        //  设置时间格式：
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println("接收到的消息"+msg+"时间：\t"+simpleDateFormat.format(new Date()));

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    //  监听延迟队列消息：基于插件
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void getMsg2(String msg,Message message,Channel channel) throws IOException {
        //  设置时间格式：
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println("接收到的消息"+msg+"时间：\t"+simpleDateFormat.format(new Date()));

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

}
