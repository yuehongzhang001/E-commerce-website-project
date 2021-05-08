package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author mqx
 */
@RestController
@RequestMapping("api/cart")
public class CartApiController {

    //  注入服务层
    @Autowired
    private CartInfoService cartInfoService;

    @PostMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId,
                            @PathVariable Integer skuNum,
                            HttpServletRequest request){

        //  缺少一个用户Id    在网关中已经获取过了！
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            //  未登录：没有用户Id,给一个临时用户Id！
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  userId=""
        cartInfoService.addToCart(skuId,userId,skuNum);
        //  返回
        return Result.ok();
    }

    //  查询购物车列表
    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request){
        //  缺少一个用户Id    在网关中已经获取过了！
        String userId = AuthContextHolder.getUserId(request);
        //  未登录：没有用户Id,给一个临时用户Id！
        String userTempId = AuthContextHolder.getUserTempId(request);
        //  调用服务层查询方法！
        List<CartInfo> cartList = cartInfoService.getCartList(userId, userTempId);
        //  将购物车列表放入result 结果集
        return Result.ok(cartList);
    }

    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked, HttpServletRequest request) {
        //  获取userId
        String userId = AuthContextHolder.getUserId(request);
        if(StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  调用方法
        cartInfoService.checkCart(userId,isChecked,skuId);
        return Result.ok();
    }
    //  删除购物项
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId,
                             HttpServletRequest request) {
        //  获取userId
        String userId = AuthContextHolder.getUserId(request);
        if(StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  删除数据
        cartInfoService.deleteCart(skuId,userId);
        return Result.ok();
    }

    //  制作远程调用的数据接口
    @GetMapping("getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable(value = "userId") String userId) {
        return cartInfoService.getCartCheckedList(userId);
    }
    //  根据userId 查询最新价格
    @GetMapping("loadCartCache/{userId}")
    public Result loadCartCache(@PathVariable("userId") String userId) {
        cartInfoService.loadCartCache(userId);
        return Result.ok();
    }

}
