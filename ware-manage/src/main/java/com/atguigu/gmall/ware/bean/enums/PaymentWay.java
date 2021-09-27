package com.atguigu.gmall.ware.bean.enums;

/**
 * @param
 * @return
 */
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