package com.atguigu.gmall.model.enums;

import lombok.Getter;

@Getter
public enum CouponRangeType {
    SPU("single product (spu)"),
    CATAGORY("Category Ticket"),
    TRADEMARK("Brand Voucher");

    private String comment;

    CouponRangeType(String comment ){
        this.comment=comment;
    }

    public static String getNameByType(String type) {
        CouponRangeType arrObj[] = CouponRangeType.values();
        for (CouponRangeType obj: arrObj) {
            if (obj.name().equals(type)) {
                return obj.getComment();
            }
        }
        return "";
    }

}