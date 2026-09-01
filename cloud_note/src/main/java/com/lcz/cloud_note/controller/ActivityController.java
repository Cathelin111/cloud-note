package com.lcz.cloud_note.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lcz.cloud_note.entity.Activity;
import com.lcz.cloud_note.entity.NoteActivity;
import com.lcz.cloud_note.service.ActivityService;
import com.lcz.cloud_note.util.NoteResult;

@Controller
@RequestMapping("/activity")
public class ActivityController {
	@Resource
	private ActivityService activityService;
	
	//查询所有活动
	@RequestMapping("/findActivity.do")
	@ResponseBody
	public NoteResult<List<Activity>> findActivity(){
		NoteResult<List<Activity>> result = activityService.findActivity();
		return result;
	}
	
	//按活动ID分页查询投稿
	@RequestMapping("/findNoteActivity.do")
	@ResponseBody
	public NoteResult<List<NoteActivity>> findNoteActivity(String activityId, Integer currentPage){
		NoteResult<List<NoteActivity>> result = activityService.findNoteActivity(activityId, currentPage);
		return result;
	}
	
	//查询投稿详情
	@RequestMapping("/findNoteActivityDetail.do")
	@ResponseBody
	public NoteResult<NoteActivity> findNoteActivityDetail(String noteActivityId){
		NoteResult<NoteActivity> result = activityService.findNoteActivityDetail(noteActivityId);
		return result;
	}
	
	//参加活动(保存投稿)
	@RequestMapping("/addNoteActivity.do")
	@ResponseBody
	public NoteResult<NoteActivity> addNoteActivity(String activityId, String noteId, String userId){
		NoteResult<NoteActivity> result = activityService.addNoteActivity(activityId, noteId, userId);
		return result;
	}
	
	//顶投稿
	@RequestMapping("/upNoteActivity.do")
	@ResponseBody
	public NoteResult<Object> upNoteActivity(String noteActivityId){
		NoteResult<Object> result = activityService.upNoteActivity(noteActivityId);
		return result;
	}
	
	//踩投稿
	@RequestMapping("/downNoteActivity.do")
	@ResponseBody
	public NoteResult<Object> downNoteActivity(String noteActivityId){
		NoteResult<Object> result = activityService.downNoteActivity(noteActivityId);
		return result;
	}
}
