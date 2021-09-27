package com.atguigu.gmall.model.product;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * BaseTrademark
 * </p>
 *
 */
@Data
@ApiModel(description = "Trademark Brand")
@TableName("base_trademark")
public class BaseTrademark extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "Property value")
	@TableField("tm_name")
	private String tmName;

	@ApiModelProperty(value = "The image path of the brand logo")
	@TableField("logo_url")
	private String logoUrl;

}