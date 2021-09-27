package com.atguigu.gmall.model.enums;

import lombok.Getter;

@Getter
public enum CouponStatus {
    NOT_USED("Not used"),
    USE_RUN("in use"),
    USED("Used");

    private String comment;

    CouponStatus(String comment ){
        this.comment=comment;
    }
}