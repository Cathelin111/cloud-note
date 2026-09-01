package com.lcz.cloud_note.dao;

import java.util.List;

import com.lcz.cloud_note.entity.NoteBookType;

public interface NoteBookTypeDao {
	//查询全部笔记本类型
	public List<NoteBookType> findAllType();
	//按类型代码查询笔记本类型
	public NoteBookType findByCode(String code);
}
