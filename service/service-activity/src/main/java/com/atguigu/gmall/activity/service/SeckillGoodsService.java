package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

/**
 * @author Yuehong Zhang
 */
public interface SeckillGoodsService {

    // Query all spike products
    List<SeckillGoods> findAll();

    // Query product detail data based on product Id
    SeckillGoods findSeckillGoodsById(Long skuId);

    // listen to the message queue
    void seckillOrder(Long skuId, String userId);

    // Check the status of the spike! Purpose to provide data to the page
    Result checkOrder(Long skuId, String userId);
}