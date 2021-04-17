package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.ManageSerivce;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author mqx
 * @date 2021-4-13 11:31:04
 */
@RestController
@RequestMapping("admin/product")
public class SkuManageController {
    //  注入服务层
    @Autowired
    private ManageSerivce manageSerivce;

    //  http://api.gmall.com/admin/product/saveSkuInfo
    //  获取到前端传递过来的数据！   Json --> JavaObject
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        //  调用服务层方法
        manageSerivce.saveSkuInfo(skuInfo);
        //  返回
        return Result.ok();
    }

    //  http://api.gmall.com/admin/product/list/{page}/{limit}
    @GetMapping("list/{page}/{limit}")
    public Result getSkuInfoList(@PathVariable Long page,
                                 @PathVariable Long limit){
        //  创建一个Page 对象
        Page<SkuInfo> skuInfoPage = new Page<>(page,limit);

        //  调用服务层方法
        IPage iPage = manageSerivce.getSkuInfoList(skuInfoPage);

        return Result.ok(iPage);
    }

    //  http://api.gmall.com/admin/product/onSale/{skuId}
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){
        //  调用服务层方法
        manageSerivce.onSale(skuId);
        //  返回数据
        return Result.ok();
    }

    //  http://api.gmall.com/admin/product/cancelSale/{skuId}
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId){
        //  调用服务层方法
        manageSerivce.cancelSale(skuId);
        //  返回数据
        return Result.ok();
    }
}
