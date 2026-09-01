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

//分页加载搜索分享的笔记
//发送ajax请求
function searchSharePage(keyword,page){
	$.ajax({
		url:base_path+"/share/search.do",
		type:"post",
		data:{"keyword":keyword,"page":page},
		dataType:"json",
		success:function(result){
			if(result.status==0){
				//获取服务器返回的搜索结果
				var shares=result.data;
				//循环解析生成列表li元素
				 //循环解析生成列表li元素
				 for(var i=0;i<shares.length;i++){
					 var shareId = shares[i].cn_share_id;//分享ID
					 var shareTitle =shares[i].cn_share_title; //分享标题
					 //生成一个li
					 var sli = "";
					 sli+='<li class="online">';
					 sli+='	<a>';
					 sli+='		<i class="fa fa-file-text-o" title="online" rel="tooltip-bottom"></i>';
					 sli+= shareTitle;
					 sli+='		<button type="button" class="btn btn-default btn-xs btn_position btn_slide_down"><i class="fa fa-star"></i></button>';
					 sli+='	</a>';
					 sli+='</li>';
					 var $li = $(sli);
				     $li.data("shareId",shareId);
					 //添加到搜索结果ul中
					 $("#pc_part_6 ul").append($li);
				 }
			}
		},
		error:function(){
			alert("搜索失败");
		}
	});
};
//查看搜索结果列表的笔记信息
function load_share(){
	 //获取请求参数
	 var shareId = $(this).data("shareId");
	 //发送Ajax请求
	 $.ajax({
		 url:base_path+"/note/load_share.do",
		 type:"post",
		 data:{"shareId":shareId},
		 dataType:"json",
		 success:function(result){
			 if(result.status==0){
				 var title = result.data.cn_share_title;//获取分享标题
				 var body =	result.data.cn_share_body; //获取分享内容
				 //设置标题和内容
				 $("#noput_note_title").html(title);
				 $("#noput_note_title").next().html(body);
				 //切换显示
				 $("#pc_part_3").hide();
				 $("#pc_part_5").show();
			 }
		 },
		 error:function(){
			 alert("加载笔记信息异常");
		 }
	 });
 };
 
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

//收藏分享的笔记(搜索结果中的星标按钮)
function likeShareNote(shareId, dom){
	var userId=getCookie("userId");
	if(userId==null){
		window.location.href="log_in.html";
		return;
	}
	$.ajax({
		url:base_path+"/note/likeShareNote.do",
		type:"post",
		data:{"shareId":shareId,"userId":userId},
		dataType:"json",
		success:function(result){
			if(result.status==0){
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