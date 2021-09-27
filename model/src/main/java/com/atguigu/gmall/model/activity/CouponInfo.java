package com.atguigu.gmall.model.activity;

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
@ApiModel(description = "CouponInfo")
@TableName("coupon_info")
public class CouponInfo extends BaseEntity {

   private static final long serialVersionUID = 1L;

   @ApiModelProperty(value = "shopping voucher name")
   @TableField("coupon_name")
   private String couponName;

   @ApiModelProperty(value = "shopping voucher type")
   @TableField("coupon_type")
   private String couponType;

   @ApiModelProperty(value = "full amount")
   @TableField("condition_amount")
   private BigDecimal conditionAmount;

   @ApiModelProperty(value = "full number")
   @TableField("condition_num")
   private Long conditionNum;

   @ApiModelProperty(value = "Activity Number")
   @TableField("activity_id")
   private Long activityId;

   @ApiModelProperty(value = "Amount reduced")
   @TableField("benefit_amount")
   private BigDecimal benefitAmount;

   @ApiModelProperty(value = "discount")
   @TableField("benefit_discount")
   private BigDecimal benefitDiscount;

   @ApiModelProperty(value = "Scope Type")
   @TableField("range_type")
   private String rangeType;

   @ApiModelProperty(value = "Maximum number of requisitions")
   @TableField("limit_num")
   private Integer limitNum;

   @ApiModelProperty(value = "Number of times used")
   @TableField("taken_count")
   private Integer takenCount;

   @ApiModelProperty(value = "Start date that can be claimed")
   @TableField("start_time")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private Date startTime;

   @ApiModelProperty(value = "The end date that can be claimed")
   @TableField("end_time")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private Date endTime;

   @ApiModelProperty(value = "Creation Time")
   @TableField("create_time")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private Date createTime;

   @ApiModelProperty(value = "modification time")
   @TableField("operate_time")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private Date operateTime;

   @ApiModelProperty(value = "Expiration time")
   @TableField("expire_time")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private Date expireTime;

   @ApiModelProperty(value = "Coupon Range Description")
   @TableField("range_desc")
   private String rangeDesc;

   @TableField(exist = false)
   private String couponTypeString;

   @TableField(exist = false)
   private String rangeTypeString;

   @ApiModelProperty(value = "Whether to receive")
   @TableField(exist = false)
   private Integer isGet;

   @ApiModelProperty(value = "Shopping voucher status (1: not used 2: used)")
   @TableField(exist = false)
   private String couponStatus;

   @ApiModelProperty(value = "scope type id")
   @TableField(exist = false)
   private Long rangeId;

   @ApiModelProperty(value = "SkuId list corresponding to the coupon")
   @TableField(exist = false)
   private List<Long> skuIdList;

   @ApiModelProperty(value = "Amount reduced after discount")
   @TableField(exist = false)
   private BigDecimal reduceAmount;

   @ApiModelProperty(value = "Is the optimal option")
   @TableField(exist = false)
   private Integer isChecked = 0;

   @ApiModelProperty(value = "Is it optional")
   @TableField(exist = false)
   private Integer isSelect = 0;

}