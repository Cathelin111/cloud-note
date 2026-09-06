package com.cloudnote.service;

import com.cloudnote.entity.User;

public interface CurrentUserService {

    /** 当前登录用户(用户名来自JWT), 不存在/停用抛BizException */
    User requireUser();
}
