package com.lcz.cloud_note.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;


import com.lcz.cloud_note.dao.ActivityDao;
import com.lcz.cloud_note.dao.BookDao;
import com.lcz.cloud_note.dao.NoteBookTypeDao;
import com.lcz.cloud_note.dao.NoteDao;
import com.lcz.cloud_note.dao.ShareDao;
import com.lcz.cloud_note.entity.Book;
import com.lcz.cloud_note.entity.Note;
import com.lcz.cloud_note.entity.NoteActivity;
import com.lcz.cloud_note.entity.NoteBookType;
import com.lcz.cloud_note.entity.Share;
import com.lcz.cloud_note.util.NoteResult;
import com.lcz.cloud_note.util.NoteUtil;
@Service("noteService")//扫描到spring容器里面
public class NoteServiceImpl implements NoteService {
	@Resource
	private NoteDao noteDao;
	@Resource ShareDao shareDao;
	@Resource
	private ActivityDao activityDao;
	@Resource
	private BookDao bookDao;
	@Resource
	private NoteBookTypeDao noteBookTypeDao;
	
	public NoteResult<List<Map>> loadBookNotes(String bookId) {
		//返回数据集合
		List<Map> list = noteDao.findByBookId(bookId);
		//构建result
		NoteResult<List<Map>> result=new NoteResult<List<Map>>();
		result.setStatus(0);
		result.setMsg("加载笔记成功");
		result.setData(list);
		return result;
	}
	
	//单击笔记,加载笔记相关信息
	public NoteResult<Note> loadNote(String noteId) {
		//返回数据集合
		Note note = noteDao.findByNoteId(noteId);
		//构建result
		NoteResult<Note> result = new NoteResult<Note>();
		if(note==null) {
			result.setMsg("未找到数据!");
			result.setStatus(1);
			return result;
		}else {
			result.setStatus(0);
			result.setMsg("加载笔记信息成功");
			result.setData(note);
			return result;
		}	
		
	}
	
