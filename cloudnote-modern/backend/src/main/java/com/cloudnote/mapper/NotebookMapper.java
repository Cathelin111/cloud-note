package com.cloudnote.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.cloudnote.entity.Book;

public interface NotebookMapper {

    Book findById(@Param("notebookId") String notebookId);

    /** 用户的全部笔记本(带类型代码) */
    List<Book> findAllByUserId(@Param("userId") String userId);

    /** 用户的正常笔记本(normal), 按创建时间倒序 */
    List<Book> findNormalByUserId(@Param("userId") String userId);

    int insert(Book book);

    int updateName(@Param("notebookId") String notebookId, @Param("name") String name);

    int deleteById(@Param("notebookId") String notebookId);
}
