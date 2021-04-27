package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

/**
 * @author mqx
 */
public interface UserService {

    //  sql: select * from user_info where login_name = ? and passwd=?
    UserInfo login(UserInfo userInfo);
}
