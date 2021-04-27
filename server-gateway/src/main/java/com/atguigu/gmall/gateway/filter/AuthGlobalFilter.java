package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author mqx
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {

    @Autowired
    private RedisTemplate redisTemplate;

    //  从配置文件中获取控制器都有谁！
    @Value("${authUrls.url}")
    private String authUrlsUrl; // authUrlsUrl=trade.html,myOrder.html,list.html

    //  引个对象
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //  需要知道用户访问的URL 是谁！
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        //  判断当前的path 是属于哪一种? /**/inner/** 属于内部数据接口！需要做出响应！
        if (antPathMatcher.match("/**/inner/**",path)){
            //  获取到响应对象
            ServerHttpResponse response = exchange.getResponse();
            //  不能继续走了！
            return out(response, ResultCodeEnum.PERMISSION);
        }

        //  获取到用户Id 那么就是登录了,如果没有就没有登录！
        String userId = this.getUserId(request);
        String userTempId = this.getUserTempId(request);
        //  盗用呢！
        if ("-1".equals(userId)){
            ServerHttpResponse response = exchange.getResponse();
            return out(response,ResultCodeEnum.PERMISSION);
        }
        //  判断用户是否访问了 trade.html,myOrder.html 这样的控制器时，必须要登录！
        //  authUrlsUrl= trade.html,myOrder.html,list.html
        //  校验的规则：http://www.gmall.com/index.html 不需要  http://list.gmall.com/list.html?category3Id=61 需要！
        String[] split = authUrlsUrl.split(",");
        //  循环遍历
        for (String url : split) {
            //  path.indexOf(url)!=-1 表示当前path 包含 上述的控制器地址
            //  用户未登录！并且访问的控制器是需要登录的！
            if (path.indexOf(url)!=-1 && StringUtils.isEmpty(userId)){
                //  需要跳转到登录页面！
                ServerHttpResponse response = exchange.getResponse();
                //  做一些设置
                // 303状态码表示由于请求对应的资源存在着另一个URI，应使用重定向获取请求的资源
                response.setStatusCode(HttpStatus.SEE_OTHER);
                //  http://passport.gmall.com/login.html?originUrl=http://list.gmall.com/list.html?category3Id=61
                //  request.getURI() = http://list.gmall.com/list.html?category3Id=61
                response.getHeaders().set(HttpHeaders.LOCATION,"http://www.gmall.com/login.html?originUrl="+request.getURI());
                //  重定向！
                return response.setComplete();
            }
        }

        //  /api/**/auth/**  需要登录！
        if (antPathMatcher.match("/api/**/auth/**",path)){
            //  判断当前是否登录
            if (StringUtils.isEmpty(userId)){
                //  做出响应！
                ServerHttpResponse response = exchange.getResponse();
                //  不能继续走了！
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }

        //  验证通过之后：将用户的Id 传递给后台微服务！
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)){

            if (!StringUtils.isEmpty(userId) ){
                //  需要将用户Id 放入请求头中!
                request.mutate().header("userId", userId).build();
            }
            if(!StringUtils.isEmpty(userTempId)){
                //  需要将用户Id 放入请求头中!
                request.mutate().header("userTempId", userTempId).build();
            }
            //  request.getHeaders().set("userId",userId);
            //  ServerWebExchange build = exchange.mutate().request().build();
            return chain.filter(exchange.mutate().request(request).build());
        }
        //  默认返回
        return chain.filter(exchange);
    }

    //  获取临时用户Id
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = "";
        //  存储到cookie 中
        HttpCookie httpCookie = request.getCookies().getFirst("userTempId");
        if (httpCookie!=null){
            userTempId = httpCookie.getValue();
        }else {
            List<String> stringList = request.getHeaders().get("userTempId");
            if (!CollectionUtils.isEmpty(stringList)){
                userTempId = stringList.get(0);
            }
        }
        return userTempId;
    }

    /**
     * 响应方法
     * @param response
     * @param resultCodeEnum
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        //  提示的数据：resultCodeEnum 中 resultCodeEnum.getMessage()的数据
        //  Result 这个类
        Result result = Result.build(null,resultCodeEnum);
        //  需要将result 输入到页面！
        //  将result 变成json 字符串！ 如果出现字符集的问题！
        String strJson = JSON.toJSONString(result);
        //  想办法输入strJson
        DataBuffer wrap = response.bufferFactory().wrap(strJson.getBytes());
        //  使用响应对象设置响应头
        response.getHeaders().add("Content-Type","application/json;charset=UTF-8");
        //  wrap 要输入出去
        return response.writeWith(Mono.just(wrap));
    }

    /**
     * 获取用户Id
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {
        String token = "";
        //  用户Id 存储在缓存中！ userKey = user:login:token
        //  token 放在cookie ，header 中！
        List<String> stringList = request.getHeaders().get("token");
        if (!CollectionUtils.isEmpty(stringList)){
            token=stringList.get(0);
        }else {
            //  如果header 中 cookie 中！
            HttpCookie httpCookie = request.getCookies().getFirst("token");
            if (httpCookie!=null){
                token = httpCookie.getValue();
            }
        }
        //  组成缓存的key
        String userKey = "user:login:"+token;
        //  get userKey  "{\"ip\":\"192.168.200.1\",\"userId\":\"2\"}"
        String object = (String) redisTemplate.opsForValue().get(userKey);
        //  判断
        if (!StringUtils.isEmpty(object)){
            //  本质：JSONObject
            JSONObject jsonObject = JSON.parseObject(object, JSONObject.class);
            //  校验： 先获取到当前客户端的ip
            String currIp = IpUtil.getGatwayIpAddress(request);
            String ip = (String) jsonObject.get("ip");
            if (currIp.equals(ip)){
                String userId = (String) jsonObject.get("userId");
                return userId;
            }else {
                return "-1";
            }
        }else {
            return "";
        }

    }
}
