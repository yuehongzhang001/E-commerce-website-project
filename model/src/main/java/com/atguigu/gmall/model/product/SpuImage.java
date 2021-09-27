package com.atguigu.gmall.model.product;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * SpuImage
 * </p>
 *
 */
@Data
@ApiModel(description = "Spu Picture")
@TableName("spu_image")
public class SpuImage extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "product id")
	@TableField("spu_id")
	private Long spuId;

	@ApiModelProperty(value = "picture name")
	@TableField("img_name")
	private String imgName;

	@ApiModelProperty(value = "Picture Path")
	@TableField("img_url")
	private String imgUrl;

}