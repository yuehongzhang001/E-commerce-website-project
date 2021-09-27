package com.atguigu.gmall.model.product;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * SkuInfo
 * </p>
 *
 */
@Data
@ApiModel(description = "SkuInfo")
@TableName("sku_info")
public class SkuInfo extends BaseEntity {


// public SkuInfo(){}
// public SkuInfo(Long skuId){
// setId(skuId);
//}
// // The equals method is automatically called if it is judged to remove duplicates.
// public boolean equals(SkuInfo skuInfo){
// return getId().equals(skuInfo.getId());
//}

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "product id")
	@TableField("spu_id")
	private Long spuId;

	@ApiModelProperty(value = "price")
	@TableField("price")
	private BigDecimal price;

	@ApiModelProperty(value = "sku name")
	@TableField("sku_name")
	private String skuName;

	@ApiModelProperty(value = "Product Specification Description")
	@TableField("sku_desc")
	private String skuDesc;

	@ApiModelProperty(value = "weight")
	@TableField("weight")
	private String weight;

	@ApiModelProperty(value = "Brand (redundant)")
	@TableField("tm_id")
	private Long tmId;

	@ApiModelProperty(value = "Three-level classification id (redundant)")
	@TableField("category3_id")
	private Long category3Id;

	@ApiModelProperty(value = "Display pictures by default (redundant)")
	@TableField("sku_default_img")
	private String skuDefaultImg;

	@ApiModelProperty(value = "Whether to sell (1: yes 0: no)")
	@TableField("is_sale")
	private Integer isSale;

	@TableField(exist = false)
	List<SkuImage> skuImageList;

	@TableField(exist = false)
	List<SkuAttrValue> skuAttrValueList;

	@TableField(exist = false)
	List<SkuSaleAttrValue> skuSaleAttrValueList;
}