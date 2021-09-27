package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Yuehong Zhang
 * @date 2021-4-10 11:45:11
 */
@RestController
@RequestMapping("admin/product")
public class BaseManageController {
    // Controller: Inject the service layer
    @Autowired
    private ManageService manageService;

    // Query all first-level classification data
    // http://api.gmall.com/admin/product/getCategory1
    @GetMapping("getCategory1")
    public Result getCategory1(){
        // return data
        List<BaseCategory1> baseCategory1List = manageService.getBaseCategory1();
        return Result.ok(baseCategory1List);
    }

    // Query the second-level classification data according to the first-level classification Id
    // http://api.gmall.com/admin/product/getCategory2/{category1Id}
    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id){
        // return data
        List<BaseCategory2> baseCategory2List = manageService.getBaseCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

    // Query the third-level classification data according to the second-level classification Id
    // http://api.gmall.com/admin/product/getCategory3/{category2Id}
    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id){
        // return data
        List<BaseCategory3> baseCategory3List = manageService.getBaseCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

    // Query the platform attribute collection according to the category Id
    // http://api.gmall.com/admin/product/attrInfoList/{category1Id}/{category2Id}/{category3Id}
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable Long category1Id,
                               @PathVariable Long category2Id,
                               @PathVariable Long category3Id ){
        // return data
        return Result.ok(manageService.getBaseAttrInfoList(category1Id,category2Id,category3Id));
    }

    // Save platform attributes: http://api.gmall.com/admin/product/saveAttrInfo
    // The background controller needs to receive the parameters and data passed by the front end!
    // The attributes of the baseAttrInfo object are similar to the data format passed! Need to change Json --> JavaObject {@RequestBody}
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        // Call the service layer method
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    // http://api.gmall.com/admin/product/getAttrValueList/{attrId}
    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId){
        // select * from base_attr_value where attr_id = attrId;
        // select * from base_attr_info where id = attrId; if(baseAttrInfo! = null)
        // First judge whether the attribute currently exists: if there is an attribute, call the attribute value collection of the attribute object
        BaseAttrInfo baseAttrInfo = manageService.getBaseAttrInfo(attrId);
        if (baseAttrInfo != null){
            return Result.ok(baseAttrInfo.getAttrValueList());
        }
        // List<BaseAttrValue> baseAttrValueList = manageSerivce.getAttrValueList(attrId);
        // return data
        // return Result.ok(baseAttrValueList);
        return Result.ok();
    }

}