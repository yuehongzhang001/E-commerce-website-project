package com.atguigu.gmall.model.list;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// total data
@Data
public class SearchResponseVo implements Serializable {

    //The id field in the vo object of the brand is reserved at this time (no need to write) name is "brand" value: [{id:100,name:Huawei,logo:xxx},{id:101,name:millet,log:yyy }]
    private List<SearchResponseTmVo> trademarkList;
    //Filter attributes displayed at the top of all products
    private List<SearchResponseAttrVo> attrsList = new ArrayList<>();

    //Retrieved product information
    private List<Goods> goodsList = new ArrayList<>();

    private Long total;//Total number of records
    private Integer pageSize;//The content displayed on each page
    private Integer pageNo;//Current page
    private Long totalPages;

}