package com.atguigu.gmall.model.product;

import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Product first-level classification")
@TableName("base_category1")
public class BaseCategory1 extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Category Name")
    @TableField("name")
    private String name;

}