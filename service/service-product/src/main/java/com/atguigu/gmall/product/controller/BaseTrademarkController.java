package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Yuehong Zhang
 * @date 2021-4-12 14:05:51
 */
@RestController
@RequestMapping("admin/product/baseTrademark")
public class BaseTrademarkController {

    @Autowired
    private BaseTrademarkService baseTrademarkService;
    // http://api.gmall.com/admin/product/baseTrademark/{page}/{limit}
    @GetMapping("{page}/{limit}")
    public Result getBaseTradeMarkList(@PathVariable Long page,
                                       @PathVariable Long limit){
        // Declare a Page object
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page, limit);
        // The controller calls the service layer
        IPage iPage = baseTrademarkService.getBaseTradeMarkList(baseTrademarkPage);
        // List<T> getRecords(); under IPage
        return Result.ok(iPage);

    }

    // Through the api interface, it is known that the data passed is in Json format! Json ---> JavaObject
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        // Call the service layer method
        baseTrademarkService.save(baseTrademark);
        // return null
        return Result.ok();
    }

    // http://api.gmall.com/admin/product/baseTrademark/remove/{id}
    @PutMapping("update")
    public Result update(@RequestBody BaseTrademark baseTrademark){
        // Call the service layer method
        baseTrademarkService.updateById(baseTrademark);
        // return null
        return Result.ok();
    }

    // http://api.gmall.com/admin/product/baseTrademark/remove/{id}
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        // Call the service layer method
        baseTrademarkService.removeById(id);
        // return null
        return Result.ok();
    }

    // http://api.gmall.com/admin/product/baseTrademark/get/{id}
    @GetMapping("get/{id}")
    public Result getBaseTradeMarkList(@PathVariable Long id){
        // Get data according to Id
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        // The obtained data is returned to the page for rendering
        return Result.ok(baseTrademark);
    }

    // http://api.gmall.com/admin/product/baseTrademark/getTrademarkList
    @GetMapping("getTrademarkList")
    public Result getTrademarkList(){

        return Result.ok(baseTrademarkService.list(null));
    }

    // Obtain the corresponding brand data according to the brand Id
    // @GetMapping("inner/getTrademark/{tmId}")
    // public BaseTrademark getTrademark(@PathVariable Long tmId){
    // return baseTrademarkService.getById(tmId);
    //}

}