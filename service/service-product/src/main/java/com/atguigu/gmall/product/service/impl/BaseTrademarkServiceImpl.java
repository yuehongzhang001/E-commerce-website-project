package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Yuehong Zhang
 * @date 2021-4-12 14:11:58
 */
@Service
public class BaseTrademarkServiceImpl extends ServiceImpl<BaseTrademarkMapper,BaseTrademark> implements BaseTrademarkService {

    // The service layer calls the mapper layer
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    @Override
    public IPage getBaseTradeMarkList(Page<BaseTrademark> baseTrademarkPage) {

        // select * from base_trademark order by id
        // Add a sorting rule
        QueryWrapper<BaseTrademark> baseTrademarkQueryWrapper = new QueryWrapper<>();
        baseTrademarkQueryWrapper.orderByDesc("id");
        // call query method
        return baseTrademarkMapper.selectPage(baseTrademarkPage,baseTrademarkQueryWrapper);
    }

    // @Override
    // public void save(BaseTrademark baseTrademark) {
    //
    // baseTrademarkMapper.insert(baseTrademark);
    //
    //}
}