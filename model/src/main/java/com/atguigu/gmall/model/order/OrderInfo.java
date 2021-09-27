package com.atguigu.gmall.model.order;

import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel(description = "Order Information")
@TableName("order_info")
public class OrderInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Consignee")
    @TableField("consignee")
    private String consignee;

    @ApiModelProperty(value = "recipient phone")
    @TableField("consignee_tel")
    private String consigneeTel;

    @ApiModelProperty(value = "Total Amount")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "Order Status")
    @TableField("order_status")
    private String orderStatus;

    @ApiModelProperty(value = "user id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "payment method")
    @TableField("payment_way")
    private String paymentWay;

    @ApiModelProperty(value = "shipping address")
    @TableField("delivery_address")
    private String deliveryAddress;

    @ApiModelProperty(value = "Order Remarks")
    @TableField("order_comment")
    private String orderComment;

    @ApiModelProperty(value = "Order transaction number (for third-party payment)")
    @TableField("out_trade_no")
    private String outTradeNo;

    @ApiModelProperty(value = "Order description (for third-party payment)")
    @TableField("trade_body")
    private String tradeBody;

    @ApiModelProperty(value = "Creation Time")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "expiration time")
    @TableField("expire_time")
    private Date expireTime;

    @ApiModelProperty(value = "Progress Status")
    @TableField("process_status")
    private String processStatus;

    @ApiModelProperty(value = "Logistics Order Number")
    @TableField("tracking_no")
    private String trackingNo;

    @ApiModelProperty(value = "parent order number")
    @TableField("parent_order_id")
    private Long parentOrderId;

    @ApiModelProperty(value = "Picture Path")
    @TableField("img_url")
    private String imgUrl;

    @TableField(exist = false)
    private List<OrderDetail> orderDetailList;

    @TableField(exist = false)
    private String wareId;

    @ApiModelProperty(value = "Area")
    @TableField("province_id")
    private Long provinceId;

    @ApiModelProperty(value = "Promotional Amount")
    @TableField("activity_reduce_amount")
    private BigDecimal activityReduceAmount;

    @ApiModelProperty(value = "coupon")
    @TableField("coupon_amount")
    private BigDecimal couponAmount;

    @ApiModelProperty(value = "original price amount")
    @TableField("original_total_amount")
    private BigDecimal originalTotalAmount;

    @ApiModelProperty(value = "Refundable date (30 days after receipt)")
    @TableField("refundable_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date refundableTime;

    @ApiModelProperty(value = "Shipping Fee")
    @TableField("feight_fee")
    private BigDecimal feightFee;

    @ApiModelProperty(value = "operation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("operate_time")
    private Date operateTime;

    // Calculate the amount of the event or coupon
    @TableField(exist = false)
    private List<OrderDetailVo> orderDetailVoList;

    @TableField(exist = false)
    private CouponInfo couponInfo;

    // Calculate the total price
    public void sumTotalAmount(){
        BigDecimal totalAmount = new BigDecimal("0");
        BigDecimal originalTotalAmount = new BigDecimal("0");
        BigDecimal couponAmount = new BigDecimal("0");
        // minus coupons
        if(null != couponInfo) {
            couponAmount = couponAmount.add(couponInfo.getReduceAmount());
            totalAmount = totalAmount.subtract(couponInfo.getReduceAmount());
        }
        // minus activity
        if(null != this.getActivityReduceAmount()) {
            totalAmount = totalAmount.subtract(this.getActivityReduceAmount());
        }
        // Calculate the last 10*2=20
        for (OrderDetail orderDetail: orderDetailList) {
            BigDecimal skuTotalAmount = orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum()));
            originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
            totalAmount = totalAmount.add(skuTotalAmount);
        }
        this.setTotalAmount(totalAmount);
        this.setOriginalTotalAmount(originalTotalAmount);
        this.setCouponAmount(couponAmount);
    }

}