package com.atguigu.gmall.model.activity;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(description = "Activity sku range")
@TableName("activity_sku")
public class ActivitySku extends BaseEntity {

   private static final long serialVersionUID = 1L;

   @ApiModelProperty(value = "activity id")
   @TableField("activity_id")
   private Long activityId;

   @ApiModelProperty(value = "sku_id")
   @TableField("sku_id")
   private Long skuId;

   @ApiModelProperty(value = "Creation Time")
   @TableField("start_time")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private Date startTime;

}