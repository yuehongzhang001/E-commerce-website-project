package com.atguigu.gmall.model.activity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(description = "Activity Rules")
public class ActivityRuleVo implements Serializable {

   private static final long serialVersionUID = 1L;

   @ApiModelProperty(value = "activity id")
   private Long activityId;

   @ApiModelProperty(value = "active rule list")
   private List<ActivityRule> activityRuleList;

   @ApiModelProperty(value = "Activity participating product list")
   private List<ActivitySku> activitySkuList;

   @ApiModelProperty(value = "coupon id list")
   private List<Long> couponIdList;

}