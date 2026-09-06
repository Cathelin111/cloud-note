package com.cloudnote.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudnote.common.ApiResult;
import com.cloudnote.dto.NotebookCreateRequest;
import com.cloudnote.dto.NotebookRenameRequest;
import com.cloudnote.entity.Book;
import com.cloudnote.service.NotebookService;

import jakarta.validation.Valid;

/**
 * 笔记本接口(需登录): RESTful, 替代旧 /book/* 与 /notebook/*
 */
@RestController
@RequestMapping("/api/notebooks")
public class NotebookController {

    private final NotebookService notebookService;

    public NotebookController(NotebookService notebookService) {
        this.notebookService = notebookService;
    }

    /** 正常笔记本列表(旧 loadBooks.do) */
    @GetMapping
    public ApiResult<List<Book>> list() {
        return ApiResult.ok("查询成功", notebookService.listNormal());
    }

    /** 新建笔记本(旧 add.do) */
    @PostMapping
    public ApiResult<Book> create(@Valid @RequestBody NotebookCreateRequest request) {
        return ApiResult.ok("创建笔记本成功", notebookService.create(request.getTitle()));
    }

    /** 重命名(旧 updateName.do) */
    @PutMapping("/{notebookId}")
    public ApiResult<Void> rename(@PathVariable String notebookId,
                                  @Valid @RequestBody NotebookRenameRequest request) {
        notebookService.rename(notebookId, request.getName());
        return ApiResult.ok("重命名成功", null);
    }

    /** 删除笔记本(旧 delete.do; 后端校验非空) */
    @DeleteMapping("/{notebookId}")
    public ApiResult<Void> delete(@PathVariable String notebookId) {
        notebookService.delete(notebookId);
        return ApiResult.ok("删除笔记本成功", null);
    }

    /** 特殊笔记本(旧 findSpecial.do): favorites/recycle/action, 不存在自动创建 */
    @GetMapping("/special")
    public ApiResult<Map<String, Book>> special() {
        return ApiResult.ok("查询成功", notebookService.special());
    }
}
