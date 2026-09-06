package com.cloudnote.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudnote.common.ApiResult;
import com.cloudnote.dto.ActivityJoinRequest;
import com.cloudnote.entity.NoteActivity;
import com.cloudnote.service.ActivityService;

import jakarta.validation.Valid;

/**
 * 活动操作(需登录): 投稿/顶/踩/收藏投稿
 * 替代旧 addNoteActivity/up/down/likeActivityNote
 */
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /** 参加活动(投稿) */
    @PostMapping("/{activityId}/join")
    public ApiResult<NoteActivity> join(@PathVariable String activityId,
                                        @Valid @RequestBody ActivityJoinRequest request) {
        return ApiResult.ok("参加活动成功", activityService.join(activityId, request.getNoteId()));
    }

    /** 顶投稿 */
    @PostMapping("/submissions/{submissionId}/up")
    public ApiResult<Void> up(@PathVariable String submissionId) {
        activityService.vote(submissionId, true);
        return ApiResult.ok("顶成功", null);
    }

    /** 踩投稿 */
    @PostMapping("/submissions/{submissionId}/down")
    public ApiResult<Void> down(@PathVariable String submissionId) {
        activityService.vote(submissionId, false);
        return ApiResult.ok("踩成功", null);
    }

    /** 收藏活动投稿到我的收藏笔记本 */
    @PostMapping("/submissions/{submissionId}/collect")
    public ApiResult<Void> collect(@PathVariable String submissionId) {
        activityService.collect(submissionId);
        return ApiResult.ok("收藏成功", null);
    }
}
