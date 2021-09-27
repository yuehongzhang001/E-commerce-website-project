package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author Yuehong Zhang
 */
public interface OrderService extends IService<OrderInfo> {

    // Save the order:
    Long saveOrderInfo(OrderInfo orderInfo);

    /**
     * Get transaction number
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * Compare transaction number
     * @param tradeNo
     * @param userId
     * @return
     */
    Boolean checkTradeNo(String tradeNo,String userId);

    /**
     * Delete transaction number
     * @param userId
     */
    void deleteTradeNo(String userId);

    /**
     * Verify inventory
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(Long skuId, Integer skuNum);

    /**
     * Modify order status!
     * @param orderId
     */
    void execExpiredOrder(Long orderId);

    /**
     * Update the order status and order progress status according to the order ID
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(Long orderId, ProcessStatus processStatus);

    /**
     * Query order information according to order Id
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(Long orderId);

    /**
     * Send a message to the inventory according to the order Id, and modify the order status!
     * @param orderId
     */
    void sendOrderStatus(Long orderId);

    /**
     * Change orderInfo data to Map
     * @param orderInfo
     * @return
     */
    Map initWareOrder(OrderInfo orderInfo);

    /**
     * Splitting method
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);

    /**
     * Close expired orders
     * @param orderId
     * @param flag whether paymentInfo needs to be closed
     */
    void execExpiredOrder(Long orderId, String flag);
}