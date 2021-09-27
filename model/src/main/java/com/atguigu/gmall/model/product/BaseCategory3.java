package com.atguigu.gmall.model.product;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * BaseCategory3
 * </p>
 *
 */
@Data
@ApiModel(description = "Product three-level classification")
@TableName("base_category3")
public class BaseCategory3 extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "three-level category name")
	@TableField("name")
	private String name;

	@ApiModelProperty(value = "Secondary Classification Number")
	@TableField("category2_id")
	private Long category2Id;

}