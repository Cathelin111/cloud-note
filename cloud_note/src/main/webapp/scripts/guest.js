/**
 * guest.js 分享广场页面处理(游客与登录用户共用)
 * 功能: 搜索公开分享(标题/正文, 服务端分页), 查看详情, 收藏(需登录)
 */
$(function(){
	//顶部用户区
	renderUserArea();
	//搜索按钮
	$("#gs_search").click(function(){
		doSearch(1);
	});
	//回车搜索
	$("#gs_keyword").keydown(function(event){
		if(event.keyCode==13){
			doSearch(1);
		}
	});
	//点击列表项查看分享详情
	$("#gs_list").on("click",".gs-item",function(){
		$("#gs_list .gs-item").removeClass("active");
		$(this).addClass("active");
		loadShareDetail($(this).attr("data-shareId"));
	});
	//翻页
	$("#gs_prev").click(function(){ doSearch(g_curPage-1); });
	$("#gs_next").click(function(){ doSearch(g_curPage+1); });
	//收藏分享(需登录)
	$("#gs_favorite").click(function(){
		var userId=getCookie("userId");
		if(!userId){
			alert("请先登录或注册, 登录后才能收藏分享笔记!");
			setTimeout(function(){ window.location.href="log_in.html"; },1200);
			return;
		}
		var shareId=$(this).attr("data-shareId");
		$.ajax({
			url:base_path+"/note/likeShareNote.do",
			type:"post",
			data:{"shareId":shareId,"userId":userId},
			dataType:"json",
			success:function(result){
				if(result.status==0){
					alert(result.msg);
					$("#gs_favorite").html('<i class="fa fa-star"></i> 已收藏');
					$("#gs_favorite").attr("disabled","disabled");
					$("#gs_fav_tip").text("可在\"我的笔记-收藏笔记本\"中查看");
				}else{
					alert(result.msg);
				}
			},
			error:function(){ alert("收藏失败, 请稍后重试"); }
		});
	});
	//关闭自定义弹窗
	$(document).on("click",".close,.cancle,.sure",function(){
		$("#can").empty();
		$(".opacity_bg").hide();
	});
	//重写alert为友好弹窗
	window.alert=function(e){
		$('#can').load('./alert/alert_error.html',function(){
			$('#error_info').text(' '+e);
			$('.opacity_bg').show();
		});
	}
	//默认加载全部分享(第一页)
	doSearch(1);
});

//搜索状态
var g_curPage=1;   //当前页
var g_total=0;      //命中总数
var g_pages=0;      //总页数

//顶部用户区: 已登录显示昵称/入口, 未登录显示游客提示
function renderUserArea(){
	var nick=getCookie("userNick")||getCookie("userName");
	var role=getCookie("userRole");
	var html='<i class="fa fa-user-o"></i> ';
	if(nick){
		html+='欢迎, '+nick;
		html+='<a href="edit.html"><i class="fa fa-book"></i> 我的笔记</a>';
		if(role=='admin'){
			html+='<a href="admin.html"><i class="fa fa-cog"></i> 后台管理</a>';
		}
		html+='<a href="javascript:void(0)" id="gs_logout"><i class="fa fa-sign-out"></i> 退出</a>';
		$("#gs_user_area").html(html);
		$("#gs_fav_tip").html("收藏后将保存到您的\"收藏笔记本\"中, 可随时在我的笔记里查看");
		$("#gs_logout").click(function(){
			delCookie("userName");delCookie("userId");delCookie("userNick");delCookie("userRole");
			window.location.href="guest.html";
		});
	}else{
		html+='游客浏览中';
		html+='<a href="log_in.html"><i class="fa fa-sign-in"></i> 登录</a>';
		html+='<a href="log_in.html"><i class="fa fa-user-plus"></i> 注册</a>';
		$("#gs_user_area").html(html);
	}
}

