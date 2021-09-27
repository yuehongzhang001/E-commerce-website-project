package com.atguigu.gmall.model.cart;

import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@ApiModel(description = "shopping cart")

public class CartInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "user id")
    @TableField("user_id")
    private String userId;

    @ApiModelProperty(value = "skuid")
    @TableField("sku_id")
    private Long skuId;

    @ApiModelProperty(value = "The price when placed in the shopping cart")
    @TableField("cart_price")
    private BigDecimal cartPrice;

    @ApiModelProperty(value = "quantity")
    @TableField("sku_num")
    private Integer skuNum;

    @ApiModelProperty(value = "Picture File")
    @TableField("img_url")
    private String imgUrl;

    @ApiModelProperty(value = "sku name (redundant)")
    @TableField("sku_name")
    private String skuName;

    @ApiModelProperty(value = "isChecked")
    @TableField("is_checked")
    private Integer isChecked = 1;

    @TableField(value = "create_time")
    private Timestamp createTime;

    @TableField(value = "update_time")
    private Timestamp updateTime;

    // Real-time price skuInfo.price
    @TableField(exist = false)
    BigDecimal skuPrice;

    // List of coupon information
    @ApiModelProperty(value = "The coupon information corresponding to the shopping item")
    @TableField(exist = false)
    private List<CouponInfo> couponInfoList;

}