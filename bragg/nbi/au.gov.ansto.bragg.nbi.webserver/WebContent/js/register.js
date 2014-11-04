var noCodeWarning = "You need an invitation code to register. Please contact IT administrator of the Bragg Institute if you don't have one.";
var hasCodeWarning = "You have been provided with the invitation code. Please set up your password.";

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
    var invCode = $("#register_code").val();
    if (!invCode) {
        $("#register_result").html("Please provide a valid invitation code.");
        return false;
    } 
    return true;
}

function getUrlVars()
{
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

function fillVars(){
    var email = getUrlVars()["login_email"];
    if (email) {
        $('#login_email').val(decodeURIComponent(email));
        $("#login_email").prop('readonly', true);
    }
    var code = getUrlVars()["code"];
    if (code) {
        $('#register_code').val(code);
        $("#register_code").prop('readonly', true);
        $('#div_register_warning').html(hasCodeWarning);
    } else {
        $('#div_register_warning').html(noCodeWarning);
    }
}

jQuery(document).ready(function(){
    fillVars();
    
    $('#register_submit').click(function() {
        if (checkInputs()){
            register();
        }
	});
    
    $(document).delegate('#div_register_warning', 'keydown', function(e) {
        var keyCode = e.keyCode || e.which;
        if (keyCode == 13) {
            e.preventDefault();
            $('#login_email').focus();
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