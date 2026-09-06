package com.cloudnote.service.impl;

import org.springframework.stereotype.Service;

import com.cloudnote.common.BizException;
import com.cloudnote.entity.User;
import com.cloudnote.mapper.UserMapper;
import com.cloudnote.security.SecurityUtils;
import com.cloudnote.service.CurrentUserService;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    private final UserMapper userMapper;

    public CurrentUserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User requireUser() {
        String username = SecurityUtils.currentUsername();
        User user = userMapper.findByName(username);
        if (user == null) {
            throw new BizException(401, "账号不存在, 请重新登录");
        }
        if ("disabled".equals(user.getCn_user_status())) {
            throw new BizException(3, "该账号已被管理员停用");
        }
        return user;
    }
}
