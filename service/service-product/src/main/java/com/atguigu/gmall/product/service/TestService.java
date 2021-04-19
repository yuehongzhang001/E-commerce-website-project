package com.atguigu.gmall.product.service;

/**
 * @author mqx
 * @date 2021-4-16 15:35:38
 */
public interface TestService {
    /**
     * 测试本地锁
     */
    void testLock() throws InterruptedException;

    /**
     * 读锁
     * @return
     */
    String readLock();

    /**
     * 写锁
     * @return
     */
    String writeLock();
}
