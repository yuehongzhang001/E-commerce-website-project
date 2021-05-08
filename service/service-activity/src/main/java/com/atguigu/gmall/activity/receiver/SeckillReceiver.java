package com.atguigu.gmall.activity.receiver;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * @author mqx
 */
@Component
public class SeckillReceiver {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsService seckillGoodsService;



    //  监听消息将秒杀商品数据放入缓存！
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_1,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_1}
    ))
    public void importToRedis(Message message, Channel channel){
        //  查询当前的秒杀商品数据集合： 当天，审核状态 ，剩余库存数
        //  当天时间new Date();
        QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
        seckillGoodsQueryWrapper.eq("status",1).gt("stock_count",0);
        //  数据库的年月日，与当前系统时间的年月日进行比较！
        //  如果精确到秒：涉及到商品展示，什么时候开始，什么时候结束！
        seckillGoodsQueryWrapper.eq("DATE_FORMAT(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));
        List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(seckillGoodsQueryWrapper);
        //  将集合数据放入缓存
        if (!CollectionUtils.isEmpty(seckillGoodsList)){
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                //  将数据放入缓存！ 数据类型， key ！
                String seckillKey = RedisConst.SECKILL_GOODS;
                //  判断当前秒杀商品在缓存中是否存在！
                //  判断当前field ，在这个key 中是否存在！ field  = skuId
                Boolean flag = redisTemplate.boundHashOps(seckillKey).hasKey(seckillGoods.getSkuId().toString());
                //  flag = true 表示有数据，不要插入数据了。
                if (flag){
                    continue;
                }
                //  第一种放入方式！
                //  redisTemplate.opsForHash().put(seckillKey,seckillGoods.getSkuId().toString(),seckillGoods);
                //  第二种放入方式：
                redisTemplate.boundHashOps(seckillKey).put(seckillGoods.getSkuId().toString(),seckillGoods);
                //  如何控制库存超买? 将商品的库存数量放入缓存！num = 10;
                for (Integer i = 0; i < seckillGoods.getStockCount(); i++) {
                    //  定义key ，value！
                    String key = RedisConst.SECKILL_STOCK_PREFIX+seckillGoods.getSkuId();
                    //  key = seckill:stock:skuId
                    //  value = skuId  seckillGoods.getSkuId().toString()
                    //  redisTemplate.opsForList().leftPush(key,seckillGoods.getSkuId().toString());
                    redisTemplate.boundListOps(key).leftPush(seckillGoods.getSkuId().toString());

                }

                //  状态位初始化：skuId:1  |  46:1
                redisTemplate.convertAndSend("seckillpush",seckillGoods.getSkuId()+":1");

            }
        }
        //  手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    //  监听秒杀队列中的数据！
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),
            key = {MqConst.ROUTING_SECKILL_USER}
    ))
    public void seckill(UserRecode userRecode, Message message, Channel channel){
        //  判断当前的对象是否为空
        if(userRecode!=null){
            //  调用方法！ 预下单
            seckillGoodsService.seckillOrder(userRecode.getSkuId(),userRecode.getUserId());
        }
        //  手动确认！
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }

    //  监听事件：
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_18,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_18}
    ))
    public void clearRedis( Message message, Channel channel){
        //  查询秒杀结束的商品！
        //活动结束清空缓存
        QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.le("end_time", new Date());
        //  查到的是当天结束的秒杀商品！
        List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(queryWrapper);

        for (SeckillGoods seckillGoods : seckillGoodsList) {
            //  RedisConst.SECKILL_GOODS seckill:goods
            //  秒杀商品所有数据： seckill:goods  field = skuId
            //  如果有多个秒杀商品则使用以下删除方式！
            //  redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).delete(seckillGoods.getSkuId().toString());
            //  删除库存！ seckill:stock:skuId  seckill:stock:46
            redisTemplate.delete(RedisConst.SECKILL_STOCK_PREFIX+seckillGoods.getSkuId());
        }

        //  如果秒杀商品只有一个！
        redisTemplate.delete(RedisConst.SECKILL_GOODS);
        //  seckill:orders 为啥要删除它！
        redisTemplate.delete(RedisConst.SECKILL_ORDERS);
        //  删除真正的订单
        redisTemplate.delete(RedisConst.SECKILL_ORDERS_USERS);

        //  修改一下数据库
        //  将秒杀结束商品的状态变为 2
        SeckillGoods seckillGoods = new SeckillGoods();
        seckillGoods.setStatus("2");
        seckillGoodsMapper.update(seckillGoods,queryWrapper);

        //  手动确认！
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }

}
