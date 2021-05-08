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
 * @author mqx
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
        1，首先判断产品状态位，我们前面不是已经判断过了吗？因为产品可能随时售罄，mq队列里面可能堆积了十万数据，
                但是已经售罄了，那么后续流程就没有必要再走了；
        2，判断用户是否已经下过订单，这个地方就是控制用户重复下单，同一个用户只能抢购一个下单资格，怎么控制呢？
                很简单，我们可以利用setnx控制用户，当用户第一次进来时，返回true，可以抢购，以后进入返回false，直接返回，
                过期时间可以根据业务自定义，这样用户这一段咋们就控制注了
        3，获取队列中的商品，如果能够获取，则商品有库存，可以下单。如果获取的商品id为空，则商品售罄，
                商品售罄我们要第一时间通知兄弟节点，更新状态位，所以在这里发送redis广播
        4，将订单记录放入redis缓存，说明用户已经获得下单资格，秒杀成功

        5，秒杀成功要更新库存
         */
        //  OrderRecode 订单记录：
        //  校验状态位state  redis 订阅发布；
        String state = (String) CacheHelper.get(skuId.toString());
        //  状态位 0 表示已经售罄， 1 表示可以秒杀！
        if("0".equals(state)){
            //  已售罄
            return;
        }
        //  判断用户是否已经下过订单 setnx
        //  key  = seckill:user:userId  value = skuId
        String userOrderKey = RedisConst.SECKILL_USER + userId;
        //  执行setnx 命令
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(userOrderKey, skuId, RedisConst.SECKILL__TIMEOUT, TimeUnit.SECONDS);
        //  flag = true;
        if (!flag){
            // 用户在缓存中已经存在
            return;
        }
        //  从缓存中减少商品的库存数
        //  key = seckill:stock:46
        String seckillKey = RedisConst.SECKILL_STOCK_PREFIX + skuId;
        String skuIdValues = (String) redisTemplate.boundListOps(seckillKey).rightPop();
        //  skuIdValues 不为空，则说明减库存成功，为空则说明库存已经售罄
        if (StringUtils.isEmpty(skuIdValues)){
            //  通知其他兄弟节点： skuId:0
            redisTemplate.convertAndSend("seckillpush",skuId+":0");
            //  已售罄
            return;
        }
        //  上述验证通过，将OrderRecode 对象存储到缓存中，说明用户有下单资格！
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setUserId(userId);
        orderRecode.setSeckillGoods(findSeckillGoodsById(skuId));
        orderRecode.setNum(1);
        orderRecode.setOrderStr(MD5.encrypt(skuId+userId));

        //  放入缓存！
        //  hset key field value
        //  orderKey = seckill:orders  field = userId value = orderRecode
        String orderKey = RedisConst.SECKILL_ORDERS;
        redisTemplate.boundHashOps(orderKey).put(userId,orderRecode);

        //  更新库存！
        this.updateStockCount(skuId);

    }

    @Override
    public Result checkOrder(Long skuId, String userId) {
        /*
            1.  判断用户是否在缓存中存在
            2.  判断用户是否抢单成功
                    {在缓存中：seckill:orders userId orderRecode} 预下单成功！
            3.  判断用户是否下过订单
                    {redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId, orderId.toString());}
            4.  判断状态位
         */
        String userOrderKey = RedisConst.SECKILL_USER + userId;
        Boolean flag = redisTemplate.hasKey(userOrderKey);
        //  flag = true
        if (flag){
            //  说明用户在缓存中存在，然后判断用户是否抢单成功！
            //  orderKey = seckill:orders  field = userId value = orderRecode
            String orderKey = RedisConst.SECKILL_ORDERS;
            Boolean result = redisTemplate.boundHashOps(orderKey).hasKey(userId);
            //  result = true 表示抢单成功！
            if (result){
                //  获取订单数据
                OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(orderKey).get(userId);
                //  返回数据
                return Result.build(orderRecode, ResultCodeEnum.SECKILL_SUCCESS);
            }
        }

        //  判断用户是否下过订单
        String orderUserKey = RedisConst.SECKILL_ORDERS_USERS;
        Boolean res = redisTemplate.boundHashOps(orderUserKey).hasKey(userId);
        //  判断 res = true
        if (res){
            //  表示已经下过订单
            String orderId = (String) redisTemplate.boundHashOps(orderUserKey).get(userId);
            //  返回数据
            return Result.build(orderId, ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }
        //  判断状态位
        //  校验状态位state  redis 订阅发布；
        String state = (String) CacheHelper.get(skuId.toString());
        //  状态位 0 表示已经售罄， 1 表示可以秒杀！
        if("0".equals(state)){
            //  返回数据
            return Result.build(null, ResultCodeEnum.SECKILL_FAIL);
        }
        //  默认排队中！
        return Result.build(null, ResultCodeEnum.SECKILL_RUN);
    }

    //  更新库存！
    private void updateStockCount(Long skuId) {
        //  缓存，数据库
        //  剩余库存数：seckill:stock:46
        String seckillKey = RedisConst.SECKILL_STOCK_PREFIX + skuId;
        //  count 剩余库存
        Long count = redisTemplate.boundListOps(seckillKey).size();

        //  制定规则更新库存！
        if(count%2==0){
            //  更新缓存：
            SeckillGoods seckillGoods = findSeckillGoodsById(skuId);
            seckillGoods.setStockCount(count.intValue());

            //  写入缓存
            redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(skuId.toString(),seckillGoods);

            //  更新数据库：
            //            UpdateWrapper<SeckillGoods> seckillGoodsUpdateWrapper = new UpdateWrapper<>();
            //            seckillGoodsUpdateWrapper.eq("sku_id",skuId);
            //            SeckillGoods seckillGoods1 = new SeckillGoods();
            //            seckillGoods.setStockCount(count.intValue());
            //            seckillGoodsMapper.update(seckillGoods1,seckillGoodsUpdateWrapper);

            seckillGoodsMapper.updateById(seckillGoods);
        }
    }
}
