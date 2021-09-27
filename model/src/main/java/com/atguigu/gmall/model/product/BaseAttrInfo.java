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
 * BaseAttrInfo
 * </p>
 *
 */
@Data
@ApiModel(description = "Platform Properties")
@TableName("base_attr_info")
public class BaseAttrInfo extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "Property name")
	@TableField("attr_name")
	private String attrName;

	@ApiModelProperty(value = "category id")
	@TableField("category_id")
	private Long categoryId;

	@ApiModelProperty(value = "Classification level")
	@TableField("category_level")
	private Integer categoryLevel;

	// Platform attribute value collection
	@TableField(exist = false)
	private List<BaseAttrValue> attrValueList;

}