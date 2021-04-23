package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;


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

    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable("skuId") Long skuId) {
        // 调用服务层
        searchService.incrHotScore(skuId);
        return Result.ok();
    }

    //  定义检索结果的URL
    //  @RequestBody：将Json 数据转换为Java 对象！
    //  用户在访问检索的url: http://list.gmall.com/list.html?category3Id=61
    //  web-all 如何接收用户传递的数据
    @PostMapping
    public Result list(@RequestBody SearchParam searchParam){
        //  调用服务层方法
        SearchResponseVo responseVo = searchService.search(searchParam);
        return Result.ok(responseVo);
    }

}
