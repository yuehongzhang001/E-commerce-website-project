package com.atguigu.gmall.ware.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author helen
 * @since 2019/9/23
 */
@EnableTransactionManagement
@Configuration
@MapperScan("com.atguigu.gmall.*.mapper")
public class MybatisPlusConfig {

    /**
     * Paging plugin
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // paginationInterceptor.setLimit (your maximum single page limit, the default is 500, less than 0 such as -1 is not limited);
        return paginationInterceptor;
    }

    @Bean
    public ISqlInjector sqlInjector() {
        return new LogicSqlInjector();
    }

    /**
     * SQL execution efficiency plugin
     */
    @Bean
    @Profile({"dev","test"})// Set the dev test environment to open
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        performanceInterceptor.setMaxTime(2000);
        performanceInterceptor.setFormat(true);
        return performanceInterceptor;
    }
}