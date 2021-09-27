package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Yuehong Zhang
 * @date 2021-4-12 14:10:48
 */
public interface BaseTrademarkService extends IService<BaseTrademark> {

    /**
     * Query all brand data
     * @param baseTrademarkPage
     * @return
     */
    IPage getBaseTradeMarkList(Page<BaseTrademark> baseTrademarkPage);


    // void save(BaseTrademark baseTrademark);
}