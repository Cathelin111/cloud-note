package com.cloudnote.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.cloudnote.common.ApiResult;
import com.cloudnote.common.BizException;

/**
 * 全局异常处理: 统一返回 {status,msg,data}
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ApiResult<Void> handleBiz(BizException e) {
        return ApiResult.error(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResult<Void> handleNotFound(NoHandlerFoundException e) {
        return ApiResult.error(404, "接口不存在: " + e.getRequestURL());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数不合法";
        return ApiResult.error(1, msg);
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleOther(Exception e) {
        log.error("系统异常", e);
        return ApiResult.error(500, "系统异常: " + e.getMessage());
    }
}
