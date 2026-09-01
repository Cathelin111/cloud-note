package com.lcz.cloud_note.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lcz.cloud_note.service.NoteService;
import com.lcz.cloud_note.util.NoteResult;

@Controller
@RequestMapping("/note")
public class DeleteRecycleNoteController {
	@Resource
	private NoteService noteService;
	
	//彻底删除笔记(回收站物理删除)
	@RequestMapping("/deleteRecycle.do")
	@ResponseBody
	public NoteResult execute(String noteId){
		NoteResult result = noteService.deleteRecycleNote(noteId);
		return result;
	}
}
