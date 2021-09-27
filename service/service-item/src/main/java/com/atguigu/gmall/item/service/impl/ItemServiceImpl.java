package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Yuehong Zhang
 * @date 2021-4-14 14:10:55
 */
@Service
public class ItemServiceImpl implements ItemService {

    //  远程调用service-product-client
    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private ListFeignClient listFeignClient;

    @Override
    public Map<String, Object> getItemBySkuId(Long skuId) {
        //  声明对象
        Map<String, Object> result = new HashMap<>();
        //  Supplier T get();
        //  使用异步编排！
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //  获取到的数据是skuInfo + skuImageList
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            //  map 中 key 对应的谁? Thymeleaf 获取数据的时候 ${skuInfo.skuName}
            result.put("skuInfo",skuInfo);
            //  返回数据
            return skuInfo;
        },threadPoolExecutor);

        //  Consumer  void accept(T t);
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo -> {
            //  获取分类数据
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            result.put("categoryView", categoryView);
        }),threadPoolExecutor);

        CompletableFuture<Void> spuSaleAttrCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo -> {
            //  获取销售属性+销售属性值
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            result.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        }),threadPoolExecutor);

        CompletableFuture<Void> mapCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo -> {

            //  查询销售属性值Id 与skuId 组合的map
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            //  将这个map 转换为页面需要的Json 对象
            String valueJson = JSON.toJSONString(skuValueIdsMap);
            result.put("valuesSkuJson", valueJson);
        }),threadPoolExecutor);

        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            //  获取价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            //  保存数据
            result.put("price", skuPrice);
        },threadPoolExecutor);

        //  调用热度排名方法
        CompletableFuture<Void> hotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        },threadPoolExecutor);

        //  使用多任务进行组合
        CompletableFuture.allOf(
                skuInfoCompletableFuture,
                categoryViewCompletableFuture,
                spuSaleAttrCompletableFuture,
                mapCompletableFuture,
                priceCompletableFuture,
                hotScoreCompletableFuture
                ).join();

        return result;
    }
}
