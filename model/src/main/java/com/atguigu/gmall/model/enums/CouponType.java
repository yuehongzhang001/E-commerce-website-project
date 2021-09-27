package com.atguigu.gmall.model.enums;

import lombok.Getter;

@Getter
public enum CouponType {
    FULL_REDUCTION("full reduction"),
    FULL_DISCOUNT("Full amount discount"),
    CASH("Cash Roll"),
    DISCOUNT("Discount Coupon" );

    private String comment;

    CouponType(String comment ){
        this.comment=comment;
    }

    public static String getNameByType(String type) {
        CouponType arrObj[] = CouponType.values();
        for (CouponType obj: arrObj) {
            if (obj.name().equals(type)) {
                return obj.getComment();
            }
        }
        return "";
    }
}