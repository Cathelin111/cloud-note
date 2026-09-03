/*
 * 笔记的加载
 */
//加载笔记本相关的笔记
function loadBookNotes(){
	//设置选中效果
	$("#book_ul a").removeClass("checked");
	$(this).find("a").addClass("checked");				
	//获取参数
	var bookId=$(this).data("bookId");
	//发送ajax请求
	$.ajax({
		url:base_path+"/note/loadnotes.do",
		type:"post",
		data:{"bookId":bookId},
		dataType:"json",
		success:function(result){
			//获取笔记信息
			var notes=result.data;//(List集合中存储)
			//清除原来的列表信息
			$("#note_ul").empty();
			//循环添加li
			for(var i=0;i<notes.length;i++){
				//获取笔记ID
				var noteId=notes[i].cn_note_id;
				//获取笔记主题
				var noteTitle=notes[i].cn_note_title;
				//生成笔记li
				createNoteLi(noteId,noteTitle);
			}
		},
		error:function(){
			alert("获取失败");
		}
	});
};

//生成笔记li
function createNoteLi(noteId,noteTitle){
	var sli="";
	sli+='<li class="online">';
	sli+='<a>';
	sli+='<i class="fa fa-file-text-o" title="online" rel="tooltip-bottom"></i>';
	sli+=noteTitle;
	sli+='<button type="button" class="btn btn-default btn-xs btn_position btn_slide_down"><i class="fa fa-chevron-down"></i></button>';	
	sli+='</a>';
	sli+='<div class="note_menu" tabindex="-1">';
	sli+='<dl>';
	sli+='<dt><button type="button" class="btn btn-default btn-xs btn_move" title="移动至..."><i class="fa fa-random"></i></button></dt>';		
	sli+='<dt><button type="button" class="btn btn-default btn-xs btn_share" title="分享"><i class="fa fa-sitemap"></i></button></dt>';		
	sli+='<dt><button type="button" class="btn btn-default btn-xs btn_delete" title="删除"><i class="fa fa-times"></i></button></dt>';		
	sli+='</dl>';	
	sli+='</div>';
	sli+='</li>';
	//将字符串转换为jquery对象
	var $li=$(sli);
	//保存noteId
	$li.data("noteId",noteId);
	//将li添加到ul中
	$("#note_ul").append($li);
}
//笔记信息的标题与内容的显示
function loadNote(){
	//设置选中效果
	$("#note_ul a").removeClass("checked");
	$(this).find("a").addClass("checked");
	//获取请求参数
	var noteId=$(this).data("noteId");
	//发送ajax请求
	$.ajax({
		url:base_path+"/note/load.do",
		type:"post",
		data:{"noteId":noteId},
		dataType:"json",
		success:function(result){
			if(result.status==0){
				//获取笔记的标题
				var title=result.data.cn_note_title;
				//获取返回的笔记内容
				var body=result.data.cn_note_body;
				//设置页面中笔记标题
				$("#input_note_title").val(title);
				//设置笔记内容
				um.setContent(body);
				//若处于回收站/收藏/活动预览面板, 同步填充右侧预览区
				$("#noput_note_title").html(title);
				$("#share_meta").html("");
				$("#share_body").html(body);
				$("#share_actions").hide();
			}
		},
		error:function(){
			alert("加载笔记信息失败");
		}
	});
};

//更新笔记信息（保存笔记）事件
function updateNote() {
	//获取参数
	var $li=$("#note_ul a.checked").parent();
	//获取笔记Id
	var noteId=$li.data("noteId");
	//获取笔记的标题和内容
	var noteTitle=$("#input_note_title").val().trim();
	var noteBody=um.getContent();
	//发送ajax请求
	$.ajax({
		url:base_path+"/note/update.do",
		type:"post",
		data:{"noteId":noteId,"title":noteTitle,"body":noteBody},
		dataType:"json",
		success:function(result){
			if(result.status==0){
				var str="";
				str+='<i class="fa fa-file-text-o" title="online" rel="tooltip-bottom"></i>';
				str+=noteTitle;
				str+='<button type="button" class="btn btn-default btn-xs btn_position btn_slide_down"><i class="fa fa-chevron-down"></i></button>';	
				//将str替换到li的a元素中
				$li.find("a").html(str);
				//提示成功
				alert(result.msg);
			}
		},
		error:function(){
			alert("保存笔记失败");
		}
	});
};

