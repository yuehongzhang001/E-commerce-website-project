package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Yuehong Zhang
 * @date 2021-4-16 15:36:08
 */
@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void testLock() throws InterruptedException {
       /*
        1. Store a num in the cache with an initial value of 0
        2. The current num value obtained by using the cached StringRedisTemplate
        3. If num is not empty, you need to +1 the current value and write it back to the cache
        4. If num is empty, just return!
         */
        RLock lock = redissonClient.getLock("lock");
        // call method
        // lock.lock(10,TimeUnit.SECONDS);
        boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
        if (res){
            try {
                // Successfully locked!
                // If flag = true: it means the lock is acquired! Execute business logic!
                // The current num value obtained by using the cached StringRedisTemplate
                String num = redisTemplate.opsForValue().get("num");
                //  judge
                if(StringUtils.isEmpty(num)){
                    return;
                }
                // If num is not empty, you need to +1 the current value and write it back to the cache
                int numValue = Integer.parseInt(num);
                // write back to cache
                redisTemplate.opsForValue().set("num",String.valueOf(++numValue));
            } finally {
                // unlock
                lock.unlock();
            }
        }
    }

    @Override
    public String readLock() {
        // Create object
        RReadWriteLock rwlock = redissonClient.getReadWriteLock("anyRWLock");
        // Lock: Automatically unlock in 10 seconds!
        rwlock.readLock().lock(10,TimeUnit.SECONDS);
        // Read data from the cache
        String msg = redisTemplate.opsForValue().get("msg");
        // Return the data read
        return msg;
    }

    @Override
    public String writeLock() {
        // Create object
        RReadWriteLock rwlock = redissonClient.getReadWriteLock("anyRWLock");
        // Lock: Automatically unlock in 10 seconds!
        rwlock.writeLock().lock(10,TimeUnit.SECONDS);
        // Write the content directly to the cache
        String uuid = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("msg",uuid);
        // return data
        return "write complete"+uuid;
    }

    /*
    1. Store a num in the cache with an initial value of 0
    2. The current num value obtained by using the cached StringRedisTemplate
    3. If num is not empty, you need to +1 the current value and write it back to the cache
    4. If num is empty, just return!
    http://doc.redisfans.com/
    String:
    List:
    Hash:
    Set:
    ZSet:
     */
// @Override
// public void testLock() {
//
// // Use the setnx command: setnx lock ok
// // Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", "ok");
// // use setex
// // Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", "ok",3L, TimeUnit.SECONDS);
// // Use uuId to prevent accidental deletion of locks!
// String uuid = UUID.randomUUID().toString();
// Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", uuid,3L, TimeUnit.SECONDS);
//
//        //  judge
// if(flag){
// // If flag = true: it means the lock is acquired! Execute business logic!
// // The current num value obtained by using the cached StringRedisTemplate
// String num = redisTemplate.opsForValue().get("num");
//            //  judge
// if(StringUtils.isEmpty(num)){
// return;
//}
// // If num is not empty, you need to +1 the current value and write it back to the cache
// int numValue = Integer.parseInt(num);
// // write back to cache
// redisTemplate.opsForValue().set("num",String.valueOf(++numValue));
//
// // If the uuid in the cache is consistent with the current uuid! Delete it, otherwise don't delete it!
// // if (redisTemplate.opsForValue().get("lock").equals(uuid)){
// // // Release the lock del key index1
// // redisTemplate.delete("lock");
// //}
//
// // Define a LUA script:
// String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//
// // Create object
// DefaultRedisScript<Long> redisScript = new DefaultRedisScript();
// // Set up lua script
// redisScript.setScriptText(script);
// redisScript.setResultType(Long.class);
// // redis calls the LUA script
// redisTemplate.execute(redisScript, Arrays.asList("lock"),uuid);
//
// }else {
// // The lock was not acquired!
// try {
// Thread.sleep(500);
//} catch (InterruptedException e) {
// e.printStackTrace();
//}
// // Spin:
// testLock();
//}
//}
}
