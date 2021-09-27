package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

/**
 * @author Yuehong Zhang
 */
public interface SearchService {

    // Goods on the shelves
    void upperGoods(Long skuId);
    // product off the shelf
    void lowerGoods(Long skuId);
    /**
     * Update popularity
     * @param skuId
     */
    void incrHotScore(Long skuId);

    /**
     * Retrieve data interface
     * @param searchParam
     * @return
     */
    SearchResponseVo search(SearchParam searchParam);

}