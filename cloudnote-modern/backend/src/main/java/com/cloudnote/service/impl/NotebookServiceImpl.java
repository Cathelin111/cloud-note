package com.cloudnote.service.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudnote.common.BizException;
import com.cloudnote.entity.Book;
import com.cloudnote.entity.NoteBookType;
import com.cloudnote.entity.User;
import com.cloudnote.mapper.NoteBookTypeMapper;
import com.cloudnote.mapper.NoteMapper;
import com.cloudnote.mapper.NotebookMapper;
import com.cloudnote.service.CurrentUserService;
import com.cloudnote.service.NotebookService;

@Service
public class NotebookServiceImpl implements NotebookService {

    private final NotebookMapper notebookMapper;
    private final NoteMapper noteMapper;
    private final NoteBookTypeMapper typeMapper;
    private final CurrentUserService currentUserService;

    public NotebookServiceImpl(NotebookMapper notebookMapper, NoteMapper noteMapper,
                               NoteBookTypeMapper typeMapper, CurrentUserService currentUserService) {
        this.notebookMapper = notebookMapper;
        this.noteMapper = noteMapper;
        this.typeMapper = typeMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public List<Book> listNormal() {
        User user = currentUserService.requireUser();
        return notebookMapper.findNormalByUserId(user.getCn_user_id());
    }

    @Override
    @Transactional
    public Book create(String title) {
        User user = currentUserService.requireUser();
        NoteBookType normal = typeMapper.findByCode("normal");
        if (normal == null) {
            throw new BizException(500, "笔记本类型字典缺失(normal)");
        }
        Book book = new Book();
        book.setCn_notebook_id(UUID.randomUUID().toString().replace("-", ""));
        book.setCn_user_id(user.getCn_user_id());
        book.setCn_notebook_type_id(normal.getCn_notebook_type_id());
        book.setCn_notebook_name(title.trim());
        book.setCn_notebook_createtime(new Timestamp(System.currentTimeMillis()));
        notebookMapper.insert(book);
        return book;
    }

    @Override
    public Map<String, Book> special() {
        User user = currentUserService.requireUser();
        Map<String, Book> map = new HashMap<String, Book>();
        map.put("favorites", ensureSpecial("favorites", "收藏"));
        map.put("recycle", ensureSpecial("recycle", "回收站"));
        map.put("action", ensureSpecial("action", "活动"));
        return map;
    }

    @Override
    @Transactional
    public Book ensureSpecial(String code, String displayName) {
        User user = currentUserService.requireUser();
        List<Book> books = notebookMapper.findAllByUserId(user.getCn_user_id());
        for (Book book : books) {
            if (code.equals(book.getCn_notebook_type_code())) {
                return book;
            }
        }
        NoteBookType type = typeMapper.findByCode(code);
        if (type == null) {
            throw new BizException(500, "笔记本类型字典缺失(" + code + ")");
        }
        Book book = new Book();
        book.setCn_notebook_id(UUID.randomUUID().toString().replace("-", ""));
        book.setCn_user_id(user.getCn_user_id());
        book.setCn_notebook_type_id(type.getCn_notebook_type_id());
        book.setCn_notebook_name(displayName);
        book.setCn_notebook_createtime(new Timestamp(System.currentTimeMillis()));
        notebookMapper.insert(book);
        book.setCn_notebook_type_code(code);
        return book;
    }

    @Override
    public void rename(String notebookId, String name) {
        Book book = requireOwned(notebookId);
        notebookMapper.updateName(book.getCn_notebook_id(), name.trim());
    }

    @Override
    public void delete(String notebookId) {
        Book book = requireOwned(notebookId);
        if ("normal".equals(book.getCn_notebook_type_code())) {
            int count = noteMapper.countByBookId(notebookId);
            if (count > 0) {
                throw new BizException(1, "笔记本下仍有 " + count + " 条笔记, 请先清空");
            }
        }
        notebookMapper.deleteById(notebookId);
    }

    private Book requireOwned(String notebookId) {
        User user = currentUserService.requireUser();
        Book book = notebookMapper.findById(notebookId);
        if (book == null) {
            throw new BizException(2, "笔记本不存在");
        }
        if (!user.getCn_user_id().equals(book.getCn_user_id())) {
            throw new BizException(403, "无权操作他人的笔记本");
        }
        return book;
    }
}
