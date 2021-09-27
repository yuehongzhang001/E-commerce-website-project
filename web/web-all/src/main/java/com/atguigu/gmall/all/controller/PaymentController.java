package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Yuehong Zhang
 */
@Controller
public class PaymentController {

    @Autowired
    private OrderFeignClient orderFeignClient;


    @GetMapping("pay.html")
    public String pay(HttpServletRequest request){
        // Get orderId
        String orderId = request.getParameter("orderId");

        // ${orderInfo} stores orderInfo
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(Long.parseLong(orderId));
        // Save the object!
        request.setAttribute("orderInfo",orderInfo);
        return "payment/pay";
    }

    // Return to the successful payment page!
    @GetMapping("pay/success.html")
    public String paySuccess(){
        // Return to the successful payment page!
        return "payment/success";
    }
}