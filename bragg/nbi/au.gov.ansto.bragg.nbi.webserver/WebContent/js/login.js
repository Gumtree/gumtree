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

function logout(){
    var action = getUrlVars()["action"];
    if (action == "signout"){
        var getUrl = "jython/user?type=LOGOUT";
        $.get(getUrl, function(data, status) {
            if (status == "success") {
                if (data['result'] == "OK") {
                    $("#login_result").html("You have successfully signed out. To use the scripting sevice, please sign in again.");
                } else {
                    $("#login_result").html(data['result']);
                }
            }
        })
        .fail(function(e) {
                alert( "error loading password reset page.");
        });
    }
}

function login(){
    var postUrl = "jython/user?type=LOGIN";
    $.post( postUrl, $("form#login_form").serialize(), function(data, status) {
//        if (status == "success") {
        if (data['result'] == 'OK'){
        	$("#login_result").html("You have signed in successfully. Now redirect to the reduction service page.");
            window.location = "pyscript.html";
        } else {
        	$("#login_result").html(data['result']);
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

jQuery(document).ready(function(){
    logout();
    $('#login_submit').click(function() {
        login();
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
            login();
        }
    });
});