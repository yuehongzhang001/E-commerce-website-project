package com.atguigu.gmall.model.product;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * SkuSaleAttrValue
 * </p>
 *
 */
@Data
@ApiModel(description = "Sku sales attribute value")
@TableName("sku_sale_attr_value")
public class SkuSaleAttrValue extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "stock unit id")
	@TableField("sku_id")
	private Long skuId;

	@ApiModelProperty(value = "spu_id(redundancy)")
	@TableField("spu_id")
	private Long spuId;

	@ApiModelProperty(value = "Sales property value id")
	@TableField("sale_attr_value_id")
	private Long saleAttrValueId;

}