//创建笔记按钮的点击事件
function addNote(){
	//获取请求参数
	//获取笔记标题
	var title=$("#input_note").val().trim();
	//获取用户ID
	var userId=getCookie("userId");
	//获取笔记本ID
	var $li=$("#book_ul a.checked").parent();
	var bookId=$li.data("bookId");
	//数据格式检查
	var ok=true;
	if(title==""){//判断是否为空
		ok=false;
		$("#title_span").html("标题不能为空");
	}
	if(userId==null){//检查是否生效
		ok=false;
		window.location.href="log_in.html";
	}
	if(ok){
		//发送ajax请求
		$.ajax({
			url:base_path+"/note/add.do",
			type:"post",
			data:{"userId":userId,"bookId":bookId,"title":title},
			dataType:"json",
			success:function(result){
				var note=result.data;
				if(result.status==0){
					var id=note.cn_note_id;
					var title=note.cn_note_title;
					createNoteLi(id,title);
					alert(result.msg);
				}
			},
			error:function(){
				alert("创建笔记失败");
			}
		});
	}
};
//分享笔记的点击事件
function shareNotes(){
	//获取请求参数
	$li=$(this).parents("li");
	var noteId=$li.data("noteId");
	//发送ajax请求
	$.ajax({
		url:base_path+"/share/add.do",
		type:"post",
		data:{"noteId":noteId},
		dataType:"json",
		success:function(result){
			var noteTitle=$li.text();
			var sli="";
			sli+='<i class="fa fa-file-text-o" title="online" rel="tooltip-bottom"></i>';
			sli+=noteTitle;
			sli+='<i class="fa fa-sitemap"></i>'
			sli+='<button type="button" class="btn btn-default btn-xs btn_position btn_slide_down"><i class="fa fa-chevron-down"></i></button>';	
			//将笔记li元素的<a>标记内容提花
			$li.find("a").html(sli);
			alert("笔记分享成功");
		},
		error:function(){
			alert("分享笔记失败!");
		}
	});
};

//当前正在查看的分享ID(供"收藏该分享"按钮使用)
var g_viewShareId=null;

//分页加载搜索分享的笔记(标题或正文命中, 服务端分页, 每页10条)
function searchSharePage(keyword,page){
	//新搜索时清空列表
	if(page==1){
		$("#pc_part_6 ul").empty();
		$("#more_note").hide();
	}
	$.ajax({
		url:base_path+"/share/searchPage.do",
		type:"post",
		data:{"keyword":keyword,"page":page},
		dataType:"json",
		success:function(result){
			if(result.status==0){
				var d=result.data;
				var rows=d.rows||[];
				var total=d.total||0;
				var size=d.size||10;
				var curPage=d.page||1;
				//空结果提示
				if(page==1 && rows.length==0){
					$("#pc_part_6 ul").append('<li class="online"><a><i class="fa fa-inbox" title="empty" rel="tooltip-bottom"></i> 未找到匹配的分享笔记</a></li>');
				}
				//循环解析生成列表li元素
				for(var i=0;i<rows.length;i++){
					var share=rows[i];
					var shareId=share.cn_share_id;//分享ID
					var shareTitle=htmlEsc(share.cn_share_title); //分享标题
					var author=htmlEsc(share.cn_share_author||"佚名");//分享人
					var sli = "";
					sli+='<li class="online">';
					sli+='	<a>';
					sli+='		<i class="fa fa-file-text-o" title="online" rel="tooltip-bottom"></i>';
					sli+='		<span style="display:block;">'+shareTitle+'</span>';
					sli+='		<small style="display:block;color:#98a2ab;">分享者: '+author+'</small>';
					sli+='		<button type="button" class="btn btn-default btn-xs btn_position btn_like" title="收藏"><i class="fa fa-star-o"></i></button>';
					sli+='	</a>';
					sli+='</li>';
					var $li = $(sli);
					$li.data("shareId",shareId);
					//添加到搜索结果ul中
					$("#pc_part_6 ul").append($li);
				}
				//还有下一页时显示"更多", 否则隐藏
				if(total>curPage*size){
					$("#more_note").show();
				}else{
					$("#more_note").hide();
				}
			}else{
				if(page==1){
					$("#pc_part_6 ul").append('<li class="online"><a>搜索失败: '+htmlEsc(result.msg)+'</a></li>');
				}
			}
		},
		error:function(){
			if(page==1){
				$("#pc_part_6 ul").append('<li class="online"><a><i class="fa fa-exclamation-circle" rel="tooltip-bottom"></i> 搜索失败, 请稍后重试</a></li>');
			}
		}
	});
}
//查看搜索结果列表的笔记信息(分享详情)
function load_share(){
	 //获取请求参数
	 var shareId = $(this).data("shareId");
	 if(!shareId){return;}
	 loadShareById(shareId);
 };
