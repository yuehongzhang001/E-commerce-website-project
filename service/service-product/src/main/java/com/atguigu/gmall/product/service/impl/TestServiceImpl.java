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
 * @author mqx
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
        1.  在缓存中存储一个num 初始值为 0
        2.  利用缓存的StringRedisTemplate 获取到的当前的num 值
        3.  如果num 不为空，则需要对当前值 进行 +1 操作,写回缓存
        4.  如果num 为空，则返回即可！
         */
        RLock lock = redissonClient.getLock("lock");
        //  调用方法
        //  lock.lock(10,TimeUnit.SECONDS);
          boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
        if (res){
            try {
                //  上锁成功！
                //  如果flag = true: 表示获取到锁！ 执行业务逻辑！
                //  利用缓存的StringRedisTemplate 获取到的当前的num 值
                String num = redisTemplate.opsForValue().get("num");
                //  判断
                if(StringUtils.isEmpty(num)){
                    return;
                }
                //  如果num 不为空，则需要对当前值 进行 +1 操作,写回缓存
                int numValue = Integer.parseInt(num);
                //  写回缓存
                redisTemplate.opsForValue().set("num",String.valueOf(++numValue));
            } finally {
                //  解锁
                lock.unlock();
            }
        }
    }

    @Override
    public String readLock() {
        //  创建对象
        RReadWriteLock rwlock = redissonClient.getReadWriteLock("anyRWLock");
        //  上锁： 10 秒钟自动解锁！
        rwlock.readLock().lock(10,TimeUnit.SECONDS);
        //  从缓存中读取数据
        String msg = redisTemplate.opsForValue().get("msg");
        //  返回读取到的数据
        return msg;
    }

    @Override
    public String writeLock() {
        //  创建对象
        RReadWriteLock rwlock = redissonClient.getReadWriteLock("anyRWLock");
        //  上锁： 10 秒钟自动解锁！
        rwlock.writeLock().lock(10,TimeUnit.SECONDS);
        //  直接将内容写入缓存
        String uuid = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("msg",uuid);
        //  返回数据
        return "写入完成"+uuid;
    }

    /*
    1.  在缓存中存储一个num 初始值为 0
    2.  利用缓存的StringRedisTemplate 获取到的当前的num 值
    3.  如果num 不为空，则需要对当前值 进行 +1 操作,写回缓存
    4.  如果num 为空，则返回即可！
    http://doc.redisfans.com/
    String ：
    List ：
    Hash：
    Set ：
    ZSet ：
     */
//    @Override
//    public void testLock() {
//
//        //  使用setnx 命令： setnx lock ok
//        //  Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", "ok");
//        //  使用setex
//        //  Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", "ok",3L, TimeUnit.SECONDS);
//        //  使用uuId 防止误删锁！
//        String uuid = UUID.randomUUID().toString();
//        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", uuid,3L, TimeUnit.SECONDS);
//
//        //  判断
//        if(flag){
//            //  如果flag = true: 表示获取到锁！ 执行业务逻辑！
//            //  利用缓存的StringRedisTemplate 获取到的当前的num 值
//            String num = redisTemplate.opsForValue().get("num");
//            //  判断
//            if(StringUtils.isEmpty(num)){
//                return;
//            }
//            //  如果num 不为空，则需要对当前值 进行 +1 操作,写回缓存
//            int numValue = Integer.parseInt(num);
//            //  写回缓存
//            redisTemplate.opsForValue().set("num",String.valueOf(++numValue));
//
//            //  如果缓存中的uuid 与 当前uuid 一致！则删除，否则不删除！
//            //            if (redisTemplate.opsForValue().get("lock").equals(uuid)){
//            //                //  释放锁 del key index1
//            //                redisTemplate.delete("lock");
//            //            }
//
//            //  定义一个LUA 脚本：
//            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//
//            //  创建对象
//            DefaultRedisScript<Long> redisScript = new DefaultRedisScript();
//            //  设置lua脚本
//            redisScript.setScriptText(script);
//            redisScript.setResultType(Long.class);
//            //  redis 调用LUA 脚本
//            redisTemplate.execute(redisScript, Arrays.asList("lock"),uuid);
//
//        }else {
//            //  没有获取到锁！
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            //  自旋：
//            testLock();
//        }
//    }
}
