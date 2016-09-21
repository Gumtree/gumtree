var isLoggedIn = false;

function signout(redirect){
	var new_loc;
	if (typeof redirect !== 'undefined') {
		new_loc = "signin.html?redirect=" + redirect;
	} else {
		new_loc = "signin.html";
	}
    var getUrl = "signin/LOGOUT";
    $.get(getUrl, function(data, status) {
        if (status == "success") {
            if (data['result'] == "OK") {
            	$("#id_div_main").html("<div class=\"id_span_infoText\">You have successfully signed out. Now jump to the sign in page. "
            			+ "If the browser doesn't redirect automatically, please click <a href=\"signin.html\">here</a>.</div>");
                setTimeout(function() {
                	window.location = new_loc;
    			}, 500);
            } else {
            	$("#id_div_main").html(data['result']);
            }
        }
    })
    .fail(function(e) {
    	alert( "error in signing out.");
    });
}

function updateUserArea(loggedIn, redirect) {
	var redirect = (typeof redirect !== 'undefined') ? redirect : 'home.html';
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
			var win = window.open("signin.html", '_blank');
			win.focus();
		});	
		try {
			showLogoutMessage("User session is expired. Please sign in again.");
		} catch (e) {
		}
	}
}