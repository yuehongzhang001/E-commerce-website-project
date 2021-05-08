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
 * @author mqx
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public void savePaymentInfo(OrderInfo orderInfo, String paymentType) {
        //  条件判断
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderInfo.getId());
        queryWrapper.eq("payment_type", paymentType);
        Integer count = paymentInfoMapper.selectCount(queryWrapper);
        if (count!=0){
            return;
        }

        //  直接插入数据到paymentInfo;
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
        //  直接写
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoQueryWrapper.eq("payment_type",name);
        return paymentInfoMapper.selectOne(paymentInfoQueryWrapper);

    }

    @Override
    public void paySuccess(String outTradeNo, String name, Map<String, String> paramMap) {
        //  交易记录状态如何是CLOSED ，PAID ,则直接返回不需要继续处理！
        PaymentInfo paymentInfoQuery = getPaymentInfo(outTradeNo, name);

        //  判断状态
        if("CLOSED".equals(paymentInfoQuery.getPaymentStatus())
            || "PAID".equals(paymentInfoQuery.getPaymentStatus())){
            return;
        }

        //  trade_no , payment_status,callback_time,callback_content
        //  第一个参数：更新的内容！ 第二个参数更新的条件
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setTradeNo(paramMap.get("trade_no"));
        paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(paramMap.toString());

        //  构建条件
        UpdateWrapper<PaymentInfo> paymentInfoUpdateWrapper = new UpdateWrapper<>();
        paymentInfoUpdateWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoUpdateWrapper.eq("payment_type",name);
        //  更新数据
        paymentInfoMapper.update(paymentInfo,paymentInfoUpdateWrapper);

        //  this.updatePaymentInfo(outTradeNo,name,paymentInfo);
        //  发送消息给订单！ 更新订单状态： 可以传递orderId , outTradeNo;
        //  outTradeNo = paramMap.get("out_trade_no");  paymentInfoQuery.getOutTradeNo();
        //  orderId = paymentInfoQuery.getOrderId();
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,MqConst.ROUTING_PAYMENT_PAY,paymentInfoQuery.getOrderId());

    }
    //  内部封装的！
    public void updatePaymentInfo(String outTradeNo, String name, PaymentInfo paymentInfo) {
        UpdateWrapper<PaymentInfo> paymentInfoUpdateWrapper = new UpdateWrapper<>();
        paymentInfoUpdateWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoUpdateWrapper.eq("payment_type",name);
        //  更新数据
        paymentInfoMapper.update(paymentInfo,paymentInfoUpdateWrapper);
    }

    @Override
    public void closePayment(Long orderId) {
        //  在做订单的时候，什么时候才会在paymentInfo 中产生交易记录? 在有二维码的时候才会有本地paymentInfo 记录！
        //  关闭交易记录;payment_status
        UpdateWrapper<PaymentInfo> paymentInfoUpdateWrapper = new UpdateWrapper<>();
        paymentInfoUpdateWrapper.eq("order_id",orderId);
        //  判断是否有数据产生！
        Integer count = paymentInfoMapper.selectCount(paymentInfoUpdateWrapper);
        if (count==0){
            return;
        }

        //  更新
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());
        //  第一个参数表示修改的内容，第二个参数表示更新的条件！
        paymentInfoMapper.update(paymentInfo,paymentInfoUpdateWrapper);
    }
}
