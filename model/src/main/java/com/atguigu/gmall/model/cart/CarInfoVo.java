package com.atguigu.gmall.model.cart;

import com.atguigu.gmall.model.activity.ActivityRule;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CarInfoVo implements Serializable {

   private static final long serialVersionUID = 1L;

   /**
    * Which skuId in the shopping cart corresponds to the same set of activity rules
    * For example: shopping items with skuId of 1 and 2 correspond to the rules of activity 1 (less than 1,000 minus 100, minus 2,000 minus 200)
    */
   // Different skuId corresponds to the same activity
   @ApiModelProperty(value = "cartInfoList")
   private List<CartInfo> cartInfoList;

   // activityRuleList stores the same activity
   @ApiModelProperty(value = "active rule list")
   private List<ActivityRule> activityRuleList;

}