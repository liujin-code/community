$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});
function like(btn, entityType, entityId, targetUserId,postId) {
    // 发送ajax之前，将csrf设置到请求消息头中
    // var token = $("meta[name='_csrf']").attr("content");
    // var header = $("meta[name='_csrf_header']").attr("content");
    // $(document).ajaxSend(function (e,xhr,options) {
    //     xhr.setRequestHeader(header,token);
    // });
    $.post(
        CONTEX_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"targetUserId":targetUserId,"postId":postId},
        function (data) {
            var data = $.parseJSON(data)

            if (data.code==0){
                $(btn).children('b').text(data.likeStatus==1?'已赞':"赞");
                $(btn).children("i").text(data.likeCount);
            }else {
                alert(data.msg);
            }
        }
    )
};

// 置顶
function setTop() {

    $.post(
        CONTEX_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                window.location.reload();
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful() {

    $.post(
        CONTEX_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                window.location.reload();
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {

    $.post(
        CONTEX_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                location.href = CONTEX_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}