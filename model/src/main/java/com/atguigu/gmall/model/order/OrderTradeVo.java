package com.atguigu.gmall.model.order;

import com.atguigu.gmall.model.activity.CouponInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderTradeVo implements Serializable {

   private static final long serialVersionUID = 1L;

   //   delivery order
   @ApiModelProperty(value = "Order Details")
   private List<OrderDetailVo> orderDetailVoList;

   // Total amount of cash back
   @ApiModelProperty(value = "Promotional discount amount")
   private BigDecimal activityReduceAmount;

   // Use discount/credit
   @ApiModelProperty(value = "Order Coupon List")
   private List<CouponInfo> couponInfoList;

}