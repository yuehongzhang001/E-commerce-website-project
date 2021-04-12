package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author mqx
 * @date 2021-4-12 14:10:48
 */
public interface BaseTrademarkService extends IService<BaseTrademark> {

    /**
     * 查询所有品牌数据
     * @param baseTrademarkPage
     * @return
     */
    IPage getBaseTradeMarkList(Page<BaseTrademark> baseTrademarkPage);


    //  void save(BaseTrademark baseTrademark);
}
