package com.cloudnote.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudnote.common.ApiResult;
import com.cloudnote.dto.NoteCreateRequest;
import com.cloudnote.dto.NoteMoveRequest;
import com.cloudnote.dto.NoteUpdateRequest;
import com.cloudnote.entity.Note;
import com.cloudnote.service.NoteService;

import jakarta.validation.Valid;

/**
 * 笔记接口(需登录): RESTful, 替代旧 /note/*
 */
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /** 笔记本下笔记列表(旧 loadnotes.do) */
    @GetMapping
    public ApiResult<List<Note>> listByBook(String bookId) {
        return ApiResult.ok("加载笔记成功", noteService.listByBook(bookId));
    }

    /** 笔记详情(旧 load.do) */
    @GetMapping("/{noteId}")
    public ApiResult<Note> detail(@PathVariable String noteId) {
        return ApiResult.ok("加载笔记信息成功", noteService.detail(noteId));
    }

    /** 新建笔记(旧 add.do) */
    @PostMapping
    public ApiResult<Note> create(@Valid @RequestBody NoteCreateRequest request) {
        return ApiResult.ok("创建笔记成功", noteService.create(request.getBookId(), request.getTitle()));
    }

    /** 保存笔记(旧 update.do) */
    @PutMapping("/{noteId}")
    public ApiResult<Void> update(@PathVariable String noteId,
                                  @Valid @RequestBody NoteUpdateRequest request) {
        noteService.update(noteId, request.getTitle(), request.getBody());
        return ApiResult.ok("保存笔记成功", null);
    }

    /** 删除笔记->回收站(旧 delete.do) */
    @DeleteMapping("/{noteId}")
    public ApiResult<Void> softDelete(@PathVariable String noteId) {
        noteService.softDelete(noteId);
        return ApiResult.ok("删除笔记成功", null);
    }

    /** 移动/恢复(旧 move.do) */
    @PatchMapping("/{noteId}/move")
    public ApiResult<Void> move(@PathVariable String noteId,
                                @Valid @RequestBody NoteMoveRequest request) {
        noteService.move(noteId, request.getBookId());
        return ApiResult.ok("移动笔记成功", null);
    }

    /** 回收站彻底删除(旧 deleteRecycle.do) */
    @DeleteMapping("/{noteId}/permanent")
    public ApiResult<Void> permanentDelete(@PathVariable String noteId) {
        noteService.permanentDelete(noteId);
        return ApiResult.ok("彻底删除成功", null);
    }
}
