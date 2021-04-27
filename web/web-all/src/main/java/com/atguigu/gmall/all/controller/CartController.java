package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mqx
 */
@Controller
public class CartController {

    //  远程调用service-cart-client
    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    //  http://cart.gmall.com/addCart.html?skuId=45&skuNum=1
    @GetMapping("addCart.html")
    public String addCart(HttpServletRequest request){
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        //  添加购物车
        cartFeignClient.addToCart(Long.parseLong(skuId),Integer.parseInt(skuNum));
        //  存储skuInfo 对象
        SkuInfo skuInfo = productFeignClient.getSkuInfo(Long.parseLong(skuId));
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);

        //  返回视图名
        return "cart/addCart";
    }

    //  http://cart.gmall.com/cart.html
    @GetMapping("cart.html")
    public String cartPage(){

        //  返回购物车列表页面！
        return "cart/index";
    }
}
