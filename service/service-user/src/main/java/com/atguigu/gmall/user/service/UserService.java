package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

/**
 * @author Yuehong Zhang
 */
public interface UserService {

    //  sql: select * from user_info where login_name = ? and passwd=?
    UserInfo login(UserInfo userInfo);
}
