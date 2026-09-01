package com.lcz.cloud_note.entity;

import java.io.Serializable;

/**
 * 活动投稿实体，对应数据表cn_note_activity
 * 一张笔记可以投稿到多个活动，一个活动可以收到多个投稿
 */
public class NoteActivity implements Serializable {
	private String cn_note_activity_id;
	private String cn_activity_id;
	private String cn_note_id;
	private Integer cn_note_activity_up;
	private Integer cn_note_activity_down;
	private String cn_note_activity_title;
	private String cn_note_activity_body;
	
	public String getCn_note_activity_id() {
		return cn_note_activity_id;
	}
	public void setCn_note_activity_id(String cn_note_activity_id) {
		this.cn_note_activity_id = cn_note_activity_id;
	}
	public String getCn_activity_id() {
		return cn_activity_id;
	}
	public void setCn_activity_id(String cn_activity_id) {
		this.cn_activity_id = cn_activity_id;
	}
	public String getCn_note_id() {
		return cn_note_id;
	}
	public void setCn_note_id(String cn_note_id) {
		this.cn_note_id = cn_note_id;
	}
	public Integer getCn_note_activity_up() {
		return cn_note_activity_up;
	}
	public void setCn_note_activity_up(Integer cn_note_activity_up) {
		this.cn_note_activity_up = cn_note_activity_up;
	}
	public Integer getCn_note_activity_down() {
		return cn_note_activity_down;
	}
	public void setCn_note_activity_down(Integer cn_note_activity_down) {
		this.cn_note_activity_down = cn_note_activity_down;
	}
	public String getCn_note_activity_title() {
		return cn_note_activity_title;
	}
	public void setCn_note_activity_title(String cn_note_activity_title) {
		this.cn_note_activity_title = cn_note_activity_title;
	}
	public String getCn_note_activity_body() {
		return cn_note_activity_body;
	}
	public void setCn_note_activity_body(String cn_note_activity_body) {
		this.cn_note_activity_body = cn_note_activity_body;
	}
	@Override
	public String toString() {
		return "NoteActivity [cn_note_activity_id=" + cn_note_activity_id + ", cn_activity_id=" + cn_activity_id
				+ ", cn_note_id=" + cn_note_id + ", cn_note_activity_up=" + cn_note_activity_up
				+ ", cn_note_activity_down=" + cn_note_activity_down + ", cn_note_activity_title="
				+ cn_note_activity_title + ", cn_note_activity_body=" + cn_note_activity_body + "]";
	}
}
