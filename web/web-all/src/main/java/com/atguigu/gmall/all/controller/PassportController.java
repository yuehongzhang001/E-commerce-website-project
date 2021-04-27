package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mqx
 */
@Controller
public class PassportController {

    //  http://passport.gmall.com/login.html?originUrl=http://www.gmall.com/
    @RequestMapping("login.html")
    public String loginIndex(HttpServletRequest request){
        //  获取到路径
        String originUrl = request.getParameter("originUrl");
        //  结合登录流程： 当用户在某个url 中点击登录 ，用户输入正确的用户名，密码点击登录成功之后， 回跳到原来的URL!
        request.setAttribute("originUrl",originUrl);
        return "login";
    }
}
