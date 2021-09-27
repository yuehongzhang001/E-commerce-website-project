package com.atguigu.gmall.model.list;

import lombok.Data;

// Encapsulate query conditions
@Data
public class SearchParam {

    // ?category3Id=61&trademark=2:Huawei&props=23:4G:Running memory&order=1:desc
    private Long category1Id;;//Three-level category id
    private Long category2Id;
    private Long category3Id;
    // trademark=2: Huawei
    private String trademark;//brand

    private String keyword;//Keyword retrieved

    // Sorting rules
    // 1:hotScore 2:price
    private String order = ""; // 1: Comprehensive sorting/hotspot 2: Price

    // props=23:4G: running memory
    // Platform attribute Id platform attribute value name, platform attribute name
    private String[] props;//Array submitted by the page

    private Integer pageNo = 1;//Paging information
    private Integer pageSize = 3;


}