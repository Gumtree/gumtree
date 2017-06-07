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

function getParam(sParam) {
	var sPageURL = window.location.search.substring(1);
	var sURLVariables = sPageURL.split('&');
	for (var i = 0; i < sURLVariables.length; i++)
	{
		var sParameterName = sURLVariables[i].split('=');
		if (sParameterName[0] == sParam)
		{
			return sParameterName[1];
		}
	}
	return null;
}

function logout(){
    var action = getUrlVars()["action"];
    if (action == "signout"){
        var getUrl = "signin/LOGOUT";
        $.get(getUrl, function(data, status) {
            if (status == "success") {
                if (data['result'] == "OK") {
                    $("#login_result").html("You have successfully signed out. Now jump to the sign in page. "
        			+ "If the browser doesn't redirect automatically, please click <a href=\"signin.html\">here</a>.");
                    setTimeout(function() {
                    	window.location = "signin.html";
        			}, 2000);
                } else {
                    $("#login_result").html(data['result']);
                }
            }
        })
        .fail(function(e) {
                alert( "error in signing out.");
        });
    }
}

function login(){
    var postUrl = "signin/LOGIN";
    $.post( postUrl, $("form#login_form").serialize(), function(data, status) {
//        if (status == "success") {
        if (data['result'] == 'OK'){
            var newUrl = getParam("redirect");
            if (newUrl == null) {
            	newUrl = "home.html";
            }
        	$("#login_result").html("You have signed in successfully. Now jump to the next page. "
        			+ "If the browser doesn't redirect automatically, please click <a href=\"" + newUrl + "\">here</a>.");
            setTimeout(function() {
            	window.location = newUrl;
			}, 2000);
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
        alert( "error submitting the form");
    });
}

jQuery(document).ready(function(){
	var titleText = "Neutron Scattering Instrument Sign in - " + title;
	$(document).attr("title", titleText);
	$('#titleString').text(titleText);

	$('#id_img_sideimage').attr('src', 'images/' + title + '.jpg');
//    logout();
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