	//更新笔记信息（保存笔记）事件
	public NoteResult<Object> updateNote(String noteId, String title, String body) {
		//创建note参数
		Note note=new Note();
		note.setCn_note_id(noteId);
		note.setCn_note_title(title);
		note.setCn_note_body(body);
		long time = System.currentTimeMillis();
		note.setCn_note_last_modify_time(time);
		//更新数据库记录
		int rows = noteDao.updateNote(note);
		//构建result
		NoteResult<Object> result = new NoteResult<Object>();
		if(rows==1) {
			result.setStatus(0);
			result.setMsg("保存笔记成功");
			return result;
		}else {
			result.setStatus(1);
			result.setMsg("保存笔记失败");
			return result;
		}
	}
	//增加笔记事件
	public NoteResult<Note> addNote(String userId, String bookId, String title) {
		Note note=new Note();
		//用户ID
		note.setCn_user_id(userId);
		//笔记本ID
		note.setCn_notebook_id(bookId);
		//笔记本标题
		note.setCn_note_title(title);
		//笔记Id
		String noteId=NoteUtil.createId();
		note.setCn_note_id(noteId);
		//笔记内容
		note.setCn_note_body("");
		//创建时间
		long time = System.currentTimeMillis();
		note.setCn_note_create_time(time);
		//最后修改事件
		note.setCn_note_last_modify_time(time);
		//约定1-normal 2-delete
		note.setCn_note_status_id("1");
		//约定1-normal 2-favor 3-share
		note.setCn_note_type_id("1");
		noteDao.save(note);
		NoteResult<Note> result = new NoteResult<Note>();
		result.setStatus(0);
		result.setMsg("创建笔记成功");
		result.setData(note);
		return result;
	}
	//删除笔记事件(将笔记移入回收站笔记本)
	public NoteResult deleteNote(String noteId) {
		if(noteId==null) {
			NoteResult result = new NoteResult();
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		//查询笔记，获得所属用户
		Note note = noteDao.findByNoteId(noteId);
		if(note==null) {
			NoteResult result = new NoteResult();
			result.setStatus(2);
			result.setMsg("笔记不存在");
			return result;
		}
		//获取用户的回收站笔记本(不存在则自动创建)
		Book recycleBook = getOrCreateSpecialBook(note.getCn_user_id(), "recycle", "回收站");
		if(recycleBook==null) {
			NoteResult result = new NoteResult();
			result.setStatus(3);
			result.setMsg("回收站笔记本创建失败");
			return result;
		}
		//把笔记移入回收站
		Note temp = new Note();
		temp.setCn_note_id(noteId);
		temp.setCn_notebook_id(recycleBook.getCn_notebook_id());
		int rows = noteDao.dynamicUpdate(temp);
		//创建返回结果
		NoteResult result = new NoteResult();
		if(rows >= 1){//成功
			result.setStatus(0);
			result.setMsg("删除笔记成功");
		}else{
			result.setStatus(1);
			result.setMsg("删除笔记失败");
		}
		return result;
	}
	
	//彻底删除笔记(回收站物理删除)
	public NoteResult deleteRecycleNote(String noteId) {
		NoteResult result = new NoteResult();
		if(noteId==null || noteId.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		noteDao.deleteById(noteId);
		result.setStatus(0);
		result.setMsg("彻底删除成功");
		return result;
	}
	
	//收藏分享的笔记
	public NoteResult<Object> likeShareNote(String shareId, String userId) {
		NoteResult<Object> result = new NoteResult<Object>();
		if(shareId==null || userId==null) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		Share share = shareDao.findById(shareId);
		if(share==null) {
			result.setStatus(2);
			result.setMsg("分享不存在");
			return result;
		}
		//被管理员下架的分享不允许收藏
		if(!"normal".equals(share.getCn_share_status())) {
			result.setStatus(3);
			result.setMsg("该分享已被管理员下架, 无法收藏");
			return result;
		}
		//获取用户的收藏笔记本(不存在则自动创建)
		Book favoritesBook = getOrCreateSpecialBook(userId, "favorites", "收藏");
		if(favoritesBook==null) {
			result.setStatus(3);
			result.setMsg("收藏笔记本创建失败");
			return result;
		}
		//将分享内容保存为一条笔记到收藏笔记本
		Note note = new Note();
		note.setCn_note_id(NoteUtil.createId());
		note.setCn_user_id(userId);
		note.setCn_notebook_id(favoritesBook.getCn_notebook_id());
		note.setCn_note_status_id("1");//约定1-normal
		note.setCn_note_type_id("2");//约定2-favor
		note.setCn_note_title(share.getCn_share_title());
		note.setCn_note_body(share.getCn_share_body());
		long now = System.currentTimeMillis();
		note.setCn_note_create_time(now);
		note.setCn_note_last_modify_time(now);
		noteDao.save(note);
		result.setStatus(0);
		result.setMsg("收藏成功");
		return result;
	}
	//转移笔记事件
	public NoteResult<Object> moveNote(String noteId, String bookId) {
		Note note = new Note();
		note.setCn_note_id(noteId);//设置笔记ID
		note.setCn_notebook_id(bookId);//设置笔记本ID
//		int rows = 
//			noteDao.updateBookId(note);//更新
		int rows = noteDao.dynamicUpdate(note);
		//创建返回结果
		NoteResult result = new NoteResult();
		if(rows>=1){
			result.setStatus(0);
			result.setMsg("转移笔记成功");
		}else{
			result.setStatus(1);
			result.setMsg("转移笔记失败");
		}
		return result;
	}
	
	//查询笔记本中的笔记(活动投稿时选择笔记用)
	public NoteResult<List<Map>> findNotesByBookId(String bookId) {
		List<Map> list = noteDao.findByBookId(bookId);
		NoteResult<List<Map>> result = new NoteResult<List<Map>>();
		result.setStatus(0);
		result.setMsg("加载笔记成功");
		result.setData(list);
		return result;
	}
	
	//收藏活动投稿笔记
	public NoteResult<Object> likeActivityNote(String noteActivityId, String userId) {
		NoteResult<Object> result = new NoteResult<Object>();
		if(noteActivityId==null || userId==null) {
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
		//获取用户的收藏笔记本(不存在则自动创建)
		Book favoritesBook = getOrCreateSpecialBook(userId, "favorites", "收藏");
		if(favoritesBook==null) {
			result.setStatus(3);
			result.setMsg("收藏笔记本创建失败");
			return result;
		}
		//将投稿内容保存为一条笔记到收藏笔记本
		Note note = new Note();
		note.setCn_note_id(NoteUtil.createId());
		note.setCn_user_id(userId);
		note.setCn_notebook_id(favoritesBook.getCn_notebook_id());
		note.setCn_note_status_id("1");//约定1-normal
		note.setCn_note_type_id("2");//约定2-favor
		note.setCn_note_title(noteActivity.getCn_note_activity_title());
		note.setCn_note_body(noteActivity.getCn_note_activity_body());
		long now = System.currentTimeMillis();
		note.setCn_note_create_time(now);
		note.setCn_note_last_modify_time(now);
		noteDao.save(note);
		result.setStatus(0);
		result.setMsg("收藏成功");
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
