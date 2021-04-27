package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartAsyncService;
import com.atguigu.gmall.model.cart.CarInfoVo;
import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author mqx
 */
@Service
public class CartAsyncServiceImpl implements CartAsyncService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Override
    @Async
    public void saveCartInfo(CartInfo cartInfo) {
            //  int i= 1/0;
          cartInfoMapper.insert(cartInfo);

    }

    @Override
    @Async
    public void updateCartInfo(CartInfo cartInfo) {
        //  代码有问题的！
        //  update cart_info set sku_num = ? where sku_id = ? and user_id = ?
        //  cartInfoMapper.updateById(cartInfo);
        //  构架更新条件
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id",cartInfo.getSkuId());
        queryWrapper.eq("user_id",cartInfo.getUserId());
        cartInfoMapper.update(cartInfo,queryWrapper);
    }

    @Override
    @Async
    public void deleteCartInfo(CartInfo cartInfo) {
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",cartInfo.getUserId());
        cartInfoMapper.delete(queryWrapper);
    }

    @Override
    @Async
    public void deleteCartInfo(String userId) {
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        cartInfoMapper.delete(queryWrapper);
    }

    @Override
    @Async
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        //  update cart_info set is_checked = ? where userId= ? and skuId= ?
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);

        //        UpdateWrapper<CartInfo> updateWrapper = new UpdateWrapper<>();
        //        updateWrapper.eq("user_id",userId);
        //        updateWrapper.eq("sku_id",skuId);
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("sku_id",skuId);

        //  updateWrapper.set("is_checked",isChecked);
        //  第一个参数cartInfo:更新的内容  第二个参数updateWrapper 更新条件！
        cartInfoMapper.update(cartInfo,queryWrapper);
    }

    @Override
    @Async
    public void deleteCartInfo(String userId, Long skuId) {
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("sku_id",skuId);
        cartInfoMapper.delete(queryWrapper);
    }
}
