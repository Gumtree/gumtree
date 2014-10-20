function reset(){
    var postUrl = "jython/user?type=RESET";
    $.post( postUrl, $("form#reset_form").serialize(), function(data, status) {
//        if (status == "success") {
        $("#reset_result").html(data['result']);
//            processStatus(data);
//            if (data['status'] == "BUSY"){
//                    setUpdateInterval();
//            }
//            if (data['error'] == null || data['error'].trim().length == 0){
//                $("#tab2").click();
//                window.location = $('#tab2').attr('href');
//            }
//        }
    })
    .fail(function(e) {
        alert( "error submitting the script");
    });
}

function checkInputs(){
    var email = $('#login_email').val();
    if (email.indexOf('@') <= 0 || email.indexOf('.') <= 0) {
        $("#reset_result").html("Please type in a valid email address.");
        return false;
    }
    return true;
}

jQuery(document).ready(function(){
    $('#reset_submit').click(function() {
        if (checkInputs()){
            reset();
        }
	});
    
    $(document).delegate('#login_email', 'keydown', function(e) {
        var keyCode = e.keyCode || e.which;
        if (keyCode == 13) {
            e.preventDefault();
            if (checkInputs()){
                reset();
            }
        }
    });
});