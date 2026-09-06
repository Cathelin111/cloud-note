package com.cloudnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NotebookCreateRequest {

    @NotBlank(message = "笔记本名称不能为空")
    @Size(max = 100, message = "笔记本名称过长")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
