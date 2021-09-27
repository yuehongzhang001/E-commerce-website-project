package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author Yuehong Zhang
 */
@Service
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private PaymentService paymentService;

    @Override
    public String createaliPay(Long orderId) throws AlipayApiException {


        // Get orderInfo or paymentInfo
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        // Save the transaction record while producing the QR code
        // Default Alipay payment
        paymentService.savePaymentInfo(orderInfo, PaymentType.ALIPAY.name());

        /*
        1. Produce QR code! Cancel order: 5 seconds;
        2. If the order is cancelled, the QR code cannot be produced!
         */
        if("CLOSED".equals(orderInfo.getOrderStatus())){
            return "The order has been cancelled!";
        }

        // AlipayClient alipayClient = new DefaultAlipayClient( "https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //Get initialized AlipayClient
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest(); //Create the request corresponding to the API
        // Synchronous callback
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        // Asynchronous callback
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url); //Set the return jump and notification address in the public parameters

        // Need json string
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        // map.put("total_amount",orderInfo.getTotalAmount());
        map.put("total_amount","0.01");
        map.put("subject",orderInfo.getTradeBody());
        map.put("timeout_express","5m");
        // map is converted to Json
        String jsonStr = JSON.toJSONString(map);
        alipayRequest.setBizContent(jsonStr);

        String form = alipayClient.pageExecute(alipayRequest).getBody();

        return form;
    }

    @Override
    public boolean refund(Long orderId) {

        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);

        // Declare the refund request object
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        // Construct Json string
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        // refund amount <payment amount
        map.put("refund_amount","0.01");
        map.put("refund_reason","Not good!");

        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("Call successful");
            // E-commerce platform orders are closed!
            PaymentInfo updPaymentInfo = new PaymentInfo();
            updPaymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());
            this.paymentService.updatePaymentInfo(orderInfo.getOutTradeNo(),PaymentType.ALIPAY.name(),updPaymentInfo);
            // Order status is closed!

            return true;
        } else {
            System.out.println("Call failed");
            return false;
        }

    }

    @Override
    public boolean closePay(Long orderId) {
        // Get orderInfo according to orderId
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        // Create a close object
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("operator_id","YX01");
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeCloseResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("Call successful");
            return true;
        } else {
            System.out.println("Call failed");
            return false;
        }
    }

    @Override
    public boolean checkPayment(Long orderId) {
        // Get orderInfo according to orderId
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        // Build parameters
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        request.setBizContent(JSON.toJSONString(map));

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("Call successful");
            return true;
        } else {
            System.out.println("Call failed");
            return false;
        }
    }
}
