package com.cloudnote.service;

import java.util.List;

import com.cloudnote.entity.Note;

public interface NoteService {

    /** 笔记本下的笔记列表(id+title) */
    List<Note> listByBook(String bookId);

    /** 笔记详情 */
    Note detail(String noteId);

    /** 新建笔记 */
    Note create(String bookId, String title);

    /** 保存笔记内容 */
    void update(String noteId, String title, String body);

    /** 删除(移入回收站) */
    void softDelete(String noteId);

    /** 移动/恢复(回收站恢复或转移笔记本) */
    void move(String noteId, String bookId);

    /** 回收站彻底删除 */
    void permanentDelete(String noteId);
}
