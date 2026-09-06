package com.cloudnote.service;

import java.util.List;
import java.util.Map;

import com.cloudnote.entity.Activity;
import com.cloudnote.entity.User;

/**
 * 管理员服务(调用方需为 ROLE_ADMIN; 服务内再次校验角色)
 */
public interface AdminService {

    //========= 用户管理 =========
    Map<String, Object> userPage(String keyword, Integer page);

    void updateUserStatus(String targetId, String status);

    void deleteUser(String targetId);

    void promoteUser(String targetId);

    //========= 管理员管理 =========
    User createAdmin(String username, String password, String nick);

    List<User> listAdmins();

    //========= 分享管理 =========
    Map<String, Object> sharePage(String keyword, Integer page);

    void updateShareStatus(String shareId, String status);

    void deleteShare(String shareId);

    //========= 活动管理 =========
    List<Activity> listActivities();

    Activity saveActivity(String activityId, String title, String body, Long endTime);

    void deleteActivity(String activityId);

    //========= 系统 =========
    String backup();
}
