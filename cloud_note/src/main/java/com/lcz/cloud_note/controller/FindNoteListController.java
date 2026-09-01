package com.lcz.cloud_note.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lcz.cloud_note.service.NoteService;
import com.lcz.cloud_note.util.NoteResult;

@Controller
@RequestMapping("/note")
public class FindNoteListController {
	@Resource
	private NoteService noteService;
	
	//查询笔记本中的笔记(活动投稿时选择笔记用)
	@RequestMapping("/findNote.do")
	@ResponseBody
	public NoteResult<List<Map>> execute(String noteBookId){
		NoteResult<List<Map>> result = noteService.findNotesByBookId(noteBookId);
		return result;
	}
}
