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
 * @author Yuehong Zhang
 */
@Component
public class SeckillReceiver {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsService seckillGoodsService;



    // Listen to the message and put the spike product data into the cache!
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_1,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_1}
    ))
    public void importToRedis(Message message, Channel channel){
        // Query the current spike product data collection: that day, review status, remaining inventory
        // Time of day new Date();
        QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
        seckillGoodsQueryWrapper.eq("status",1).gt("stock_count",0);
        // The year, month, and day of the database are compared with the year, month, and day of the current system time!
        // If accurate to the second: when it comes to product display, when will it start and when will it end!
        seckillGoodsQueryWrapper.eq("DATE_FORMAT(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));
        List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(seckillGoodsQueryWrapper);
        // Put the collection data into the cache
        if (!CollectionUtils.isEmpty(seckillGoodsList)){
            for (SeckillGoods seckillGoods: seckillGoodsList) {
                // Put the data in the cache! Data type, key!
                String seckillKey = RedisConst.SECKILL_GOODS;
                // Determine whether the current spike product exists in the cache!
                // Determine whether the current field exists in this key! field = skuId
                Boolean flag = redisTemplate.boundHashOps(seckillKey).hasKey(seckillGoods.getSkuId().toString());
                // flag = true means there is data, do not insert data.
                if (flag){
                    continue;
                }
                // The first way to put it!
                // redisTemplate.opsForHash().put(seckillKey,seckillGoods.getSkuId().toString(),seckillGoods);
                // The second way to put it in:
                redisTemplate.boundHashOps(seckillKey).put(seckillGoods.getSkuId().toString(),seckillGoods);
                // How to control inventory overbought? Put the inventory quantity of the goods into the cache! num = 10;
                for (Integer i = 0; i <seckillGoods.getStockCount(); i++) {
                    // Define key, value!
                    String key = RedisConst.SECKILL_STOCK_PREFIX+seckillGoods.getSkuId();
                    // key = seckill:stock:skuId
                    // value = skuId seckillGoods.getSkuId().toString()
                    // redisTemplate.opsForList().leftPush(key,seckillGoods.getSkuId().toString());
                    redisTemplate.boundListOps(key).leftPush(seckillGoods.getSkuId().toString());

                }

                // Status bit initialization: skuId:1 | 46:1
                redisTemplate.convertAndSend("seckillpush",seckillGoods.getSkuId()+":1");

            }
        }
        // manual confirmation
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    // Listen to the data in the spike queue!
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),
            key = {MqConst.ROUTING_SECKILL_USER}
    ))
    public void seckill(UserRecode userRecode, Message message, Channel channel){
        // Determine whether the current object is empty
        if(userRecode!=null){
            // Call the method! Pre-order
            seckillGoodsService.seckillOrder(userRecode.getSkuId(),userRecode.getUserId());
        }
        // Manually confirm!
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }

    // Listen for events:
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_18,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_18}
    ))
    public void clearRedis( Message message, Channel channel){
        // Query the products that the spike has ended!
        //Clear the cache at the end of the event
        QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.le("end_time", new Date());
        // What I found was the spike product at the end of the day!
        List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(queryWrapper);

        for (SeckillGoods seckillGoods: seckillGoodsList) {
            // RedisConst.SECKILL_GOODS seckill:goods
            // All data of seckill goods: seckill:goods field = skuId
            // If there are multiple spike products, use the following deletion method!
            // redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).delete(seckillGoods.getSkuId().toString());
            // Delete inventory! seckill:stock:skuId seckill:stock:46
            redisTemplate.delete(RedisConst.SECKILL_STOCK_PREFIX+seckillGoods.getSkuId());
        }

        // If there is only one spike product!
        redisTemplate.delete(RedisConst.SECKILL_GOODS);
        // seckill:orders why delete it!
        redisTemplate.delete(RedisConst.SECKILL_ORDERS);
        // delete the real order
        redisTemplate.delete(RedisConst.SECKILL_ORDERS_USERS);

        // Modify the database
        // Change the status of the seckill end product to 2
        SeckillGoods seckillGoods = new SeckillGoods();
        seckillGoods.setStatus("2");
        seckillGoodsMapper.update(seckillGoods,queryWrapper);

        // Manually confirm!
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }

}
