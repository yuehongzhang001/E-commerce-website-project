package com.atguigu.gmall.product.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Yuehong Zhang
 * @date 2021-4-14 14:25:32
 */
// Use for other microservices!
@RestController
@RequestMapping("api/product")
public class ProductApiController {

    @Autowired
    private ManageService manageService;

    // The url with inner represents the data interface provided to internal microservices
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId){
        // Get data according to skuId
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        // return skuInfo;
        return skuInfo;
    }

    // Obtain the corresponding category name according to the three-level category Id
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){
        // Obtain the corresponding category name according to the three-level category Id
        return manageService.getCategoryViewByCategory3Id(category3Id);
    }

    // Get the price of the product according to skuId
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){
        // Get the price of the product according to skuId
        return manageService.getSkuPrice(skuId);
    }

    // Query sales attribute + sales attribute value according to spuId, skuId
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId){
        // Call the service layer method
        return manageService.getSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    // Get the map composed of sales attribute value Id and skuId according to spuId
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId){
        // Call the service layer method
        return manageService.getSkuValueIdsMap(spuId);
    }

    @GetMapping("getBaseCategoryList")
    public Result getBaseCategoryList(){
        // Get all the classification data sets
        List<JSONObject> baseCategoryList = manageService.getBaseCategoryList();
        return Result.ok(baseCategoryList);
    }

    /**
     * Query data through brand Id collection
     * @param tmId
     * @return
     */
    @GetMapping("inner/getTrademark/{tmId}")
    public BaseTrademark getTrademark(@PathVariable("tmId")Long tmId){
        return manageService.getTrademarkByTmId(tmId);
    }

    // Obtain platform attributes and platform attribute value collection according to skuId
    @GetMapping("inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId){
        return manageService.getAttrList(skuId);
    }
}