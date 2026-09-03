/**
 * admin.js 系统管理员后台管理页面
 * 模块: 用户管理(停用/启用/删除) | 分享管理(下架/上架/删除) | 活动管理(发布/编辑/删除)
 * 每个后台接口均携带操作者管理员自己的 userId, 由服务端校验管理员身份
 */
var ADMIN_PAGE_SIZE=10;

$(function(){
	//非管理员强制回登录页
	var role=getCookie("userRole");
	if(role!="admin"){
		window.location.href="log_in.html";
		return;
	}
	//顶部管理员信息
	$("#adm_user_area").html(
		'<i class="fa fa-shield"></i> 系统管理员: '+(getCookie("userNick")||getCookie("userName")||"admin")
		+'<a href="edit.html"><i class="fa fa-book"></i> 前台</a>'
		+'<a href="javascript:void(0)" id="adm_logout"><i class="fa fa-sign-out"></i> 退出登录</a>'
	);
	$("#adm_logout").click(function(){
		delCookie("userName");delCookie("userId");delCookie("userNick");delCookie("userRole");
		window.location.href="log_in.html";
	});
	//自定义弹窗与关闭
	$(document).on("click",".close,.cancle,.sure",function(){
		$("#can").empty();
		$(".opacity_bg").hide();
	});
	window.alert=function(e){
		$('#can').load('./alert/alert_error.html',function(){
			$('#error_info').text(' '+e);
			$('.opacity_bg').show();
		});
	}
	//菜单切换
	$(".adm-menu a[data-panel]").click(function(){
		$(".adm-menu a").removeClass("active");
		$(this).addClass("active");
		$(".adm-panel").hide();
		var panel=$(this).attr("data-panel");
		$("#panel_"+panel).show();
		if(panel=="user"){loadUsers(1);}
		else if(panel=="admins"){loadAdmins();}
		else if(panel=="share"){loadShares(1);}
		else if(panel=="activity"){loadActivities();}
	});
	//搜索回车
	$("#user_keyword").keydown(function(e){ if(e.keyCode==13){loadUsers(1);} });
	$("#share_keyword").keydown(function(e){ if(e.keyCode==13){loadShares(1);} });
	$("#user_search").click(function(){loadUsers(1);});
	$("#share_search").click(function(){loadShares(1);});

	//用户行操作: 停用/启用/删除
	$("#user_rows").on("click",".act-status",function(){
		var targetId=$(this).attr("data-id");
		var status=$(this).attr("data-status");
		if(!confirm("确认要"+(status=="disabled"?"停用":"启用")+"该用户吗?")){return;}
		adminPost("user/updateStatus.do",{targetId:targetId,status:status},function(result){
			alert(result.msg);
			if(result.status==0){loadUsers(curUserPage);}
		});
	});
	$("#user_rows").on("click",".act-delete",function(){
		var targetId=$(this).attr("data-id");
		var name=$(this).attr("data-name");
		if(!confirm("确认删除用户 \""+name+"\" 吗?\n其笔记本、笔记、分享与投稿记录将一并删除!")){return;}
		adminPost("user/delete.do",{targetId:targetId},function(result){
			alert(result.msg);
			if(result.status==0){loadUsers(curUserPage);}
		});
	});
	//用户行操作: 设为管理员
	$("#user_rows").on("click",".act-promote",function(){
		var targetId=$(this).attr("data-id");
		var name=$(this).attr("data-name");
		if(!confirm("确认将用户 \""+name+"\" 提升为系统管理员吗?\n提升后他可用登录页的\"管理员登录\"入口进入后台。")){return;}
		adminPost("user/setAdmin.do",{targetId:targetId},function(result){
			alert(result.msg);
			if(result.status==0){loadUsers(curUserPage);}
		});
	});
	//管理员管理: 直接新建管理员账号
	$("#adm_create").click(function(){
		var name=$("#adm_name").val().trim();
		var password=$("#adm_password").val().trim();
		var nick=$("#adm_nick").val().trim();
		if(!name){alert("用户名不能为空!");return;}
		if(password.length<6){alert("密码长度不能小于6位!");return;}
		if(!confirm("确认创建管理员账号 \""+name+"\" 吗?")){return;}
		adminPost("user/addAdmin.do",{name:name,password:password,nick:nick},function(result){
			alert(result.msg);
			if(result.status==0){
				$("#adm_name").val("");$("#adm_password").val("");$("#adm_nick").val("");
				loadAdmins();
			}
		});
	});
	//分享行操作: 下架/上架/删除
	$("#share_rows").on("click",".act-status",function(){
		var shareId=$(this).attr("data-id");
		var status=$(this).attr("data-status");
		adminPost("share/updateStatus.do",{shareId:shareId,status:status},function(result){
			alert(result.msg);
			if(result.status==0){loadShares(curSharePage);}
		});
	});
	$("#share_rows").on("click",".act-delete",function(){
		var shareId=$(this).attr("data-id");
		if(!confirm("确认删除该分享吗? 删除后前台将不再展示!")){return;}
		adminPost("share/delete.do",{shareId:shareId},function(result){
			alert(result.msg);
			if(result.status==0){loadShares(curSharePage);}
		});
	});
	//活动: 发布/修改/取消
	$("#act_save").click(saveActivity);
	$("#act_cancel").click(function(){
		$("#activity_form").data("editId","");
		$("#act_title").val("");$("#act_body").val("");$("#act_end_time").val("");
		$("#act_save").text("发布活动");
		$("#act_cancel").hide();
	});
	//活动行操作: 编辑/删除
	$("#activity_rows").on("click",".act-edit",function(){
		var row=$(this).parents("tr");
		$("#activity_form").data("editId",$(this).attr("data-id"));
		$("#act_title").val(row.find("td:eq(0)").attr("data-title"));
		$("#act_body").val(row.find("td:eq(1)").attr("data-body"));
		var endText=row.find("td:eq(2)").attr("data-end");
		if(endText==""){$("#act_end_time").val("");}
		else{$("#act_end_time").val(toLocalInput(endText));}
		$("#act_save").text("保存修改");
		$("#act_cancel").show();
		$("html,body").animate({scrollTop:0},300);
	});
	$("#activity_rows").on("click",".act-delete",function(){
		if(!confirm("确认删除该活动吗?\n该活动下的全部投稿将一并删除!")){return;}
		adminPost("activity/delete.do",{activityId:$(this).attr("data-id")},function(result){
			alert(result.msg);
			if(result.status==0){loadActivities();}
		});
	});

	//默认加载用户列表
	loadUsers(1);
});

