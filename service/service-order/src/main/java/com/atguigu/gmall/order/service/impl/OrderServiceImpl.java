package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Yuehong Zhang
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper,OrderInfo> implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitService rabbitService;


    @Value("${ware.url}")
    private String wareUrl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrderInfo(OrderInfo orderInfo) {
       /*
        1. orderInfo ,orderDetail
        Need to manually set: total_amount, order_status, user_id, out_trade_no, trade_body, create_time, expire_time, process_status
         */
        // Total amount: unit price * quantity
        orderInfo.sumTotalAmount(); // automatic assignment
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        // Can I get the userId in the implementation class? Get it in the controller!
        // The third-party transaction number must be unique!
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        // Order description
        orderInfo.setTradeBody("If you have money, you want to spend it, just play!");
        // You can splice the skuName in the delivery list! Put it in TradeBody!
        orderInfo.setCreateTime(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime()); // Expires in 24 hours by default!

        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        orderInfoMapper.insert(orderInfo);
        // Get order details:
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail: orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        }
        Long orderId = orderInfo.getId();

        // The content sent is: Order Id
        rabbitService.sendDelayMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,MqConst.ROUTING_ORDER_CANCEL,orderId,MqConst.DELAY_TIME);
        // Return order Id
        return orderId;
    }

    @Override
    public String getTradeNo(String userId) {
        // make serial number
        String tradeNo = UUID.randomUUID().toString();
        // Store in the cache
        String tradeNoKey = "user:" + userId + ":tradeCode";
        redisTemplate.opsForValue().set(tradeNoKey,tradeNo);
        //  return!
        return tradeNo;
    }

    @Override
    public Boolean checkTradeNo(String tradeNo, String userId) {
        // Get the cached key
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String tradeNoRedis = (String) redisTemplate.opsForValue().get(tradeNoKey);
        // Compare and return the result!
        return tradeNo.equals(tradeNoRedis);
    }

    @Override
    public void deleteTradeNo(String userId) {
        // Get the cached key
        String tradeNoKey = "user:" + userId + ":tradeCode";
        redisTemplate.delete(tradeNoKey);
    }

    @Override
    public boolean checkStock(Long skuId, Integer skuNum) {
        // http://localhost:9001/hasStock?skuId=10221&num=2
        // http://localhost:9001
        String result = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        // 0: not in stock 1: in stock
        // judge and return!
        return "1".equals(result);
    }

    @Override
    public void execExpiredOrder(Long orderId) {
        // Execute the update statement!
        updateOrderStatus(orderId,ProcessStatus.CLOSED);
        // You may need to update the order progress status in the future, and you also need to update the order status!
        // void updateOrderStatus(Long orderId, ProcessStatus processStatus);

        // Send a message to close the transaction record
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE,MqConst.ROUTING_PAYMENT_CLOSE,orderId);
    }

    @Override
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        // Get the order status from the order progress status!
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        orderInfo.setProcessStatus(processStatus.name());
        orderInfo.setId(orderId);
        // orderInfoMapper.update();
        orderInfoMapper.updateById(orderInfo);

    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        // Query the order object according to the order Id
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        // Order details may be used in the follow-up:
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(new QueryWrapper<OrderDetail>().eq("order_id", orderId));
        if (orderInfo!=null){
            orderInfo.setOrderDetailList(orderDetailList);
        }
        // Return the order object
        return orderInfo;
    }

    @Override
    public void sendOrderStatus(Long orderId) {
        // change order status
        this.updateOrderStatus(orderId,ProcessStatus.NOTIFIED_WARE);
        // Message sent:
        String wareJson = initWareOrder(orderId);
        //  send messages:
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK,MqConst.ROUTING_WARE_STOCK,wareJson);
    }

    /**
     * Send the string of the inventory reduction message!
     * @param orderId
     * @return
     */
    private String initWareOrder(Long orderId) {
        // The string sent is composed of some fields in OrderInfo!
        OrderInfo orderInfo = this.getOrderInfo(orderId);

        // {"orderId":"1","consignee":"admin",.....}
        // Need to combine the data in orderInfo into Map
        Map map = initWareOrder(orderInfo);
        // map.put("orderId",orderInfo.getId()); map.put("consignee","aaa");

        return JSON.toJSONString(map);
    }

    /**
     * Convert some fields of orderInfo to map collection
     * @param orderInfo
     * @return
     */
    public Map initWareOrder(OrderInfo orderInfo) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel", orderInfo.getConsigneeTel());
        map.put("orderComment", orderInfo.getOrderComment());
        map.put("orderBody", orderInfo.getTradeBody());
        map.put("deliveryAddress", orderInfo.getDeliveryAddress());
        map.put("paymentWay", "2");
        map.put("wareId", orderInfo.getWareId());  // 拆单的时候使用！

        // Declare a collection to get data
        List<Map> orderDetails = new ArrayList<>();
        // Must get order details first
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail: orderDetailList) {
            HashMap<String, Object> detailMap = new HashMap<>();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailMap.put("skuName",orderDetail.getSkuName());
            orderDetails.add(detailMap);
        }
        /*
        details:[{skuId:101,skuNum:1,skuName:’小米手64G’},
        {skuId:201,skuNum:1,skuName:’Sony Headphones’}]
         */
        map.put("details", orderDetails); // Order details:
        // return map collection
        return map;
    }

    // Get the collection of all sub-orders
    @Override
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {
        /*
            1. The original order obtained first!
            2. According to wareSkuMap [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}] Converted into operable data!
            3. Generate a new sub-order, assign value, and save the sub-order!
            4. Add the sub-order to the collection!
            5. Modify the status of the original order!
         */
        // Define a collection of sub-orders
        List<OrderInfo> subOrderInfoList = new ArrayList<>();
        // {44,45,46}
        OrderInfo orderInfoOrigin = this.getOrderInfo(Long.parseLong(orderId));
        // [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        // [{"wareId":"1","skuIds":["45","44"]},{"wareId":"2","skuIds":["46"]}]
        List<Map> mapList = JSON.parseArray(wareSkuMap, Map.class);
        if (!CollectionUtils.isEmpty(mapList)){
            // loop traversal
            for (Map map: mapList) {
                // Warehouse Id
                String wareId = (String) map.get("wareId");
                // Get the skuId list
                List<String> skuIds = (List<String>) map.get("skuIds");
                // Create a new sub-order
                OrderInfo subOrderInfo = new OrderInfo();
                // sequential assignment
                BeanUtils.copyProperties(orderInfoOrigin,subOrderInfo);
                // Total amount {related to order details}, parent Id
                subOrderInfo.setParentOrderId(Long.parseLong(orderId));
                // id increment
                subOrderInfo.setId(null);
                // Assign warehouse Id
                subOrderInfo.setWareId(wareId);
                // The order details of the goods needed to calculate the total amount:
                List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
                List<OrderDetail> orderDetails = new ArrayList<>();
                // loop traversal
                for (OrderDetail orderDetail: orderDetailList) {
                    for (String skuId: skuIds) {
                        // compare skuId
                        if (orderDetail.getSkuId()==Long.parseLong(skuId)){
                            // Get the order details of the current warehouse
                            orderDetails.add(orderDetail);
                        }
                    }
                }
                // Assign order details to sub-orders
                subOrderInfo.setOrderDetailList(orderDetails);
                subOrderInfo.sumTotalAmount();

                // save the sub-order
                saveOrderInfo(subOrderInfo);
                // Add sub-orders to the collection
                subOrderInfoList.add(subOrderInfo);
            }
        }
        // Modify the original order status
        this.updateOrderStatus(Long.parseLong(orderId),ProcessStatus.SPLIT);
        // Return to the collection of sub-orders
        return subOrderInfoList;
    }

    @Override
    public void execExpiredOrder(Long orderId, String flag) {
        // Execute the update statement!
        updateOrderStatus(orderId,ProcessStatus.CLOSED);

        if ("2".equals(flag)){
            // Send a message to close the transaction record
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE,MqConst.ROUTING_PAYMENT_CLOSE,orderId);
        }


    }
}
