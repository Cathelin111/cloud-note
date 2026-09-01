package com.lcz.cloud_note.service;

import java.util.List;

import com.lcz.cloud_note.entity.Activity;
import com.lcz.cloud_note.entity.NoteActivity;
import com.lcz.cloud_note.util.NoteResult;

public interface ActivityService {
	//查询所有活动
	public NoteResult<List<Activity>> findActivity();
	//按活动ID分页查询投稿
	public NoteResult<List<NoteActivity>> findNoteActivity(String activityId, Integer currentPage);
	//查询投稿详情
	public NoteResult<NoteActivity> findNoteActivityDetail(String noteActivityId);
	//参加活动(保存投稿)
	public NoteResult<NoteActivity> addNoteActivity(String activityId, String noteId, String userId);
	//顶投稿
	public NoteResult<Object> upNoteActivity(String noteActivityId);
	//踩投稿
	public NoteResult<Object> downNoteActivity(String noteActivityId);
}
