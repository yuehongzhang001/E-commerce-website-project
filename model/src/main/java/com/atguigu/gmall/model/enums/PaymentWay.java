package com.atguigu.gmall.model.enums;

public enum PaymentWay {
    ONLINE("Online Payment"),
    OUTLINE("Cash on delivery" );

    private String comment;


    PaymentWay(String comment ){
        this.comment=comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}