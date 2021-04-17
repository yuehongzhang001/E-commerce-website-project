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
 * @author mqx
 * @date 2021-4-16 11:50:47
 */
//@RestController   不能使用！
@Controller
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    @RequestMapping("{skuId}.html")
    public String skuItem(@PathVariable Long skuId, Model model){
        //  返回数据
        Result<Map> result = itemFeignClient.getItemById(skuId);
        // 获取 map=result.getData();
        //  存储map 数据
        //  request.setAttribute("name",result.getData()); // ${name}
        model.addAllAttributes(result.getData());
        //  返回item目录下的视图名称
        return "item/index";
    }
}
