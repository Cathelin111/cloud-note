package com.lcz.cloud_note.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lcz.cloud_note.dao.ActivityDao;
import com.lcz.cloud_note.dao.BookDao;
import com.lcz.cloud_note.dao.NoteBookTypeDao;
import com.lcz.cloud_note.dao.NoteDao;
import com.lcz.cloud_note.entity.Activity;
import com.lcz.cloud_note.entity.Book;
import com.lcz.cloud_note.entity.Note;
import com.lcz.cloud_note.entity.NoteActivity;
import com.lcz.cloud_note.entity.NoteBookType;
import com.lcz.cloud_note.util.NoteResult;
import com.lcz.cloud_note.util.NoteUtil;

@Service("activityService")//扫描到Spring容器
@Transactional
public class ActivityServiceImpl implements ActivityService {
	//每页显示条数(与原版base.properties的page_size保持一致)
	private static final int PAGE_SIZE = 10;
	
	@Resource
	private ActivityDao activityDao;
	@Resource
	private NoteDao noteDao;
	@Resource
	private BookDao bookDao;
	@Resource
	private NoteBookTypeDao noteBookTypeDao;
	
	//查询所有活动
	public NoteResult<List<Activity>> findActivity() {
		List<Activity> list = activityDao.findAll();
		NoteResult<List<Activity>> result = new NoteResult<List<Activity>>();
		result.setStatus(0);
		result.setMsg("查询活动成功");
		result.setData(list);
		return result;
	}
	
	//按活动ID分页查询投稿
	public NoteResult<List<NoteActivity>> findNoteActivity(String activityId, Integer currentPage) {
		NoteResult<List<NoteActivity>> result = new NoteResult<List<NoteActivity>>();
		if(activityId==null || activityId.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		//当前页码默认第一页
		int page = (currentPage==null || currentPage<1) ? 1 : currentPage;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("activityId", activityId);
		params.put("begin", (page-1)*PAGE_SIZE);
		params.put("pageSize", PAGE_SIZE);
		List<NoteActivity> list = activityDao.findNoteActivityByPage(params);
		result.setStatus(0);
		result.setMsg("查询投稿成功");
		result.setData(list);
		return result;
	}
	
	//查询投稿详情
	public NoteResult<NoteActivity> findNoteActivityDetail(String noteActivityId) {
		NoteResult<NoteActivity> result = new NoteResult<NoteActivity>();
		if(noteActivityId==null || noteActivityId.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		NoteActivity noteActivity = activityDao.findNoteActivityById(noteActivityId);
		if(noteActivity==null) {
			result.setStatus(2);
			result.setMsg("投稿不存在");
			return result;
		}
		result.setStatus(0);
		result.setMsg("查询投稿详情成功");
		result.setData(noteActivity);
		return result;
	}
	
	//参加活动(保存投稿)
	public NoteResult<NoteActivity> addNoteActivity(String activityId, String noteId, String userId) {
		NoteResult<NoteActivity> result = new NoteResult<NoteActivity>();
		if(activityId==null || noteId==null || userId==null) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		//查询笔记信息
		Note note = noteDao.findByNoteId(noteId);
		if(note==null) {
			result.setStatus(2);
			result.setMsg("笔记不存在");
			return result;
		}
		//构建投稿记录
		NoteActivity noteActivity = new NoteActivity();
		noteActivity.setCn_note_activity_id(NoteUtil.createId());//主键
		noteActivity.setCn_activity_id(activityId);//活动ID
		noteActivity.setCn_note_id(noteId);//笔记ID
		noteActivity.setCn_note_activity_up(0);//初始顶数
		noteActivity.setCn_note_activity_down(0);//初始踩数
		noteActivity.setCn_note_activity_title(note.getCn_note_title());//投稿标题
		noteActivity.setCn_note_activity_body(note.getCn_note_body());//投稿内容
		//保存投稿记录
		activityDao.saveNoteActivity(noteActivity);
		//将笔记复制一份到用户的"活动"笔记本(不存在则自动创建)
		Book actionBook = getOrCreateSpecialBook(userId, "action", "活动");
		if(actionBook!=null) {
			note.setCn_note_id(NoteUtil.createId());//复制笔记使用新主键
			note.setCn_notebook_id(actionBook.getCn_notebook_id());
			long now = System.currentTimeMillis();
			note.setCn_note_create_time(now);
			note.setCn_note_last_modify_time(now);
			noteDao.save(note);
		}
		result.setStatus(0);
		result.setMsg("参加活动成功");
		result.setData(noteActivity);
		return result;
	}
	
	//顶投稿
	public NoteResult<Object> upNoteActivity(String noteActivityId) {
		return updateVote(noteActivityId, true);
	}
	
	//踩投稿
	public NoteResult<Object> downNoteActivity(String noteActivityId) {
		return updateVote(noteActivityId, false);
	}
	
	//顶/踩公共处理
	private NoteResult<Object> updateVote(String noteActivityId, boolean up) {
		NoteResult<Object> result = new NoteResult<Object>();
		if(noteActivityId==null || noteActivityId.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		NoteActivity noteActivity = activityDao.findNoteActivityById(noteActivityId);
		if(noteActivity==null) {
			result.setStatus(2);
			result.setMsg("投稿不存在");
			return result;
		}
		if(up) {
			Integer count = noteActivity.getCn_note_activity_up();
			noteActivity.setCn_note_activity_up(count==null?1:count+1);
		}else {
			Integer count = noteActivity.getCn_note_activity_down();
			noteActivity.setCn_note_activity_down(count==null?1:count+1);
		}
		activityDao.updateNoteActivity(noteActivity);
		result.setStatus(0);
		result.setMsg(up?"顶成功":"踩成功");
		return result;
	}
	
	/**
	 * 获取用户指定类型的特殊笔记本；不存在时根据类型字典自动创建
	 * @param userId 用户ID
	 * @param code 笔记本类型代码(favorites/recycle/action/push)
	 * @param name 自动创建时使用的笔记本名称
	 */
	private Book getOrCreateSpecialBook(String userId, String code, String name) {
		List<Book> specialBooks = bookDao.findSpecialByUserId(userId);
		for(Book book : specialBooks) {
			if(code.equals(book.getCn_notebook_type_code())) {
				return book;
			}
		}
		//未找到，查询类型字典并创建
		NoteBookType type = noteBookTypeDao.findByCode(code);
		if(type==null) {
			return null;
		}
		Book book = new Book();
		book.setCn_notebook_id(NoteUtil.createId());
		book.setCn_user_id(userId);
		book.setCn_notebook_type_id(type.getCn_notebook_type_id());
		book.setCn_notebook_name(name);
		book.setCn_notebook_createtime(new Timestamp(System.currentTimeMillis()));
		bookDao.save(book);
		return book;
	}
}
