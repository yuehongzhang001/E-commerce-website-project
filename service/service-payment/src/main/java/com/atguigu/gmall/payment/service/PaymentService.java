package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

/**
 * @author mqx
 */
public interface PaymentService {

    //  保存交易记录接口
    void savePaymentInfo(OrderInfo orderInfo, String paymentType);

    //  根据商户订单号查询交易记录
    PaymentInfo getPaymentInfo(String outTradeNo, String name);

    //  更新交易记录
    void paySuccess(String outTradeNo, String name, Map<String, String> paramMap);

    //  更新交易记录
    void updatePaymentInfo(String outTradeNo, String name, PaymentInfo paymentInfo);

    //  关闭交易记录
    void closePayment(Long orderId);

}
