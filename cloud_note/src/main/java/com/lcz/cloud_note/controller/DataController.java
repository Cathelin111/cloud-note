package com.lcz.cloud_note.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lcz.cloud_note.service.DataService;
import com.lcz.cloud_note.util.NoteResult;

/**
 * 数据接口控制器
 * 为"用户数据跨设备同步"预留: 先提供导出, 导入/合并接口将在同步版本提供
 */
@Controller
@RequestMapping("/data")
public class DataController {
	@Resource
	private DataService dataService;

	//导出指定用户全部数据(JSON)
	@RequestMapping("/export.do")
	@ResponseBody
	public NoteResult<Map<String,Object>> exportData(String userId){
		return dataService.exportUserData(userId);
	}
}
