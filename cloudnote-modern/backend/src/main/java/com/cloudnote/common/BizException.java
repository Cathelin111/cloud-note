package com.cloudnote.common;

/**
 * 业务异常: 携带与旧协议一致的 status 码
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;

    public BizException(int status, String msg) {
        super(msg);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
