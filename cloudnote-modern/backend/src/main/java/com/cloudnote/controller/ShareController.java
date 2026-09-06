package com.cloudnote.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudnote.common.ApiResult;
import com.cloudnote.dto.ShareNoteRequest;
import com.cloudnote.service.ShareService;

import jakarta.validation.Valid;

/**
 * 分享操作(需登录)
 * 替代旧 /share/add.do 与 /note/likeShareNote.do
 */
@RestController
@RequestMapping("/api/shares")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    /** 分享我的笔记 */
    @PostMapping
    public ApiResult<Void> shareMine(@Valid @RequestBody ShareNoteRequest request) {
        shareService.shareMyNote(request.getNoteId());
        return ApiResult.ok("分享笔记成功", null);
    }

    /** 收藏他人分享到我的收藏笔记本 */
    @PostMapping("/{shareId}/collect")
    public ApiResult<Void> collect(@PathVariable String shareId) {
        shareService.collect(shareId);
        return ApiResult.ok("收藏成功", null);
    }
}
