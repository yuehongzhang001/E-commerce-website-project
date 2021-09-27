package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Yuehong Zhang
 */
@Controller
public class CartController {

    // call service-cart-client remotely
    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    // http://cart.gmall.com/addCart.html?skuId=45&skuNum=1
    @GetMapping("addCart.html")
    public String addCart(HttpServletRequest request){
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        // add shopping cart
        cartFeignClient.addToCart(Long.parseLong(skuId),Integer.parseInt(skuNum));
        // Store skuInfo object
        SkuInfo skuInfo = productFeignClient.getSkuInfo(Long.parseLong(skuId));
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        // return view name
        return "cart/addCart";
    }

    // http://cart.gmall.com/cart.html
    @GetMapping("cart.html")
    public String cartPage(){

        // Return to the shopping cart list page!
        return "cart/index";
    }
}