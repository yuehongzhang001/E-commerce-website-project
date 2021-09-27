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
 * @author Yuehong Zhang
 */
@RestController
@RequestMapping("api/list")
public class ListApiController {

    // Introduce the client
    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    // ElasticsearchRestTemplate uses RestHighLevelClient

    @Autowired
    private SearchService searchService;

    @GetMapping("inner/createIndex")
    public Result createIndex(){
        // Create index library, mapping mapping
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        //  return
        return Result.ok();
    }

    // Commodity on the shelf
    @GetMapping("inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable Long skuId){
        searchService.upperGoods(skuId);
        return Result.ok();
    }

    // Commodity removal
    @GetMapping("inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable Long skuId){
        searchService.lowerGoods(skuId);
        return Result.ok();
    }

    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable("skuId") Long skuId) {
        // Call the service layer
        searchService.incrHotScore(skuId);
        return Result.ok();
    }

    // Define the URL of the search result
    // @RequestBody: Convert Json data into Java objects!
    // The user is visiting the retrieved url: http://list.gmall.com/list.html?category3Id=61
    // How does web-all receive the data passed by the user
    @PostMapping
    public Result list(@RequestBody SearchParam searchParam){
        // Call the service layer method
        SearchResponseVo responseVo = searchService.search(searchParam);
        return Result.ok(responseVo);
    }

}