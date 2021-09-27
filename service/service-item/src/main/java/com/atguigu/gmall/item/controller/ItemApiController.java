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
 * @author Yuehong Zhang
 * @date 2021-4-14 14:13:09
 */
@RestController
@RequestMapping("api/item")
public class ItemApiController {

    @Autowired
    private ItemService itemService;

    // Write a controller for web-all use!
    // The return data object that has been used is Result,
    @GetMapping("{skuId}")
    public Result getItemById(@PathVariable Long skuId){
        // Call the service layer method
        Map<String, Object> map = itemService.getItemBySkuId(skuId);
        // Return the data and put the data in Result.data
        return Result.ok(map);
    }

}