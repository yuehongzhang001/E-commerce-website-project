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
 * @author Yuehong Zhang
 */
@RestController
@RequestMapping("api/cart")
public class CartApiController {

    // Inject the service layer
    @Autowired
    private CartInfoService cartInfoService;

    @PostMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId,
                            @PathVariable Integer skuNum,
                            HttpServletRequest request){

        // Missing a user ID has already been obtained in the gateway!
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            // Not logged in: No user Id, give a temporary user Id!
            userId = AuthContextHolder.getUserTempId(request);
        }
        // userId=""
        cartInfoService.addToCart(skuId,userId,skuNum);
        //  return
        return Result.ok();
    }

    // Query the shopping cart list
    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request){
        // Missing a user ID has already been obtained in the gateway!
        String userId = AuthContextHolder.getUserId(request);
        // Not logged in: No user Id, give a temporary user Id!
        String userTempId = AuthContextHolder.getUserTempId(request);
        // Call the service layer query method!
        List<CartInfo> cartList = cartInfoService.getCartList(userId, userTempId);
        // Put the shopping cart list into the result set
        return Result.ok(cartList);
    }

    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked, HttpServletRequest request) {
        // Get userId
        String userId = AuthContextHolder.getUserId(request);
        if(StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        // call method
        cartInfoService.checkCart(userId,isChecked,skuId);
        return Result.ok();
    }
    // delete shopping item
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId,
                             HttpServletRequest request) {
        // Get userId
        String userId = AuthContextHolder.getUserId(request);
        if(StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  delete data
        cartInfoService.deleteCart(skuId,userId);
        return Result.ok();
    }

    // Make a data interface for remote calls
    @GetMapping("getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable(value = "userId") String userId) {
        return cartInfoService.getCartCheckedList(userId);
    }
    // Query the latest price based on userId
    @GetMapping("loadCartCache/{userId}")
    public Result loadCartCache(@PathVariable("userId") String userId) {
        cartInfoService.loadCartCache(userId);
        return Result.ok();
    }

}