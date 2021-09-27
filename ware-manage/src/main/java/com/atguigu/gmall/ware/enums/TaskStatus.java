package com.atguigu.gmall.ware.enums;

/**
 * @param
 * @return
 */
public enum TaskStatus {
    PAID("paid"),
    DEDUCTED("Deduced Inventory"),
    OUT_OF_STOCK("paid, inventory is oversold"),
    DELEVERED("Out of the library"),
    SPLIT("Split");

    private String comment;

    TaskStatus(String comment) {
        this.comment=comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}