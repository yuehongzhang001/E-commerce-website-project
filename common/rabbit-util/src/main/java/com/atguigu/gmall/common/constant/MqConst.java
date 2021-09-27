package com.atguigu.gmall.common.constant;

public class MqConst {
    /**
     * Message compensation
     */
    public static final String MQ_KEY_PREFIX = "mq:list";
    public static final int RETRY_COUNT = 3;
    /**
     * Commodity on and off shelves
     */
    public static final String EXCHANGE_DIRECT_GOODS = "exchange.direct.goods";
    public static final String ROUTING_GOODS_UPPER = "goods.upper";
    public static final String ROUTING_GOODS_LOWER = "goods.lower";
    //queue
    public static final String QUEUE_GOODS_UPPER = "queue.goods.upper";
    public static final String QUEUE_GOODS_LOWER = "queue.goods.lower";
    /**
     * Cancel order, send delay queue
     */
    public static final String EXCHANGE_DIRECT_ORDER_CANCEL = "exchange.direct.order.cancel";//"exchange.direct.order.create" test_exchange;
    public static final String ROUTING_ORDER_CANCEL = "order.create";
    //Delayed cancellation of order queue
    public static final String QUEUE_ORDER_CANCEL = "queue.order.cancel";
    //Cancel order delay time unit: second real business
    public static final int DELAY_TIME = 24*60*60;
    // Test cancel order
    // public static final int DELAY_TIME = 5;
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

    /**
     * Close transaction
     */
    public static final String EXCHANGE_DIRECT_PAYMENT_CLOSE = "exchange.direct.payment.close";
    public static final String ROUTING_PAYMENT_CLOSE = "payment.close";
    //queue
    public static final String QUEUE_PAYMENT_CLOSE = "queue.payment.close";
    /**
     * Timing tasks
     */
    public static final String EXCHANGE_DIRECT_TASK = "exchange.direct.task";
    public static final String ROUTING_TASK_1 = "seckill.task.1";
    //queue
    public static final String QUEUE_TASK_1 = "queue.task.1";
    /**
     * Spike
     */
    public static final String EXCHANGE_DIRECT_SECKILL_USER = "exchange.direct.seckill.user";
    public static final String ROUTING_SECKILL_USER = "seckill.user";
    //queue
    public static final String QUEUE_SECKILL_USER = "queue.seckill.user";

    /**
     * Timing tasks
     */

    public static final String ROUTING_TASK_18 = "seckill.task.18";
    //queue
    public static final String QUEUE_TASK_18 = "queue.task.18";


}