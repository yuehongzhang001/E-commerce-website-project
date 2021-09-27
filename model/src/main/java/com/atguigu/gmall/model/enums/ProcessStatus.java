package com.atguigu.gmall.model.enums;

public enum ProcessStatus {
    UNPAID("Unpaid", OrderStatus.UNPAID),
    PAID("paid", OrderStatus.PAID),
    NOTIFIED_WARE("Warehouse notified", OrderStatus.PAID),
    WAITING_DELEVER("Pending Shipment", OrderStatus.WAITING_DELEVER),
    STOCK_EXCEPTION("Inventory exception", OrderStatus.PAID),
    DELEVERED("Delivered", OrderStatus.DELEVERED),
    CLOSED("Closed", OrderStatus.CLOSED),
    FINISHED("Finished", OrderStatus.FINISHED),
    PAY_FAIL("payment failed", OrderStatus.UNPAID),
    SPLIT("Order has been split", OrderStatus.SPLIT);

    private String comment;
    private OrderStatus orderStatus;

    ProcessStatus(String comment, OrderStatus orderStatus){
        this.comment=comment;
        this.orderStatus=orderStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

}