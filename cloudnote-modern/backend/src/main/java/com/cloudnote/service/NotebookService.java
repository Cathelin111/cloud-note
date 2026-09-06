package com.cloudnote.service;

import java.util.List;
import java.util.Map;

import com.cloudnote.entity.Book;

public interface NotebookService {

    /** 当前用户正常笔记本列表 */
    List<Book> listNormal();

    /** 新建正常笔记本 */
    Book create(String title);

    /** 特殊笔记本Map: favorites/recycle/action(不存在自动创建) */
    Map<String, Book> special();

    /** 重命名(校验属主) */
    void rename(String notebookId, String name);

    /** 删除笔记本(校验属主与"笔记本下仍有笔记") */
    void delete(String notebookId);

    /** 获取(或自动创建)用户指定类型笔记本 */
    Book ensureSpecial(String code, String displayName);
}
