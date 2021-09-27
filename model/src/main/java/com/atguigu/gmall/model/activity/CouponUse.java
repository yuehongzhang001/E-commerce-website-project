package com.atguigu.gmall.model.activity;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(description = "coupon claim record form")
@TableName("coupon_use")
public class CouponUse extends BaseEntity {

   private static final long serialVersionUID = 1L;

   @ApiModelProperty(value = "shopping coupon ID")
   @TableField("coupon_id")
   private Long couponId;

   @ApiModelProperty(value = "User ID")
   @TableField("user_id")
   private Long userId;

   @ApiModelProperty(value = "Order ID")
   @TableField("order_id")
   private Long orderId;

   @ApiModelProperty(value = "shopping voucher status")
   @TableField("coupon_status")
   private String couponStatus;

   @ApiModelProperty(value = "Ticket collection time")
   @TableField("get_time")
   private Date getTime;

   @ApiModelProperty(value = "use time")
   @TableField("using_time")
   private Date usingTime;

   @ApiModelProperty(value = "Payment Time")
   @TableField("used_time")
   private Date usedTime;

   @ApiModelProperty(value = "Expiration time")
   @TableField("expire_time")
   private Date expireTime;

}