package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author Yuehong Zhang
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public void savePaymentInfo(OrderInfo orderInfo, String paymentType) {
        // Conditional judgment
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderInfo.getId());
        queryWrapper.eq("payment_type", paymentType);
        Integer count = paymentInfoMapper.selectCount(queryWrapper);
        if (count!=0){
            return;
        }

        // Insert data directly into paymentInfo;
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());

        paymentInfoMapper.insert(paymentInfo);

    }

    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo, String name) {
        // write directly
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoQueryWrapper.eq("payment_type",name);
        return paymentInfoMapper.selectOne(paymentInfoQueryWrapper);

    }

    @Override
    public void paySuccess(String outTradeNo, String name, Map<String, String> paramMap) {
        // If the transaction record status is CLOSED, PAID, it will return directly without further processing!
        PaymentInfo paymentInfoQuery = getPaymentInfo(outTradeNo, name);

        // Judgment status
        if("CLOSED".equals(paymentInfoQuery.getPaymentStatus())
                || "PAID".equals(paymentInfoQuery.getPaymentStatus())){
            return;
        }

        // trade_no, payment_status, callback_time, callback_content
        // The first parameter: updated content! The condition of the second parameter update
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setTradeNo(paramMap.get("trade_no"));
        paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(paramMap.toString());

        // Build conditions
        UpdateWrapper<PaymentInfo> paymentInfoUpdateWrapper = new UpdateWrapper<>();
        paymentInfoUpdateWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoUpdateWrapper.eq("payment_type",name);
        //  update data
        paymentInfoMapper.update(paymentInfo,paymentInfoUpdateWrapper);

        // this.updatePaymentInfo(outTradeNo,name,paymentInfo);
        // Send a message to the order! Update order status: orderId, outTradeNo can be passed;
        // outTradeNo = paramMap.get("out_trade_no"); paymentInfoQuery.getOutTradeNo();
        // orderId = paymentInfoQuery.getOrderId();
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,MqConst.ROUTING_PAYMENT_PAY,paymentInfoQuery.getOrderId());

    }
    // Internally encapsulated!
    public void updatePaymentInfo(String outTradeNo, String name, PaymentInfo paymentInfo) {
        UpdateWrapper<PaymentInfo> paymentInfoUpdateWrapper = new UpdateWrapper<>();
        paymentInfoUpdateWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoUpdateWrapper.eq("payment_type",name);
        //  update data
        paymentInfoMapper.update(paymentInfo,paymentInfoUpdateWrapper);
    }

    @Override
    public void closePayment(Long orderId) {
        // When making an order, when will the transaction record be generated in paymentInfo? Only when there is a QR code will there be a local paymentInfo record!
        // Close the transaction record; payment_status
        UpdateWrapper<PaymentInfo> paymentInfoUpdateWrapper = new UpdateWrapper<>();
        paymentInfoUpdateWrapper.eq("order_id",orderId);
        // Determine whether there is data generated!
        Integer count = paymentInfoMapper.selectCount(paymentInfoUpdateWrapper);
        if (count==0){
            return;
        }

        //  renew
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());
        // The first parameter indicates the modified content, and the second parameter indicates the update condition!
        paymentInfoMapper.update(paymentInfo,paymentInfoUpdateWrapper);
    }
}
