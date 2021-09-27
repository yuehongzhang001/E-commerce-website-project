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
 * @author Yuehong Zhang
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {

    @Autowired
    private RedisTemplate redisTemplate;

    // Who gets the controller from the configuration file!
    @Value("${authUrls.url}")
    private String authUrlsUrl; // authUrlsUrl=trade.html,myOrder.html,list.html

    // quote an object
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Need to know who the user is visiting the URL!
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        // Determine which kind of path does the current path belong to? /**/inner/** belongs to the internal data interface! Need to respond!
        if (antPathMatcher.match("/**/inner/**",path)){
            // Get the response object
            ServerHttpResponse response = exchange.getResponse();
            // Can't go anymore!
            return out(response, ResultCodeEnum.PERMISSION);
        }

        // If the user ID is obtained, then it is logged in, if not, there is no login!
        String userId = this.getUserId(request);
        String userTempId = this.getUserTempId(request);
        // Misappropriation!
        if ("-1".equals(userId)){
            ServerHttpResponse response = exchange.getResponse();
            return out(response,ResultCodeEnum.PERMISSION);
        }
        // When judging whether the user has visited a controller like trade.html, myOrder.html, you must log in!
        // authUrlsUrl = trade.html,myOrder.html,list.html
        // Rule of verification: http://www.gmall.com/index.html not required http://list.gmall.com/list.html?category3Id=61 required!
        String[] split = authUrlsUrl.split(",");
        // loop traversal
        for (String url: split) {
            // path.indexOf(url)!=-1 means that the current path contains the above controller address
            // The user is not logged in! And the access controller needs to log in!
            if (path.indexOf(url)!=-1 && StringUtils.isEmpty(userId)){
                // Need to jump to the login page!
                ServerHttpResponse response = exchange.getResponse();
                // make some settings
                // The 303 status code indicates that because there is another URI for the resource corresponding to the request, redirection should be used to obtain the requested resource
                response.setStatusCode(HttpStatus.SEE_OTHER);
                // http://passport.gmall.com/login.html?originUrl=http://list.gmall.com/list.html?category3Id=61
                // request.getURI() = http://list.gmall.com/list.html?category3Id=61
                response.getHeaders().set(HttpHeaders.LOCATION,"http://www.gmall.com/login.html?originUrl="+request.getURI());
                // Redirect!
                return response.setComplete();
            }
        }

        // /api/**/auth/** Login required!
        if (antPathMatcher.match("/api/**/auth/**",path)){
            // Determine whether you are currently logged in
            if (StringUtils.isEmpty(userId)){
                // Respond!
                ServerHttpResponse response = exchange.getResponse();
                // Can't go anymore!
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }

        // After verification: Pass the user's Id to the background microservice!
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)){

            if (!StringUtils.isEmpty(userId) ){
                // Need to put the user ID in the request header!
                request.mutate().header("userId", userId).build();
            }
            if(!StringUtils.isEmpty(userTempId)){
                // Need to put the user ID in the request header!
                request.mutate().header("userTempId", userTempId).build();
            }
            // request.getHeaders().set("userId",userId);
            // ServerWebExchange build = exchange.mutate().request().build();
            return chain.filter(exchange.mutate().request(request).build());
        }
        // default return
        return chain.filter(exchange);
    }

    // Get temporary user Id
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = "";
        // Store in cookie
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
     * Response method
     * @param response
     * @param resultCodeEnum
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        // Prompt data: the data of resultCodeEnum.getMessage() in resultCodeEnum
        // Result this class
        Result result = Result.build(null,resultCodeEnum);
        // Need to enter result into the page!
        // Turn result into a json string! If there is a problem with the character set!
        String strJson = JSON.toJSONString(result);
        // Find a way to enter strJson
        DataBuffer wrap = response.bufferFactory().wrap(strJson.getBytes());
        // Use the response object to set the response header
        response.getHeaders().add("Content-Type","application/json;charset=UTF-8");
        // wrap needs to be entered
        return response.writeWith(Mono.just(wrap));
    }

    /**
     * Get user ID
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {
        String token = "";
        // The user ID is stored in the cache! userKey = user:login:token
        // The token is placed in the cookie and header!
        List<String> stringList = request.getHeaders().get("token");
        if (!CollectionUtils.isEmpty(stringList)){
            token=stringList.get(0);
        }else {
            // If the cookie in the header!
            HttpCookie httpCookie = request.getCookies().getFirst("token");
            if (httpCookie!=null){
                token = httpCookie.getValue();
            }
        }
        // The key that composes the cache
        String userKey = "user:login:"+token;
        // get userKey "{\"ip\":\"192.168.200.1\",\"userId\":\"2\"}"
        String object = (String) redisTemplate.opsForValue().get(userKey);
        //  judge
        if (!StringUtils.isEmpty(object)){
            // Essence: JSONObject
            JSONObject jsonObject = JSON.parseObject(object, JSONObject.class);
            // Verification: First get the ip of the current client
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
