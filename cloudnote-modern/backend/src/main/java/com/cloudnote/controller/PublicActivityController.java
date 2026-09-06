package com.cloudnote.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudnote.common.ApiResult;
import com.cloudnote.entity.Activity;
import com.cloudnote.entity.NoteActivity;
import com.cloudnote.service.ActivityService;

/**
 * 公开接口(无需登录): 活动列表/投稿浏览
 * 替代旧 /activity/findActivity.do 等浏览接口
 */
@RestController
@RequestMapping("/api/public/activities")
public class PublicActivityController {

    private final ActivityService activityService;

    public PublicActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public ApiResult<List<Activity>> list() {
        return ApiResult.ok("查询活动成功", activityService.listActivities());
    }

    @GetMapping("/{activityId}/submissions")
    public ApiResult<Map<String, Object>> submissions(@PathVariable String activityId,
                                                      @RequestParam(defaultValue = "1") Integer page) {
        return ApiResult.ok("查询投稿成功", activityService.submissionPage(activityId, page));
    }

    @GetMapping("/submissions/{submissionId}")
    public ApiResult<NoteActivity> submissionDetail(@PathVariable String submissionId) {
        return ApiResult.ok("查询投稿详情成功", activityService.submissionDetail(submissionId));
    }
}
