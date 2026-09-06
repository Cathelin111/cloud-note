package com.cloudnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NoteCreateRequest {

    @NotBlank(message = "笔记本ID不能为空")
    private String bookId;

    @NotBlank(message = "笔记标题不能为空")
    @Size(max = 200, message = "笔记标题过长")
    private String title;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
