package com.lcz.cloud_note.service;

import java.util.Map;

import com.lcz.cloud_note.util.NoteResult;

/**
 * 数据服务: 为用户级数据导出/未来跨设备同步预留的接口
 */
public interface DataService {
	/**
	 * 导出指定用户的全部数据(档案+笔记本+笔记+分享+活动投稿)
	 * 返回JSON结构可用于其他设备/实例导入(导入接口将在同步版本提供)
	 */
	public NoteResult<Map<String,Object>> exportUserData(String userId);
}
