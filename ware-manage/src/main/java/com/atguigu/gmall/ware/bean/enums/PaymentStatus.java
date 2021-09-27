package com.atguigu.gmall.ware.bean.enums;

/**
 * @param
 * @return
 */
public enum PaymentStatus {

    UNPAID("Paying"),
    PAID("paid"),
    PAY_FAIL("payment failed"),
    ClOSED("Closed");

    private String name;

    PaymentStatus(String name) {
        this.name=name;
    }
}