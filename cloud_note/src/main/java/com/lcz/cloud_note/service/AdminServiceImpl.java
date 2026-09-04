package com.lcz.cloud_note.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lcz.cloud_note.dao.ActivityDao;
import com.lcz.cloud_note.dao.BookDao;
import com.lcz.cloud_note.dao.NoteDao;
import com.lcz.cloud_note.dao.ShareDao;
import com.lcz.cloud_note.dao.UserDao;
import com.lcz.cloud_note.entity.Activity;
import com.lcz.cloud_note.entity.Share;
import com.lcz.cloud_note.entity.User;
import com.lcz.cloud_note.util.NoteResult;
import com.lcz.cloud_note.util.NoteUtil;
import com.lcz.cloud_note.web.DataBackup;

@Service("adminService")
@Transactional
public class AdminServiceImpl implements AdminService {
	//后台列表每页显示条数
	private static final int PAGE_SIZE = 10;

	@Resource
	private UserDao userDao;
	@Resource
	private ShareDao shareDao;
	@Resource
	private ActivityDao activityDao;
	@Resource
	private BookDao bookDao;
	@Resource
	private NoteDao noteDao;
	@Resource
	private DataBackup dataBackup;

	//================== 权限校验 ==================

	/**
	 * 校验操作者是否为已启用的系统管理员
	 * @return 校验失败时返回null
	 */
	private User checkAdmin(String adminId) {
		if (adminId == null || adminId.trim().isEmpty()) {
			return null;
		}
		User admin = userDao.findById(adminId);
		if (admin == null) {
			return null;
		}
		if (!"admin".equals(admin.getCn_user_role())) {
			return null;
		}
		if (!"normal".equals(admin.getCn_user_status())) {
			return null;
		}
		return admin;
	}

	private <T> NoteResult<T> noPermission() {
		NoteResult<T> result = new NoteResult<T>();
		result.setStatus(4);
		result.setMsg("无管理员权限, 操作被拒绝");
		return result;
	}

	//================== 用户管理 ==================

	public NoteResult<Map<String,Object>> userList(String adminId, String keyword, Integer page) {
		NoteResult<Map<String,Object>> result = new NoteResult<Map<String,Object>>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		Map<String,Object> params = new HashMap<String,Object>();
		String kw = (keyword == null) ? "" : keyword.trim();
		if (!kw.isEmpty()) {
			params.put("keyword", "%" + kw + "%");
		}
		int total = userDao.countAdmin(params);
		int p = (page == null || page < 1) ? 1 : page;
		params.put("begin", (p - 1) * PAGE_SIZE);
		params.put("pageSize", PAGE_SIZE);
		List<User> rows = userDao.findAdminPage(params);
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", total);
		data.put("page", p);
		data.put("size", PAGE_SIZE);
		data.put("rows", rows);
		result.setStatus(0);
		result.setMsg("查询成功");
		result.setData(data);
		return result;
	}

	public NoteResult<Object> setUserStatus(String adminId, String userId, String status) {
		NoteResult<Object> result = new NoteResult<Object>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		if (userId == null || userId.trim().isEmpty() || status == null) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		if (!"normal".equals(status) && !"disabled".equals(status)) {
			result.setStatus(1);
			result.setMsg("非法的状态值");
			return result;
		}
		User target = userDao.findById(userId);
		if (target == null) {
			result.setStatus(2);
			result.setMsg("用户不存在");
			return result;
		}
		if ("admin".equals(target.getCn_user_role())) {
			result.setStatus(3);
			result.setMsg("不能停用/启用管理员账号");
			return result;
		}
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("userId", userId);
		params.put("status", status);
		userDao.updateStatus(params);
		result.setStatus(0);
		result.setMsg("disabled".equals(status) ? "用户已停用" : "用户已启用");
		return result;
	}

