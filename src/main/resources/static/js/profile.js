$(function(){
	$(".follow-btn").click(follow);

	$(".manage-btn").click(setManage);
});
function setManage() {
	var userId = $('#entityId').val();
	// alert(userId);
	$.post(
		CONTEX_PATH + "/managed",
		{"userId":userId},
		function (data){
			var data = $.parseJSON(data)
			if (data.code==0){
				window.location.reload();
				// alert(data.msg);
			}else {
				alert(data.msg);
			}
		}
	)

}
function follow() {
	var btn = this;

	// 发送ajax之前，将csrf设置到请求消息头中
	// var token = $("meta[name='_csrf']").attr("content");
	// var header = $("meta[name='_csrf_header']").attr("content");
	// $(document).ajaxSend(function (e,xhr,options) {
	// 	xhr.setRequestHeader(header,token);
	// });

	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
			CONTEX_PATH+"/follow",
			{"entityType":3,"entityId":$(btn).prev().val()},
			function (data) {
				var data = $.parseJSON(data)
				if (data.code==0){
					window.location.reload();
				}else {
					alert(data.msg);
				}
			}
		)
		// $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		$.post(
			CONTEX_PATH+"/unfollow",
			{"entityType":3,"entityId":$(btn).prev().val()},
			function (data) {
				var data = $.parseJSON(data)
				if (data.code==0){
					window.location.reload();
				}else {
					alert(data.msg);
				}
			}
		)
		// $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}

}