package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author mqx
 * @date 2021-4-16 15:36:08
 */
@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private StringRedisTemplate redisTemplate;

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
    @Override
    public synchronized void testLock() {
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

    }
}
