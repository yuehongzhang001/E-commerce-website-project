package com.atguigu.gmall.model.enums;

public enum PaymentStatus {
    UNPAID("Paying"),
    PAID("paid"),
    PAY_FAIL("payment failed"),
    CLOSED("Closed");

    private String name;

    PaymentStatus(String name) {
        this.name=name;
    }

}