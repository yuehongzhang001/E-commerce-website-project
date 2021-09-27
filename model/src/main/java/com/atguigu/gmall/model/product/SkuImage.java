package com.atguigu.gmall.model.product;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * SkuImage
 * </p>
 *
 */
@Data
@ApiModel(description = "Sku picture")
@TableName("sku_image")
public class SkuImage extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "product id")
	@TableField("sku_id")
	private Long skuId;

	@ApiModelProperty(value = "Picture name (redundant)")
	@TableField("img_name")
	private String imgName;

	@ApiModelProperty(value = "Picture path (redundancy)")
	@TableField("img_url")
	private String imgUrl;

	@ApiModelProperty(value = "product image id")
	@TableField("spu_img_id")
	private Long spuImgId;

	@ApiModelProperty(value = "Is it the default")
	@TableField("is_default")
	private String isDefault;

}