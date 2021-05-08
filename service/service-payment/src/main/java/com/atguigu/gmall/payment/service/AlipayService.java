package com.atguigu.gmall.payment.service;

import com.alipay.api.AlipayApiException;

/**
 * @author mqx
 */
public interface AlipayService {

    //  支付：
    String createaliPay(Long orderId) throws AlipayApiException;

    //  退款
    boolean refund(Long orderId);
    //  关闭支付宝交易
    boolean closePay(Long orderId);

    //  查询是否有交易记录
    boolean checkPayment(Long orderId);
}
