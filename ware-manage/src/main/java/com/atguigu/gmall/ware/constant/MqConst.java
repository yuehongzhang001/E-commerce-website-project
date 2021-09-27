package com.atguigu.gmall.ware.constant;

public class MqConst {

    /**
     * Cancel order, send delay queue
     */
    public static final String EXCHANGE_DIRECT_ORDER_CANCEL = "exchange.direct.order.cancel";//"exchange.direct.order.create" test_exchange;
    public static final String ROUTING_ORDER_CANCEL = "order.create";
    //Delayed cancellation of order queue
    public static final String QUEUE_ORDER_CANCEL = "queue.order.cancel";
    public static final String QUEUE_SECKILL_ORDER_CANCEL = "queue.seckill.order.cancel";
    //delay
    public static final int DELAY_TIME = 30*60;

    /**
     * s
     */
    public static final String EXCHANGE_DIRECT_PAYMENT_PAY = "exchange.direct.payment.pay";
    public static final String ROUTING_PAYMENT_PAY = "payment.pay";
    //queue
    public static final String QUEUE_PAYMENT_PAY = "queue.payment.pay";

    /**
     * Reduce inventory
     */
    public static final String EXCHANGE_DIRECT_WARE_STOCK = "exchange.direct.ware.stock";
    public static final String ROUTING_WARE_STOCK = "ware.stock";
    //queue
    public static final String QUEUE_WARE_STOCK = "queue.ware.stock";

    /**
     * The inventory reduction is successful, and the order status is updated
     */
    public static final String EXCHANGE_DIRECT_WARE_ORDER = "exchange.direct.ware.order";
    public static final String ROUTING_WARE_ORDER = "ware.order";
    //queue
    public static final String QUEUE_WARE_ORDER = "queue.ware.order";

    public static final String MQ_KEY_PREFIX = "mq:list";
    public static final int RETRY_COUNT = 3;

}