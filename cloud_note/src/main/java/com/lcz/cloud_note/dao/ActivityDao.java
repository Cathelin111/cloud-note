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
	//后台: 新建活动
	public void saveActivity(Activity activity);
	//后台: 修改活动
	public int updateActivity(Activity activity);
	//后台: 删除活动
	public int deleteById(String activityId);
	//后台: 删除某活动下的全部投稿
	public int deleteNoteActivityByActivityId(String activityId);
	//删除引用指定笔记列表的投稿(删除用户时级联清理)
	public int deleteNoteActivityByNoteIds(List<String> noteIds);
	//查询引用指定笔记列表的投稿(数据导出用)
	public List<NoteActivity> findNoteActivityByNoteIds(List<String> noteIds);
}
