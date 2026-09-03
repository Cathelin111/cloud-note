package com.lcz.cloud_note.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lcz.cloud_note.entity.Share;
import com.lcz.cloud_note.service.ShareService;
import com.lcz.cloud_note.util.NoteResult;

@Controller
@RequestMapping("/share")
public class ShareSearchController {
	@Resource
	private ShareService shareService;
	
	//搜索分享笔记(按标题模糊, 返回全部命中; 兼容旧调用)
	@RequestMapping("/search.do")
	@ResponseBody
	public NoteResult<List<Share>> execute(String keyword){
		NoteResult<List<Share>> result = shareService.searchNote(keyword);
		return result;
	}
	
	//分享广场搜索(标题或正文命中, 服务端分页)
	//data = {total, page, size, rows:[Share...]}
	@RequestMapping("/searchPage.do")
	@ResponseBody
	public NoteResult<Map<String,Object>> searchPage(String keyword, Integer page){
		NoteResult<Map<String,Object>> result = shareService.searchPage(keyword, page);
		return result;
	}
}
