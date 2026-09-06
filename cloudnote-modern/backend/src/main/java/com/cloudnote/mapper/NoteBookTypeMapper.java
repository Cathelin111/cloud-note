package com.cloudnote.mapper;

import org.apache.ibatis.annotations.Param;

import com.cloudnote.entity.NoteBookType;

public interface NoteBookTypeMapper {

    NoteBookType findByCode(@Param("code") String code);
}
