package com.atguigu.gmall.model.user;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "user address")
@TableName("user_address")
public class UserAddress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "user address")
    @TableField("user_address")
    private String userAddress;

    @ApiModelProperty(value = "user id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "Recipient")
    @TableField("consignee")
    private String consignee;

    @ApiModelProperty(value = "Contact Information")
    @TableField("phone_num")
    private String phoneNum;

    @ApiModelProperty(value = "Is it the default")
    @TableField("is_default")
    private String isDefault;

}