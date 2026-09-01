package com.lcz.cloud_note.dao;

import java.util.List;
import java.util.Map;

import com.lcz.cloud_note.entity.Activity;
import com.lcz.cloud_note.entity.NoteActivity;

public interface ActivityDao {
	//查询所有活动
	public List<Activity> findAll();
	//按活动ID分页查询投稿
	public List<NoteActivity> findNoteActivityByPage(Map<String,Object> params);
	//按投稿ID查询投稿
	public NoteActivity findNoteActivityById(String id);
	//保存投稿(参加活动)
	public void saveNoteActivity(NoteActivity noteActivity);
	//更新投稿(顶/踩)
	public void updateNoteActivity(NoteActivity noteActivity);
}
