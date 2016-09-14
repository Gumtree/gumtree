var checkUserIntervalSeconds = 5;
var checkUserIntervalId;

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
		if (sParameterName[0] == sParam) {
			return sParameterName[1];
		}
	}
	return null;
}

function showLogoutMessage(msg) {
	$("#id_div_main").html("<div class=\"id_span_infoText\">" + msg + "</div>");
	$("#cssmenu").html("");
	alert(msg);
	window.location = "signin.html";
}

function checkUser() {
	var getUrl = "home/user";
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			if (data["status"] == "OK") {
				if (data["user"] != "NONE") {
					updateUserArea(true);
					return;
				}
			}
		} 
		updateUserArea(false);
		stopCheckUser();
	}).fail(function(e) {
		if (e.status == 401) {
			updateUserArea(false);
		}
	});
}

function startCheckUser() {
	checkUserIntervalId = setInterval(function(){
		checkUser();
		}, checkUserIntervalSeconds * 1000);
}

function stopCheckUser() {
	if (checkUserIntervalId != null) {
		clearInterval(checkUserIntervalId);
	}
}

//function logout(){
//    var action = getUrlVars()["action"];
//    if (action == "signout"){
//        var getUrl = "login?type=LOGOUT";
//        $.get(getUrl, function(data, status) {
//            if (status == "success") {
//                if (data['result'] == "OK") {
//                    $("#login_result").html("You have successfully signed out. To use the scripting sevice, please sign in again.");
//                } else {
//                    $("#login_result").html(data['result']);
//                }
//            }
//        })
//        .fail(function(e) {
//                alert( "error loading password reset page.");
//        });
//    }
//}

//function signout(){
//    var getUrl = "signin/LOGOUT";
//    $.get(getUrl, function(data, status) {
//        if (status == "success") {
//            if (data['result'] == "OK") {
//            	$("#id_div_main").html("<div class=\"id_span_infoText\">You have successfully signed out. Now jump to the sign in page. "
//            			+ "If the browser doesn't redirect automatically, please click <a href=\"signin.html\">here</a>.</div>");
//                setTimeout(function() {
//                	window.location = "signin.html";
//    			}, 2000);
//            } else {
//            	$("#id_div_main").html(data['result']);
//            }
//        }
//    })
//    .fail(function(e) {
//    	alert( "error in signing out.");
//    });
//}

jQuery(document).ready(function(){
	var titleText = "Neutron Scattering Instrument - " + title;
	$(document).attr("title", titleText);
	$('#titleString').text(titleText);
	$('#id_img_sideimage').attr('src', 'images/' + title + '.jpg');
	
	var getUrl = "home/menu?" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			var re = data["status"];
			if (re != "OK") {
				$("#id_div_main").html("<div class=\"id_span_infoText\">Status: <span style=\"color:red\">" 
					+ re + "</span>. Now jump to the sign in page. "
	        		+ "If the browser doesn't redirect automatically, please click "
	        		+ "<a href=\"signin.html\">here</a>.</div>");
	            setTimeout(function() {
	            	window.location = "signin.html";
				}, 2000);
	        } else {
				$.each(data["menu"], function(link, text) {
					$("#cssmenu").append('<ul><li><a href="' + link + '">' + text + '</a></li></ul>');
				});
//				$("#id_div_main").text(data["info"]);
				$.each(data["info"], function(link, text) {
					$("#id_div_main").append("<div class=\"id_span_infoText\">" + text + "</div>");
				});
				updateUserArea(true);
				startCheckUser();
			}
		}
	}).fail(function(e) {
		window.location = "signin.html";
	}).always(function() {
	});
	
//	$("#id_a_signout").click(function() {
//		console.log('click');
//		signout();
//	});

});