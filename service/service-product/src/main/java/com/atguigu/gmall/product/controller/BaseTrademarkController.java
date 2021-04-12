package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author mqx
 * @date 2021-4-12 14:05:51
 */
@RestController
@RequestMapping("admin/product/baseTrademark")
public class BaseTrademarkController {

    @Autowired
    private BaseTrademarkService baseTrademarkService;
    //  http://api.gmall.com/admin/product/baseTrademark/{page}/{limit}
    @GetMapping("{page}/{limit}")
    public Result getBaseTradeMarkList(@PathVariable Long page,
                                       @PathVariable Long limit){
        //  声明一个Page 对象
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page, limit);
        //  控制器调用服务层
        IPage iPage = baseTrademarkService.getBaseTradeMarkList(baseTrademarkPage);
        //  IPage 下有List<T> getRecords();
        return  Result.ok(iPage);

    }

    //  通过api 接口得知传递过来的是Json 格式的数据！ 将Json ---> JavaObject
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        //  调用服务层方法
        baseTrademarkService.save(baseTrademark);
        //  返回null
        return Result.ok();
    }

    //  http://api.gmall.com/admin/product/baseTrademark/remove/{id}
    @PutMapping("update")
    public Result update(@RequestBody BaseTrademark baseTrademark){
        //  调用服务层方法
        baseTrademarkService.updateById(baseTrademark);
        //  返回null
        return Result.ok();
    }

    //  http://api.gmall.com/admin/product/baseTrademark/remove/{id}
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        //  调用服务层方法
        baseTrademarkService.removeById(id);
        //  返回null
        return Result.ok();
    }

    //  http://api.gmall.com/admin/product/baseTrademark/get/{id}
    @GetMapping("get/{id}")
    public Result getBaseTradeMarkList(@PathVariable Long id){
        //  根据Id 获取数据
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        //  获取到的数据返回给页面进行渲染
        return  Result.ok(baseTrademark);
    }

    //  http://api.gmall.com/admin/product/baseTrademark/getTrademarkList
    @GetMapping("getTrademarkList")
    public Result getTrademarkList(){

        return Result.ok(baseTrademarkService.list(null));
    }

}
