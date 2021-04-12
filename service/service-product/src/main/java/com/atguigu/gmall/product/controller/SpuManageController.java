package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.ManageSerivce;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mqx
 * @date 2021-4-12 11:37:48
 */
@RestController
@RequestMapping("admin/product")
public class SpuManageController {

    @Autowired
    private ManageSerivce manageSerivce;
    //  http://api.gmall.com/admin/product/{page}/{limit}?category3Id=61 {"category3Id":61}
    //  @GetMapping("{page}/{limit}?category3Id=61")    //  不用！ HttpServletRequest request String category3Id SpuInfo spuInfo
    //  springmvc 有个知识点，对象方式传值！
    //  获取三级分类Id 有n种方式！
    @GetMapping("{page}/{limit}")
    public Result getSpuInfoList(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SpuInfo spuInfo){
        //  分页查询！
        Page<SpuInfo> spuInfoPage = new Page<>(page,limit);
        //  调用服务层方法
        IPage<SpuInfo> iPage =  manageSerivce.getSpuInfoList(spuInfoPage,spuInfo);

        return Result.ok(iPage);
    }
}
