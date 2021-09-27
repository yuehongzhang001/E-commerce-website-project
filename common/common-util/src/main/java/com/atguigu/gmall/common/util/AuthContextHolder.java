package com.atguigu.gmall.common.util;

//import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Get login user information
 *
 */
public class AuthContextHolder {

    /**
     * Get the currently logged-in user id
     * @param request
     * @return
     */
    public static String getUserId(HttpServletRequest request) {
        String userId = request.getHeader("userId");
        return StringUtils.isEmpty(userId)? "": userId;
    }

    /**
     * Get the id of a temporary user who is not currently logged in
     * @param request
     * @return
     */
    public static String getUserTempId(HttpServletRequest request) {
        String userTempId = request.getHeader("userTempId");
        return StringUtils.isEmpty(userTempId)? "": userTempId;
    }
}