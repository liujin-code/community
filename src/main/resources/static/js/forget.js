$(function(){
	$("form").submit(check_data);
	$("input").focus(clear_error);
});

function check_data() {
	var pwd1 = $("#your-password").val();
	console.log(pwd1);
	var pwd2 = $("#confirm-password").val();
	console.log(pwd2);
	if(pwd1 != pwd2) {
		$("#confirm-password").addClass("is-invalid");
		return false;
	}
	return true;
}

function clear_error() {
	$(this).removeClass("is-invalid");
}

function getCode() {
	var email = $("#your-email").val();
	$.post(
		CONTEX_PATH+ "/forgetCode",
		{"email":email},
		function(data){
			data = $.parseJSON(data);
			if (data.code==0){
				alert(data.msg);
			}else {
				alert(data.msg);
			}
		}
	)
}