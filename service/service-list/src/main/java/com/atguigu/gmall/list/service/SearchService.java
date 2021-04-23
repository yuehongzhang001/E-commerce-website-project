package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

/**
 * @author mqx
 */
public interface SearchService {

    //  商品上架
    void upperGoods(Long skuId);
    //  商品下架
    void lowerGoods(Long skuId);
    /**
     * 更新热度
     * @param skuId
     */
    void incrHotScore(Long skuId);

    /**
     * 检索数据接口
     * @param searchParam
     * @return
     */
    SearchResponseVo search(SearchParam searchParam);

}
