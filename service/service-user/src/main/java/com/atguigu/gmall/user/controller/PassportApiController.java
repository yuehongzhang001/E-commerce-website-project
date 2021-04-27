package com.atguigu.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import com.sun.org.apache.regexp.internal.RE;
import org.redisson.misc.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author mqx
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    // Url是login
    //  前台页面传递的Json 字符串
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request){

        UserInfo info = userService.login(userInfo);
        //  登录成功需要生成token 页面需要获取！ 然后将token 放入cookie 中！ 还需要放入userInfo 对象！
        //        Cookie cookie = new Cookie("name","刘德华");
        //        cookie.setDomain("gmall.com");
        //        cookie.setPath("/");
        //        cookie.setMaxAge(7*24*60*60);
        if (info!=null){
            //  用户登录成功了！
            //  当用户登录成功，生成token ，同时将nickName 一起放入map 中。后台将数据返回给页面！页面通过js 将数据存储到了cookie！
            String token = UUID.randomUUID().toString();
            //  判断用户是否登录的真实依据，应该是将数据放入缓存！
            //  确定数据类型以及key = user:login:
            //  userKey=user:login:uusef123123 value={"ip":"192.168.200.128","userId":"2"}
            String userKey = RedisConst.USER_LOGIN_KEY_PREFIX+token;
            //  String userKey = RedisConst.USER_LOGIN_KEY_PREFIX+info.getId();
            //  缓存中到底要存储什么?  info {有点多} info.getId(); 只存储用户Id！
            //  为了防止别人伪造 token 做登录！ 此时，需要存储一个Ip 地址！
            //  获取到了当前的服务器的IP 地址
            String ip = IpUtil.getIpAddress(request);
            //  定义一个JsonObject 对象
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId",info.getId().toString());
            jsonObject.put("ip",ip);

            //  将登录之后的数据放入缓存！
            redisTemplate.opsForValue().set(userKey,jsonObject.toJSONString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);

            HashMap<Object, Object> hashMap = new HashMap<>();
            hashMap.put("token",token);
            hashMap.put("nickName",info.getNickName());
            //  将map 中的数据返回给前台页面！
            return Result.ok(hashMap);
        }else {
            return Result.fail().message("登录失败！");
        }
    }

    @GetMapping("logout")
    public Result logout(HttpServletRequest request){
        //  登录的时候将数据 redis cookie！  删除对应的数据！
        //  缓存的key  = user:login:1e583d6c-7042-4b7d-814d-8384ffb114b7
        //  获取到token 组成缓存的key！
        String token =  request.getHeader("token");;  //  token 存在cookie 中！ 还存在哪呢? header 中！
        String userKey = "user:login:"+token;
        this.redisTemplate.delete(userKey);
        return Result.ok();
    }

}
