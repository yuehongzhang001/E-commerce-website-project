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
 * @author mqx
 */
//  @Configuration // 制作配置类的！
@Component
public class OrderReceiver {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentFeignClient paymentFeignClient;

    //  第一种：绑定：
    //  使用延迟消息：制作配置类：直接监听队列
    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void orderCancel(Long orderId,Message message,Channel channel){
        //  关闭 orderInfo，paymentInfo, 支付宝交易记录 ！
        //  取消订单的业务逻辑
        if (orderId!=null){
            //  根据当前的orderId 查询订单对象
            OrderInfo orderInfo = orderService.getById(orderId);
            //  判断状态！ 订单的状态，进度状态是未付款！
            if (orderInfo!=null && "UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())){
                //  是否存在电商交易记录
                PaymentInfo paymentInfo = paymentFeignClient.getPaymentInfo(orderInfo.getOutTradeNo());
                //  判断paymentInfo
                if (paymentInfo!=null && "UNPAID".equals(paymentInfo.getPaymentStatus())){
                    //  说明电商本地有交易记录：
                    //  orderService.execExpiredOrder(orderId,"2");
                    //  调用查询支付宝的交易记录：
                    Boolean result = paymentFeignClient.checkPayment(orderId);
                    //  result = true ;
                    if (result){
                         // 判断是否能够关闭支付宝交易！
                        Boolean flag = paymentFeignClient.closePay(orderId);
                        //  flag = true;
                        if (flag){
                            //  表示未支付 ：orderInfo ，paymentInfo
                            orderService.execExpiredOrder(orderId,"2");
                        }else {
                            //  表示支付成功！
                            //  用户正常支付成功了！
                        }

                    }else {
                        //  支付宝没有交易记录！orderInfo，paymentInfo;
                        orderService.execExpiredOrder(orderId,"2");
                    }

                }else {
                    //  只存在orderInfo
                    //  修改订单状态！ CLOSED!
                    //  execExpiredOrder 这个方法有关闭orderInfo，还有关闭paymentInfo;
                    orderService.execExpiredOrder(orderId,"1");
                }
            }
        }
        //  手动确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    //  监听订单消息
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            key = {MqConst.ROUTING_PAYMENT_PAY}
    ))
    public void paymentPay(Long orderId, Message message, Channel channel){
        //  判断
        if(orderId!=null){
            //  更新订单状态！
            //  根据当前的orderId 查询订单对象
            OrderInfo orderInfo = orderService.getById(orderId);
            if (orderInfo!=null && "UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())){
                //  修改订单状态！ PAID!
                orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
                //  发送消息给库存！ 库存要做什么事? 减少库存数量！
                orderService.sendOrderStatus(orderId);
            }
        }
        //  手动确认ack
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }


    //  监听库存系统发送的减库存结果： 发送过来的数据！
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = {MqConst.ROUTING_WARE_ORDER}
    ))
    public void updateOrderStatus(String wareJson, Message message, Channel channel){
        //  判断
        if(!StringUtils.isEmpty(wareJson)){
            // wareJson 转华为Map
            Map map = JSON.parseObject(wareJson, Map.class);
            String orderId = (String) map.get("orderId");
            String status = (String) map.get("status");
            //  判断
            if ("DEDUCTED".equals(status)){
                //  减库存成功！更新订单状态
                orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.WAITING_DELEVER);
            }else {
                //  异常情况下：  1.  远程调用补货！ 2.  人工客服小姐姐：
                orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.STOCK_EXCEPTION);
                //  订单--- 支付 --- 库存！ 看做一个分布式事务！mq 解决！ 最终一致性！
            }
        }
        //  手动确认ack
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }



}
