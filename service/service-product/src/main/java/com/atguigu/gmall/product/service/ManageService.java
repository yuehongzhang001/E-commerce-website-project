package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Yuehong Zhang
 * @date 2021-4-10 10:24:49
 */
public interface ManageService {

   /*
        3.1 First load all the first-level classification data!

3.2 Load the second-level classification data by selecting the first-level classification Id data!

3.3 Load the third-level classification data by selecting the second-level classification data!

3.4 Load the platform attribute list according to the category Id!
    */

    /**
     * Get all first-level classification data
     * @return
     */
    List<BaseCategory1> getBaseCategory1();

    /**
     * According to the first-level classification Id, obtain the second-level classification data
     * @param category1Id
     * @return
     */
    List<BaseCategory2> getBaseCategory2(Long category1Id);

    /**
     * According to the second-level classification Id, obtain the third-level classification data
     * @param category2Id
     * @return
     */
    List<BaseCategory3> getBaseCategory3(Long category2Id);

    /**
     * Obtain the platform attribute list according to the category Id
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> getBaseAttrInfoList(Long category1Id,Long category2Id,Long category3Id);


    /**
     * Save platform properties
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * Obtain the platform attribute value collection according to the platform attribute Id
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(Long attrId);

    /**
     * Get data according to the attribute Id
     * @param attrId
     * @return
     */
    BaseAttrInfo getBaseAttrInfo(Long attrId);

    /**
     * Obtain spuInfo collection data according to the three-level classification Id
     * @param spuInfoPage
     * @param spuInfo
     * @return
     */
    IPage<SpuInfo> getSpuInfoList(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo);

    /**
     * Get a list of all sales attributes
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * Save spuInfo data
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * Query spuImage list according to spuId
     * @param spuId
     * @return
     */
    List<SpuImage> getSpuImageList(Long spuId);

    /**
     * Get the list of sales attributes according to spuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    /**
     * Save skuInfo data
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * Query skuInfo with pagination
     * @param skuInfoPage
     * @return
     */
    IPage getSkuInfoList(Page<SkuInfo> skuInfoPage);

    /**
     * According to skuId for listing operations
     * @param skuId
     */
    void onSale(Long skuId);

    /**
     * Delisting operations based on skuId
     * @param skuId
     */
    void cancelSale(Long skuId);

    /**
     * Query skuInfo and skuImageList according to skuId
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * Query the attribute name according to the three-level classification Id
     * @param category3Id
     * @return
     */
    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);

    /**
     * Query product price according to skuId
     * @param skuId
     * @return
     */
    BigDecimal getSkuPrice(Long skuId);

    /**
     * Query sales attribute + sales attribute value according to spuId, skuId
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    // The return value can customize an entity class {skuId, valueIds} You can also use the map data structure to receive data map(key, value);
    // skuId = 46 valueIds = 124|126; map(skuId,"46") map.put("valueIds","124|126")

    /**
     * Obtain the combined data of the sales attribute value Id and skuId according to spuId
     * @param spuId
     * @return
     */
    Map getSkuValueIdsMap(Long spuId);

    /**
     * Get all classification information
     * @return
     */
    List<JSONObject> getBaseCategoryList();

    /**
     * Query data by brand Id
     * @param tmId
     * @return
     */
    BaseTrademark getTrademarkByTmId(Long tmId);

    /**
     * Query data through the skuId collection
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> getAttrList(Long skuId);




}