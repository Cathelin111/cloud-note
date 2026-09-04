package com.lcz.cloud_note.service;

import java.util.List;
import java.util.Map;

import com.lcz.cloud_note.entity.Activity;
import com.lcz.cloud_note.entity.User;
import com.lcz.cloud_note.util.NoteResult;

/**
 * 后台管理服务(所有操作前校验操作者必须是启用的系统管理员)
 */
public interface AdminService {
	//分页查询普通用户(用户管理)
	public NoteResult<Map<String,Object>> userList(String adminId, String keyword, Integer page);
	//停用/启用用户
	public NoteResult<Object> setUserStatus(String adminId, String userId, String status);
	//删除用户(级联清理其笔记本/笔记/分享/投稿)
	public NoteResult<Object> deleteUser(String adminId, String userId);
	//把普通用户提升为管理员
	public NoteResult<Object> setAdmin(String adminId, String targetId);
	//直接新建一个管理员账号(用户名/密码/昵称)
	public NoteResult<Object> addAdmin(String adminId, String name, String password, String nick);
	//查询当前全部管理员账号
	public NoteResult<List<User>> adminList(String adminId);
	//分页查询分享内容(分享管理)
	public NoteResult<Map<String,Object>> shareList(String adminId, String keyword, Integer page);
	//下架/上架分享
	public NoteResult<Object> setShareStatus(String adminId, String shareId, String status);
	//删除分享
	public NoteResult<Object> deleteShare(String adminId, String shareId);
	//查询活动列表(活动管理)
	public NoteResult<List<Activity>> activityList(String adminId);
	//新建/修改活动
	public NoteResult<Object> saveActivity(String adminId, String activityId, String title, String body, String endTime);
	//删除活动(同时删除其下投稿)
	public NoteResult<Object> deleteActivity(String adminId, String activityId);
	//系统: 立即备份数据库(H2), 返回备份文件路径
	public NoteResult<Object> systemBackup(String adminId);
}
