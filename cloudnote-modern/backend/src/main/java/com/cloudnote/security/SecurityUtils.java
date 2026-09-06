package com.cloudnote.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cloudnote.common.BizException;

/**
 * 从 SecurityContext 获取当前登录用户名(JWT subject)
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BizException(401, "未登录或登录已过期");
        }
        return String.valueOf(auth.getPrincipal());
    }
}
