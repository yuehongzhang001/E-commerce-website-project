package com.atguigu.gmall.model.list;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Platform attribute related objects
@Data
public class SearchResponseAttrVo implements Serializable {

    // Platform attribute Id
    private Long attrId;//1
    //The collection of current attribute values
    private List<String> attrValueList = new ArrayList<>();
    //Attribute name
    private String attrName;//Network standard, classification
}