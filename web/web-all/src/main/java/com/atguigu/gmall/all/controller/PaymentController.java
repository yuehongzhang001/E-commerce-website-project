package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mqx
 */
@Controller
public class PaymentController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    //  http://payment.gmall.com/pay.html?orderId=192
    @GetMapping("pay.html")
    public String pay(HttpServletRequest request){
        //  获取orderId
        String orderId = request.getParameter("orderId");

        //  ${orderInfo} 存储orderInfo
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(Long.parseLong(orderId));
        //  保存对象！
        request.setAttribute("orderInfo",orderInfo);
        return "payment/pay";
    }

    //  返回支付成功页面！
    @GetMapping("pay/success.html")
    public String paySuccess(){
        //  返回支付成功页面！
        return "payment/success";
    }
}
