package com.atguigu.gmall.model.order;

import com.atguigu.gmall.model.activity.ActivityRule;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderDetailVo implements Serializable {

   private static final long serialVersionUID = 1L;

   // Order details corresponding to a set of rules
   @ApiModelProperty(value = "Order Details")
   private List<OrderDetail> orderDetailList;

   @ApiModelProperty(value = "Activity Rule")
   private ActivityRule activityRule;

}