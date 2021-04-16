package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManageSerivce;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    //  http://api.gmall.com/admin/product/baseSaleAttrList
    @GetMapping("baseSaleAttrList")
    public Result baseSaleAttrList(){
        //  调用服务层方法获取所有的销售属性列表
        List<BaseSaleAttr> baseSaleAttrList = manageSerivce.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    //  http://api.gmall.com/admin/product/saveSpuInfo
    //  获取到前端传递过来的数据 Json ---> JavaObject
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        //  调用服务层方法
        manageSerivce.saveSpuInfo(spuInfo);
        //  返回
        return Result.ok();
    }

    //  http://api.gmall.com/admin/product/spuImageList/29
    @GetMapping("spuImageList/{spuId}")
    public Result getSpuImageList(@PathVariable Long spuId){
        //  调用服务层方法
        List<SpuImage> spuImageList = manageSerivce.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }

    //  http://api.gmall.com/admin/product/spuSaleAttrList/29
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result getSpuSaleAttrList(@PathVariable Long spuId){
        //  调用服务层方法
        List<SpuSaleAttr> spuSaleAttrList = manageSerivce.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }


}
