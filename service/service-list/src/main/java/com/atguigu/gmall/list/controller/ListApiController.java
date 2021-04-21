package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mqx
 */
@RestController
@RequestMapping("api/list")
public class ListApiController {

    //  引入客户端
    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    //  ElasticsearchRestTemplate 使用RestHighLevelClient

    @Autowired
    private SearchService searchService;

    @GetMapping("inner/createIndex")
    public Result createIndex(){
        //  创建索引库，mapping 映射
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        //  返回
        return Result.ok();
    }

    //  商品的上架
    @GetMapping("inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable Long skuId){
        searchService.upperGoods(skuId);
        return Result.ok();
    }

    //  商品的下架
    @GetMapping("inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable Long skuId){
        searchService.lowerGoods(skuId);
        return Result.ok();
    }

}
