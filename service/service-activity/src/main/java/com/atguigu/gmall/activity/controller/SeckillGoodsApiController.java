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
 * @author mqx
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

    //  查询所有秒杀商品
    @GetMapping("/findAll")
    public Result findAll(){
        return Result.ok(seckillGoodsService.findAll());
    }
    //  根据skuId 获取秒杀商品数据
    @GetMapping("/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable Long skuId){
        return Result.ok(seckillGoodsService.findSeckillGoodsById(skuId));
    }

    //  获取下单码:为什么要下单码? 防止用户非法抢购！
    @GetMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable Long skuId, HttpServletRequest request){
        //  下单码生成的方式：userId MD5加密！
        //  必须在秒杀时间范围内！
        //  根据当前skuId 查询到秒杀商品是谁！
        SeckillGoods seckillGoods = seckillGoodsService.findSeckillGoodsById(skuId);
        //  获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        if (!StringUtils.isEmpty(userId)){
            //  判断时间范围
            Date currentTime = new Date();
            //  时间做比较
            if (DateUtil.dateCompare(seckillGoods.getStartTime(),currentTime) &&
                DateUtil.dateCompare(currentTime,seckillGoods.getEndTime())){
                //  在秒杀范围时间内：
                String skuIdStr = MD5.encrypt(userId);
                //  返回下单码
                return Result.ok(skuIdStr);
            }
        }
        //  返回数据：
        return Result.fail().message("获取下单码失败!");
    }

    //  下单：
    //  /api/activity/seckill/auth/seckillOrder/{skuId}?skuIdStr=xxxx
    @PostMapping("auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable Long skuId ,HttpServletRequest request){
        //  校验下单码，只有正确获得下单码的请求才是合法请求
        String skuIdStr = request.getParameter("skuIdStr");
        //  获取用户Id然后在加密 与 skuIdStr 做个比较！
        String userId = AuthContextHolder.getUserId(request);
        if (!skuIdStr.equals(MD5.encrypt(userId))){
            //  下单码不正确！
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        //  校验状态位state  redis 订阅发布；
        String state = (String) CacheHelper.get(skuId.toString());
        if (StringUtils.isEmpty(state)){
            //  请求不合法
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }else if ("0".equals(state)){
            //  已售罄
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        }else {
            //  商品可以秒杀！ state = 1
            UserRecode userRecode = new UserRecode();
            userRecode.setUserId(userId);
            userRecode.setSkuId(skuId);
            //  发送到mq 上！
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER,MqConst.ROUTING_SECKILL_USER,userRecode);
            return Result.ok();
        }
    }

    //  检查秒杀状态！
    //  /api/activity/seckill/auth/checkOrder/{skuId}
    @GetMapping("auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable Long skuId,HttpServletRequest request){
        //  获取到用户Id
        String userId = AuthContextHolder.getUserId(request);
        //  调用服务层方法
        return seckillGoodsService.checkOrder(skuId,userId);
    }

    //  秒杀下单页面数据回显！
    @GetMapping("/auth/trade")
    public Result seckillTrade(HttpServletRequest request){
        //  获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        //  声明一个map
        HashMap<String, Object> map = new HashMap<>();

        //  远程调用service-user feign client
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(Long.parseLong(userId));

        //  送货清单： orderKey = seckill:orders  field = userId value = orderRecode
        String orderKey = RedisConst.SECKILL_ORDERS;
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(orderKey).get(userId);
        if (orderRecode==null){
            //  返回停止
            return Result.fail().message("下单失败");
        }
        //  获取到当前的秒杀商品！
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();
        List<OrderDetail> detailArrayList = new ArrayList<>();
        //  声明一个订单明细
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setSkuNum(orderRecode.getNum());
        orderDetail.setOrderPrice(seckillGoods.getCostPrice());
        detailArrayList.add(orderDetail);

        //  计算总金额：
        //        OrderInfo orderInfo = new OrderInfo();
        //        orderInfo.setOrderDetailList(detailArrayList);
        //        orderInfo.sumTotalAmount();
        //        map.put("totalAmount",orderInfo.getTotalAmount());
        //  将页面需要的key 保存到map 中！
        map.put("detailArrayList",detailArrayList);
        map.put("userAddressList",userAddressList);
        map.put("totalNum","1");
        map.put("totalAmount",seckillGoods.getCostPrice());
        //  返回数据
        return Result.ok(map);
    }

    //  提交订单
    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request){
        //  在实现类中没有设置userId
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));

        //  调用service-order 的feign-client
        Long orderId = orderFeignClient.submitOrder(orderInfo);
        if (orderId==null){
            return Result.fail().message("提交订单失败!");
        }

        //  删除缓存的信息{预下单信息}： setnx; RedisConst.SECKILL_ORDERS; userId
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).delete(userId);

        //  存储一个真正下单的数据到缓存！
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId, orderId.toString());

        //  返回数据
        return Result.ok(orderId);
    }


}
