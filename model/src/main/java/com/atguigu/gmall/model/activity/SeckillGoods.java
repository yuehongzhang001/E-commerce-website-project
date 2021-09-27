package com.atguigu.gmall.model.activity;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(description = "SeckillGoods")
@TableName("seckill_goods")
public class SeckillGoods extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "spu ID")
	@TableField("spu_id")
	private Long spuId;

	@ApiModelProperty(value = "sku ID")
	@TableField("sku_id")
	private Long skuId;

	@ApiModelProperty(value = "Title")
	@TableField("sku_name")
	private String skuName;

	@ApiModelProperty(value = "product image")
	@TableField("sku_default_img")
	private String skuDefaultImg;

	@ApiModelProperty(value = "original price")
	@TableField("price")
	private BigDecimal price;

	@ApiModelProperty(value = "seckill price")
	@TableField("cost_price")
	private BigDecimal costPrice;

	@ApiModelProperty(value = "Add Date")
	@TableField("create_time")
	private Date createTime;

	@ApiModelProperty(value = "review date")
	@TableField("check_time")
	private Date checkTime;

	@ApiModelProperty(value = "Audit Status")
	@TableField("status")
	private String status;

	@ApiModelProperty(value = "start time")
	@TableField("start_time")
	private Date startTime;

	@ApiModelProperty(value = "End Time")
	@TableField("end_time")
	private Date endTime;

	@ApiModelProperty(value = "Number of products in seconds")
	@TableField("num")
	private Integer num;

	@ApiModelProperty(value = "Remaining inventory number")
	@TableField("stock_count")
	private Integer stockCount;

	@ApiModelProperty(value = "Description")
	@TableField("sku_desc")
	private String skuDesc;

}