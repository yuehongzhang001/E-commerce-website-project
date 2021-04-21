package com.atguigu.gmall.product.controller;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author mqx
 */
public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        //  创建线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                5,
                3L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3)
        );

        //  futureA
        // 线程1执行返回的结果：hello
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> "hello");

        //  Consumer
        // hello futureB
        CompletableFuture<Void> futureB = futureA.thenAcceptAsync((t) -> {
            delaySec(3);
            printCurrTime(t+" futureB");
        },threadPoolExecutor);
        // hello futureC
        CompletableFuture<Void> futureC = futureA.thenAcceptAsync((t) -> {
            delaySec(1);
            printCurrTime(t+" futureC");
        },threadPoolExecutor);

        futureB.get();
        futureC.get();


    }
    //  打印方法
    private static void printCurrTime(String s) {
        System.out.println(s);
    }

    //  睡眠方法
    private static void delaySec(int i) {
        try {
            Thread.sleep(i*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
