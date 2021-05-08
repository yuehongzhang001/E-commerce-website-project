package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

/**
 * @author mqx
 */
public interface SeckillGoodsService {

    //  查询所有的秒杀商品
    List<SeckillGoods> findAll();

    //  根据商品Id 查询商品详情数据
    SeckillGoods findSeckillGoodsById(Long skuId);

    //  监听消息队列
    void seckillOrder(Long skuId, String userId);

    //  检查秒杀的状态！目的给页面提供数据
    Result checkOrder(Long skuId, String userId);
}
