package com.atguigu.gmall.model.list;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
public class SearchAttr {
    // Platform attribute Id
    @Field(type = FieldType.Long)
    private Long attrId;
    // Platform attribute value name
    @Field(type = FieldType.Keyword)
    private String attrValue;
    // Platform attribute name
    @Field(type = FieldType.Keyword)
    private String attrName;


}