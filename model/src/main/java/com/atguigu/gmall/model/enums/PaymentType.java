package com.atguigu.gmall.model.enums;

public enum PaymentType {
    ALIPAY("Alipay"),
    WEIXIN("WeChat" );

    private String comment;


    PaymentType(String comment ){
        this.comment=comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}