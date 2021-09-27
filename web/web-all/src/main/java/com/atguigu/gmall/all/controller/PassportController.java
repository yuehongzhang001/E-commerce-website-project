package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Yuehong Zhang
 */
@Controller
public class PassportController {


    @RequestMapping("login.html")
    public String loginIndex(HttpServletRequest request){
        // Get the path
        String originUrl = request.getParameter("originUrl");
        // Combined with the login process: When the user clicks to log in in a certain URL, the user enters the correct user name, and the password is clicked to log in successfully, and it will jump back to the original URL!
        request.setAttribute("originUrl",originUrl);
        return "login";
    }
}