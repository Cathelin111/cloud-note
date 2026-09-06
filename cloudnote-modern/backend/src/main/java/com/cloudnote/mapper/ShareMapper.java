package com.cloudnote.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.cloudnote.entity.Share;

public interface ShareMapper {

    /** 公开分享分页(标题或正文命中, 仅normal, 带作者昵称) */
    List<Share> findPublicPage(Map<String, Object> params);

    /** 公开分享命中总数 */
    int countPublic(Map<String, Object> params);

    /** 分享详情(带作者昵称) */
    Share findDetailById(@Param("shareId") String shareId);

    /** 原始行(含状态, 内部使用) */
    Share findById(@Param("shareId") String shareId);

    int insert(Share share);

    //================= 管理员后台 =================

    /** 全部分享分页(含已下架, 带作者昵称) */
    List<Share> findAdminPage(@Param("keyword") String keyword,
                              @Param("begin") int begin,
                              @Param("pageSize") int pageSize);

    int countAdmin(@Param("keyword") String keyword);

    int updateStatus(@Param("shareId") String shareId, @Param("status") String status);

    int deleteById(@Param("shareId") String shareId);

    /** 删除引用这些笔记的分享(级联删用户) */
    int deleteByNoteIds(List<String> noteIds);
}
