package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

/**
 * @author Yuehong Zhang
 */
public interface PaymentService {

    // Save transaction record interface
    void savePaymentInfo(OrderInfo orderInfo, String paymentType);

    // Query transaction records according to the merchant order number
    PaymentInfo getPaymentInfo(String outTradeNo, String name);

    // Update transaction records
    void paySuccess(String outTradeNo, String name, Map<String, String> paramMap);

    // Update transaction records
    void updatePaymentInfo(String outTradeNo, String name, PaymentInfo paymentInfo);

    // Close transaction history
    void closePayment(Long orderId);

}