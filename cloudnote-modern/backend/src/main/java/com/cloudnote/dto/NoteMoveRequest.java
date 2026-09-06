package com.cloudnote.dto;

import jakarta.validation.constraints.NotBlank;

public class NoteMoveRequest {

    @NotBlank(message = "目标笔记本ID不能为空")
    private String bookId;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
}
