package com.cloudnote.service;

import java.util.List;
import java.util.Map;

import com.cloudnote.entity.Activity;
import com.cloudnote.entity.NoteActivity;

public interface ActivityService {

    /** 活动列表(公开) */
    List<Activity> listActivities();

    /** 活动投稿分页(公开), data={total,page,size,rows} */
    Map<String, Object> submissionPage(String activityId, Integer page);

    /** 投稿详情(公开) */
    NoteActivity submissionDetail(String noteActivityId);

    /** 参加活动: 投稿并复制一份到我的"活动"笔记本 */
    NoteActivity join(String activityId, String noteId);

    /** 顶/踩 */
    void vote(String noteActivityId, boolean up);

    /** 收藏活动投稿到我的收藏笔记本 */
    void collect(String noteActivityId);
}
