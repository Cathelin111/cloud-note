package com.cloudnote.dto;

import jakarta.validation.constraints.NotBlank;

public class ActivityJoinRequest {

    /** 用于投稿的笔记ID(当前用户自己的笔记) */
    @NotBlank(message = "笔记ID不能为空")
    private String noteId;

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }
}
