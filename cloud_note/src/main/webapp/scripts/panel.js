/**
 * panel.js 回收站/收藏/活动面板 与 笔记本管理(重命名/删除)事件处理
 * 依赖：notebook.js、note.js、alert.js
 */
$(function(){
	//--------- 底部三个特殊面板按钮 ----------
	//回收站
	$("#rollback_button").click(function(){
		$('#pc_part_2,#pc_part_3,#pc_part_6,#pc_part_7,#pc_part_8').hide();
		$('#pc_part_4,#pc_part_5').show();
		$('#first_side_right li a').removeClass('checked');
		$('#like_button,#action_button').removeClass('clicked');
		$(this).addClass('clicked');
		showSpecialPanel('recycle', '#pc_part_4 ul', 'recycle');
	});
	//收藏
	$("#like_button").click(function(){
		$('#pc_part_2,#pc_part_3,#pc_part_4,#pc_part_6,#pc_part_8').hide();
		$('#pc_part_7,#pc_part_5').show();
		$('#first_side_right li a').removeClass('checked');
		$('#rollback_button,#action_button').removeClass('clicked');
		$(this).addClass('clicked');
		showSpecialPanel('favorites', '#pc_part_7 ul', 'like');
	});
	//活动
	$("#action_button").click(function(){
		$('#pc_part_2,#pc_part_3,#pc_part_6,#pc_part_7,#pc_part_4').hide();
		$('#pc_part_8,#pc_part_5').show();
		$('#first_side_right li a').removeClass('checked');
		$('#rollback_button,#like_button').removeClass('clicked');
		$(this).addClass('clicked');
		showSpecialPanel('action', '#pc_part_8 ul', null);
	});
	
	//点击正常笔记本时,回到主界面(全部笔记+编辑区)
	$(document).on("click", "#book_ul li", function(){
		$('#pc_part_2,#pc_part_3').show();
		$('#pc_part_4,#pc_part_5,#pc_part_6,#pc_part_7,#pc_part_8').hide();
		$('#rollback_button,#like_button,#action_button').removeClass('clicked');
	});
	
	//--------- 回收站面板操作 ----------
	//点击回收站笔记,加载详情到编辑区
	$(document).on("click", "#pc_part_4 li", function(){
		$(this).siblings('li').children('a').removeClass('checked');
		$(this).children('a').addClass('checked');
		loadNote.call(this);
	});
	//恢复笔记
	$(document).on("click", "#pc_part_4 .btn_replay", function(){
		$(this).parents("li").children("a").addClass("checked");
		alertReplayWindow();
	});
	//彻底删除笔记
	$(document).on("click", "#pc_part_4 .btn_delete", function(){
		$(this).parents("li").children("a").addClass("checked");
		alertDeleteRollbackWindow();
	});
	//恢复确认
	$(document).on("click", "#modalBasic_3 .sure", restoreNote);
	//彻底删除确认
	$(document).on("click", "#modalBasic_10 .sure", deleteRecycleNote);
	
	//--------- 收藏面板操作 ----------
	//点击收藏笔记,加载详情到编辑区
	$(document).on("click", "#pc_part_7 li", function(){
		$(this).siblings('li').children('a').removeClass('checked');
		$(this).children('a').addClass('checked');
		loadNote.call(this);
	});
	//取消收藏
	$(document).on("click", "#pc_part_7 .btn_delete", function(){
		$(this).parents("li").children("a").addClass("checked");
		alertDeleteLikeWindow();
	});
	//取消收藏确认
	$(document).on("click", "#modalBasic_9 .sure", unlikeNote);
	
	//--------- 活动面板操作 ----------
	//点击活动笔记,加载详情到编辑区
	$(document).on("click", "#pc_part_8 li", function(){
		$(this).siblings('li').children('a').removeClass('checked');
		$(this).children('a').addClass('checked');
		loadNote.call(this);
	});
	
	//--------- 搜索结果中收藏分享笔记 ----------
	$(document).on("click", "#pc_part_6 .btn_like", function(event){
		event.stopPropagation();//阻止冒泡, 避免触发查看详情
		var $li=$(this).parents("li");
		var shareId=$li.data("shareId");
		var $btn=$(this);
		likeShareNote(shareId, $btn, function(){
			//收藏成功: 星标置亮并禁止重复点击
			$btn.html('<i class="fa fa-star"></i>');
			$btn.attr("disabled","disabled");
			$btn.attr("title","已收藏");
		});
	});
	
	//--------- 笔记本重命名/删除 ----------
	//笔记本删除按钮(有笔记时alertDeleteBookWindow内部拦截)
	$(document).on("click", "#book_ul .btn_delete", function(){
		$(this).parents("li").children("a").addClass("checked");
		alertDeleteBookWindow();
	});
	//重命名确认
	$(document).on("click", "#modalBasic_4 .sure", function(){
		var $li=$("#book_ul a.checked").parent();
		var notebookId=$li.data("bookId");
		var name=$("#input_notebook_rename").val().trim();
		if(notebookId==null||name==""){
			alert("笔记本名称不能为空");
			return;
		}
		updateBookName(notebookId, name, $li);
	});
	//删除笔记本确认
	$(document).on("click", "#modalBasic_6 .sure", function(){
		var $li=$("#book_ul a.checked").parent();
		var notebookId=$li.data("bookId");
		if(notebookId==null){
			return;
		}
		deleteBook(notebookId, $li);
	});
});
