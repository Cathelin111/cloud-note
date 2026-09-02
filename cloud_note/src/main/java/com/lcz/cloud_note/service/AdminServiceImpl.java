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

	/**
	 * 级联删除用户数据: 分享/活动投稿(引用其笔记)→笔记→笔记本→用户
	 */
	private void cascadeDeleteUser(String userId) {
		List<String> noteIds = new ArrayList<String>();
		List<com.lcz.cloud_note.entity.Book> books = bookDao.findByUserId(userId);
		if (books != null) {
			for (com.lcz.cloud_note.entity.Book book : books) {
				List<Map> notes = noteDao.findByBookId(book.getCn_notebook_id());
				if (notes != null) {
					for (Map note : notes) {
						if (note.get("cn_note_id") != null) {
							noteIds.add((String) note.get("cn_note_id"));
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
}