//搜索公开分享(第page页): 标题或正文命中, 服务端分页
function doSearch(page){
	if(page<1){page=1;}
	var keyword=$("#gs_keyword").val().trim();
	$("#gs_list").html('<div class="gs-empty"><i class="fa fa-spinner fa-spin"></i> 搜索中...</div>');
	$("#gs_page").html("");
	$.ajax({
		url:base_path+"/share/searchPage.do",
		type:"post",
		data:{"keyword":keyword,"page":page},
		dataType:"json",
		success:function(result){
			if(result.status==0){
				var d=result.data;
				g_total=d.total||0;
				g_curPage=d.page||1;
				g_pages=Math.ceil(g_total/(d.size||10));
				renderShareList(d.rows||[],keyword);
			}else{
				$("#gs_list").html('<div class="gs-empty">搜索失败: '+(result.msg||"请稍后重试")+'</div>');
				alert(result.msg||"搜索失败");
			}
		},
		error:function(){
			$("#gs_list").html('<div class="gs-empty">搜索失败, 请检查网络后重试</div>');
			alert("搜索失败, 请稍后重试");
		}
	});
}

//渲染当前页分享列表
function renderShareList(rows,keyword){
	$("#gs_list").empty();
	if(!rows || rows.length==0){
		$("#gs_list").html('<div class="gs-empty">没有找到匹配的分享笔记<br/><small>'+(keyword?'换个关键字试试, 或':'或')+'<a href="log_in.html">登录</a>后自己分享笔记</small></div>');
		$("#gs_page").html("");
		return;
	}
	for(var i=0;i<rows.length;i++){
		var s=rows[i];
		var author=s.cn_share_author||"佚名";
		var excerpt=plainText(s.cn_share_body);
		excerpt=excerpt.length>56?excerpt.substring(0,56)+"...":excerpt;
		var li='<div class="gs-item" data-shareId="'+s.cn_share_id+'">'
			+'<h4><i class="fa fa-file-text-o" style="color:#0e7d76;"></i> '+escapeHtml(s.cn_share_title)+'</h4>'
			+'<div class="gs-meta">分享者: '+escapeHtml(author)+'</div>'
			+'<div class="gs-excerpt">'+escapeHtml(excerpt)+'</div>'
			+'</div>';
		$("#gs_list").append(li);
	}
	//分页信息
	if(g_pages>1){
		var pageHtml='<button type="button" class="btn btn-default btn-xs" id="gs_prev"'+(g_curPage<=1?' disabled':'')+'>上一页</button>';
		pageHtml+=' 第 '+g_curPage+' / '+g_pages+' 页, 共 '+g_total+' 条 ';
		pageHtml+='<button type="button" class="btn btn-default btn-xs" id="gs_next"'+(g_curPage>=g_pages?' disabled':'')+'>下一页</button>';
		$("#gs_page").html(pageHtml);
	}else{
		$("#gs_page").html('共 '+g_total+' 条分享');
	}
}

//加载分享详情
function loadShareDetail(shareId){
	$("#gs_detail").html('<div class="gs-empty"><i class="fa fa-spinner fa-spin"></i> 加载中...</div>');
	$("#gs_actions").hide();
	$.ajax({
		url:base_path+"/note/load_share.do",
		type:"post",
		data:{"shareId":shareId},
		dataType:"json",
		success:function(result){
			if(result.status==0){
				var s=result.data;
				var html='<h3 class="gs-title">'+escapeHtml(s.cn_share_title)+'</h3>'
					+'<div class="gs-author">分享者: '+escapeHtml(s.cn_share_author||"佚名")+'</div>'
					+'<div id="gs_content"></div>';
				$("#gs_detail").html(html);
				$("#gs_content").html(s.cn_share_body||"<p style='color:#98a2ab'>该分享暂无内容</p>");
				$("#gs_favorite").html('<i class="fa fa-star-o"></i> 收藏该分享');
				$("#gs_favorite").removeAttr("disabled");
				$("#gs_favorite").attr("data-shareId",shareId);
				if(getCookie("userNick")||getCookie("userName")){
					$("#gs_fav_tip").text("收藏后将保存到您的\"收藏笔记本\"中");
				}else{
					$("#gs_fav_tip").text("登录后可将分享收藏到自己的笔记中");
				}
				$("#gs_actions").show();
			}else{
				alert(result.msg||"该分享暂时无法查看");
				$("#gs_detail").html('<div class="gs-empty">'+(result.msg||"该分享暂时无法查看")+'</div>');
				$("#gs_actions").hide();
			}
		},
		error:function(){ alert("加载分享内容失败"); }
	});
}

//HTML转义, 防止标题/作者注入
function escapeHtml(str){
	if(str==null){return "";}
	return String(str).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;");
}
//去除HTML标签得到纯文本
function plainText(html){
	if(html==null){return "";}
	var div=document.createElement("div");
	div.innerHTML=html;
	return div.textContent||div.innerText||"";
}
