package com.lcz.cloud_note.entity;

import java.io.Serializable;

public class Share implements Serializable{
	private String cn_share_id;
	private String cn_share_title;
	private String cn_share_body;
	private String cn_note_id;
	//状态: normal=公开可见 disabled=已被管理员下架
	private String cn_share_status;
	//分享人昵称(关联查询, 分享人注销后可能为null)
	private String cn_share_author;
	
	public String getCn_share_id() {
		return cn_share_id;
	}
	public void setCn_share_id(String cn_share_id) {
		this.cn_share_id = cn_share_id;
	}
	public String getCn_share_title() {
		return cn_share_title;
	}
	public void setCn_share_title(String cn_share_title) {
		this.cn_share_title = cn_share_title;
	}
	public String getCn_share_body() {
		return cn_share_body;
	}
	public void setCn_share_body(String cn_share_body) {
		this.cn_share_body = cn_share_body;
	}
	public String getCn_note_id() {
		return cn_note_id;
	}
	public void setCn_note_id(String cn_note_id) {
		this.cn_note_id = cn_note_id;
	}
	public String getCn_share_status() {
		return cn_share_status;
	}
	public void setCn_share_status(String cn_share_status) {
		this.cn_share_status = cn_share_status;
	}
	public String getCn_share_author() {
		return cn_share_author;
	}
	public void setCn_share_author(String cn_share_author) {
		this.cn_share_author = cn_share_author;
	}
	@Override
	public String toString() {
		return "Share [cn_share_id=" + cn_share_id + ", cn_share_title=" + cn_share_title + ", cn_share_body="
				+ cn_share_body + ", cn_note_id=" + cn_note_id + ", cn_share_status=" + cn_share_status
				+ ", cn_share_author=" + cn_share_author + "]";
	}
	
}
