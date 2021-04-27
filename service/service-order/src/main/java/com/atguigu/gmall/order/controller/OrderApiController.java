package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.redisson.misc.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mqx
 */
@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    //  编写一个远程调用地址！ api/order/auth/trade
    @GetMapping("auth/trade")
    public Result trade(HttpServletRequest request){
        //  获取用户Id
        String userId = AuthContextHolder.getUserId(request);

        HashMap<String, Object> map = new HashMap<>();
        //  远程调用才能获取到收货地址列表
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(Long.parseLong(userId));
        //  获取送货清单：
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        //  声明一个订单明细集合
        List<OrderDetail> detailArrayList = new ArrayList<>();
        //  int totalNum = 0;
        //  cartCheckedList 集合数据赋值给订单明细集合
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setCreateTime(new Date());
            detailArrayList.add(orderDetail);
            //  totalNum+=cartInfo.getSkuNum();
        }
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();
        map.put("totalAmount",orderInfo.getTotalAmount());
        //  件数：第一种：就看集合的长度 第二种：计算每个skuId 的总件数
        map.put("totalNum",detailArrayList.size());
        map.put("detailArrayList",detailArrayList);
        map.put("userAddressList",userAddressList);
        return Result.ok(map);
    }

}
