package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author mqx
 * @date 2021-4-10 11:45:11
 */
@RestController
@RequestMapping("admin/product")
public class BaseManageController {
    //  控制器：注入服务层
    @Autowired
    private ManageService manageService;

    //  查询所有的一级分类数据
    //  http://api.gmall.com/admin/product/getCategory1
    @GetMapping("getCategory1")
    public Result getCategory1(){
        //  返回数据
        List<BaseCategory1> baseCategory1List = manageService.getBaseCategory1();
        return Result.ok(baseCategory1List);
    }

    //  根据一级分类Id 查询二级分类数据
    //  http://api.gmall.com/admin/product/getCategory2/{category1Id}
    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id){
        //  返回数据
        List<BaseCategory2> baseCategory2List = manageService.getBaseCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

    //  根据二级分类Id 查询三级分类数据
    //  http://api.gmall.com/admin/product/getCategory3/{category2Id}
    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id){
        //  返回数据
        List<BaseCategory3> baseCategory3List = manageService.getBaseCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

    //  根据分类Id 查询平台属性集合
    //  http://api.gmall.com/admin/product/attrInfoList/{category1Id}/{category2Id}/{category3Id}
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable Long category1Id ,
                               @PathVariable Long category2Id ,
                               @PathVariable Long category3Id ){
        //  返回数据
        return Result.ok(manageService.getBaseAttrInfoList(category1Id,category2Id,category3Id));
    }

    //  保存平台属性：http://api.gmall.com/admin/product/saveAttrInfo
    //  后台控制器需要 接收到前端传递的参数、 数据！
    //  baseAttrInfo 这个对象的属性与传递的数据格式类似！ 需要将Json --> JavaObject {@RequestBody}
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        //  调用服务层方法
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    //  http://api.gmall.com/admin/product/getAttrValueList/{attrId}
    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId){
        //  select * from base_attr_value where attr_id = attrId;
        //  select * from base_attr_info where id  =  attrId; if(baseAttrInfo ! =null )
        //  先判断一下当前是否存在该属性：如果有属性，则调用属性对象的属性值集合
        BaseAttrInfo baseAttrInfo =  manageService.getBaseAttrInfo(attrId);
        if (baseAttrInfo != null){
            return Result.ok(baseAttrInfo.getAttrValueList());
        }
        //  List<BaseAttrValue> baseAttrValueList = manageSerivce.getAttrValueList(attrId);
        //  返回数据
        //  return Result.ok(baseAttrValueList);
        return Result.ok();
    }

}
