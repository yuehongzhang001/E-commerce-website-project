package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Yuehong Zhang
 */
@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;

    // Cut annotations:
    @SneakyThrows
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object gmallCacheGetData(ProceedingJoinPoint joinPoint){
        Object object = null;
        /*
            1. Get the annotations on the method
            2. Get the prefix of the annotation and form the cached key
            3. Get the data in the cache according to the key
            4. Determine whether the data is acquired {business logic of distributed lock}
         */
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);

        // Get the prefix of the annotation
        String prefix = gmallCache.prefix();

        // Get the parameters on the method
        Object[] args = joinPoint.getArgs();
        // Define the key of the cache
        String key = prefix + Arrays.asList(args);

        try {
            // Get method from cache
            object = gitCache(key,signature);
            //  judge
            if (object == null){
                // The business logic of distributed locks is here!
                // first lock
                RLock lock = redissonClient.getLock(key + ":lock");
                // Locked:
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                // Get the lock object
                if (res){
                    try {
                        // Execute the code block in the method body indicated by the GmallCache annotation!
                        object = joinPoint.proceed(joinPoint.getArgs());
                        // Judgment to prevent cache penetration
                        if (object==null){
                            Object object1 = new Object();
                            redisTemplate.opsForValue().set(key,JSON.toJSONString(object1),RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                            return object1;
                        }
                        //  not null!
                        // skuInfo is not empty
                        // set key value; Object, string!
                        redisTemplate.opsForValue().set(key, JSON.toJSONString(object),RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        // Return data!
                        return object;
                    }finally {
                        lock.unlock();
                    }
                }else {
                    // The lock object is not acquired
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return gmallCacheGetData(joinPoint);
                }
            }else {
                // The cache is there, just return!
                return object;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        // Go back to the database directly!
        Object proceed = joinPoint.proceed(joinPoint.getArgs());
        return proceed;
    }

    private Object gitCache(String key,MethodSignature signature) {
        // return String
        String sObject = (String) redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(sObject)){
            // Return data! Get the return type
            // If cached: public BigDecimal getSkuPrice(Long skuId) return value BigDecimal
            // If cached: public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) return SpuSaleAttr
            // If cached: public SkuInfo getSkuInfo(Long skuId) returns SkuInfo
            Class returnType = signature.getReturnType();

            // Change the string to the data type to be returned!
            return JSON.parseObject(sObject,returnType);
        }
        return null;
    }
}