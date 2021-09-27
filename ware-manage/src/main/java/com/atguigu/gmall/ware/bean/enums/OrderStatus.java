package com.atguigu.gmall.ware.bean.enums;

/**
 * @param
 * @return
 */
public enum OrderStatus {
    UNPAID("Unpaid"),
    PAID("paid"),
    WAITING_DELEVER("Pending Shipment"),
    DELEVERED("Delivered"),
    CLOSED("Closed"),
    FINISHED("finished"),
    SPLIT("Order has been split");

    private String comment;


    OrderStatus(String comment ){
        this.comment=comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


}