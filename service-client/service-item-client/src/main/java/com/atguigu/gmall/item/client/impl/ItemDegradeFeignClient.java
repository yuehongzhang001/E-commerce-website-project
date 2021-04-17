package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.stereotype.Component;

/**
 * @author mqx
 * @date 2021-4-16 11:30:57
 */
@Component
public class ItemDegradeFeignClient implements ItemFeignClient {
    @Override
    public Result getItemById(Long skuId) {
        return null;
    }
}
