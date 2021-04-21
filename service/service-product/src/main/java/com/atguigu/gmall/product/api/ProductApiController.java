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
 * @author mqx
 * @date 2021-4-14 14:25:32
 */
//  给其他微服务使用！
@RestController
@RequestMapping("api/product")
public class ProductApiController {

    @Autowired
    private ManageService manageService;

    //  带有inner 的url 表示给内部微服务提供的数据接口
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId){
        //  根据skuId 获取数据
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        //  返回skuInfo;
        return skuInfo;
    }

    //   根据三级分类Id 获取对应的分类名称
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){
        //  根据三级分类Id 获取对应的分类名称
        return manageService.getCategoryViewByCategory3Id(category3Id);
    }

    //  根据skuId 获取商品的价格
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){
        //   根据skuId 获取商品的价格
        return manageService.getSkuPrice(skuId);
    }

    //  根据spuId,skuId 查询销售属性+销售属性值
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId){
        //  调用服务层方法
        return manageService.getSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    //  根据spuId 获取销售属性值Id 与skuId 组成的map
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId){
        //  调用服务层方法
        return manageService.getSkuValueIdsMap(spuId);
    }

    @GetMapping("getBaseCategoryList")
    public Result getBaseCategoryList(){
        //  获取到所有的分类数据集合
        List<JSONObject> baseCategoryList = manageService.getBaseCategoryList();
        return Result.ok(baseCategoryList);
    }

    /**
     * 通过品牌Id 集合来查询数据
     * @param tmId
     * @return
     */
    @GetMapping("inner/getTrademark/{tmId}")
    public BaseTrademark getTrademark(@PathVariable("tmId")Long tmId){
        return manageService.getTrademarkByTmId(tmId);
    }

    //  根据skuId 获取到平台属性，平台属性值集合
    @GetMapping("inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId){
        return manageService.getAttrList(skuId);
    }
}
