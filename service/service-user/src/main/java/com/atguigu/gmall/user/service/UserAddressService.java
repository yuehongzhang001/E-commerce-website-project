package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * @author mqx
 */
public interface UserAddressService {
    //  根据用户Id 查询收货地址列表！
    List<UserAddress> findUserAddressListByUserId(Long userId);

}
