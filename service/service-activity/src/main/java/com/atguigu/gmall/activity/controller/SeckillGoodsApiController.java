package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Yuehong Zhang
 */
@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillGoodsApiController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderFeignClient orderFeignClient;

    // Query all spike products
    @GetMapping("/findAll")
    public Result findAll(){
        return Result.ok(seckillGoodsService.findAll());
    }
    // Obtain seckill product data according to skuId
    @GetMapping("/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable Long skuId){
        return Result.ok(seckillGoodsService.findSeckillGoodsById(skuId));
    }

    // Get the order code: Why do you want to place the order code? Prevent users from illegally buying!
    @GetMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable Long skuId, HttpServletRequest request){
        // The way the order code is generated: userId MD5 encryption!
        // Must be within the spike time range!
        // According to the current skuId to find out who the spike product is!
        SeckillGoods seckillGoods = seckillGoodsService.findSeckillGoodsById(skuId);
        // Get user ID
        String userId = AuthContextHolder.getUserId(request);
        if (!StringUtils.isEmpty(userId)){
            // Determine the time range
            Date currentTime = new Date();
            // time to compare
            if (DateUtil.dateCompare(seckillGoods.getStartTime(),currentTime) &&
                    DateUtil.dateCompare(currentTime,seckillGoods.getEndTime())){
                // Within the scope of the spike:
                String skuIdStr = MD5.encrypt(userId);
                // Return the order code
                return Result.ok(skuIdStr);
            }
        }
        // Return data:
        return Result.fail().message("Failed to obtain the order code!");
    }

    // Place an order:
    // /api/activity/seckill/auth/seckillOrder/{skuId}?skuIdStr=xxxx
    @PostMapping("auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable Long skuId ,HttpServletRequest request){
        // Check the order code, only the request to get the order code correctly is a legal request
        String skuIdStr = request.getParameter("skuIdStr");
        // Get the user ID and compare it with skuIdStr in encryption!
        String userId = AuthContextHolder.getUserId(request);
        if (!skuIdStr.equals(MD5.encrypt(userId))){
            // The order code is incorrect!
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        // Check the status bit state redis subscription release;
        String state = (String) CacheHelper.get(skuId.toString());
        if (StringUtils.isEmpty(state)){
            // request is illegal
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }else if ("0".equals(state)){
            //  Sold out
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        }else {
            // Commodities can be killed in seconds! state = 1
            UserRecode userRecode = new UserRecode();
            userRecode.setUserId(userId);
            userRecode.setSkuId(skuId);
            // Send to mq!
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER,MqConst.ROUTING_SECKILL_USER,userRecode);
            return Result.ok();
        }
    }

    // Check the spike status!
    // /api/activity/seckill/auth/checkOrder/{skuId}
    @GetMapping("auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable Long skuId,HttpServletRequest request){
        // Get the user ID
        String userId = AuthContextHolder.getUserId(request);
        // Call the service layer method
        return seckillGoodsService.checkOrder(skuId,userId);
    }

    // Spike back the order page data!
    @GetMapping("/auth/trade")
    public Result seckillTrade(HttpServletRequest request){
        // Get user ID
        String userId = AuthContextHolder.getUserId(request);
        // Declare a map
        HashMap<String, Object> map = new HashMap<>();

        // remotely call service-user feign client
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(Long.parseLong(userId));

        // Shipping list: orderKey = seckill:orders field = userId value = orderRecode
        String orderKey = RedisConst.SECKILL_ORDERS;
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(orderKey).get(userId);
        if (orderRecode==null){
            // return to stop
            return Result.fail().message("Order failed");
        }
        // Get the current spike product!
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();
        List<OrderDetail> detailArrayList = new ArrayList<>();
        // Declare an order details
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setSkuNum(orderRecode.getNum());
        orderDetail.setOrderPrice(seckillGoods.getCostPrice());
        detailArrayList.add(orderDetail);

        // Calculate the total amount:
        // OrderInfo orderInfo = new OrderInfo();
        // orderInfo.setOrderDetailList(detailArrayList);
        // orderInfo.sumTotalAmount();
        // map.put("totalAmount",orderInfo.getTotalAmount());
        // Save the key required by the page to the map!
        map.put("detailArrayList",detailArrayList);
        map.put("userAddressList",userAddressList);
        map.put("totalNum","1");
        map.put("totalAmount",seckillGoods.getCostPrice());
        // return data
        return Result.ok(map);
    }

    //  Submit orders
    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request){
        // UserId is not set in the implementation class
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));

        // Call the feign-client of service-order
        Long orderId = orderFeignClient.submitOrder(orderInfo);
        if (orderId==null){
            return Result.fail().message("Failed to submit the order!");
        }

        // Delete cached information {pre-order information}: setnx; RedisConst.SECKILL_ORDERS; userId
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).delete(userId);

        // Store the data of a real order to the cache!
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId, orderId.toString());

        // return data
        return Result.ok(orderId);
    }


}