//当前页状态
var curUserPage=1;
var curSharePage=1;

//统一后台请求
function adminPost(action,data,callback){
	data=data||{};
	data.userId=getCookie("userId");
	$.ajax({
		url:base_path+"/admin/"+action,
		type:"post",
		data:data,
		dataType:"json",
		success:function(result){
			if(result.status==4){
				alert(result.msg);
				setTimeout(function(){window.location.href="log_in.html";},1200);
				return;
			}
			callback(result);
		},
		error:function(){ alert("请求失败, 请稍后重试"); }
	});
}

//================== 用户管理 ==================
function loadUsers(page){
	curUserPage=page||1;
	var keyword=$("#user_keyword").val().trim();
	adminPost("user/list.do",{keyword:keyword,page:curUserPage},function(result){
		if(result.status!=0){alert(result.msg);return;}
		var data=result.data;
		$("#user_total").text("共 "+data.total+" 位普通用户");
		var rows=data.rows||[];
		var html="";
		if(rows.length==0){
			html='<tr><td colspan="6" class="adm-empty">暂无用户</td></tr>';
		}
		for(var i=0;i<rows.length;i++){
			var u=rows[i];
			var statusHtml=u.cn_user_status=="disabled"
				?'<span class="adm-tag warn">已停用</span>'
				:'<span class="adm-tag ok">正常</span>';
			var actHtml='<span class="t-actions">';
			if(u.cn_user_status=="disabled"){
				actHtml+='<button type="button" class="btn btn-default btn-xs act-status" data-id="'+u.cn_user_id+'" data-status="normal">启用</button>';
			}else{
				actHtml+='<button type="button" class="btn btn-default btn-xs act-status" data-id="'+u.cn_user_id+'" data-status="disabled">停用</button>';
			}
			actHtml+='<button type="button" class="btn btn-default btn-xs act-delete" data-id="'+u.cn_user_id+'" data-name="'+u.cn_user_name+'">删除</button>';
			actHtml+='<button type="button" class="btn btn-default btn-xs act-promote" data-id="'+u.cn_user_id+'" data-name="'+u.cn_user_name+'" title="提升为系统管理员"><i class="fa fa-shield"></i> 设为管理员</button></span>';
			html+='<tr>'
				+'<td>'+escapeHtml(u.cn_user_name)+'</td>'
				+'<td>'+escapeHtml(u.cn_user_nick||"-")+'</td>'
				+'<td><span class="adm-tag info">普通用户</span></td>'
				+'<td>'+statusHtml+'</td>'
				+'<td class="t-time">'+formatTime(u.cn_user_create_time)+'</td>'
				+'<td>'+actHtml+'</td>'
				+'</tr>';
		}
		$("#user_rows").html(html);
		renderPage("user_page",data.page,data.total,"loadUsers");
	});
}

