package com.cloudnote.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudnote.common.BizException;
import com.cloudnote.entity.Book;
import com.cloudnote.entity.Note;
import com.cloudnote.entity.Share;
import com.cloudnote.entity.User;
import com.cloudnote.mapper.NoteMapper;
import com.cloudnote.mapper.ShareMapper;
import com.cloudnote.service.CurrentUserService;
import com.cloudnote.service.NotebookService;
import com.cloudnote.service.ShareService;

@Service
public class ShareServiceImpl implements ShareService {

    private static final int PAGE_SIZE = 10;

    private final ShareMapper shareMapper;
    private final NoteMapper noteMapper;
    private final NotebookService notebookService;
    private final CurrentUserService currentUserService;

    public ShareServiceImpl(ShareMapper shareMapper, NoteMapper noteMapper,
                            NotebookService notebookService, CurrentUserService currentUserService) {
        this.shareMapper = shareMapper;
        this.noteMapper = noteMapper;
        this.notebookService = notebookService;
        this.currentUserService = currentUserService;
    }

    @Override
    public Map<String, Object> publicPage(String keyword, Integer page) {
        String kw = (keyword == null) ? "" : keyword.trim();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("keyword", "%" + kw + "%");
        int total = shareMapper.countPublic(params);
        int p = (page == null || page < 1) ? 1 : page;
        params.put("begin", (p - 1) * PAGE_SIZE);
        params.put("pageSize", PAGE_SIZE);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("page", p);
        data.put("size", PAGE_SIZE);
        data.put("rows", shareMapper.findPublicPage(params));
        return data;
    }

    @Override
    public Share publicDetail(String shareId) {
        Share share = shareMapper.findDetailById(shareId);
        if (share == null) {
            throw new BizException(1, "分享不存在或已被删除");
        }
        if (!"normal".equals(share.getCn_share_status())) {
            throw new BizException(2, "该分享已被管理员下架");
        }
        return share;
    }

    @Override
    @Transactional
    public void shareMyNote(String noteId) {
        User user = currentUserService.requireUser();
        Note note = noteMapper.findByNoteId(noteId);
        if (note == null) {
            throw new BizException(2, "笔记不存在");
        }
        if (!user.getCn_user_id().equals(note.getCn_user_id())) {
            throw new BizException(403, "只能分享自己的笔记");
        }
        Share share = new Share();
        share.setCn_share_id(UUID.randomUUID().toString().replace("-", ""));
        share.setCn_note_id(noteId);
        share.setCn_share_title(note.getCn_note_title());
        share.setCn_share_body(note.getCn_note_body());
        shareMapper.insert(share);
    }

    @Override
    @Transactional
    public void collect(String shareId) {
        User user = currentUserService.requireUser();
        Share share = shareMapper.findById(shareId);
        if (share == null) {
            throw new BizException(2, "分享不存在");
        }
        if (!"normal".equals(share.getCn_share_status())) {
            throw new BizException(3, "该分享已被管理员下架, 无法收藏");
        }
        Book favorites = notebookService.ensureSpecial("favorites", "收藏");
        long now = System.currentTimeMillis();
        Note note = new Note();
        note.setCn_note_id(UUID.randomUUID().toString().replace("-", ""));
        note.setCn_user_id(user.getCn_user_id());
        note.setCn_notebook_id(favorites.getCn_notebook_id());
        note.setCn_note_status_id("1");
        note.setCn_note_type_id("2"); // 2=favor 收藏
        note.setCn_note_title(share.getCn_share_title());
        note.setCn_note_body(share.getCn_share_body());
        note.setCn_note_create_time(now);
        note.setCn_note_last_modify_time(now);
        noteMapper.insert(note);
    }
}
