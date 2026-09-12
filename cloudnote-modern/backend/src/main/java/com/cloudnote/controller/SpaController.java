package com.cloudnote.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端是 Vue 单页应用（history 路由），浏览器直接访问 /shares、/notes 这类地址
 * 或在这些页面上按 F5 刷新时，服务端要把 index.html 返回给浏览器，再由前端路由接管。
 *
 * <p>只映射前端真实存在的路由，不做通配，避免影响 {@code /api/**} 的 404 语义。
 */
@Controller
public class SpaController {

    @GetMapping({
            "/",
            "/login",
            "/register",
            "/shares",
            "/activities",
            "/activities/{id}",
            "/notes",
            "/admin"
    })
    public String index() {
        return "forward:/index.html";
    }
}
