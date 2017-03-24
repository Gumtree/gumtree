var isLoggedIn = false;
var HOME_PATH = '';
var CURRENT_PATH = '';

function signout(redirect){
	var new_loc;
	if (typeof redirect !== 'undefined') {
		new_loc = HOME_PATH + "home.html?redirect=" + CURRENT_PATH + redirect;
	} else {
		new_loc = HOME_PATH + "home.html";
	}
    var getUrl = HOME_PATH + "signin/LOGOUT";
    $.get(getUrl, function(data, status) {
        if (status == "success") {
            if (data['result'] == "OK") {
            	$("#id_div_main").html("<div class=\"id_span_infoText\">You have successfully signed out. Now jump to the sign in page. "
            			+ "If the browser doesn't redirect automatically, please click <a href=\"" + HOME_PATH + "signin.html\">here</a>.</div>");
            } else {
            	$("#id_div_main").html(data['result']);
            }
        }
    })
    .fail(function(e) {
    	alert( "error in signing out.");
    });
    logout();
    setTimeout(function() {
    	window.location = new_loc;
    }, 500);
}

function updateUserArea(loggedIn, redirect) {
	var redirect = (typeof redirect !== 'undefined') ? redirect : HOME_PATH + 'home.html';
	if (loggedIn == null) {
		$('#id_a_account').html("");
		return;
	}
	if (loggedIn) {
		if (isLoggedIn) {
			return;
		}
		$('#id_a_account').html("<img src=\"images/signout_blue.png\"/>Sign Out ");
		$("#id_a_account").unbind("click");
		$("#id_a_account").click(function() {
			signout(redirect);
		});
	} else {
		$('#id_a_account').html("<img src=\"images/signin.png\"/>Sign In ");
		$("#id_a_account").unbind("click");
		$("#id_a_account").click(function() {
			var win = window.open(HOME_PATH + "signin.html", '_blank');
			win.focus();
		});	
		try {
			showLogoutMessage("User session is expired. Please sign in again.");
		} catch (e) {
		}
	}
}

function logout() {
	
//	var xhr = new XMLHttpRequest();
//	xhr.open("GET", "signin/CLEAR", true);
//	xhr.withCredentials = true;
//	xhr.setRequestHeader("Authorization", 'Basic ' + btoa('myuser:mypswd'));
//	xhr.onload = function () {
//	    console.log(xhr.responseText);
//	};
//	xhr.send();
//	jQuery.ajax({
//            type: "GET",
//            url: "signin/CLEAR",
//            async: false,
//            username: "logmeout",
//            password: "123456",
//            headers: { "Authorization": "Basic" }
//	})
//	.done(function(data){
//	    // If we don't get an error, we actually got an error as we expect an 401!
//		console.log(data);
//	})
//	.fail(function(){
//	    // We expect to get an 401 Unauthorized error! In this case we are successfully 
//            // logged out and we redirect the user.
////	    window.location = "/myapp/index.html";
//		console.log("sign out successfully");
//    });
 
	$.ajax({
	    xhrFields: {
	        withCredentials: true
	    },
	    beforeSend: function (xhr) {
	        xhr.setRequestHeader('Authorization', 'Basic ' + btoa('myuser:mypswd'));
	    },
	    url: "signin/CLEAR"
	});
    return false;
}

function logout(secUrl) {
    
        $.ajax({
            async: false,
            url: secUrl,
            type: 'GET',
            username: 'logout'
        });
}

