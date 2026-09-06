package com.cloudnote.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudnote.common.ApiResult;
import com.cloudnote.dto.CreateAdminRequest;
import com.cloudnote.dto.StatusRequest;
import com.cloudnote.entity.User;
import com.cloudnote.service.AdminService;

import jakarta.validation.Valid;

/**
 * 管理员-用户与管理员管理(需ROLE_ADMIN)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    /** 普通用户分页 */
    @GetMapping("/users")
    public ApiResult<Map<String, Object>> users(@RequestParam(defaultValue = "") String keyword,
                                                @RequestParam(defaultValue = "1") Integer page) {
        return ApiResult.ok("查询成功", adminService.userPage(keyword, page));
    }

    /** 停用/启用 */
    @PatchMapping("/users/{userId}/status")
    public ApiResult<Void> status(@PathVariable String userId, @Valid @RequestBody StatusRequest request) {
        adminService.updateUserStatus(userId, request.getStatus());
        return ApiResult.ok("操作成功", null);
    }

    /** 提升为管理员 */
    @PutMapping("/users/{userId}/promote")
    public ApiResult<Void> promote(@PathVariable String userId) {
        adminService.promoteUser(userId);
        return ApiResult.ok("已提升为管理员", null);
    }

    /** 删除用户(级联) */
    @DeleteMapping("/users/{userId}")
    public ApiResult<Void> deleteUser(@PathVariable String userId) {
        adminService.deleteUser(userId);
        return ApiResult.ok("用户已删除", null);
    }

    /** 管理员列表 */
    @GetMapping("/admins")
    public ApiResult<java.util.List<User>> admins() {
        return ApiResult.ok("查询成功", adminService.listAdmins());
    }

    /** 新建管理员 */
    @PostMapping("/admins")
    public ApiResult<User> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        return ApiResult.ok("管理员账号创建成功", adminService.createAdmin(
                request.getUsername(), request.getPassword(), request.getNick()));
    }
}
