/**
 * local.js 活动页面事件处理
 * 依赖：scripts/activity.js（活动Ajax请求）、scripts/cookie.js（cookie工具）
 */

//获取活动列表(activity.html页面onload调用)
function activity_list(){
	getActivityList();
}

//获取活动详情页的投稿列表(activity_detail.html页面onload调用)
function get_activity_list(){
	var param=window.location.hash;
	global_ac_id=param.replace(/#/,'');
	$("#fifth_side_right .contact-body").empty();
	$("#more_activity_note").val(1);
	getNoteActivitys(global_ac_id,1);
}

$(function(){
	//显示用户名
	$(".profile-username").text(getCookie("userName"));
	
	//关闭/取消弹窗
	$(document).on("click", ".close,.cancle", function() {
		$('.modal.fade.in').hide();
		$('.opacity_bg').hide();
	});
	
	/***********活动详情页操作************/
	//更多活动笔记(分页加载)
	$(document).on("click", "#more_activity_note", function() {
		var page = parseInt($('#more_activity_note').val()||1);
		$('#more_activity_note').val(page+1);
		getNoteActivitys(global_ac_id,page+1);
	});
	
	//点击投稿笔记,加载详情
	$(document).on("click", "#action_part_1 li", function() {
		$(this).siblings('li').children('a').removeClass('checked');
		$(this).children('a').addClass('checked');
		$("#content_body").empty();
		getNoteActivityDetail($(this).data('noteActivity').cn_note_activity_id);
	});
	
	//点击参加活动按钮,弹出选择笔记对话框
	$(document).on("click", "#join_action", function() {
		$('#modalBasic_15,.opacity_bg').show();
		$('#select_notebook ul').empty();
		$('#select_note ul').empty();
		getSelectNoteBook();
	});
	
	//选择笔记本,加载该笔记本下的笔记
	$(document).on("click", "#select_notebook li", function() {
		$(this).siblings('li').children('a').removeClass('checked');
		$(this).children('a').addClass('checked');
		var noteBookId=$(this).data('notebook').cn_notebook_id;
		$('#select_note ul').empty();
		getSelectNoteList(noteBookId);
	});
	
	//选择笔记
	$(document).on("click", "#select_note li", function() {
		$(this).siblings('li').children('a').removeClass('checked');
		$(this).children('a').addClass('checked');
	});
	
	//确认选择的笔记,参加活动
	$(document).on("click", "#modalBasic_15 .btn.btn-primary.sure", function() {
		var dom=$("#select_note ul .checked").parent();
		var noteId=dom.data('note').cn_note_id;
		createNoteActivity(noteId,global_ac_id,dom);
	});
	
	//收藏投稿
	$(document).on('click', "#first_action .btn_like", function() {
		var dom = $(this).parents("li");
		var noteActivityId = dom.data("noteActivity").cn_note_activity_id;
		likeActivityNote(noteActivityId, $(this));
	});
	
	//顶投稿
	$(document).on("click", "#first_action .btn_up", function() {
		var dom = $(this).parents("li");
		up(dom.data("noteActivity").cn_note_activity_id,$(this));
	});
	
	//踩投稿
	$(document).on("click", "#first_action .btn_down", function() {
		var dom = $(this).parents("li");
		down(dom.data("noteActivity").cn_note_activity_id,$(this));
	});
});
