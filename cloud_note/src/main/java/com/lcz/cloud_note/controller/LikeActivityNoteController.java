package com.lcz.cloud_note.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lcz.cloud_note.service.NoteService;
import com.lcz.cloud_note.util.NoteResult;

@Controller
@RequestMapping("/note")
public class LikeActivityNoteController {
	@Resource
	private NoteService noteService;
	
	//收藏活动投稿笔记
	@RequestMapping("/likeActivityNote.do")
	@ResponseBody
	public NoteResult<Object> execute(String noteActivityId, String userId){
		NoteResult<Object> result = noteService.likeActivityNote(noteActivityId, userId);
		return result;
	}
}
