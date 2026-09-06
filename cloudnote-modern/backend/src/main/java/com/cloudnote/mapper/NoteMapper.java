package com.cloudnote.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.cloudnote.entity.Note;

public interface NoteMapper {

    /** 笔记本下的笔记列表(仅标题与ID) */
    List<Note> findByBookId(@Param("bookId") String bookId);

    /** 笔记本下正常笔记数量(删除笔记本前校验) */
    int countByBookId(@Param("bookId") String bookId);

    Note findByNoteId(@Param("noteId") String noteId);

    int insert(Note note);

    int updateContent(@Param("noteId") String noteId,
                      @Param("title") String title,
                      @Param("body") String body,
                      @Param("lastModify") Long lastModify);

    /** 移动/恢复笔记(仅改所属笔记本) */
    int move(@Param("noteId") String noteId, @Param("bookId") String bookId);

    int deleteById(@Param("noteId") String noteId);
}
