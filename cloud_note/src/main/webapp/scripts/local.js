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

/**
 * 游客/未登录拦截: 返回true表示当前为游客, 已弹出提示并即将跳转登录页
 * 活动浏览对游客公开, 但"参加活动/顶/踩/收藏"等操作要求登录
 */
function guestPrompt(){
	if(getCookie("userId")){
		return false;
	}
	alert("该操作需要登录账号才能进行!\n游客可以浏览公开内容, 注册成为普通用户后即可参加活动、收藏与顶踩。");
	setTimeout(function(){
		window.location.href="log_in.html";
	},1600);
	return true;
}

$(function(){
	//显示用户名; 未登录(游客)时提供登录/注册入口
	var userName=getCookie("userName");
	var userNick=getCookie("userNick");
	if(userName){
		$(".profile-username").text(userNick||userName);
		//绑定退出登录
		$("#logout").click(function(){
			delCookie("userName");
			delCookie("userId");
			delCookie("userNick");
			delCookie("userRole");
			window.location.href="activity.html";
		});
	}else{
		//游客模式: 直接显示登录/注册链接
		$(".profile-nav").html(
			'<a href="log_in.html" style="color:#fff;line-height:58px;display:inline-block;padding:0 8px;" title="登录">游客? 登录</a>'+
			'<a href="log_in.html" style="color:#9ad5cf;line-height:58px;display:inline-block;padding:0 8px;" title="注册账号">注册</a>'
		);
	}
	
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
	
	//点击投稿笔记,加载详情(游客可浏览)
	$(document).on("click", "#action_part_1 li", function() {
		$(this).siblings('li').children('a').removeClass('checked');
		$(this).children('a').addClass('checked');
		$("#content_body").empty();
		getNoteActivityDetail($(this).data('noteActivity').cn_note_activity_id);
	});
	
	//点击参加活动按钮,弹出选择笔记对话框(需登录)
	$(document).on("click", "#join_action", function() {
		if(guestPrompt()){
			return;
		}
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
	
	//收藏投稿(需登录)
	$(document).on('click', "#first_action .btn_like", function() {
		if(guestPrompt()){
			return;
		}
		var dom = $(this).parents("li");
		var noteActivityId = dom.data("noteActivity").cn_note_activity_id;
		likeActivityNote(noteActivityId, $(this));
	});
	
	//顶投稿(需登录)
	$(document).on("click", "#first_action .btn_up", function() {
		if(guestPrompt()){
			return;
		}
		var dom = $(this).parents("li");
		up(dom.data("noteActivity").cn_note_activity_id,$(this));
	});
	
	//踩投稿(需登录)
	$(document).on("click", "#first_action .btn_down", function() {
		if(guestPrompt()){
			return;
		}
		var dom = $(this).parents("li");
		down(dom.data("noteActivity").cn_note_activity_id,$(this));
	});
});
