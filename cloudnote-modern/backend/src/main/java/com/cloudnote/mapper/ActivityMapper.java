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
}
