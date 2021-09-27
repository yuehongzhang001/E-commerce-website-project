package com.atguigu.gmall.model.list;

import lombok.Data;

import java.io.Serializable;

// Brand data
@Data
public class SearchResponseTmVo implements Serializable {
    //All values of the current attribute value
    private Long tmId;
    //Attribute name
    private String tmName;//Network standard, classification
    //Picture name
    private String tmLogoUrl;//Network standard, classification
}