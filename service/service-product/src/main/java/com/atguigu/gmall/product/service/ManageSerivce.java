package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-4-10 10:24:49
 */
public interface ManageSerivce {

   /*
        3.1	先加载所有的一级分类数据！

		3.2	通过选择一级分类Id数据加载二级分类数据！

		3.3	通过选择二级分类数据加载三级分类数据！

		3.4	根据分类Id 加载 平台属性列表！
    */

    /**
     * 获取所有一级分类数据
      * @return
     */
   List<BaseCategory1> getBaseCategory1();

    /**
     * 根据一级分类Id ，获取二级分类数据
     * @param category1Id
     * @return
     */
   List<BaseCategory2> getBaseCategory2(Long category1Id);

    /**
     * 根据二级分类Id ，获取三级分类数据
     * @param category2Id
     * @return
     */
    List<BaseCategory3> getBaseCategory3(Long category2Id);

    /**
     * 根据分类Id 获取平台属性列表
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> getBaseAttrInfoList(Long category1Id,Long category2Id,Long category3Id);


    /**
     * 保存平台属性
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据平台属性Id 获取平台属性值集合
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(Long attrId);

    /**
     * 根据属性Id 获取数据
     * @param attrId
     * @return
     */
    BaseAttrInfo getBaseAttrInfo(Long attrId);

    /**
     * 根据三级分类Id 获取spuInfo集合数据
     * @param spuInfoPage
     * @param spuInfo
     * @return
     */
    IPage<SpuInfo> getSpuInfoList(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo);

  /**
   * 获取所有的销售属性列表
   * @return
   */
  List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存spuInfo数据
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuId 查询spuImage 列表
     * @param spuId
     * @return
     */
    List<SpuImage> getSpuImageList(Long spuId);

    /**
     * 根据spuId 获取销售属性列表
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    /**
     * 保存skuInfo 数据
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 查询skuInfo 带分页
     * @param skuInfoPage
     * @return
     */
    IPage getSkuInfoList(Page<SkuInfo> skuInfoPage);

    /**
     * 根据skuId 进行上架操作
     * @param skuId
     */
    void onSale(Long skuId);

    /**
     * 根据skuId 进行下架操作
     * @param skuId
     */
    void cancelSale(Long skuId);

    /**
     * 根据skuId 查询skuInfo 以及 skuImageList
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * 根据三级分类Id 查询属性名称
     * @param category3Id
     * @return
     */
    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);

    /**
     * 根据skuId 查询商品价格
     * @param skuId
     * @return
     */
    BigDecimal getSkuPrice(Long skuId);

    /**
     * 根据spuId,skuId 查询销售属性+销售属性值
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    //  返回值可以自定义一个实体类 {skuId, valueIds } 还可以使用map数据结构接收数据 map(key,value);
    //  skuId = 46  valueIds = 124|126;  map(skuId,"46")  map.put("valueIds","124|126")

    /**
     * 根据spuId 获取销售属性值Id与skuId 的组合数据
     * @param spuId
     * @return
     */
    Map getSkuValueIdsMap(Long spuId);
}
