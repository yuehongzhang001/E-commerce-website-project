package com.atguigu.gmall.model.activity;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(description = "Activity Rules")
@TableName("activity_rule")
public class ActivityRule extends BaseEntity {

   private static final long serialVersionUID = 1L;

   @ApiModelProperty(value = "Type")
   @TableField("activity_id")
   private Long activityId;

   @ApiModelProperty(value = "full reduction amount")
   @TableField("condition_amount")
   private BigDecimal conditionAmount;

   @ApiModelProperty(value = "Number of pieces reduced when full")
   @TableField("condition_num")
   private Long conditionNum;

   @ApiModelProperty(value = "Preferential Amount")
   @TableField("benefit_amount")
   private BigDecimal benefitAmount;

   @ApiModelProperty(value = "Preferential Discount")
   @TableField("benefit_discount")
   private BigDecimal benefitDiscount;

   @ApiModelProperty(value = "Promotion Level")
   @TableField("benefit_level")
   private Long benefitLevel;

   @ApiModelProperty(value = "Activity type (1: full reduction, 2: discount)")
   @TableField(exist = false)
   private String activityType;

   // add a skuId
   @ApiModelProperty(value = "activity skuId")
   @TableField(exist = false)
   private Long skuId;

   @ApiModelProperty(value = "Amount reduced after discount")
   @TableField(exist = false)
   private BigDecimal reduceAmount;

   @ApiModelProperty(value = "SkuId list corresponding to the activity")
   @TableField(exist = false)
   private List<Long> skuIdList;
}