package com.lcz.cloud_note.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.lcz.cloud_note.entity.Book;


public interface BookDao {
	//根据登录的uid查找笔记本的数据
	public List<Book> findByUserId(String userId);
	//增加笔记本的操作
	public void save(Book book);
	//查询用户的特殊笔记本(收藏/回收站/活动/推送)，带类型代码
	public List<Book> findSpecialByUserId(String userId);
	//修改笔记本名称
	public void updateName(@Param("notebookId") String notebookId, @Param("name") String name);
	//彻底删除笔记本
	public void deleteById(String notebookId);
}
