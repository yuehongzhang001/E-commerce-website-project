package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Yuehong Zhang
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Override
    public List<SeckillGoods> findAll() {
        //  hvals
        List<SeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();
        return seckillGoodsList;
    }

    @Override
    public SeckillGoods findSeckillGoodsById(Long skuId) {
        //  hget(key,field);
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId.toString());
        return seckillGoods;
    }

    @Override
    public void seckillOrder(Long skuId, String userId) {
        /*
        1. First judge the product status bit. Haven't we already judged it before? Because the product may be sold out at any time, there may be 100,000 data accumulated in the mq queue,
                But it has been sold out, then the follow-up process does not need to go anymore;
        2. Determine whether the user has placed an order. This place is to control the user to place an order repeatedly. The same user can only snap up one order qualification. How to control it?
                It’s very simple. We can use setnx to control the user. When the user comes in for the first time, it returns true and can be snapped up. After entering, it returns false and returns directly.
                The expiration time can be customized according to the business, so that users can control the note during this period
        3. Get the goods in the queue. If they can be obtained, the goods are in stock and you can place an order. If the obtained product id is empty, the product is sold out.
                When the product is sold out, we need to notify the brother node as soon as possible to update the status bit, so send the redis broadcast here
        4. Put the order record into the redis cache, indicating that the user has obtained the qualification to place an order, and the spike is successful

        5. If the spike is successful, the inventory needs to be updated
         */
        // OrderRecode order record:
        // Check the status bit state redis subscription release;
        String state = (String) CacheHelper.get(skuId.toString());
        // Status bit 0 means it has been sold out, 1 means it can be killed in seconds!
        if("0".equals(state)){
            //  Sold out
            return;
        }
        // Determine whether the user has placed an order setnx
        // key = seckill:user:userId value = skuId
        String userOrderKey = RedisConst.SECKILL_USER + userId;
        // execute setnx command
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(userOrderKey, skuId, RedisConst.SECKILL__TIMEOUT, TimeUnit.SECONDS);
        // flag = true;
        if (!flag){
            // The user already exists in the cache
            return;
        }
        // Reduce the inventory of the product from the cache
        // key = seckill:stock:46
        String seckillKey = RedisConst.SECKILL_STOCK_PREFIX + skuId;
        String skuIdValues = (String) redisTemplate.boundListOps(seckillKey).rightPop();
        // If skuIdValues is not empty, it means the inventory reduction is successful, if it is empty, it means the inventory has been sold out
        if (StringUtils.isEmpty(skuIdValues)){
            // Notify other sibling nodes: skuId:0
            redisTemplate.convertAndSend("seckillpush",skuId+":0");
            //  Sold out
            return;
        }
        // The above verification is passed and the OrderRecode object is stored in the cache, indicating that the user is eligible to place an order!
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setUserId(userId);
        orderRecode.setSeckillGoods(findSeckillGoodsById(skuId));
        orderRecode.setNum(1);
        orderRecode.setOrderStr(MD5.encrypt(skuId+userId));

        // Put it in the cache!
        // hset key field value
        // orderKey = seckill:orders field = userId value = orderRecode
        String orderKey = RedisConst.SECKILL_ORDERS;
        redisTemplate.boundHashOps(orderKey).put(userId,orderRecode);

        // Update inventory!
        this.updateStockCount(skuId);

    }

    @Override
    public Result checkOrder(Long skuId, String userId) {
        /*
            1. Determine whether the user exists in the cache
            2. Determine whether the user has successfully grabbed the order
                    {In cache: seckill:orders userId orderRecode} Pre-order successfully!
            3. Determine whether the user has placed an order
                    {redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId, orderId.toString());}
            4. Judging the status bit
         */
        String userOrderKey = RedisConst.SECKILL_USER + userId;
        Boolean flag = redisTemplate.hasKey(userOrderKey);
        // flag = true
        if (flag){
            // Indicates that the user exists in the cache, and then judge whether the user has successfully grabbed the order!
            // orderKey = seckill:orders field = userId value = orderRecode
            String orderKey = RedisConst.SECKILL_ORDERS;
            Boolean result = redisTemplate.boundHashOps(orderKey).hasKey(userId);
            // result = true means that the order was successfully grabbed!
            if (result){
                // Get order data
                OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(orderKey).get(userId);
                // return data
                return Result.build(orderRecode, ResultCodeEnum.SECKILL_SUCCESS);
            }
        }

        // Determine whether the user has placed an order
        String orderUserKey = RedisConst.SECKILL_ORDERS_USERS;
        Boolean res = redisTemplate.boundHashOps(orderUserKey).hasKey(userId);
        // judge res = true
        if (res){
            // Indicates that an order has been placed
            String orderId = (String) redisTemplate.boundHashOps(orderUserKey).get(userId);
            // return data
            return Result.build(orderId, ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }
        // Determine the status bit
        // Check the status bit state redis subscription release;
        String state = (String) CacheHelper.get(skuId.toString());
        // Status bit 0 means it has been sold out, 1 means it can be killed in seconds!
        if("0".equals(state)){
            // return data
            return Result.build(null, ResultCodeEnum.SECKILL_FAIL);
        }
        // Queued by default!
        return Result.build(null, ResultCodeEnum.SECKILL_RUN);
    }

    // Update inventory!
    private void updateStockCount(Long skuId) {
        // cache, database
        // Remaining inventory: seckill:stock:46
        String seckillKey = RedisConst.SECKILL_STOCK_PREFIX + skuId;
        // count remaining inventory
        Long count = redisTemplate.boundListOps(seckillKey).size();

        // Make rules to update inventory!
        if(count%2==0){
            //  refresh cache:
            SeckillGoods seckillGoods = findSeckillGoodsById(skuId);
            seckillGoods.setStockCount(count.intValue());

            // write cache
            redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(skuId.toString(),seckillGoods);

            // Update the database:
            // UpdateWrapper<SeckillGoods> seckillGoodsUpdateWrapper = new UpdateWrapper<>();
            // seckillGoodsUpdateWrapper.eq("sku_id",skuId);
            // SeckillGoods seckillGoods1 = new SeckillGoods();
            // seckillGoods.setStockCount(count.intValue());
            // seckillGoodsMapper.update(seckillGoods1,seckillGoodsUpdateWrapper);

            seckillGoodsMapper.updateById(seckillGoods);
        }
    }
}
