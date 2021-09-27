package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yuehong Zhang
 * @date 2021-4-16 11:50:47
 */
//@RestController cannot be used!
@Controller
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    @RequestMapping("{skuId}.html")
    public String skuItem(@PathVariable Long skuId, Model model){
        // return data
        Result<Map> result = itemFeignClient.getItemById(skuId);
        // Get map=result.getData();
        // Store map data
        // request.setAttribute("name",result.getData()); // ${name}
        model.addAllAttributes(result.getData());
        // Return the name of the view in the item directory
        return "item/index";
    }
}