package com.atguigu.gmall.model.user;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "userInfo")
@TableName("user_info")
public class UserInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "User Name")
    @TableField("login_name")
    private String loginName;

    @ApiModelProperty(value = "user nickname")
    @TableField("nick_name")
    private String nickName;

    @ApiModelProperty(value = "User Password")
    @TableField("passwd")
    private String passwd;

    @ApiModelProperty(value = "user name")
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "mobile phone number")
    @TableField("phone_num")
    private String phoneNum;

    @ApiModelProperty(value = "Mailbox")
    @TableField("email")
    private String email;

    @ApiModelProperty(value = "Avatar")
    @TableField("head_img")
    private String headImg;

    @ApiModelProperty(value = "User Level")
    @TableField("user_level")
    private String userLevel;

}