//================== 管理员管理 ==================
function loadAdmins(){
	adminPost("user/admins.do",{},function(result){
		if(result.status!=0){alert(result.msg);return;}
		var rows=result.data||[];
		var html="";
		if(rows.length==0){
			html='<tr><td colspan="5" class="adm-empty">暂无管理员账号</td></tr>';
		}
		for(var i=0;i<rows.length;i++){
			var a=rows[i];
			var me=(getCookie("userId")==a.cn_user_id)?' <span class="adm-tag info">当前登录</span>':'';
			var statusHtml=a.cn_user_status=="disabled"
				?'<span class="adm-tag warn">已停用</span>'
				:'<span class="adm-tag ok">正常</span>';
			html+='<tr>'
				+'<td>'+escapeHtml(a.cn_user_name)+me+'</td>'
				+'<td>'+escapeHtml(a.cn_user_nick||"-")+'</td>'
				+'<td>'+statusHtml+'</td>'
				+'<td class="t-time">'+formatTime(a.cn_user_create_time)+'</td>'
				+'<td><span class="adm-tag info">系统管理员</span></td>'
				+'</tr>';
		}
		$("#admin_rows").html(html);
	});
}

//================== 分享管理 ==================
function loadShares(page){
	curSharePage=page||1;
	var keyword=$("#share_keyword").val().trim();
	adminPost("share/list.do",{keyword:keyword,page:curSharePage},function(result){
		if(result.status!=0){alert(result.msg);return;}
		var data=result.data;
		$("#share_total").text("共 "+data.total+" 条分享");
		var rows=data.rows||[];
		var html="";
		if(rows.length==0){
			html='<tr><td colspan="4" class="adm-empty">暂无分享内容</td></tr>';
		}
		for(var i=0;i<rows.length;i++){
			var s=rows[i];
			var statusHtml=s.cn_share_status=="disabled"
				?'<span class="adm-tag warn">已下架</span>'
				:'<span class="adm-tag ok">公开</span>';
			var actHtml='<span class="t-actions">';
			if(s.cn_share_status=="disabled"){
				actHtml+='<button type="button" class="btn btn-default btn-xs act-status" data-id="'+s.cn_share_id+'" data-status="normal">上架</button>';
			}else{
				actHtml+='<button type="button" class="btn btn-default btn-xs act-status" data-id="'+s.cn_share_id+'" data-status="disabled">下架</button>';
			}
			actHtml+='<button type="button" class="btn btn-default btn-xs act-delete" data-id="'+s.cn_share_id+'">删除</button></span>';
			html+='<tr>'
				+'<td class="t-title">'+escapeHtml(s.cn_share_title)+'</td>'
				+'<td>'+escapeHtml(s.cn_share_author||"佚名(笔记已删除)")+'</td>'
				+'<td>'+statusHtml+'</td>'
				+'<td>'+actHtml+'</td>'
				+'</tr>';
		}
		$("#share_rows").html(html);
		renderPage("share_page",data.page,data.total,"loadShares");
	});
}

