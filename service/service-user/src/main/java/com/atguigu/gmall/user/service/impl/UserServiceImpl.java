package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author mqx
 */
@Service
public class UserServiceImpl implements UserService {
    //  注入mapper
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public UserInfo login(UserInfo userInfo) {
        //  执行sql：select * from user_info where login_name = ? and passwd=?
        //  构建查询条件
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("login_name",userInfo.getLoginName());
        //  对用户输入的密码进行加密操作！MD5
        String newPassword = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());

        userInfoQueryWrapper.eq("passwd",newPassword);

        UserInfo info = userInfoMapper.selectOne(userInfoQueryWrapper);
        if (info!=null){
            return info;
        }
        return null;
    }
}
