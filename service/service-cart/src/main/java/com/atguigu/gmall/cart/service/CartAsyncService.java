package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

/**
 * @author mqx
 */
public interface CartAsyncService {

    //  新增
    void saveCartInfo(CartInfo cartInfo);
    //  更新
    void updateCartInfo(CartInfo cartInfo);
    //  删除
    void deleteCartInfo(CartInfo cartInfo);
    //  删除
    void deleteCartInfo(String userId);
    //  选中状态变更
    void checkCart(String userId, Integer isChecked, Long skuId);
    /**
     * 删除
     * @param userId
     * @param skuId
     */
    void deleteCartInfo(String userId, Long skuId);

}
