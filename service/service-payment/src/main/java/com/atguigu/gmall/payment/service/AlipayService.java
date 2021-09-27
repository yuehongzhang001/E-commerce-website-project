package com.atguigu.gmall.payment.service;

import com.alipay.api.AlipayApiException;

/**
 * @author Yuehong Zhang
 */
public interface AlipayService {

    // Payment:
    String createaliPay(Long orderId) throws AlipayApiException;

    //  Refund
    boolean refund(Long orderId);
    // Close Alipay transaction
    boolean closePay(Long orderId);

    // Query whether there are transaction records
    boolean checkPayment(Long orderId);
}
