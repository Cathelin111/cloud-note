package com.lcz.cloud_note.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lcz.cloud_note.entity.Book;
import com.lcz.cloud_note.service.NoteBookService;
import com.lcz.cloud_note.util.NoteResult;

@Controller
@RequestMapping("/notebook")
public class NoteBookController {
	@Resource
	private NoteBookService noteBookService;
	
	//查询笔记本列表(活动投稿时选择笔记本用)
	@RequestMapping("/findList.do")
	@ResponseBody
	public NoteResult<List<Book>> findList(String userId){
		NoteResult<List<Book>> result = noteBookService.findNoteBookList(userId);
		return result;
	}
	
	//查询特殊笔记本(收藏/回收站/活动)，不存在时自动创建
	@RequestMapping("/findSpecial.do")
	@ResponseBody
	public NoteResult<Map<String,Book>> findSpecial(String userId){
		NoteResult<Map<String,Book>> result = noteBookService.findSpecialBooks(userId);
		return result;
	}
	
	//修改笔记本名称
	@RequestMapping("/updateName.do")
	@ResponseBody
	public NoteResult<Object> updateName(String notebookId, String name){
		NoteResult<Object> result = noteBookService.updateBookName(notebookId, name);
		return result;
	}
	
	//彻底删除笔记本
	@RequestMapping("/delete.do")
	@ResponseBody
	public NoteResult<Object> delete(String notebookId){
		NoteResult<Object> result = noteBookService.deleteBook(notebookId);
		return result;
	}
}
