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
 * @author mqx
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
        HashSet<Object> objects = new HashSet<>();
        objects.add("a");

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

        //  获取流水号，并存储！
        String tradeNo = orderService.getTradeNo(userId);
        map.put("tradeNo",tradeNo);
        return Result.ok(map);
    }

    //  保存订单的控制器
    //  前端页面传递的是Json 数据 ，后台使用@RequestBody
    //  http://api.gmall.com/api/order/auth/submitOrder?tradeNo=tyuiopasdfg32456
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,HttpServletRequest request){
        //  在实现类中没有设置userId
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));
        //  获取页面传递的交易号
        String tradeNo = request.getParameter("tradeNo");
        //  调用比较方法
        Boolean result = orderService.checkTradeNo(tradeNo, userId);
        //  判断
        //        if(result){
        //            //  比较成功！可以提交
        //        }else{
        //            //  不能提交了，return！
        //        }
        //  当比较失败时！
        if(!result){
            return Result.fail().message("不能无刷新回退提交订单!");
        }
        //  删除
        orderService.deleteTradeNo(userId);

        //  可以使用多线程：
        List<CompletableFuture> futureList = new ArrayList<>();
        //  存储错误信息的集合
        List<String> errorList = new ArrayList<>();
        //  远程调用
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //  开线程CompletableFuture
            CompletableFuture<Void> stockCompletableFuture = CompletableFuture.runAsync(() -> {
                boolean flag = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                if (!flag) {
                    //  库存不足！
                    //  return Result.fail().message(orderDetail.getSkuName()+"库存不足!");
                    errorList.add(orderDetail.getSkuName() + "库存不足!");
                }
            },threadPoolExecutor);

            //  添加到这个集合
            futureList.add(stockCompletableFuture);

            CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
                //  订单价格
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                //  实时价格：   @GmallCache
                BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                //  判断
                if (orderPrice.compareTo(skuPrice)!=0){
                    //  价格有变动，需要更新价格：
                    cartFeignClient.loadCartCache(userId);
                    errorList.add(orderDetail.getSkuName()+"价格有变动!");
                }
            },threadPoolExecutor);
            //  添加到这个集合
            futureList.add(priceCompletableFuture);
        }

        //  所有数据结果：errorList ； futureList 执行异步编排的集合！
        //  CompletableFuture [] com = new CompletableFuture[30];
        //  多任务组合
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();
        //  判断errorList 中是否有数据！
        if (errorList.size()>0){
            //  return Result.fail().message(orderDetail.getSkuName()+"价格有变动!",orderDetail.getSkuName()+"库存不走");
            return Result.fail().message(StringUtils.join(errorList,","));
        }
        //  调用服务层方法
        Long orderId = orderService.saveOrderInfo(orderInfo);
        //  返回订单Id
        return Result.ok(orderId);
    }

    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId){
        return orderService.getOrderInfo(orderId);
    }

    //  拆单：
    //  http://localhost:8204/api/order/orderSplit?orderId=xxx&wareSkuMap=xxx
    @PostMapping("orderSplit")
    public String orderSplit(HttpServletRequest request){
        //  先获取传递过来的参数
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");

        //  拆单需要根据这个两个参数进行拆单！
        List<OrderInfo> orderInfoList = orderService.orderSplit(orderId,wareSkuMap);

        //  声明一个map 获取数据
        List<Map> maps = new ArrayList<>();
        //  需要循环遍历
        for (OrderInfo orderInfo : orderInfoList) {
            //  orderInfo 转换为Map
            Map map = orderService.initWareOrder(orderInfo);
            maps.add(map);
        }
        //  返回数据
        return JSON.toJSONString(maps);

    }
    //  秒杀订单数据接口
    @PostMapping("inner/seckill/submitOrder")
    public Long submitOrder(@RequestBody OrderInfo orderInfo){
        //  调用服务层方法：
        Long orderId = orderService.saveOrderInfo(orderInfo);
        //  返回订单Id
        return orderId;
    }


}
