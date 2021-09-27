//
//
package com.atguigu.gmall.model.product;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * BaseCategoryView
 * </p>
 *
 */
@Data
@ApiModel(description = "BaseCategoryView")
@TableName("base_category_view")
public class BaseCategoryView extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "First level classification number")
	@TableField("category1_id")
	private Long category1Id;

	@ApiModelProperty(value = "First level category name")
	@TableField("category1_name")
	private String category1Name;

	@ApiModelProperty(value = "Secondary Classification Number")
	@TableField("category2_id")
	private Long category2Id;

	@ApiModelProperty(value = "second-level category name")
	@TableField("category2_name")
	private String category2Name;

	@ApiModelProperty(value = "Three-level classification number")
	@TableField("category3_id")
	private Long category3Id;

	@ApiModelProperty(value = "three-level category name")
	@TableField("category3_name")
	private String category3Name;

}

