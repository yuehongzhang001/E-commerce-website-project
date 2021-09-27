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
@ApiModel(description = "Activity Information")
@TableName("activity_info")
public class ActivityInfo extends BaseEntity {

   private static final long serialVersionUID = 1L;

   @ApiModelProperty(value = "Activity name")
   @TableField("activity_name")
   private String activityName;

   @ApiModelProperty(value = "Activity type (full reduction, discount)")
   @TableField("activity_type")
   private String activityType;

   @ApiModelProperty(value = "Activity Description")
   @TableField("activity_desc")
   private String activityDesc;

   @ApiModelProperty(value = "start time")
   @TableField("start_time")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private Date startTime;

   @ApiModelProperty(value = "End Time")
   @TableField("end_time")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private Date endTime;

   @ApiModelProperty(value = "Creation Time")
   @TableField("create_time")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private Date createTime;

   // type of activity
   @TableField(exist = false)
   private String activityTypeString;
}