package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Yuehong Zhang
 */
@Controller
public class SeckillController {

    @Autowired
    private ActivityFeignClient activityFeignClient;


    // spike list
    @GetMapping("seckill.html")
    public String seckill(Model model){
        Result result = activityFeignClient.findAll();
        // Store a list in the background:
        model.addAttribute("list",result.getData());
        // spike view name
        return "seckill/index";
    }

    // th:href="'/seckill/'+${item.skuId}+'.html'"
    @GetMapping("seckill/{skuId}.html")
    public String seckillItem(@PathVariable Long skuId,Model model){
        Result result = activityFeignClient.getSeckillGoods(skuId);
        model.addAttribute("item",result.getData());
        // Return to the view page
        return "seckill/item";
    }

    // http://activity.gmall.com/seckill/queue.html?skuId=46&skuIdStr=c81e728d9d4c2f636f067f89cc14862c
    @GetMapping("seckill/queue.html")
    public String queue(HttpServletRequest request){

        String skuId = request.getParameter("skuId");
        String skuIdStr = request.getParameter("skuIdStr");
        // ${skuId} ${skuIdStr}
        request.setAttribute("skuId",skuId);
        request.setAttribute("skuIdStr",skuIdStr);
        // return view name
        return "seckill/queue";

    }

    // Order page controller
    @GetMapping("seckill/trade.html")
    public String trade(Model model){
        // The background needs to store detailArrayList, userAddressList, totalNum, totalAmount
        // Remotely call the feignClient of spike
        Result<Map<String, Object>> result = activityFeignClient.trade();
        // data is normal
        if (result.isOk()){
            //  save data
            model.addAllAttributes(result.getData());
            // return view name
            return "seckill/trade";
        }else {
            //  save data
            model.addAttribute("message","Order failed");
            // return view name
            return "seckill/fail";
        }

    }


}