package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @author mqx
 */
public interface CartInfoService {

    /**
     *  添加购物车的方法！
     * @param skuId
     * @param userId
     * @param skuNum
     */
    void addToCart(Long skuId, String userId, Integer skuNum);

    /**
     *  根据用户Id，临时用户Id 获取购物车列表
     * @param userId
     * @param userTempId
     * @return
     */
    List<CartInfo> getCartList(String userId, String userTempId);

    /**
     *  更新选中状态！
     * @param userId
     * @param isChecked
     * @param skuId
     */
    void checkCart(String userId, Integer isChecked, Long skuId);

    /**
     * 删除购物项
     * @param skuId
     * @param userId
     */
    void deleteCart(Long skuId, String userId);

    /**
     * 根据用户Id 获取购物车{选中}列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);

    /**
     * 根据userId 查询购物车最新数据
     * @param userId
     * @return
     */
    List<CartInfo> loadCartCache(String userId);
}
