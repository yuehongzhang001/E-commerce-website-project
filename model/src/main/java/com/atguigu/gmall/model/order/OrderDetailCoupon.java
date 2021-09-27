package com.atguigu.gmall.model.order;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(description = "Order Coupon Association Table")
@TableName("order_detail_coupon")
public class OrderDetailCoupon extends BaseEntity {

   private static final long serialVersionUID = 1L;

   @ApiModelProperty(value = "Order id")
   @TableField("order_id")
   private Long orderId;

   @ApiModelProperty(value = "Order details id")
   @TableField("order_detail_id")
   private Long orderDetailId;

   @ApiModelProperty(value = "shopping coupon ID")
   @TableField("coupon_id")
   private Long couponId;

   @ApiModelProperty(value = "skuID")
   @TableField("sku_id")
   private Long skuId;

   @ApiModelProperty(value = "Creation Time")
   @TableField("create_time")
   private Date createTime;

}