//加载分享详情并切换到预览面板
function loadShareById(shareId){
	$("#noput_note_title").html("加载中...");
	$("#share_body").html("");
	$("#share_meta").html("");
	$.ajax({
		 url:base_path+"/note/load_share.do",
		 type:"post",
		 data:{"shareId":shareId},
		 dataType:"json",
		 success:function(result){
			 if(result.status==0){
				 var share=result.data;
				 g_viewShareId=shareId;
				 var title=htmlEsc(share.cn_share_title);//分享标题
				 var author=htmlEsc(share.cn_share_author||"佚名");//分享人
				 var body=share.cn_share_body||"";//分享内容(富文本HTML)
				 //设置标题、作者与内容
				 $("#noput_note_title").html(title);
				 $("#share_meta").html('分享者: '+author);
				 $("#share_body").html(body);
				 //重置收藏按钮状态
				 $("#share_like_btn").html('<i class="fa fa-star-o"></i> 收藏该分享');
				 $("#share_like_btn").removeAttr("disabled");
				 $("#share_like_tip").text('收藏后可在"收藏笔记本"中查看');
				 //切换显示: 隐藏编辑器, 显示分享详情
				 $("#pc_part_3").hide();
				 $("#pc_part_5").show();
			 }else{
				 //分享被下架或不存在
				 alert(result.msg||"该分享暂时无法查看");
				 if(result.status==2){
				 	//下架的分享从列表中移除
				 	$('#pc_part_6 li').filter(function(){return $(this).data("shareId")==shareId;}).remove();
				 }
			 }
		 },
		 error:function(){
			 alert("加载分享内容异常");
		 }
	 });
 }
//关闭分享详情, 返回笔记编辑区
function closeShareView(){
	g_viewShareId=null;
	$("#pc_part_5").hide();
	$("#pc_part_3").show();
}
//分享标题/作者HTML转义
function htmlEsc(str){
	if(str==null){return "";}
	return String(str).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;");
}
 
//删除笔记
 function deleteNote(){
 	 //获取请求参数
 	 var $li =$("#note_ul a.checked").parent();
 	 var noteId = $li.data("noteId");
 	 //发送Ajax请求
 	 $.ajax({
 		 url:base_path+"/note/delete.do",
 		 type:"post",
 		 data:{"noteId":noteId},
 		 dataType:"json",
 		 success:function(result){
 			 if(result.status==0){
 				 //删除li
 				 $li.remove();
 				 //提示成功
 				 alert(result.msg);
 			 }
 		 },
 		 error:function(){
 			 alert("删除笔记异常");
 		 }
 	 });
  };
 
//============ 以下为回收站/收藏/活动面板相关处理 ============

