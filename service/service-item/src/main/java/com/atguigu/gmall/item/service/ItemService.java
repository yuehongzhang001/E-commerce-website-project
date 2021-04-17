package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * @author mqx
 * @date 2021-4-14 14:08:38
 */
public interface ItemService {

    //  编写抽象方法 方法返回值，方法的参数！
    Map<String,Object> getItemBySkuId(Long skuId);

}
