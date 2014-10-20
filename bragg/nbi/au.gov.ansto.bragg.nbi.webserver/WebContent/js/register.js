function register(){
    var postUrl = "jython/user?type=REGISTER";
    $.post( postUrl, $("form#register_form").serialize(), function(data, status) {
//        if (status == "success") {
        $("#register_result").html(data['result']);
        if (data['result'] == 'OK'){
            window.location = "pyscript.html";
        }
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
        $("#register_result").html("Please type in a valid email address.");
        return false;
    }
    var password = $('#login_password').val();
    if (password.length < 6 || password.length > 12) {
        $("#register_result").html("Password length should be between 6 and 12.");
        return false;
    }
    var repassword = $('#login_repassword').val();
    if (password != repassword) {
        $("#register_result").html("Password doesn't match.");
        return false;
    } 
    return true;
}

jQuery(document).ready(function(){
    $('#register_submit').click(function() {
        if (checkInputs()){
            register();
        }
	});
    
    $(document).delegate('#login_email', 'keydown', function(e) {
        var keyCode = e.keyCode || e.which;
        if (keyCode == 13) {
            e.preventDefault();
            $('#login_password').focus();
        }
    });
    
    $(document).delegate('#login_password', 'keydown', function(e) {
        var keyCode = e.keyCode || e.which;
        if (keyCode == 13) {
            e.preventDefault();
            $('#login_repassword').focus();
        }
    });
    
    $(document).delegate('#login_repassword', 'keydown', function(e) {
        var keyCode = e.keyCode || e.which;
        if (keyCode == 13) {
            if (checkInputs()){
                register();
            }
        }
    });
});