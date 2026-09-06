package com.cloudnote.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudnote.common.ApiResult;
import com.cloudnote.dto.ActivitySaveRequest;
import com.cloudnote.dto.StatusRequest;
import com.cloudnote.entity.Activity;
import com.cloudnote.service.AdminService;

import jakarta.validation.Valid;

/**
 * 管理员-分享/活动/系统(需ROLE_ADMIN)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminContentController {

    private final AdminService adminService;

    public AdminContentController(AdminService adminService) {
        this.adminService = adminService;
    }

    //=========== 分享管理 ===========

    @GetMapping("/shares")
    public ApiResult<Map<String, Object>> shares(@RequestParam(defaultValue = "") String keyword,
                                                 @RequestParam(defaultValue = "1") Integer page) {
        return ApiResult.ok("查询成功", adminService.sharePage(keyword, page));
    }

    /** 下架/上架 */
    @PatchMapping("/shares/{shareId}/status")
    public ApiResult<Void> shareStatus(@PathVariable String shareId, @Valid @RequestBody StatusRequest request) {
        adminService.updateShareStatus(shareId, request.getStatus());
        return ApiResult.ok("操作成功", null);
    }

    @DeleteMapping("/shares/{shareId}")
    public ApiResult<Void> deleteShare(@PathVariable String shareId) {
        adminService.deleteShare(shareId);
        return ApiResult.ok("分享已删除", null);
    }

    //=========== 活动管理 ===========

    @GetMapping("/activities")
    public ApiResult<List<Activity>> activities() {
        return ApiResult.ok("查询成功", adminService.listActivities());
    }

    /** 新建(activityId空)或修改活动 */
    @PostMapping("/activities")
    public ApiResult<Activity> saveActivity(@Valid @RequestBody ActivitySaveRequest request) {
        return ApiResult.ok("保存成功", adminService.saveActivity(
                request.getActivityId(), request.getTitle(), request.getBody(), request.getEndTime()));
    }

    @DeleteMapping("/activities/{activityId}")
    public ApiResult<Void> deleteActivity(@PathVariable String activityId) {
        adminService.deleteActivity(activityId);
        return ApiResult.ok("活动已删除(投稿一并清理)", null);
    }

    //=========== 系统 ===========

    /** 立即备份数据库(mysqldump) */
    @PostMapping("/system/backup")
    public ApiResult<String> backup() {
        return ApiResult.ok("备份完成", adminService.backup());
    }
}
