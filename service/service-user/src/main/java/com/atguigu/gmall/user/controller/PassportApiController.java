package com.atguigu.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;

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
 * @author Yuehong Zhang
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    // Url is login
    // Json string passed by the front page
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request){

        UserInfo info = userService.login(userInfo);
        // A token page needs to be generated for successful login! Then put the token in the cookie! You also need to put in the userInfo object!
        // Cookie cookie = new Cookie("name","Andy Lau");
        // cookie.setDomain("gmall.com");
        // cookie.setPath("/");
        // cookie.setMaxAge(7*24*60*60);
        if (info!=null){
            // User login is successful!
            // When the user logs in successfully, a token is generated and the nickName is put into the map together. The background returns the data to the page! The page stores the data in the cookie through js!
            String token = UUID.randomUUID().toString();
            // The real basis for judging whether a user is logged in should be to put the data in the cache!
            // Determine the data type and key = user:login:
            // userKey=user:login:uusef123123 value={"ip":"192.168.200.128","userId":"2"}
            String userKey = RedisConst.USER_LOGIN_KEY_PREFIX+token;
            // String userKey = RedisConst.USER_LOGIN_KEY_PREFIX+info.getId();
            // What exactly should be stored in the cache? info {a bit too much} info.getId(); Only store the user ID!
            // To prevent others from forging tokens to log in! At this time, an IP address needs to be stored!
            // Get the IP address of the current server
            String ip = IpUtil.getIpAddress(request);
            // Define a JsonObject object
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId",info.getId().toString());
            jsonObject.put("ip",ip);

            // Put the data after login into the cache!
            redisTemplate.opsForValue().set(userKey,jsonObject.toJSONString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);

            HashMap<Object, Object> hashMap = new HashMap<>();
            hashMap.put("token",token);
            hashMap.put("nickName",info.getNickName());
            // Return the data in the map to the front page!
            return Result.ok(hashMap);
        }else {
            return Result.fail().message("Login failed!");
        }
    }

    @GetMapping("logout")
    public Result logout(HttpServletRequest request){
        // The data will be redis cookie when logging in! Delete the corresponding data!
        // Cached key = user:login:1e583d6c-7042-4b7d-814d-8384ffb114b7
        // Get the key of the token that forms the cache!
        String token = request.getHeader("token");; // The token is stored in the cookie! Where does it still exist? In the header!
        String userKey = "user:login:"+token;
        this.redisTemplate.delete(userKey);
        return Result.ok();
    }

}