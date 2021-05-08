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
 * @author mqx
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


        //  获取到orderInfo 或者是paymentInfo
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        //  生产二维码的同时，保存交易记录
        //  默认支付宝支付
        paymentService.savePaymentInfo(orderInfo, PaymentType.ALIPAY.name());

        /*
        1.  生产二维码！  取消订单： 5秒钟;
        2.  取消订单了，则不能生产二维码！
         */
        if("CLOSED".equals(orderInfo.getOrderStatus())){
            return "该订单已经取消！";
        }

        //  AlipayClient alipayClient =  new DefaultAlipayClient( "https://openapi.alipay.com/gateway.do" , APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);  //获得初始化的AlipayClient
        AlipayTradePagePayRequest alipayRequest =  new  AlipayTradePagePayRequest(); //创建API对应的request
        //  同步回调
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        //  异步回调
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url); //在公共参数中设置回跳和通知地址

        //  需要json 字符串
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        //  map.put("total_amount",orderInfo.getTotalAmount());
        map.put("total_amount","0.01");
        map.put("subject",orderInfo.getTradeBody());
        map.put("timeout_express","5m");
        //  map 转换为Json
        String jsonStr = JSON.toJSONString(map);
        alipayRequest.setBizContent(jsonStr);

        String form = alipayClient.pageExecute(alipayRequest).getBody();

        return form;
    }

    @Override
    public boolean refund(Long orderId) {

        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);

        //  声明退款请求对象
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        //  构建Json 字符串
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        //  退款金额 < 支付金额
        map.put("refund_amount","0.01");
        map.put("refund_reason","不好使！");

        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
            //  电商平台订单关闭！
            PaymentInfo updPaymentInfo = new PaymentInfo();
            updPaymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());
            this.paymentService.updatePaymentInfo(orderInfo.getOutTradeNo(),PaymentType.ALIPAY.name(),updPaymentInfo);
            //  订单状态关闭！

            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }

    }

    @Override
    public boolean closePay(Long orderId) {
        //  根据orderId 获取orderInfo
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        //  创建关闭对象
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
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }

    @Override
    public boolean checkPayment(Long orderId) {
        //  根据orderId 获取orderInfo
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        //  构建参数
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
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }
}
