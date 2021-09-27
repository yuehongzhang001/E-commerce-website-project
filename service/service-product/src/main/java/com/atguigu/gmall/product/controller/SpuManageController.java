package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Yuehong Zhang
 * @date 2021-4-12 11:37:48
 */
@RestController
@RequestMapping("admin/product")
public class SpuManageController {

    @Autowired
    private ManageService manageService;
    // http://api.gmall.com/admin/product/{page}/{limit}?category3Id=61 {"category3Id":61}
    // @GetMapping("{page}/{limit}?category3Id=61") // No need! HttpServletRequest request String category3Id SpuInfo spuInfo
    // springmvc has a point of knowledge, passing values ​​by object!
    // There are n ways to get the three-level classification Id!
    @GetMapping("{page}/{limit}")
    public Result getSpuInfoList(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SpuInfo spuInfo){
        //  Paging query!
        Page<SpuInfo> spuInfoPage = new Page<>(page,limit);
        // Call the service layer method
        IPage<SpuInfo> iPage = manageService.getSpuInfoList(spuInfoPage,spuInfo);

        return Result.ok(iPage);
    }

    // http://api.gmall.com/admin/product/baseSaleAttrList
    @GetMapping("baseSaleAttrList")
    public Result baseSaleAttrList(){
        // Call the service layer method to get a list of all sales attributes
        List<BaseSaleAttr> baseSaleAttrList = manageService.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    // http://api.gmall.com/admin/product/saveSpuInfo
    // Get the data passed to the front end Json ---> JavaObject
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        // Call the service layer method
        manageService.saveSpuInfo(spuInfo);
        //  return
        return Result.ok();
    }

    // http://api.gmall.com/admin/product/spuImageList/29
    @GetMapping("spuImageList/{spuId}")
    public Result getSpuImageList(@PathVariable Long spuId){
        // Call the service layer method
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }

    // http://api.gmall.com/admin/product/spuSaleAttrList/29
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result getSpuSaleAttrList(@PathVariable Long spuId){
        // Call the service layer method
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }


}