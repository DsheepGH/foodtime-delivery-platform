package com.foodtime.service;

import com.foodtime.dto.UserLoginDTO;
import com.foodtime.entity.User;

public interface UserService {

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}
