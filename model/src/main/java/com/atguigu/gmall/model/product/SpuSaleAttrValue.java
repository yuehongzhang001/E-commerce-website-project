package com.atguigu.gmall.model.product;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * <p>
 * SpuSaleAttrValue
 * </p>
 *
 */
@Data
@ApiModel(description = "Sales attribute value")
@TableName("spu_sale_attr_value")
public class SpuSaleAttrValue extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "product id")
	@TableField("spu_id")
	private Long spuId;

	@ApiModelProperty(value = "Sales Property ID")
	@TableField("base_sale_attr_id")
	private Long baseSaleAttrId;

	@ApiModelProperty(value = "Sales Property Value Name")
	@TableField("sale_attr_value_name")
	private String saleAttrValueName;

	@ApiModelProperty(value = "Sales property name (redundant)")
	@TableField("sale_attr_name")
	private String saleAttrName;

	// Is it the default selected state
// @TableField("sale_attr_name")
// String isChecked;
	@TableField(exist = false)
	String isChecked;

}