//================== 活动管理 ==================
function loadActivities(){
	adminPost("activity/list.do",{},function(result){
		if(result.status!=0){alert(result.msg);return;}
		var list=result.data||[];
		var html="";
		if(list.length==0){
			html='<tr><td colspan="4" class="adm-empty">暂无活动, 请点击上方表单发布</td></tr>';
		}
		for(var i=0;i<list.length;i++){
			var a=list[i];
			var actHtml='<span class="t-actions">'
				+'<button type="button" class="btn btn-default btn-xs act-edit" data-id="'+a.cn_activity_id+'">编辑</button>'
				+'<button type="button" class="btn btn-default btn-xs act-delete" data-id="'+a.cn_activity_id+'">删除</button></span>';
			html+='<tr>'
				+'<td class="t-title" data-title="'+escapeAttr(a.cn_activity_title)+'">'+escapeHtml(a.cn_activity_title)+'</td>'
				+'<td data-body="'+escapeAttr(a.cn_activity_body||"")+'">'+escapeHtml(excerptOf(a.cn_activity_body,50))+'</td>'
				+'<td class="t-time" data-end="'+(a.cn_activity_end_time||"")+'">'+(a.cn_activity_end_time?formatTime(a.cn_activity_end_time):'<span class="adm-tag ok">长期有效</span>')+'</td>'
				+'<td>'+actHtml+'</td>'
				+'</tr>';
		}
		$("#activity_rows").html(html);
	});
}

//保存(发布/修改)活动
function saveActivity(){
	var editId=$("#activity_form").data("editId")||"";
	var title=$("#act_title").val().trim();
	var body=$("#act_body").val().trim();
	if(!title){alert("活动标题不能为空!");return;}
	var endMs="";
	var endVal=$("#act_end_time").val();
	if(endVal){
		//datetime-local => 毫秒时间戳
		var dt=new Date(endVal);
		if(isNaN(dt.getTime())){alert("结束时间格式不正确!");return;}
		endMs=String(dt.getTime());
	}
	adminPost("activity/save.do",{activityId:editId,title:title,body:body,endTime:endMs},function(result){
		alert(result.msg);
		if(result.status==0){
			$("#act_cancel").click();
			loadActivities();
		}
	});
}

//分页栏渲染
function renderPage(domId,page,total,fnName){
	var pageCount=Math.ceil(total/ADMIN_PAGE_SIZE);
	if(pageCount<=1){$("#"+domId).html("");return;}
	var html='<button type="button" class="btn btn-default btn-xs" onclick="'+fnName+'('+(page-1)+')"'+(page<=1?' disabled':'')+'>上一页</button>';
	html+=' 第 '+page+'/'+pageCount+' 页 ';
	html+='<button type="button" class="btn btn-default btn-xs" onclick="'+fnName+'('+(page+1)+')"'+(page>=pageCount?' disabled':'')+'>下一页</button>';
	$("#"+domId).html(html);
}

//================== 工具函数 ==================
function escapeHtml(str){
	if(str==null){return "";}
	return String(str).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;");
}
function escapeAttr(str){
	if(str==null){return "";}
	return escapeHtml(str).replace(/'/g,"&#39;");
}
function excerptOf(html,maxLen){
	if(html==null){return "";}
	var div=document.createElement("div");
	div.innerHTML=html;
	var text=div.textContent||div.innerText||"";
	text=text.replace(/\s+/g," ").trim();
	return text.length>maxLen?text.substring(0,maxLen)+"...":text;
}
function formatTime(ms){
	if(!ms){return "-";}
	var d=new Date(Number(ms));
	function p(n){return n<10?"0"+n:n;}
	return d.getFullYear()+"-"+p(d.getMonth()+1)+"-"+p(d.getDate())+" "+p(d.getHours())+":"+p(d.getMinutes());
}
function toLocalInput(ms){
	var d=new Date(Number(ms));
	function p(n){return n<10?"0"+n:n;}
	return d.getFullYear()+"-"+p(d.getMonth()+1)+"-"+p(d.getDate())+"T"+p(d.getHours())+":"+p(d.getMinutes());
}
