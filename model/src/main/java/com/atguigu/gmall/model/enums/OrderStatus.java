package com.atguigu.gmall.model.enums;

public enum OrderStatus {
    UNPAID("Unpaid"),
    PAID("paid"),
    WAITING_DELEVER("Pending Shipment"),
    DELEVERED("Delivered"),
    CLOSED("Closed"),
    FINISHED("finished"),
    SPLIT("Order has been split");

    private String comment;

    public static String getStatusNameByStatus(String status) {
        OrderStatus arrObj[] = OrderStatus.values();
        for (OrderStatus obj: arrObj) {
            if (obj.name().equals(status)) {
                return obj.getComment();
            }
        }
        return "";
    }

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