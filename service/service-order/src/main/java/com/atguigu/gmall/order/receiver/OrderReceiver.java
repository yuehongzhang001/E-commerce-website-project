package com.atguigu.gmall.order.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.payment.client.PaymentFeignClient;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author Yuehong Zhang
 */
// @Configuration // Make the configuration class!
@Component
public class OrderReceiver {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentFeignClient paymentFeignClient;

    // The first type: binding:
    // Use delayed messages: make configuration classes: directly listen to the queue
    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void orderCancel(Long orderId,Message message,Channel channel){
        // Close orderInfo, paymentInfo, and Alipay transaction records!
        // The business logic of canceling the order
        if (orderId!=null){
            // Query the order object according to the current orderId
            OrderInfo orderInfo = orderService.getById(orderId);
            // Determine the status! The status of the order, the progress status is unpaid!
            if (orderInfo!=null && "UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())){
                // Whether there is an e-commerce transaction record
                PaymentInfo paymentInfo = paymentFeignClient.getPaymentInfo(orderInfo.getOutTradeNo());
                // Determine paymentInfo
                if (paymentInfo!=null && "UNPAID".equals(paymentInfo.getPaymentStatus())){
                    // Explain that the e-commerce company has transaction records locally:
                    // orderService.execExpiredOrder(orderId,"2");
                    // Call to query Alipay's transaction records:
                    Boolean result = paymentFeignClient.checkPayment(orderId);
                    // result = true;
                    if (result){
                        // Determine whether the Alipay transaction can be closed!
                        Boolean flag = paymentFeignClient.closePay(orderId);
                        // flag = true;
                        if (flag){
                            // Indicates unpaid: orderInfo, paymentInfo
                            orderService.execExpiredOrder(orderId,"2");
                        }else {
                            // Indicates that the payment was successful!
                            // The user has successfully paid normally!
                        }

                    }else {
                        // Alipay has no transaction records! orderInfo, paymentInfo;
                        orderService.execExpiredOrder(orderId,"2");
                    }
                }else {
                    // Only orderInfo exists
                    // Modify the order status! CLOSED!
                    // The execExpiredOrder method closes orderInfo and closes paymentInfo;
                    orderService.execExpiredOrder(orderId,"1");
                }
            }
        }
        // Manually confirm the message
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    // monitor order message
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            key = {MqConst.ROUTING_PAYMENT_PAY}
    ))
    public void paymentPay(Long orderId, Message message, Channel channel){
        //  judge
        if(orderId!=null){
            // Update order status!
            // Query the order object according to the current orderId
            OrderInfo orderInfo = orderService.getById(orderId);
            if (orderInfo!=null && "UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())){
                // Modify the order status! PAID!
                orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
                // Send message to inventory! What to do with inventory? Reduce inventory!
                orderService.sendOrderStatus(orderId);
            }
        }
        // Manually confirm ack
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }


    // Monitor the inventory reduction result sent by the inventory system: the data sent!
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = {MqConst.ROUTING_WARE_ORDER}
    ))
    public void updateOrderStatus(String wareJson, Message message, Channel channel){
        //  judge
        if(!StringUtils.isEmpty(wareJson)){
            // wareJson to Huawei Map
            Map map = JSON.parseObject(wareJson, Map.class);
            String orderId = (String) map.get("orderId");
            String status = (String) map.get("status");
            //  judge
            if ("DEDUCTED".equals(status)){
                // Successfully reduce inventory! Update order status
                orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.WAITING_DELEVER);
            }else {
                // Under abnormal conditions: 1. Remotely call replenishment! 2. Little sister of manual customer service:
                orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.STOCK_EXCEPTION);
                // Order --- Payment --- Inventory! Think of it as a distributed transaction! mq solved! Ultimate consistency!
            }
        }
        // Manually confirm ack
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }



}
