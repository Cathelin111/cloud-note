package com.lcz.cloud_note.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lcz.cloud_note.entity.Note;
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
	public NoteResult<List<Note>> execute(String noteBookId){
		NoteResult<List<Note>> result = noteService.findNotesByBookId(noteBookId);
		return result;
	}
}
