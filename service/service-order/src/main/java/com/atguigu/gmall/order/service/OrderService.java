package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author mqx
 */
public interface OrderService extends IService<OrderInfo> {

    //  保存订单：
    Long saveOrderInfo(OrderInfo orderInfo);

    /**
     * 获取交易号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 比较交易号
     * @param tradeNo
     * @param userId
     * @return
     */
    Boolean checkTradeNo(String tradeNo,String userId);

    /**
     * 删除交易号
     * @param userId
     */
    void deleteTradeNo(String userId);

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(Long skuId, Integer skuNum);

    /**
     * 修改订单状态！
     * @param orderId
     */
    void execExpiredOrder(Long orderId);

    /**
     * 根据订单Id 更新订单状态，订单进度状态
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(Long orderId, ProcessStatus processStatus);

    /**
     * 根据订单Id 查询订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(Long orderId);

    /**
     * 根据订单Id 发送消息给库存，并修改订单状态！
     * @param orderId
     */
    void sendOrderStatus(Long orderId);

    /**
     * 将orderInfo 数据变为Map
     * @param orderInfo
     * @return
     */
    Map initWareOrder(OrderInfo orderInfo);

    /**
     * 拆单方法
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);

    /**
     * 关闭过期订单
     * @param orderId
     * @param flag 是否需要关闭paymentInfo
     */
    void execExpiredOrder(Long orderId, String flag);
}
