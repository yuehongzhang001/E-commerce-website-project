package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author mqx
 */
@Service
public class SearchServiceImpl implements SearchService {

    //  服务层调用 客户端！  GoodsRepository 自定义的数据接口！ 继承 ElasticsearchRepository<T, ID> 当前这个类数据接口就具有了CRUD 方法！
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public void upperGoods(Long skuId) {
        //  声明一个Goods 对象
        Goods goods = new Goods();

        //  异步编排！
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo!=null){
            goods.setId(skuId);
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setTitle(skuInfo.getSkuName());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setCreateTime(new Date());

            //  赋值品牌数据
            BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
            goods.setTmId(skuInfo.getTmId());
            goods.setTmName(trademark.getTmName());
            goods.setTmLogoUrl(trademark.getLogoUrl());

            //  赋值分类数据：
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            goods.setCategory1Id(categoryView.getCategory1Id());
            goods.setCategory2Id(categoryView.getCategory2Id());
            goods.setCategory3Id(categoryView.getCategory3Id());
            goods.setCategory1Name(categoryView.getCategory1Name());
            goods.setCategory2Name(categoryView.getCategory2Name());
            goods.setCategory3Name(categoryView.getCategory3Name());

            //  赋值平台属性集
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            //  Function R apply(T t)
            //  Stream() 流式编程
            List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
                //  创建一个对象
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                return searchAttr;
            }).collect(Collectors.toList());

            goods.setAttrs(searchAttrList);
        }

        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        //  根据id 删除
        goodsRepository.deleteById(skuId);
    }
}
