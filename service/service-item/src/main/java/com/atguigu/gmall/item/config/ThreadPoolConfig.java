package com.atguigu.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author mqx
 */
@Configuration
public class ThreadPoolConfig {

    //  创建了一个线程池并将注入到spring 容器
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                5,
                3L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3)
        );
        return threadPoolExecutor;
    }
}
