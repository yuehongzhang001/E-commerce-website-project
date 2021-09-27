package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yuehong Zhang
 * @date 2021-4-16 15:34:27
 */
@RestController
@RequestMapping("admin/product/test")
public class TestController {

    @Autowired
    private TestService testService;

    // The method corresponding to the controller
    @GetMapping("testLock")
    public void testLock(){
        try {
            testService.testLock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // read lock
    @GetMapping("read")
    public Result read(){
        String msg = testService.readLock();
        return Result.ok(msg);
    }

    // write lock
    @GetMapping("write")
    public Result write(){
        String msg = testService.writeLock();
        return Result.ok(msg);
    }
}