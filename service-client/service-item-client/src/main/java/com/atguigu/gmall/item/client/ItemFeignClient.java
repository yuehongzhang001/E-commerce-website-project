package com.atguigu.gmall.item.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.impl.ItemDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author mqx
 * @date 2021-4-16 11:30:07
 */
@FeignClient(value = "service-item",fallback = ItemDegradeFeignClient.class)
public interface ItemFeignClient {

    //  发送数据接口：
    @GetMapping("api/item/{skuId}")
    Result getItemById(@PathVariable Long skuId);
}
