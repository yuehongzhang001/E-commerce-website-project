package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author mqx
 * @date 2021-4-12 10:29:09
 */
@Configuration // 将当前的类变为XXX.xml 配置类
public class CorsConfig {

    //  @Bean 创建一个bean 对象交个spring 容器管理
    //  <bean class = "org.springframework.web.cors.reactive.CorsWebFilter" />
    @Bean
    public CorsWebFilter corsWebFilter(){

        //  创建CorsConfiguration
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*"); // 任意请求头
        corsConfiguration.addAllowedOrigin("*"); // 允许任意域名
        corsConfiguration.addAllowedMethod("*"); // GET, POST, PUT 表示任意
        corsConfiguration.setAllowCredentials(true);// 允许携带cookie

        //  需要CorsConfigurationSource 这个对象 是一个接口，所以我们需要当前接口的实现类 UrlBasedCorsConfigurationSource
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        //  第一个参数表示路径，第二个参数表示设置跨域的方式等信息 CorsConfiguration
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);

        //  返回当前对象
        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }
}
