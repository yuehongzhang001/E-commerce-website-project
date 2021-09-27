package com.atguigu.gmall.model.activity;

import com.atguigu.gmall.model.enums.CouponRangeType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(description = "Coupon Rules")
public class CouponRuleVo implements Serializable {

   private static final long serialVersionUID = 1L;

   @ApiModelProperty(value = "coupon id")
   private Long couponId;

   @ApiModelProperty(value = "Scope Type")
   private CouponRangeType rangeType;

   @ApiModelProperty(value = "full amount")
   private BigDecimal conditionAmount;

   @ApiModelProperty(value = "full number")
   private Long conditionNum;

   @ApiModelProperty(value = "Amount reduced")
   private BigDecimal benefitAmount;

   @ApiModelProperty(value = "discount")
   private BigDecimal benefitDiscount;

   @ApiModelProperty(value = "The list of products that the coupon participates in")
   private List<CouponRange> couponRangeList;

   @ApiModelProperty(value = "Coupon Range Description")
   private String rangeDesc;

}