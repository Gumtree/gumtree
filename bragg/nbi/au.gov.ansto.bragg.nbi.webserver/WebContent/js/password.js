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

function checkCode(){
    var email = getUrlVars()["login_email"];
    var code = getUrlVars()["code"];
    var getUrl = "jython/user?type=PASSWORD&login_email=" + email + "&code=" + code;
    $.get(getUrl, function(data, status) {
        if (status == "success") {
            if (data['result'] == "OK") {
                $("#p_content").html(data['html']);
                $('#password_submit').click(function() {
                    if (checkInputs()){
                        change_password();
                    }
	            });
            } else {
                $("#p_result").html(data['result']);
            }
        }
    })
    .fail(function(e) {
            alert( "error loading password reset page.");
    });
}

function checkInputs(){
    var password = $('#login_password').val();
    if (password.length < 6 || password.length > 12) {
        $("#p_result").html("Password length should be between 6 and 12.");
        return false;
    }
    var repassword = $('#login_repassword').val();
    if (password != repassword) {
        $("#p_result").html("Password doesn't match.");
        return false;
    } 
    return true;
}

function change_password(){
    var email = getUrlVars()["login_email"];
    var code = getUrlVars()["code"];
    var postUrl = "jython/user?type=CHANGEPASSWORD&login_email=" + email + "&code=" + code;
    $.post( postUrl, $("form#password_form").serialize(), function(data, status) {
        if (data['result'] == 'OK'){
            $("#p_result").html("Your password has been successfully changed. Please login to the service with the new password.");
            $("#p_content").html("");
        } else {
            $("#p_result").html(data['result']);
        }
    })
    .fail(function(e) {
        alert( "error submitting the script");
    });
}

jQuery(document).ready(function(){
    checkCode();
});