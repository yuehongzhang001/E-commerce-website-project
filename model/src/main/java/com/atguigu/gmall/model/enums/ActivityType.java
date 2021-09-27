package com.atguigu.gmall.model.enums;

import lombok.Getter;

@Getter
public enum ActivityType {
    FULL_REDUCTION("full reduction"),
    FULL_DISCOUNT("Full amount discount" );

    private String comment;

    ActivityType(String comment ){
        this.comment=comment;
    }

    public static String getNameByType(String type) {
        ActivityType arrObj[] = ActivityType.values();
        for (ActivityType obj: arrObj) {
            if (obj.name().equals(type)) {
                return obj.getComment();
            }
        }
        return "";
    }
}