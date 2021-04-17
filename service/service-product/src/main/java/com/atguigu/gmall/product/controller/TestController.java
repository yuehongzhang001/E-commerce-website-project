package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mqx
 * @date 2021-4-16 15:34:27
 */
@RestController
@RequestMapping("admin/product/test")
public class TestController {

    @Autowired
    private TestService testService;

    //  控制器对应的方法
    @GetMapping("testLock")
    public void testLock(){
        testService.testLock();
    }

}
