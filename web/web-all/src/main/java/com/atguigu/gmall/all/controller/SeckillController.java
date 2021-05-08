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
 * @author mqx
 */
@Controller
public class SeckillController {

    @Autowired
    private ActivityFeignClient activityFeignClient;

    //  http://activity.gmall.com/seckill.html
    //  秒杀列表
    @GetMapping("seckill.html")
    public String seckill(Model model){
        Result result = activityFeignClient.findAll();
        //  后台存储一个list:
        model.addAttribute("list",result.getData());
        //  秒杀视图名称
        return "seckill/index";
    }

    //  th:href="'/seckill/'+${item.skuId}+'.html'"
    @GetMapping("seckill/{skuId}.html")
    public String seckillItem(@PathVariable Long skuId,Model model){
        Result result = activityFeignClient.getSeckillGoods(skuId);
        model.addAttribute("item",result.getData());
        //  返回视图页面
        return "seckill/item";
    }

    //  http://activity.gmall.com/seckill/queue.html?skuId=46&skuIdStr=c81e728d9d4c2f636f067f89cc14862c
    @GetMapping("seckill/queue.html")
    public String queue(HttpServletRequest request){

        String skuId = request.getParameter("skuId");
        String skuIdStr = request.getParameter("skuIdStr");
        //  ${skuId}        ${skuIdStr}
        request.setAttribute("skuId",skuId);
        request.setAttribute("skuIdStr",skuIdStr);
        //  返回视图名称
        return "seckill/queue";

    }

    //  下单页面控制器
    @GetMapping("seckill/trade.html")
    public String trade(Model model){
        //  后台需要存储 detailArrayList， userAddressList ，totalNum，totalAmount
        //  远程调用秒杀的feignClient
        Result<Map<String, Object>> result = activityFeignClient.trade();
        //  数据正常
        if (result.isOk()){
            //  保存数据
            model.addAllAttributes(result.getData());
            //  返回视图名称
            return "seckill/trade";
        }else {
            //  保存数据
            model.addAttribute("message","下单失败");
            //  返回视图名称
            return "seckill/fail";
        }

    }


}
