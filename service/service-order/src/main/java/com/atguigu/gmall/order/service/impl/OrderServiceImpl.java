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
 * @author mqx
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
        1.  orderInfo ,orderDetail
        需要手动设置：total_amount，order_status，user_id，out_trade_no，trade_body，create_time,expire_time,process_status
         */
        //  总金额：单价*数量
        orderInfo.sumTotalAmount(); // 自动赋值
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //  在实现类中能否获取到userId? 在控制器获取！
        //  第三方交易编号要求唯一！
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //  订单描述
        orderInfo.setTradeBody("有钱就想花，就是玩!");
        //  可以将送货清单中的skuName 进行拼接！放入TradeBody 中！
        orderInfo.setCreateTime(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime()); // 默认24小时过期！

        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        orderInfoMapper.insert(orderInfo);
        //  获取到订单明细：
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        }
        Long orderId = orderInfo.getId();

        //  发的内容是：订单Id
        rabbitService.sendDelayMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,MqConst.ROUTING_ORDER_CANCEL,orderId,MqConst.DELAY_TIME);
        //  返回订单Id
        return orderId;
    }

    @Override
    public String getTradeNo(String userId) {
        //  制作流水号
        String tradeNo = UUID.randomUUID().toString();
        //  存储到缓存中
        String tradeNoKey = "user:" + userId + ":tradeCode";
        redisTemplate.opsForValue().set(tradeNoKey,tradeNo);
        //  返回！
        return tradeNo;
    }

    @Override
    public Boolean checkTradeNo(String tradeNo, String userId) {
        //  获取到缓存的key
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String tradeNoRedis = (String) redisTemplate.opsForValue().get(tradeNoKey);
        //  比较并返回结果！
        return tradeNo.equals(tradeNoRedis);
    }

    @Override
    public void deleteTradeNo(String userId) {
        //  获取到缓存的key
        String tradeNoKey = "user:" + userId + ":tradeCode";
        redisTemplate.delete(tradeNoKey);
    }

    @Override
    public boolean checkStock(Long skuId, Integer skuNum) {
        //  http://localhost:9001/hasStock?skuId=10221&num=2
        //  http://localhost:9001
        String result = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        //  0：无库存   1：有库存
        //  判断并返回！
        return "1".equals(result);
    }

    @Override
    public void execExpiredOrder(Long orderId) {
        //  执行update 语句！
        updateOrderStatus(orderId,ProcessStatus.CLOSED);
        //  后续可能需要更新订单进度状态，还需要更新订单状态！
        //  void updateOrderStatus(Long orderId, ProcessStatus processStatus);

        //  发送一个消息关闭交易记录
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE,MqConst.ROUTING_PAYMENT_CLOSE,orderId);
    }

    @Override
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        //  从订单进度状态中获取订单状态！
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        orderInfo.setProcessStatus(processStatus.name());
        orderInfo.setId(orderId);
        //  orderInfoMapper.update();
        orderInfoMapper.updateById(orderInfo);

    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        //  根据订单Id 查询订单对象
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        //  后续可能需要用到订单明细：
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(new QueryWrapper<OrderDetail>().eq("order_id", orderId));
        if (orderInfo!=null){
            orderInfo.setOrderDetailList(orderDetailList);
        }
        //  返回订单对象
        return orderInfo;
    }

    @Override
    public void sendOrderStatus(Long orderId) {
        //  更改订单状态
        this.updateOrderStatus(orderId,ProcessStatus.NOTIFIED_WARE);
        //  发送的消息：
        String wareJson = initWareOrder(orderId);
        //  发送消息：
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK,MqConst.ROUTING_WARE_STOCK,wareJson);
    }

    /**
     * 发送减库存消息的字符串！
     * @param orderId
     * @return
     */
    private String initWareOrder(Long orderId) {
        //  发送的字符串是由OrderInfo 中部分字段组成的！
        OrderInfo orderInfo = this.getOrderInfo(orderId);

        // {"orderId":"1","consignee":"admin",.....}
        //  需要将orderInfo 中的数据组成Map
        Map map = initWareOrder(orderInfo);
        //  map.put("orderId",orderInfo.getId()); map.put("consignee","aaa");

        return JSON.toJSONString(map);
    }

    /**
     * 将orderInfo 的部分字段转换为map集合
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

        //  声明一个集合获取数据
        List<Map> orderDetails = new ArrayList<>();
        //  必须先获取订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            HashMap<String, Object> detailMap = new HashMap<>();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailMap.put("skuName",orderDetail.getSkuName());
            orderDetails.add(detailMap);
        }
        /*
        details:[{skuId:101,skuNum:1,skuName:’小米手64G’},
        {skuId:201,skuNum:1,skuName:’索尼耳机’}]
         */
        map.put("details", orderDetails);    //  订单明细：
        //  返回map 集合
        return map;
    }

    //  获取所有子订单集合
    @Override
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {
        /*
            1.  先获取到的原始订单！
            2.  需要根据wareSkuMap [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}] 转换为能操作的数据！
            3.  生成新的子订单，并赋值 ,保存子订单!
            4.  将子订单添加到集合中！
            5.  修改原始订单状态！
         */
        //  定义子订单集合
        List<OrderInfo> subOrderInfoList = new ArrayList<>();
        //  {44,45,46}
        OrderInfo orderInfoOrigin = this.getOrderInfo(Long.parseLong(orderId));
        //  [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        //  [{"wareId":"1","skuIds":["45","44"]},{"wareId":"2","skuIds":["46"]}]
        List<Map> mapList = JSON.parseArray(wareSkuMap, Map.class);
        if (!CollectionUtils.isEmpty(mapList)){
        //  循环遍历
            for (Map map : mapList) {
                //  仓库Id
                String wareId = (String) map.get("wareId");
                //  获取到skuId 列表
                List<String> skuIds = (List<String>) map.get("skuIds");
                //  创建新的子订单
                OrderInfo subOrderInfo = new OrderInfo();
                //  顺序赋值
                BeanUtils.copyProperties(orderInfoOrigin,subOrderInfo);
                //  总金额{跟订单明细有关系}，父Id
                subOrderInfo.setParentOrderId(Long.parseLong(orderId));
                //  id 自增
                subOrderInfo.setId(null);
                //  赋值仓库Id
                subOrderInfo.setWareId(wareId);
                //  计算总金额需要商品的订单明细：
                List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
                List<OrderDetail> orderDetails = new ArrayList<>();
                //  循环遍历
                for (OrderDetail orderDetail : orderDetailList) {
                    for (String skuId : skuIds) {
                        //  比较skuId
                        if (orderDetail.getSkuId()==Long.parseLong(skuId)){
                            //  得到当前仓库的订单明细
                            orderDetails.add(orderDetail);
                        }
                    }
                }
                //  给子订单赋值订单明细
                subOrderInfo.setOrderDetailList(orderDetails);
                subOrderInfo.sumTotalAmount();

                //  保存子订单
                saveOrderInfo(subOrderInfo);
                //  将子订单添加到集合中
                subOrderInfoList.add(subOrderInfo);
            }
        }
        //  修改原始订单状态
        this.updateOrderStatus(Long.parseLong(orderId),ProcessStatus.SPLIT);
        //  返回子订单集合
        return subOrderInfoList;
    }

    @Override
    public void execExpiredOrder(Long orderId, String flag) {
        //  执行update 语句！
        updateOrderStatus(orderId,ProcessStatus.CLOSED);

        if ("2".equals(flag)){
            //  发送一个消息关闭交易记录
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE,MqConst.ROUTING_PAYMENT_CLOSE,orderId);
        }


    }
}
