package com.atguigu.gmall.common.result;

import lombok.Getter;

/**
 * Unified return result status information class
 *
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200,"Success"),
    FAIL(201, "Failed"),
    SERVICE_ERROR(2012, "Service Exception"),

    PAY_RUN(205, "Paying"),

    LOGIN_AUTH(208, "Not logged in"),
    PERMISSION(209, "No permission"),
    SECKILL_NO_START(210, "The spike has not yet started"),
    SECKILL_RUN(211, "In the queue"),
    SECKILL_NO_PAY_ORDER(212, "You have an unpaid order"),
    SECKILL_FINISH(213, "Sold Out"),
    SECKILL_END(214, "The spike has ended"),
    SECKILL_SUCCESS(215, "Successful order grab"),
    SECKILL_FAIL(216, "Failed to grab the order"),
    SECKILL_ILLEGAL(217, "The request is illegal"),
    SECKILL_ORDER_SUCCESS(218, "Order successfully placed"),
    COUPON_GET(220, "The coupon has been received"),
    COUPON_LIMIT_GET(221, "The coupon has been issued"),
    ;

    private Integer code;

    private String message;

    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}