package com.cloudnote.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudnote.common.ApiResult;
import com.cloudnote.dto.LoginRequest;
import com.cloudnote.dto.RegisterRequest;
import com.cloudnote.service.UserService;

import jakarta.validation.Valid;

/**
 * 认证接口: 注册 / 登录(游客注册为普通用户; 管理员使用同一登录入口)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResult<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ApiResult.ok("注册成功", null);
    }

    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> data = userService.login(request);
        return ApiResult.ok("登录成功", data);
    }
}