	public NoteResult<Object> deleteUser(String adminId, String userId) {
		NoteResult<Object> result = new NoteResult<Object>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		if (userId == null || userId.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		User target = userDao.findById(userId);
		if (target == null) {
			result.setStatus(2);
			result.setMsg("用户不存在");
			return result;
		}
		if ("admin".equals(target.getCn_user_role())) {
			result.setStatus(3);
			result.setMsg("不能删除管理员账号");
			return result;
		}
		cascadeDeleteUser(userId);
		result.setStatus(0);
		result.setMsg("用户已删除(其笔记本、笔记、分享与投稿记录已同步清理)");
		return result;
	}

	//把普通用户提升为管理员(同时恢复其账号为正常状态, 确保可登录)
	public NoteResult<Object> setAdmin(String adminId, String targetId) {
		NoteResult<Object> result = new NoteResult<Object>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		if (targetId == null || targetId.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		User target = userDao.findById(targetId);
		if (target == null) {
			result.setStatus(2);
			result.setMsg("用户不存在");
			return result;
		}
		if ("admin".equals(target.getCn_user_role())) {
			result.setStatus(3);
			result.setMsg("该账号已经是管理员");
			return result;
		}
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("userId", targetId);
		params.put("role", "admin");
		userDao.updateRole(params);
		params.put("status", "normal");
		userDao.updateStatus(params);
		result.setStatus(0);
		result.setMsg("已将用户 " + target.getCn_user_name() + " 提升为管理员, 可用\"管理员登录\"入口进入后台");
		return result;
	}

	//直接新建管理员账号
	public NoteResult<Object> addAdmin(String adminId, String name, String password, String nick) {
		NoteResult<Object> result = new NoteResult<Object>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		if (name == null || name.trim().isEmpty() || password == null || password.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("用户名和密码不能为空");
			return result;
		}
		if (password.trim().length() < 6) {
			result.setStatus(1);
			result.setMsg("密码长度不能小于6位");
			return result;
		}
		String userName = name.trim();
		User has = userDao.findByName(userName);
		if (has != null) {
			result.setStatus(1);
			result.setMsg("用户名已被占用");
			return result;
		}
		User user = new User();
		user.setCn_user_id(NoteUtil.createId());
		user.setCn_user_name(userName);
		user.setCn_user_password(NoteUtil.md5(password.trim()));
		user.setCn_user_nick((nick == null || nick.trim().isEmpty()) ? "管理员" : nick.trim());
		user.setCn_user_role("admin");
		user.setCn_user_status("normal");
		user.setCn_user_create_time(System.currentTimeMillis());
		userDao.save(user);
		result.setStatus(0);
		result.setMsg("管理员账号 " + userName + " 创建成功, 可用\"管理员登录\"入口进入后台");
		return result;
	}

	//查询当前全部管理员账号(用于后台展示)
	public NoteResult<List<User>> adminList(String adminId) {
		NoteResult<List<User>> result = new NoteResult<List<User>>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		List<User> list = userDao.findAdmins();
		result.setStatus(0);
		result.setMsg("查询成功");
		result.setData(list);
		return result;
	}

	/**
	 * 级联删除用户数据: 分享/活动投稿(引用其笔记)→笔记→笔记本→用户
	 */
	private void cascadeDeleteUser(String userId) {
		List<String> noteIds = new ArrayList<String>();
		List<com.lcz.cloud_note.entity.Book> books = bookDao.findByUserId(userId);
		if (books != null) {
			for (com.lcz.cloud_note.entity.Book book : books) {
				List<com.lcz.cloud_note.entity.Note> notes = noteDao.findByBookId(book.getCn_notebook_id());
				if (notes != null) {
					for (com.lcz.cloud_note.entity.Note note : notes) {
						if (note.getCn_note_id() != null) {
							noteIds.add(note.getCn_note_id());
						}
					}
				}
				bookDao.deleteById(book.getCn_notebook_id());
			}
		}
		if (!noteIds.isEmpty()) {
			shareDao.deleteByNoteIds(noteIds);
			activityDao.deleteNoteActivityByNoteIds(noteIds);
			for (String noteId : noteIds) {
				noteDao.deleteById(noteId);
			}
		}
		userDao.deleteById(userId);
	}

	//================== 分享管理 ==================

	public NoteResult<Map<String,Object>> shareList(String adminId, String keyword, Integer page) {
		NoteResult<Map<String,Object>> result = new NoteResult<Map<String,Object>>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		Map<String,Object> params = new HashMap<String,Object>();
		String kw = (keyword == null) ? "" : keyword.trim();
		if (!kw.isEmpty()) {
			params.put("keyword", "%" + kw + "%");
		}
		int total = shareDao.countAdmin(params);
		int p = (page == null || page < 1) ? 1 : page;
		params.put("begin", (p - 1) * PAGE_SIZE);
		params.put("pageSize", PAGE_SIZE);
		List<Share> rows = shareDao.findAdminPage(params);
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", total);
		data.put("page", p);
		data.put("size", PAGE_SIZE);
		data.put("rows", rows);
		result.setStatus(0);
		result.setMsg("查询成功");
		result.setData(data);
		return result;
	}

	public NoteResult<Object> setShareStatus(String adminId, String shareId, String status) {
		NoteResult<Object> result = new NoteResult<Object>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		if (shareId == null || shareId.trim().isEmpty() || status == null) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		if (!"normal".equals(status) && !"disabled".equals(status)) {
			result.setStatus(1);
			result.setMsg("非法的状态值");
			return result;
		}
		Share share = shareDao.findById(shareId);
		if (share == null) {
			result.setStatus(2);
			result.setMsg("分享不存在");
			return result;
		}
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("shareId", shareId);
		params.put("status", status);
		shareDao.updateStatus(params);
		result.setStatus(0);
		result.setMsg("disabled".equals(status) ? "分享已下架, 前台不再展示" : "分享已恢复上架");
		return result;
	}

	public NoteResult<Object> deleteShare(String adminId, String shareId) {
		NoteResult<Object> result = new NoteResult<Object>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		if (shareId == null || shareId.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		int rows = shareDao.deleteById(shareId);
		if (rows == 0) {
			result.setStatus(2);
			result.setMsg("分享不存在");
			return result;
		}
		result.setStatus(0);
		result.setMsg("分享已删除");
		return result;
	}

	//================== 活动管理 ==================

	public NoteResult<List<Activity>> activityList(String adminId) {
		NoteResult<List<Activity>> result = new NoteResult<List<Activity>>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		List<Activity> list = activityDao.findAll();
		result.setStatus(0);
		result.setMsg("查询成功");
		result.setData(list);
		return result;
	}

	public NoteResult<Object> saveActivity(String adminId, String activityId, String title, String body, String endTime) {
		NoteResult<Object> result = new NoteResult<Object>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		if (title == null || title.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("活动标题不能为空");
			return result;
		}
		Activity activity = new Activity();
		activity.setCn_activity_title(title.trim());
		activity.setCn_activity_body(body);
		//结束时间: 空值表示长期有效; 否则转换为毫秒时间戳
		Long end = null;
		if (endTime != null && !endTime.trim().isEmpty()) {
			try {
				end = Long.valueOf(endTime.trim());
			} catch (NumberFormatException e) {
				result.setStatus(1);
				result.setMsg("活动结束时间格式不正确");
				return result;
			}
		}
		activity.setCn_activity_end_time(end);
		if (activityId == null || activityId.trim().isEmpty()) {
			//新建
			activity.setCn_activity_id(NoteUtil.createId());
			activityDao.saveActivity(activity);
			result.setStatus(0);
			result.setMsg("活动发布成功");
		} else {
			//修改
			activity.setCn_activity_id(activityId.trim());
			int rows = activityDao.updateActivity(activity);
			if (rows == 0) {
				result.setStatus(2);
				result.setMsg("活动不存在");
				return result;
			}
			result.setStatus(0);
			result.setMsg("活动修改成功");
		}
		return result;
	}

	public NoteResult<Object> deleteActivity(String adminId, String activityId) {
		NoteResult<Object> result = new NoteResult<Object>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		if (activityId == null || activityId.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		int rows = activityDao.deleteById(activityId);
		if (rows == 0) {
			result.setStatus(2);
			result.setMsg("活动不存在");
			return result;
		}
		//删除该活动下的全部投稿
		activityDao.deleteNoteActivityByActivityId(activityId);
		result.setStatus(0);
		result.setMsg("活动已删除(该活动下的投稿一并清理)");
		return result;
	}

	//系统: 立即备份数据库(生成完整SQL快照到数据目录/backup)
	public NoteResult<Object> systemBackup(String adminId) {
		NoteResult<Object> result = new NoteResult<Object>();
		if (checkAdmin(adminId) == null) {
			return noPermission();
		}
		try {
			String path = dataBackup.backup(false);
			if (path == null) {
				result.setStatus(1);
				result.setMsg("当前为MySQL数据库, 请使用 mysqldump 备份");
				return result;
			}
			result.setStatus(0);
			result.setMsg("备份完成: " + path);
			return result;
		} catch (Exception e) {
			result.setStatus(5);
			result.setMsg("备份失败: " + e.getMessage());
			return result;
		}
	}
}
