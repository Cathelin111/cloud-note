package com.cloudnote.service;

import java.util.Map;

import com.cloudnote.dto.LoginRequest;
import com.cloudnote.dto.RegisterRequest;

public interface UserService {

    /** 注册(默认普通用户, 即用即生效) */
    void register(RegisterRequest request);

    /**
     * 登录: 兼容存量MD5密码与新版BCrypt; 返回 token 与用户信息(不含密码)
     * status=1 用户名不存在 / status=2 密码错误 / status=3 账号被停用
     */
    Map<String, Object> login(LoginRequest request);
}
