package com.lcz.cloud_note.service;

import java.util.List;
import java.util.Map;

import com.lcz.cloud_note.entity.Book;
import com.lcz.cloud_note.util.NoteResult;

public interface NoteBookService {
	//查询用户的笔记本列表(用于活动投稿时选择笔记本)
	public NoteResult<List<Book>> findNoteBookList(String userId);
	//查询用户的特殊笔记本(收藏/回收站/活动)，不存在时自动创建，key为类型代码
	public NoteResult<Map<String,Book>> findSpecialBooks(String userId);
	//修改笔记本名称
	public NoteResult<Object> updateBookName(String notebookId, String name);
	//彻底删除笔记本
	public NoteResult<Object> deleteBook(String notebookId);
}
