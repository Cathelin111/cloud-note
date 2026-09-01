package com.lcz.cloud_note.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.lcz.cloud_note.dao.BookDao;
import com.lcz.cloud_note.dao.NoteBookTypeDao;
import com.lcz.cloud_note.entity.Book;
import com.lcz.cloud_note.entity.NoteBookType;
import com.lcz.cloud_note.util.NoteResult;
import com.lcz.cloud_note.util.NoteUtil;

@Service("noteBookService")//扫描到Spring容器
public class NoteBookServiceImpl implements NoteBookService {
	@Resource
	private BookDao bookDao;
	@Resource
	private NoteBookTypeDao noteBookTypeDao;
	
	//查询用户的笔记本列表(用于活动投稿时选择笔记本)
	public NoteResult<List<Book>> findNoteBookList(String userId) {
		NoteResult<List<Book>> result = new NoteResult<List<Book>>();
		if(userId==null || userId.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		List<Book> books = bookDao.findByUserId(userId);
		result.setStatus(0);
		result.setMsg("查询笔记本成功");
		result.setData(books);
		return result;
	}
	
	//查询用户的特殊笔记本(收藏/回收站/活动)，不存在时自动创建，key为类型代码
	public NoteResult<Map<String,Book>> findSpecialBooks(String userId) {
		NoteResult<Map<String,Book>> result = new NoteResult<Map<String,Book>>();
		if(userId==null || userId.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		Map<String,Book> specialBooks = new HashMap<String,Book>();
		//依次确保收藏/回收站/活动三个特殊笔记本存在
		specialBooks.put("favorites", getOrCreateSpecialBook(userId, "favorites", "收藏"));
		specialBooks.put("recycle", getOrCreateSpecialBook(userId, "recycle", "回收站"));
		specialBooks.put("action", getOrCreateSpecialBook(userId, "action", "活动"));
		result.setStatus(0);
		result.setMsg("查询特殊笔记本成功");
		result.setData(specialBooks);
		return result;
	}
	
	//修改笔记本名称
	public NoteResult<Object> updateBookName(String notebookId, String name) {
		NoteResult<Object> result = new NoteResult<Object>();
		if(notebookId==null || name==null || name.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		bookDao.updateName(notebookId, name);
		result.setStatus(0);
		result.setMsg("重命名成功");
		return result;
	}
	
	//彻底删除笔记本
	public NoteResult<Object> deleteBook(String notebookId) {
		NoteResult<Object> result = new NoteResult<Object>();
		if(notebookId==null || notebookId.trim().isEmpty()) {
			result.setStatus(1);
			result.setMsg("参数不能为空");
			return result;
		}
		bookDao.deleteById(notebookId);
		result.setStatus(0);
		result.setMsg("删除笔记本成功");
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
