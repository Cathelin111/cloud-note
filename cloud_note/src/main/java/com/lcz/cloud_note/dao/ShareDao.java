package com.lcz.cloud_note.dao;

import java.util.List;
import java.util.Map;

import com.lcz.cloud_note.entity.Share;

public interface ShareDao {
	//分享功能，实际上是将分享的内容插入到share中
	public void share(Share share);
	//搜索功能(仅返回公开可见normal状态的分享, 带分享人昵称)
	public List<Share> findLikeTitle(String title);
	//按分享ID查询分享
	public Share findById(String shareId);
	//后台: 分页查询全部分享(含已下架, 带分享人昵称)
	public List<Share> findAdminPage(Map<String,Object> params);
	//后台: 统计分享总条数
	public int countAdmin(Map<String,Object> params);
	//后台: 修改分享状态(上架/下架)
	public int updateStatus(Map<String,Object> params);
	//删除分享(按分享ID)
	public int deleteById(String shareId);
	//删除引用指定笔记列表的分享(删除用户时级联清理)
	public int deleteByNoteIds(List<String> noteIds);
}
