package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

/**
 * @author Yuehong Zhang
 */
public interface CartAsyncService {

    // add
    void saveCartInfo(CartInfo cartInfo);
    //  renew
    void updateCartInfo(CartInfo cartInfo);
    //  delete
    void deleteCartInfo(CartInfo cartInfo);
    //  delete
    void deleteCartInfo(String userId);
    // Selected status change
    void checkCart(String userId, Integer isChecked, Long skuId);
    /**
     * delete
     * @param userId
     * @param skuId
     */
    void deleteCartInfo(String userId, Long skuId);

}