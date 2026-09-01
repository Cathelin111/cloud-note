package com.lcz.cloud_note.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lcz.cloud_note.service.NoteService;
import com.lcz.cloud_note.util.NoteResult;

@Controller
@RequestMapping("/note")
public class LikeShareNoteController {
	@Resource
	private NoteService noteService;
	
	//收藏分享的笔记
	@RequestMapping("/likeShareNote.do")
	@ResponseBody
	public NoteResult<Object> execute(String shareId, String userId){
		NoteResult<Object> result = noteService.likeShareNote(shareId, userId);
		return result;
	}
}
