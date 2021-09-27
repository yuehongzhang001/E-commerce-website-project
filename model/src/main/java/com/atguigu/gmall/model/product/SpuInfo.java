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
 * SpuInfo
 * </p>
 *
 */
@Data
@ApiModel(description = "SpuInfo")
@TableName("spu_info")
public class SpuInfo extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "product name")
	@TableField("spu_name")
	private String spuName;

	@ApiModelProperty(value = "Product description (Background brief description)")
	@TableField("description")
	private String description;

	@ApiModelProperty(value = "three-level classification id")
	@TableField("category3_id")
	private Long category3Id;

	@ApiModelProperty(value = "brand id")
	@TableField("tm_id")
	private Long tmId;

	// Collection of sales attributes
	@TableField(exist = false)
	private List<SpuSaleAttr> spuSaleAttrList;

	// Collection of pictures of products
	@TableField(exist = false)
	private List<SpuImage> spuImageList;

}