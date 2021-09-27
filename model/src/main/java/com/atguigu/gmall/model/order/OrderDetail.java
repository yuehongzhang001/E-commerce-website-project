package com.atguigu.gmall.model.order;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(description = "Order Details")
@TableName("order_detail")
public class OrderDetail extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Order Number")
    @TableField("order_id")
    private Long orderId;

    @ApiModelProperty(value = "sku_id")
    @TableField("sku_id")
    private Long skuId;

    @ApiModelProperty(value = "sku name (redundant)")
    @TableField("sku_name")
    private String skuName;

    @ApiModelProperty(value = "Picture name (redundant)")
    @TableField("img_url")
    private String imgUrl;

    @ApiModelProperty(value = "Purchase price (sku price when placing an order)")
    @TableField("order_price")
    private BigDecimal orderPrice;

    @ApiModelProperty(value = "Number of purchases")
    @TableField("sku_num")
    private Integer skuNum;

    // Is there enough stock!
    @TableField(exist = false)
    private String hasStock;

    @ApiModelProperty(value = "operation time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "Actual payment amount")
    @TableField("split_total_amount")
    private BigDecimal splitTotalAmount;

    @ApiModelProperty(value = "Promotional apportionment amount")
    @TableField("split_activity_amount")
    private BigDecimal splitActivityAmount;

    @ApiModelProperty(value = "Coupon allocation amount")
    @TableField("split_coupon_amount")
    private BigDecimal splitCouponAmount;

}