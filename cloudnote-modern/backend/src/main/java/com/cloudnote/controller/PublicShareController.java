package com.cloudnote.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudnote.common.ApiResult;
import com.cloudnote.entity.Share;
import com.cloudnote.service.ShareService;

/**
 * 公开接口(无需登录): 分享广场搜索/详情
 * 替代旧 /share/searchPage.do 与 /note/load_share.do
 */
@RestController
@RequestMapping("/api/public/shares")
public class PublicShareController {

    private final ShareService shareService;

    public PublicShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @GetMapping
    public ApiResult<Map<String, Object>> page(@RequestParam(defaultValue = "") String keyword,
                                               @RequestParam(defaultValue = "1") Integer page) {
        return ApiResult.ok("搜索成功", shareService.publicPage(keyword, page));
    }

    @GetMapping("/{shareId}")
    public ApiResult<Share> detail(@PathVariable String shareId) {
        return ApiResult.ok("加载成功", shareService.publicDetail(shareId));
    }
}
