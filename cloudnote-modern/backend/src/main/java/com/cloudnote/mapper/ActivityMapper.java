package com.cloudnote.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.cloudnote.entity.Activity;
import com.cloudnote.entity.NoteActivity;

public interface ActivityMapper {

    List<Activity> findAll();

    /** 活动投稿分页 */
    List<NoteActivity> findSubmissionPage(Map<String, Object> params);

    /** 活动投稿总数 */
    int countSubmissions(@Param("activityId") String activityId);

    NoteActivity findSubmissionById(@Param("noteActivityId") String noteActivityId);

    int insertSubmission(NoteActivity noteActivity);

    int upSubmission(@Param("noteActivityId") String noteActivityId);

    int downSubmission(@Param("noteActivityId") String noteActivityId);

    //================= 管理员后台(活动维护/级联) =================

    int insertActivity(Activity activity);

    int updateActivity(Activity activity);

    int deleteActivityById(@Param("activityId") String activityId);

    int deleteSubmissionByActivityId(@Param("activityId") String activityId);

    /** 删除引用这些笔记的投稿(级联删用户) */
    int deleteSubmissionByNoteIds(List<String> noteIds);
}
