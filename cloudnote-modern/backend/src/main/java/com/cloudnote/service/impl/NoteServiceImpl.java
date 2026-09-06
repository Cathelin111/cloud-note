package com.cloudnote.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudnote.common.BizException;
import com.cloudnote.entity.Book;
import com.cloudnote.entity.Note;
import com.cloudnote.entity.User;
import com.cloudnote.mapper.NoteMapper;
import com.cloudnote.mapper.NotebookMapper;
import com.cloudnote.service.CurrentUserService;
import com.cloudnote.service.NoteService;
import com.cloudnote.service.NotebookService;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final NotebookMapper notebookMapper;
    private final NotebookService notebookService;
    private final CurrentUserService currentUserService;

    public NoteServiceImpl(NoteMapper noteMapper, NotebookMapper notebookMapper,
                           NotebookService notebookService, CurrentUserService currentUserService) {
        this.noteMapper = noteMapper;
        this.notebookMapper = notebookMapper;
        this.notebookService = notebookService;
        this.currentUserService = currentUserService;
    }

    @Override
    public List<Note> listByBook(String bookId) {
        requireOwnedBook(bookId);
        return noteMapper.findByBookId(bookId);
    }

    @Override
    public Note detail(String noteId) {
        Note note = requireOwnedNote(noteId);
        return note;
    }

    @Override
    @Transactional
    public Note create(String bookId, String title) {
        User user = currentUserService.requireUser();
        requireOwnedBook(bookId);
        long now = System.currentTimeMillis();
        Note note = new Note();
        note.setCn_note_id(UUID.randomUUID().toString().replace("-", ""));
        note.setCn_notebook_id(bookId);
        note.setCn_user_id(user.getCn_user_id());
        note.setCn_note_status_id("1");
        note.setCn_note_type_id("1");
        note.setCn_note_title(title.trim());
        note.setCn_note_body("");
        note.setCn_note_create_time(now);
        note.setCn_note_last_modify_time(now);
        noteMapper.insert(note);
        return note;
    }

    @Override
    public void update(String noteId, String title, String body) {
        requireOwnedNote(noteId);
        noteMapper.updateContent(noteId, title.trim(), body == null ? "" : body, System.currentTimeMillis());
    }

    @Override
    @Transactional
    public void softDelete(String noteId) {
        Note note = requireOwnedNote(noteId);
        if (isRecycleBook(note)) {
            throw new BizException(1, "笔记已在回收站");
        }
        Book recycle = notebookService.ensureSpecial("recycle", "回收站");
        noteMapper.move(noteId, recycle.getCn_notebook_id());
    }

    @Override
    public void move(String noteId, String bookId) {
        Note note = requireOwnedNote(noteId);
        requireOwnedBook(bookId);
        noteMapper.move(noteId, bookId);
    }

    @Override
    public void permanentDelete(String noteId) {
        Note note = requireOwnedNote(noteId);
        if (!isRecycleBook(note)) {
            throw new BizException(1, "只能彻底删除回收站中的笔记");
        }
        noteMapper.deleteById(noteId);
    }

    private boolean isRecycleBook(Note note) {
        Book book = notebookMapper.findById(note.getCn_notebook_id());
        return book != null && "recycle".equals(book.getCn_notebook_type_code());
    }

    private Book requireOwnedBook(String bookId) {
        User user = currentUserService.requireUser();
        Book book = notebookMapper.findById(bookId);
        if (book == null) {
            throw new BizException(2, "笔记本不存在");
        }
        if (!user.getCn_user_id().equals(book.getCn_user_id())) {
            throw new BizException(403, "无权操作他人的笔记本");
        }
        return book;
    }

    private Note requireOwnedNote(String noteId) {
        User user = currentUserService.requireUser();
        Note note = noteMapper.findByNoteId(noteId);
        if (note == null) {
            throw new BizException(2, "笔记不存在");
        }
        if (!user.getCn_user_id().equals(note.getCn_user_id())) {
            throw new BizException(403, "无权操作他人的笔记");
        }
        return note;
    }
}
