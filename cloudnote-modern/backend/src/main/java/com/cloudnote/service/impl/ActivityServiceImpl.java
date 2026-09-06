package com.cloudnote.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudnote.common.BizException;
import com.cloudnote.entity.Activity;
import com.cloudnote.entity.Book;
import com.cloudnote.entity.Note;
import com.cloudnote.entity.NoteActivity;
import com.cloudnote.entity.User;
import com.cloudnote.mapper.ActivityMapper;
import com.cloudnote.mapper.NoteMapper;
import com.cloudnote.service.ActivityService;
import com.cloudnote.service.CurrentUserService;
import com.cloudnote.service.NotebookService;

@Service
public class ActivityServiceImpl implements ActivityService {

    private static final int PAGE_SIZE = 10;

    private final ActivityMapper activityMapper;
    private final NoteMapper noteMapper;
    private final NotebookService notebookService;
    private final CurrentUserService currentUserService;

    public ActivityServiceImpl(ActivityMapper activityMapper, NoteMapper noteMapper,
                               NotebookService notebookService, CurrentUserService currentUserService) {
        this.activityMapper = activityMapper;
        this.noteMapper = noteMapper;
        this.notebookService = notebookService;
        this.currentUserService = currentUserService;
    }

    @Override
    public List<Activity> listActivities() {
        return activityMapper.findAll();
    }

    @Override
    public Map<String, Object> submissionPage(String activityId, Integer page) {
        if (activityId == null || activityId.trim().isEmpty()) {
            throw new BizException(1, "参数不能为空");
        }
        int p = (page == null || page < 1) ? 1 : page;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("activityId", activityId);
        params.put("begin", (p - 1) * PAGE_SIZE);
        params.put("pageSize", PAGE_SIZE);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("total", activityMapper.countSubmissions(activityId));
        data.put("page", p);
        data.put("size", PAGE_SIZE);
        data.put("rows", activityMapper.findSubmissionPage(params));
        return data;
    }

    @Override
    public NoteActivity submissionDetail(String noteActivityId) {
        NoteActivity na = activityMapper.findSubmissionById(noteActivityId);
        if (na == null) {
            throw new BizException(2, "投稿不存在");
        }
        return na;
    }

    @Override
    @Transactional
    public NoteActivity join(String activityId, String noteId) {
        User user = currentUserService.requireUser();
        if (activityId == null || noteId == null) {
            throw new BizException(1, "参数不能为空");
        }
        Note note = noteMapper.findByNoteId(noteId);
        if (note == null) {
            throw new BizException(2, "笔记不存在");
        }
        if (!user.getCn_user_id().equals(note.getCn_user_id())) {
            throw new BizException(403, "只能投稿自己的笔记");
        }
        NoteActivity na = new NoteActivity();
        na.setCn_note_activity_id(UUID.randomUUID().toString().replace("-", ""));
        na.setCn_activity_id(activityId);
        na.setCn_note_id(noteId);
        na.setCn_note_activity_up(0);
        na.setCn_note_activity_down(0);
        na.setCn_note_activity_title(note.getCn_note_title());
        na.setCn_note_activity_body(note.getCn_note_body());
        activityMapper.insertSubmission(na);
        // 复制一份到我的"活动"笔记本(不存在自动创建)
        Book actionBook = notebookService.ensureSpecial("action", "活动");
        long now = System.currentTimeMillis();
        note.setCn_note_id(UUID.randomUUID().toString().replace("-", ""));
        note.setCn_notebook_id(actionBook.getCn_notebook_id());
        note.setCn_note_create_time(now);
        note.setCn_note_last_modify_time(now);
        noteMapper.insert(note);
        return na;
    }

    @Override
    public void vote(String noteActivityId, boolean up) {
        NoteActivity na = activityMapper.findSubmissionById(noteActivityId);
        if (na == null) {
            throw new BizException(2, "投稿不存在");
        }
        if (up) {
            activityMapper.upSubmission(noteActivityId);
        } else {
            activityMapper.downSubmission(noteActivityId);
        }
    }

    @Override
    @Transactional
    public void collect(String noteActivityId) {
        User user = currentUserService.requireUser();
        NoteActivity na = activityMapper.findSubmissionById(noteActivityId);
        if (na == null) {
            throw new BizException(2, "投稿不存在");
        }
        Book favorites = notebookService.ensureSpecial("favorites", "收藏");
        long now = System.currentTimeMillis();
        Note note = new Note();
        note.setCn_note_id(UUID.randomUUID().toString().replace("-", ""));
        note.setCn_user_id(user.getCn_user_id());
        note.setCn_notebook_id(favorites.getCn_notebook_id());
        note.setCn_note_status_id("1");
        note.setCn_note_type_id("2"); // 2=favor 收藏
        note.setCn_note_title(na.getCn_note_activity_title());
        note.setCn_note_body(na.getCn_note_activity_body());
        note.setCn_note_create_time(now);
        note.setCn_note_last_modify_time(now);
        noteMapper.insert(note);
    }
}
