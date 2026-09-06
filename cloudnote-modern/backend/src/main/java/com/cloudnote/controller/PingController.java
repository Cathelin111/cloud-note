package com.cloudnote.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查/连通性验证(阶段①脚手架验证用, 后续可移除)
 */
@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("status", 0);
        m.put("msg", "cloudnote-backend is running");
        m.put("time", System.currentTimeMillis());
        return m;
    }
}
