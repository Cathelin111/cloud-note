package com.lcz.cloud_note.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lcz.cloud_note.entity.Activity;
import com.lcz.cloud_note.service.AdminService;
import com.lcz.cloud_note.util.NoteResult;

/**
 * 后台管理控制器(系统管理员)
 * 所有接口均需携带操作者 userId(即管理员自己的用户ID), 服务端校验管理员身份
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
	@Resource
	private AdminService adminService;

	//用户管理: 分页查询普通用户列表
	@RequestMapping("/user/list.do")
	@ResponseBody
	public NoteResult<Map<String,Object>> userList(String userId, String keyword, Integer page){
		return adminService.userList(userId, keyword, page);
	}
	//用户管理: 停用/启用用户 status=normal/disabled
	@RequestMapping("/user/updateStatus.do")
	@ResponseBody
	public NoteResult<Object> updateUserStatus(String userId, String targetId, String status){
		return adminService.setUserStatus(userId, targetId, status);
	}
	//用户管理: 删除用户
	@RequestMapping("/user/delete.do")
	@ResponseBody
	public NoteResult<Object> deleteUser(String userId, String targetId){
		return adminService.deleteUser(userId, targetId);
	}

	//分享管理: 分页查询分享内容
	@RequestMapping("/share/list.do")
	@ResponseBody
	public NoteResult<Map<String,Object>> shareList(String userId, String keyword, Integer page){
		return adminService.shareList(userId, keyword, page);
	}
	//分享管理: 下架/上架分享 status=normal/disabled
	@RequestMapping("/share/updateStatus.do")
	@ResponseBody
	public NoteResult<Object> updateShareStatus(String userId, String shareId, String status){
		return adminService.setShareStatus(userId, shareId, status);
	}
	//分享管理: 删除分享
	@RequestMapping("/share/delete.do")
	@ResponseBody
	public NoteResult<Object> deleteShare(String userId, String shareId){
		return adminService.deleteShare(userId, shareId);
	}

	//活动管理: 活动列表
	@RequestMapping("/activity/list.do")
	@ResponseBody
	public NoteResult<List<Activity>> activityList(String userId){
		return adminService.activityList(userId);
	}
	//活动管理: 新建(activityId为空)或修改活动; endTime为毫秒时间戳, 空=长期有效
	@RequestMapping("/activity/save.do")
	@ResponseBody
	public NoteResult<Object> saveActivity(String userId, String activityId,
			String title, String body, String endTime){
		return adminService.saveActivity(userId, activityId, title, body, endTime);
	}
	//活动管理: 删除活动(连带删除其投稿)
	@RequestMapping("/activity/delete.do")
	@ResponseBody
	public NoteResult<Object> deleteActivity(String userId, String activityId){
		return adminService.deleteActivity(userId, activityId);
	}
}
