package com.cloudnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NotebookRenameRequest {

    @NotBlank(message = "笔记本名称不能为空")
    @Size(max = 100, message = "笔记本名称过长")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
