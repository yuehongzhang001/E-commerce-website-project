package com.atguigu.gmall.model.product;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * SpuSaleAttr
 * </p>
 *
 */
@Data
@ApiModel(description = "Sales attribute")
@TableName("spu_sale_attr")
public class SpuSaleAttr extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "product id")
	@TableField("spu_id")
	private Long spuId;

	@ApiModelProperty(value = "Sales Property ID")
	@TableField("base_sale_attr_id")
	private Long baseSaleAttrId;

	@ApiModelProperty(value = "Sales property name (redundant)")
	@TableField("sale_attr_name")
	private String saleAttrName;

	// Collection of sales attribute value objects
	@TableField(exist = false)
	List<SpuSaleAttrValue> spuSaleAttrValueList;

}