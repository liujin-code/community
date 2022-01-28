function like(btn, entityType, entityId, targetUserId,postId) {
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
}