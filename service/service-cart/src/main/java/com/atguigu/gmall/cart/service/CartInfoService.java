package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @author Yuehong Zhang
 */
public interface CartInfoService {

    /**
     * How to add a shopping cart!
     * @param skuId
     * @param userId
     * @param skuNum
     */
    void addToCart(Long skuId, String userId, Integer skuNum);

    /**
     * According to user Id, temporary user Id get the shopping cart list
     * @param userId
     * @param userTempId
     * @return
     */
    List<CartInfo> getCartList(String userId, String userTempId);

    /**
     * Update the selected status!
     * @param userId
     * @param isChecked
     * @param skuId
     */
    void checkCart(String userId, Integer isChecked, Long skuId);

    /**
     * Delete shopping item
     * @param skuId
     * @param userId
     */
    void deleteCart(Long skuId, String userId);

    /**
     * Get the shopping cart {selected} list according to the user Id
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);

    /**
     * Query the latest data of the shopping cart based on userId
     * @param userId
     * @return
     */
    List<CartInfo> loadCartCache(String userId);
}