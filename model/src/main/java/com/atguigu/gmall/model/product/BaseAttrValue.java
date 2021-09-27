package com.atguigu.gmall.model.product;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * BaseAttrValue
 * </p>
 *
 */
@Data
@ApiModel(description = "Platform attribute value")
@TableName("base_attr_value")
public class BaseAttrValue extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "Property value name")
	@TableField("value_name")
	private String valueName;

	@ApiModelProperty(value = "Property id")
	@TableField("attr_id")
	private Long attrId;
}