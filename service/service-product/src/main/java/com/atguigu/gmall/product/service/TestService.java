package com.atguigu.gmall.product.service;

/**
 * @author Yuehong Zhang
 * @date 2021-4-16 15:35:38
 */
public interface TestService {
    /**
     * Test local lock
     */
    void testLock() throws InterruptedException;

    /**
     * Read lock
     * @return
     */
    String readLock();

    /**
     * Write lock
     * @return
     */
    String writeLock();
}