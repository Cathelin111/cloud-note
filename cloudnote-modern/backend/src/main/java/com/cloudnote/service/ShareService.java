package com.cloudnote.service;

import java.util.Map;

import com.cloudnote.entity.Share;

public interface ShareService {

    /** 公开分享分页搜索(标题/正文, 仅normal), data={total,page,size,rows} */
    Map<String, Object> publicPage(String keyword, Integer page);

    /** 公开分享详情(仅normal可读; 下架返回业务错误) */
    Share publicDetail(String shareId);

    /** 分享我的笔记(复制标题+正文到cn_share, 校验属主) */
    void shareMyNote(String noteId);

    /** 收藏分享到当前用户收藏笔记本 */
    void collect(String shareId);
}
