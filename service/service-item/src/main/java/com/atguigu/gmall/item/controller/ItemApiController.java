package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author mqx
 * @date 2021-4-14 14:13:09
 */
@RestController
@RequestMapping("api/item")
public class ItemApiController {

    @Autowired
    private ItemService itemService;

    //  编写一个控制器 给web-all 使用！
    //  原来一直用的返回数据对象是 Result ,
    @GetMapping("{skuId}")
    public Result getItemById(@PathVariable Long skuId){
        //  调用服务层方法
        Map<String, Object> map = itemService.getItemBySkuId(skuId);
        //  返回数据，并将数据放入Result.data中
        return Result.ok(map);
    }

}
