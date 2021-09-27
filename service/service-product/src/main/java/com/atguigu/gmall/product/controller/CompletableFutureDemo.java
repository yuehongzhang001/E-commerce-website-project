package com.atguigu.gmall.product.controller;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Yuehong Zhang
 */
public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // Create thread pool
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                5,
                3L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3)
        );

        // futureA
        // Thread 1 execution return result: hello
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> "hello");

        // Consumer
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
    // Printing method
    private static void printCurrTime(String s) {
        System.out.println(s);
    }

    // sleep method
    private static void delaySec(int i) {
        try {
            Thread.sleep(i*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}