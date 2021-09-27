package com.atguigu.gmall.order.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Yuehong Zhang
 */
@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    // Write a remote call address! api/order/auth/trade
    @GetMapping("auth/trade")
    public Result trade(HttpServletRequest request){
        // Get user ID
        String userId = AuthContextHolder.getUserId(request);

        HashMap<String, Object> map = new HashMap<>();
        // Remote call to get the list of receiving addresses
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(Long.parseLong(userId));
        // Get the shipping list:
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        // Declare a collection of order details
        List<OrderDetail> detailArrayList = new ArrayList<>();
        HashSet<Object> objects = new HashSet<>();
        objects.add("a");

        // int totalNum = 0;
        // The cartCheckedList collection data is assigned to the order detail collection
        for (CartInfo cartInfo: cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setCreateTime(new Date());
            detailArrayList.add(orderDetail);
            // totalNum+=cartInfo.getSkuNum();
        }
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();
        map.put("totalAmount",orderInfo.getTotalAmount());
        // Number of pieces: The first type: It depends on the length of the collection. The second type: Calculate the total number of pieces for each skuId
        map.put("totalNum",detailArrayList.size());
        map.put("detailArrayList",detailArrayList);
        map.put("userAddressList",userAddressList);

        // Get the serial number and store it!
        String tradeNo = orderService.getTradeNo(userId);
        map.put("tradeNo",tradeNo);
        return Result.ok(map);
    }

    // The controller that saves the order
    // The front-end page transmits Json data, and the back-end uses @RequestBody
    // http://api.gmall.com/api/order/auth/submitOrder?tradeNo=tyuiopasdfg32456
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,HttpServletRequest request){
        // UserId is not set in the implementation class
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));
        // Get the transaction number passed on the page
        String tradeNo = request.getParameter("tradeNo");
        // call the comparison method
        Boolean result = orderService.checkTradeNo(tradeNo, userId);
        //  judge
        // if(result){
        // // More successful! Can submit
        // }else{
        // // Can't submit, return!
        //}
        // When the comparison fails!
        if(!result){
            return Result.fail().message("Cannot submit an order without refreshing!");
        }
        //  delete
        orderService.deleteTradeNo(userId);

        // You can use multiple threads:
        List<CompletableFuture> futureList = new ArrayList<>();
        // Store the collection of error messages
        List<String> errorList = new ArrayList<>();
        // remote call
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail: orderDetailList) {
            // open thread CompletableFuture
            CompletableFuture<Void> stockCompletableFuture = CompletableFuture.runAsync(() -> {
                boolean flag = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                if (!flag) {
                    //  Inventory shortage!
                    // return Result.fail().message(orderDetail.getSkuName()+"Insufficient inventory!");
                    errorList.add(orderDetail.getSkuName() + "Insufficient inventory!");
                }
            },threadPoolExecutor);

            // add to this collection
            futureList.add(stockCompletableFuture);

            CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
                // order price
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                // Real-time price: @GmallCache
                BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                //  judge
                if (orderPrice.compareTo(skuPrice)!=0){
                    // The price has changed, the price needs to be updated:
                    cartFeignClient.loadCartCache(userId);
                    errorList.add(orderDetail.getSkuName()+"The price has changed!");
                }
            },threadPoolExecutor);
            // add to this collection
            futureList.add(priceCompletableFuture);
        }

        // All data results: errorList; futureList executes a collection of asynchronous orchestration!
        // CompletableFuture [] com = new CompletableFuture[30];
        // Multi-task combination
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();
        // Determine whether there is data in errorList!
        if (errorList.size()>0){
            // return Result.fail().message(orderDetail.getSkuName()+"the price has changed!",orderDetail.getSkuName()+"the inventory is not moving");
            return Result.fail().message(StringUtils.join(errorList,","));
        }
        // Call the service layer method
        Long orderId = orderService.saveOrderInfo(orderInfo);
        // Return order Id
        return Result.ok(orderId);
    }

    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId){
        return orderService.getOrderInfo(orderId);
    }

    // Split the order:
    // http://localhost:8204/api/order/orderSplit?orderId=xxx&wareSkuMap=xxx
    @PostMapping("orderSplit")
    public String orderSplit(HttpServletRequest request){
        // Get the passed parameters first
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");

        // Order splitting needs to be split according to these two parameters!
        List<OrderInfo> orderInfoList = orderService.orderSplit(orderId,wareSkuMap);

        // Declare a map to get data
        List<Map> maps = new ArrayList<>();
        // Need to loop through
        for (OrderInfo orderInfo: orderInfoList) {
            // orderInfo is converted to Map
            Map map = orderService.initWareOrder(orderInfo);
            maps.add(map);
        }
        // return data
        return JSON.toJSONString(maps);

    }
    // Spike order data interface
    @PostMapping("inner/seckill/submitOrder")
    public Long submitOrder(@RequestBody OrderInfo orderInfo){
        // Call the service layer method:
        Long orderId = orderService.saveOrderInfo(orderInfo);
        // Return order Id
        return orderId;
    }


}
