package com.cloudnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NoteUpdateRequest {

    @NotBlank(message = "笔记标题不能为空")
    @Size(max = 200, message = "笔记标题过长")
    private String title;

    private String body;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
