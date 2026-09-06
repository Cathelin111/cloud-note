package com.cloudnote.common;

/**
 * 统一响应结构, 协议与旧版 NoteResult 兼容:
 * status: 0=成功, 1=用户名占用/不存在, 2=密码错误, 401=未认证, 403=无权限, 500=系统异常
 */
public class ApiResult<T> {

    private int status;
    private String msg;
    private T data;

    public ApiResult() {
    }

    public ApiResult(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResult<T> ok(String msg, T data) {
        return new ApiResult<T>(0, msg, data);
    }

    public static <T> ApiResult<T> error(int status, String msg) {
        return new ApiResult<T>(status, msg, null);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