//加载特殊笔记本中的笔记到指定面板
//code: favorites/recycle/action; ulSel: 面板ul选择器; withButtons: 是否带操作按钮(recycle带恢复/删除, like带删除)
function loadPanelNotes(bookId, ulSel, withButtons){
	if(!bookId){//没有特殊笔记本时清空列表
		$(ulSel).empty();
		return;
	}
	$.ajax({
		url:base_path+"/note/loadnotes.do",
		type:"post",
		data:{"bookId":bookId},
		dataType:"json",
		success:function(result){
			$(ulSel).empty();//清除原有列表
			if(result.status==0){
				var notes=result.data;
				for(var i=0;i<notes.length;i++){
					var sli='<li class="online"><a><i class="fa fa-file-text-o" title="online" rel="tooltip-bottom"></i>'+notes[i].cn_note_title;
					if(withButtons==='recycle'){
						sli+='<button type="button" class="btn btn-default btn-xs btn_position_2 btn_replay" title="恢复"><i class="fa fa-reply"></i></button>';
						sli+='<button type="button" class="btn btn-default btn-xs btn_position btn_delete" title="彻底删除"><i class="fa fa-times"></i></button>';
					}else if(withButtons==='like'){
						sli+='<button type="button" class="btn btn-default btn-xs btn_position btn_delete" title="取消收藏"><i class="fa fa-times"></i></button>';
					}
					sli+='</a></li>';
					var $li=$(sli);
					$li.data("noteId",notes[i].cn_note_id);
					$(ulSel).append($li);
				}
			}
		},
		error:function(){
			alert("加载失败");
		}
	});
}

//打开回收站/收藏/活动面板
function showSpecialPanel(code, ulSel, withButtons){
	var userId=getCookie("userId");
	if(userId==null){
		window.location.href="log_in.html";
		return;
	}
	$.ajax({
		url:base_path+"/notebook/findSpecial.do",
		type:"post",
		data:{"userId":userId},
		dataType:"json",
		success:function(result){
			if(result.status==0){
				var books=result.data;
				var bookId=books[code]?books[code].cn_notebook_id:null;
				loadPanelNotes(bookId, ulSel, withButtons);
			}else{
				alert(result.msg);
			}
		},
		error:function(){
			alert("加载失败");
		}
	});
}

//彻底删除笔记(回收站)
function deleteRecycleNote(){
	var $li=$("#pc_part_4 a.checked").parent();
	var noteId=$li.data("noteId");
	$.ajax({
		url:base_path+"/note/deleteRecycle.do",
		type:"post",
		data:{"noteId":noteId},
		dataType:"json",
		success:function(result){
			if(result.status==0){
				$li.remove();
				alert(result.msg);
			}else{
				alert(result.msg);
			}
		},
		error:function(){
			alert("彻底删除异常");
		}
	});
}

//恢复回收站笔记(移动到指定笔记本)
function restoreNote(){
	var $li=$("#pc_part_4 a.checked").parent();
	var noteId=$li.data("noteId");
	var bookId=$("#replaySelect").val();
	if(bookId==""||bookId==null){
		alert("请选择恢复至的笔记本");
		return;
	}
	$.ajax({
		url:base_path+"/note/move.do",
		type:"post",
		data:{"noteId":noteId,"bookId":bookId},
		dataType:"json",
		success:function(result){
			if(result.status==0){
				$li.remove();
				alert(result.msg);
			}else{
				alert(result.msg);
			}
		},
		error:function(){
			alert("恢复笔记异常");
		}
	});
}

//取消收藏(移入回收站)
function unlikeNote(){
	var $li=$("#pc_part_7 a.checked").parent();
	var noteId=$li.data("noteId");
	$.ajax({
		url:base_path+"/note/delete.do",
		type:"post",
		data:{"noteId":noteId},
		dataType:"json",
		success:function(result){
			if(result.status==0){
				$li.remove();
				alert(result.msg);
			}else{
				alert(result.msg);
			}
		},
		error:function(){
			alert("取消收藏异常");
		}
	});
}

//收藏分享的笔记(搜索结果中的星标按钮 / 分享详情按钮)
//okCallback: 收藏成功后的界面更新回调(可选)
function likeShareNote(shareId, dom, okCallback){
	var userId=getCookie("userId");
	if(userId==null){
		window.location.href="log_in.html";
		return;
	}
	if(shareId==null){
		alert("分享不存在, 无法收藏");
		return;
	}
	$.ajax({
		url:base_path+"/note/likeShareNote.do",
		type:"post",
		data:{"shareId":shareId,"userId":userId},
		dataType:"json",
		success:function(result){
			if(result.status==0){
				if(okCallback){okCallback();}
				alert(result.msg);
			}else{
				alert(result.msg);
			}
		},
		error:function(){
			alert("收藏异常");
		}
	});
}