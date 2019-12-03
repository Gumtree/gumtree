var timeString = (new Date()).getTime();
var getUrl;
var monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
var curSession = null;
//var isLoggedIn = false;

//'<img src="images/edit.png" onclick="edit(\'' + pair[1] + '\')"/>

function getParam(sParam) {
	var sPageURL = window.location.search.substring(1);
	var sURLVariables = sPageURL.split('&');
	for (var i = 0; i < sURLVariables.length; i++)
	{
		var vpair = sURLVariables[i];
		var vidx = vpair.indexOf('=');
		if (vidx > 0 && vpair.slice(0, vidx) == sParam) {
			return vpair.slice(vidx + 1);
		}
	}
	return null;
}

function getPdf(page, session){
	var getUrl = "notebook/pdf";
	if (typeof(session) !== "undefined") { 
		 getUrl += "?session=" + session + "&" + (new Date()).getTime();
	} else {
		getUrl +=  "?" + (new Date()).getTime();
	}
//    window.location.href = getUrl;
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			updateUserArea(true, 'notebookAdmin.html');
			var pair = data.split(":");
			var fileUrl = "notebook/download/" + pair[0] + ".pdf?ext=" + pair[1];
			if (typeof(session) !== "undefined") { 
				 fileUrl += "&session=" + session;
			}
		  setTimeout(function() {
			  $.fileDownload(fileUrl)
			  .done(function () {})
			  .fail(function () { alert('File download failed!'); });				
		  }, 1500);
		} 
	})
	.fail(function(e) {
		console.log(e.statusText);
		alert( "error creating PDF file for " + page + ".");
	});
}

function getWord(page, session) {
	var getUrl = "notebook/load";
	if (typeof(session) !== "undefined") { 
		 getUrl += "?session=" + session + "&" + (new Date()).getTime();
	} else {
		getUrl += "?" + (new Date()).getTime()
	}
	$.get(getUrl, function(data, status) {
		if (status == "success") {
//			var data = CKEDITOR.instances.id_editable_inner.getData();
		    var converted = htmlDocx.asBlob(data);
		    var fn = "Quokka_Notebook";
		    if (page != null) {
		    	fn = page;
		    }
		    fn += ".docx";
		    saveAs(converted, fn);
		} else {
			alert( "error downloading Word file.");
		}
	})
	.fail(function(e) {
		alert( "error downloading Word file.");
	});
}

function highlight(id) {
	if (curSession != null) {
		$('#psort_' + curSession).css("color", "#eee");
		$('#tsort_' + curSession).css("color", "#eee");
	}
	$('#psort_' + id).css("color", "orange");
	$('#tsort_' + id).css("color", "orange");
	curSession = id;
}

function load(id, name, proposal, pattern) {
	highlight(id);
	getUrl = "notebook/load?session=" + id;
	if (typeof(pattern) !== "undefined") { 
		 getUrl += "&pattern=" + pattern;
	}
	getUrl += "&" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			updateUserArea(true, 'notebookAdmin.html');
			if (typeof(proposal) !== "undefined" && $.isNumeric(proposal)) { 
				 var text = 'P' + proposal + ": " + name;
			}			
			$('#id_content_header').html('<span>' + text + '</span><a class="class_div_button" onclick="edit(\'' 
					+ id + '\')">Edit</a>&nbsp;<a class="class_div_button" onclick="getPdf(\'' + name 
					+ '\', \'' + id + '\')">PDF</a>&nbsp;<a class="class_div_button" onclick="getWord(\'' + name 
					+ '\', \'' + id + '\')">WORD</a><div class="id_div_busyIndicator">&nbsp;</div>');
			$('#id_div_content').html(data);
		}
	})
	.fail(function(e) {
		if (e.status == 401) {
			updateUserArea(false);
		} else {
			alert( "error loading notebook file " + id + ".");
		}
	});
}

function loadCurrent(id, name, proposal) {
	getUrl = "notebook/load?session=" + id;
	getUrl += "&" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			updateUserArea(true, 'notebookAdmin.html');
			var currentPath = window.location.href;
			currentPath = currentPath.substr(0, currentPath.lastIndexOf('/') + 1);
//			currentPath = currentPath.substr(0, currentPath.lastIndexOf('/') + 1);
			if (typeof(proposal) !== "undefined" && $.isNumeric(proposal)) {
				$('#id_content_header').html('<a href="' + currentPath + 'notebook.html?session=' + id 
						+ '">P' + proposal + '</a>: <div class="class_span_currentpath">' + currentPath + 'notebook.html?session=' + id + '</div>');
			} else {			
				$('#id_content_header').html('<a href="' + currentPath + 'notebook.html?session=' + id 
						+ '">Link</a>: <div class="class_span_currentpath">' + currentPath + 'notebook.html?session=' + id + '</div>');
			}
			$('#id_div_content').html(data);
		}
	})
	.fail(function(e) {
		if (e.status == 401) {
			updateUserArea(false);
		} else {
			alert( "error loading notebook file " + id + ".");
		}
	});
}

function edit(id) {
	if (id == null) {
		getUrl = "notebook.html";
		var win = window.open(getUrl, '_blank');
		win.focus();
	} else {
		getUrl = "notebook.html?session=" + id;
		var win = window.open(getUrl, '_blank');
		win.focus();
	}
}

function searchNotebook() {
	var searchPattern = encodeURIComponent($("#id_input_search").val());
	getUrl = "notebook/search?pattern=" + searchPattern + "&" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			$('#id_search_inner').html(data);
			$('.class_div_search_file').click(function(e) {
				load($(this).attr('session'), $(this).attr('name'), $(this).attr('proposal'), searchPattern);
			});
		}
	})
	.fail(function(e) {
		if (e.status == 401) {
			updateUserArea(false);
		} else {
			alert( "error searching the notebook.");
		}
	});	
}

function searchDatabase() {
	var searchPattern = encodeURIComponent($("#id_input_search_db").val());
	getUrl = "db/searchAll?pattern=" + searchPattern + "&" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			updateUserArea(true, 'notebookAdmin.html');
			$('#id_search_db_inner').html(data);
//			$('.class_div_search_file').click(function(e) {
//				load($(this).attr('session'), $(this).attr('name'), searchPattern);
//			});
		}
	})
	.fail(function(e) {
		if (e.status == 401) {
			updateUserArea(false);
		} else {
			alert( "error searching the database.");
		}
	});	
}

function rgbToHsl(r, g, b) {
	r /= 255, g /= 255, b /= 255;
	var max = Math.max(r, g, b), min = Math.min(r, g, b);
	var h, s, l = (max + min) / 2;

	if(max == min){
		h = s = 0;
	}
	else {
		var d = max - min;
		s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
		switch(max){
		case r: h = (g - b) / d + (g < b ? 6 : 0); break;
		case g: h = (b - r) / d + 2; break;
		case b: h = (r - g) / d + 4; break;
		}
		h /= 6;
	}
	return l;
}

function getColor() {
	var r, g, b;
	var textColor = $('#cssmenu').css('color');
	textColor = textColor.slice(4);
	r = textColor.slice(0, textColor.indexOf(','));
	textColor = textColor.slice(textColor.indexOf(' ') + 1);
	g = textColor.slice(0, textColor.indexOf(','));
	textColor = textColor.slice(textColor.indexOf(' ') + 1);
	b = textColor.slice(0, textColor.indexOf(')'));
	var l = rgbToHsl(r, g, b);
	if (l > 0.7) {
		$('#cssmenu>ul>li>a').css('text-shadow', '0 1px 1px rgba(0, 0, 0, .35)');
		$('#cssmenu>ul>li>a>span').css('border-color', 'rgba(0, 0, 0, .35)');
	}
	else
	{
		$('#cssmenu>ul>li>a').css('text-shadow', '0 1px 0 rgba(255, 255, 255, .35)');
		$('#cssmenu>ul>li>a>span').css('border-color', 'rgba(255, 255, 255, .35)');
	}
}

function showLogoutMessage(msg) {
	alert(msg);
}

//function signout(){
//    var getUrl = "signin/LOGOUT";
//    $.get(getUrl, function(data, status) {
//        if (status == "success") {
//            if (data['result'] == "OK") {
//            	$("#id_div_main").html("<div class=\"id_span_infoText\">You have successfully signed out. Now jump to the sign in page. "
//            			+ "If the browser doesn't redirect automatically, please click <a href=\"signin.html\">here</a>.</div>");
//                setTimeout(function() {
//                	window.location = "signin.html?redirect=notebookAdmin.html";
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

//function updateUserArea(loggedIn) {
//	if (loggedIn) {
//		if (isLoggedIn) {
//			return;
//		}
//		$('#id_a_signout').html("<img src=\"images/signout_blue.png\"/>Sign Out ");
//		$("#id_a_signout").unbind("click");
//		$("#id_a_signout").click(function() {
//			signout();
//		});
//	} else {
//		$('#id_a_signout').html("<img src=\"images/signin.png\"/>Sign In ");
//		$("#id_a_signout").unbind("click");
//		$("#id_a_signout").click(function() {
//			var win = window.open("signin.html", '_blank');
//			win.focus();
//		});	
//		alert( "User session is expired. Please sign in again.");
//	}
//}

$(function() {
	
	$(window).resize(function() {
	    var bodyheight = $(window).height();
		$(".slide-out-div").height(bodyheight - 80);
		$(".div_sidebar_inner").height(bodyheight - 118);
	});

    $('.slide-out-div').tabSlideOut({
    	tabHandleClass: '.a_sidebar_handle',
    	tabBlockClass: '.div_sidebar_block',
        tabHandles: ['#a_sidebar_search', '#a_sidebar_search_db'],                     //class of the element that will become your tab
        tabBlocks: ['#div_sidebar_search', '#div_sidebar_search_db'],
        tabHandleSize: 180,
        pathToTabImage: $('html').hasClass('ie9') ? ['images/Database.GIF', 'images/Database.GIF'] : null, //path to the image for the tab //Optionally can be set using css
        imageHeight: '218px',                     //height of tab image           //Optionally can be set using css
        imageWidth: '33px',                       //width of tab image            //Optionally can be set using css
        tabLocation: 'right',                      //side of screen where tab lives, top, right, bottom, or left
        speed: 300,                               //speed of animation
        action: 'click',                          //options: 'click' or 'hover', action to trigger animation
        topPos: '80px',                          //position from the top/ use if tabLocation is left or right
        leftPos: '20px',                          //position from left/ use if tabLocation is bottom or top
        fixedPosition: true                      //options: true makes it stick(fixed position) on scroll
    });
	
    $('#id_input_search').keyup(function(e){
        if(e.keyCode == 13)
        {
            searchNotebook();
        }
    });
    
    $('#id_input_search_db').keyup(function(e){
        if(e.keyCode == 13)
        {
            searchDatabase();
        }
    });
    
	$("#id_a_newbook").click(function(e) {
		
		$('<div></div>').appendTo('body')
		  .html('<div class="class_confirm_dialog">Please provide the proposal ID for the new page: <input type="text" id="id_input_proposal_id" placeholder="Proposal ID">'
				  + '</div><br><div><p>To continue, press Yes button. The current notebook page will be put into archive list.</p></div>')
		  .dialog({
		      modal: true, title: 'Confirm Creating New Notebook', zIndex: 10000, autoOpen: true,
		      width: 'auto', resizable: false,
		      buttons: {
		          Yes: function () {
		        	  	var proposalId = $('#id_input_proposal_id').val();
		        	  	if (proposalId == null || proposalId.trim().length ==0 || isNaN(proposalId)) {
		        	  		alert("A valid proposal ID is required.");
		        	  		return;
		        	  	}
			      		var getUrl = "notebook/new?proposal_id=" + proposalId + "&" + (new Date()).getTime();
			    		$.get(getUrl, function(data, status) {
			    			if (status == "success") {
			    				updateUserArea(true, 'notebookAdmin.html');
			    				var split = data.indexOf("=");
			    				var header = data.substr(0, split);
			    				var text = "";
			    				if (data.length > split + 1) {
			    					text = data.substr(split + 1)
			    				}
			    				var pair = header.split(";");
			    				var oldInfo = pair[0].split(":");
			    				var oldSessionId = oldInfo[0];
			    				var oldPageId = oldInfo[1];
			    				var oldProposalId = oldInfo[2];
			    				if (oldProposalId == 'null') {
			    					oldProposalId = 'Stand_Alone_Pages';
			    				}
			    				var newInfo = pair[1].split(":");
			    				var newSessionId = newInfo[0];
			    				var newPageId = newInfo[1];
			    				var newProposalId = newInfo[2];
			    				$('#id_content_header').html('<span>Current Notebook Page</span><a class="class_div_button" onclick="edit(null)">Edit</a>'
			    						+ '&nbsp;<a class="class_div_button" onclick="getPdf(\'' + newPageId 
			    						+ '\')">PDF</a>&nbsp;<a class="class_div_button" onclick="getWord(\'' + newPageId 
			    						+ '\')">WORD</a><div class="id_div_busyIndicator">&nbsp;</div>');
			    				$('#id_div_content').html(text);
			    				$("#id_a_reviewCurrent").html('Current Page - P' + newProposalId + '<span class="holder"></span>');
			    				$("#id_a_reviewCurrent").unbind('click').click(function() {
			    					$(this).removeAttr('href');
									var element = $(this).parent('li');
									if (element.hasClass('open')) {
										element.removeClass('open');
										element.find('li').removeClass('open');
										element.find('ul').slideUp();
									} else {
										element.addClass('open');
										element.children('ul').slideDown();
										element.siblings('li').children('ul').slideUp();
										element.siblings('li').removeClass('open');
										element.siblings('li').find('li').removeClass('open');
										element.siblings('li').find('ul').slideUp();
									}
									load(newSessionId, newPageId, newProposalId);
			    				});
			    				$("#id_ul_currentpage").empty().append('<li><a id="psort_' + newSessionId + '" onclick="loadCurrent(\'' + newSessionId + '\', \'' 
			    						+ newPageId + '\', \'' + newProposalId + '\')">&nbsp;&nbsp;--&nbsp;' + newPageId + '</a></li>');
			    				if (oldSessionId != null && oldSessionId != '') {
			    					if (document.getElementById("id_proposal_" + oldProposalId) !== null) {
			    						html = '<li><a id="psort_' + oldSessionId + '" onclick="load(\'' + oldSessionId + '\', \''
			    							+ oldPageId + '\', \'' + oldProposalId + '\')">&nbsp;&nbsp;--&nbsp;' + oldPageId + '</a></li>';
			    						$('#' + oldProposalId + '_ul').prepend(html);
			    					} else {
//			    						var html = '<li class="active has-sub"><a id="id_proposal_' + oldProposalId + '" onclick="load(\'' + oldSessionId + '\', \'' 
//											+ oldPageId + '\', \'' + oldProposalId + '\')">&nbsp;-&nbsp;Proposal - ' + oldProposalId + '</a>';
				    					var html = '<li class="active has-sub"><a id="id_proposal_' + oldProposalId + '">&nbsp;-&nbsp;Proposal - ' + oldProposalId + '</a>';
										html += '<ul id="' + oldProposalId + '_ul">' + '<li><a id="psort_' + oldSessionId + '" onclick="load(\'' + oldSessionId + '\', \'' 
											+ oldPageId + '\', \'' + oldProposalId + '\')">&nbsp;&nbsp;--&nbsp;' + oldPageId + '</a></li></ul>';
										html += '</li>';
										$("#id_ul_archiveList").prepend(html);
										$('#id_proposal_' + oldProposalId).on('click', function(){
											$(this).removeAttr('href');
											var element = $(this).parent('li');
											if (element.hasClass('open')) {
												element.removeClass('open');
												element.find('li').removeClass('open');
												element.find('ul').slideUp();
											} else {
												element.addClass('open');
												element.children('ul').slideDown();
												element.siblings('li').children('ul').slideUp();
												element.siblings('li').removeClass('open');
												element.siblings('li').find('li').removeClass('open');
												element.siblings('li').find('ul').slideUp();
											}
										});

//										$('#id_proposal_' + oldProposalId).append('<span class="holder"></span>');
			    					}
			    					var m = oldPageId.substr(5, 7);
			    					var mid = "id_month_" + m;
			    					if (document.getElementById(mid) !== null) {
			    						html = '<li><a id="tsort_' + oldSessionId + '" onclick="load(\'' + oldSessionId + '\', \''
			    						+ oldPageId + '\', \'' + oldProposalId + '\')"><font color="#aaa">' + oldProposalId + '</font>:' + oldPageId + '</a></li>';
			    						$('#' + m + '_ul').prepend(html);
			    					} else {
			    						var gv = monthNames[parseInt(m.substr(5, 2)) - 1] + ' ' + m.substr(0, 4);
			    						html = '<li class="active has-sub"><a id="' + mid + '">&nbsp;-&nbsp;' + gv + '</a>' 
			    						+ '<ul id="' + m + '_ul">';
			    						html += '<li><a id="tsort_' + oldSessionId + '" onclick="load(\'' + oldSessionId + '\', \''
			    						+ oldPageId + '\', \'' + oldProposalId + '\')"><font color="#aaa">' + oldProposalId + '</font>:' + oldPageId + '</a></li>';
			    						html += '</ul></li>';
			    						$("#id_ul_timeSorted").prepend(html);
			    						$('#' + mid).on('click', function(){
											$(this).removeAttr('href');
											var element = $(this).parent('li');
											if (element.hasClass('open')) {
												element.removeClass('open');
												element.find('li').removeClass('open');
												element.find('ul').slideUp();
											} else {
												element.addClass('open');
												element.children('ul').slideDown();
												element.siblings('li').children('ul').slideUp();
												element.siblings('li').removeClass('open');
												element.siblings('li').find('li').removeClass('open');
												element.siblings('li').find('ul').slideUp();
											}
										});
			    					}
			    				}
			    				highlight(newSessionId);
			    			}
			    		})
			    		.fail(function(e) {
			    			if (e.status == 401) {
			    				updateUserArea(false);
			    			} else {
				    			alert( "error creating new notebook file.");
			    			}
			    		});
				    		
				    	$(this).dialog("close");
		          },
		          No: function () {
		              $(this).dialog("close");
		          }
		      },
		      close: function (event, ui) {
		          $(this).remove();
		      }
		});
		
	    $("#id_input_proposal_id").keydown(function (event) {
	        if (event.keyCode == 13) {
	            $(this).parent().parent().parent()
	                   .find("button:eq(1)").trigger("click");
//	        	console.log($(this).parent().parent().parent()
//		                   .find("button:eq(1)").html());
	            return false;
	        }
	    });
	    
	});
});

$(function() {
	$("#id_a_manageGuide").click(function(e) {
		var getUrl = "notebook/manageguide";
		$.get(getUrl, function(data, status) {
			if (status == "success") {
				updateUserArea(true, 'notebookAdmin.html');
				$('#id_content_header').html("<span>User's Guide</span>");
				if (data.trim().length == 0) {
					$('#id_div_content').html("<p><br></p>");
				} else {
					$('#id_div_content').html(data);
				}
			}
		})
		.fail(function(e) {
			if (e.status == 401) {
				updateUserArea(false);
			} else {
				alert( "error loading the guide.");
			}
		});

	});
	
//	$("#id_a_reviewCurrent").click(function(e) {
//		var getUrl = "notebook/load?" + (new Date()).getTime();
//		$.get(getUrl, function(data, status) {
//			if (status == "success") {
//				$('#id_content_header').html('<span>Current Notebook Page</span><a class="class_div_button" onclick="edit(null)">Edit</a>');
//				if (data.trim().length == 0) {
//					$('#id_div_content').html("<p><br></p>");
//				} else {
//					$('#id_div_content').html(data);
//				}
//			}
//		})
//		.fail(function(e) {
//			alert( "error loading current notebook file.");
//		});
//	});

});

//jQuery(document).ready(function() {
jQuery(window).load(function () {
	
	jQuery(document).ajaxStart(function () {
		//show ajax indicator
		$('.id_div_busyIndicator').show();
	}).ajaxStop(function () {
		//hide ajax indicator
		$('.id_div_busyIndicator').hide();
	});
	
	var notebookTitle = 'Manage Notebook - ' + title;
	$(document).attr("title", notebookTitle);
	$('#id_span_header').text(notebookTitle);
	$('#id_div_print_header').html("<h1>Instrument Notebook - " + title + "</h1>");
	
	var bodyheight = $(window).height();
	$(".slide-out-div").height(bodyheight - 80);
	$(".div_sidebar_inner").height(bodyheight - 118);

	var getUrl = "notebook/archive?" + timeString;
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			if (data != null){
//				var files = data.split(";");
//				$.each(files, function(idx, val) {
//					if (val.trim().length > 0) {
//						var pair = val.split(":");
//						$("#id_ul_archiveList").append('<li><a id="' + pair[0] + '" onclick="load(\'' + pair[1] + '\', \'' 
//								+ pair[0] + '\')">&nbsp;-&nbsp;' + pair[0] + '</a></li>');
//					}
//				});
				var html = "";
				var obj = $.parseJSON(data);
				var st = [];
				$.each(obj, function(proposalName, sessions) {
					var proposalId = proposalName.replace(new RegExp(' ', 'g'), '_');
					if (Object.keys(sessions).length == 1) {
						var sessionId = Object.keys(sessions)[0];
						var pageId = sessions[sessionId];
						var part = '';
//						part += '<li class="active has-sub"><a id="id_proposal_' + proposalId + '" onclick="load(\'' + sessionId + '\', \'' 
//							+ pageId + '\', \'' + proposalId + '\')">&nbsp;-&nbsp;Proposal - ' + proposalId + '</a>';
						part += '<li class="active has-sub"><a id="id_proposal_' + proposalId + '">&nbsp;-&nbsp;Proposal - ' + proposalId + '</a>';
						part += '<ul id="' + proposalId + '_ul">' + '<li><a id="psort_' + sessionId + '" onclick="load(\'' + sessionId + '\', \'' 
							+ pageId + '\', \'' + proposalId + '\')">&nbsp;&nbsp;--&nbsp;' + pageId + '</a></li></ul>';
						part += '</li>';
						html = part + html;
						var d = {
							page : pageId,
							session : sessionId,
							proposal : proposalId
						};
						var idx = st.length;
						for (var i = 0; i < st.length; i ++) {
							if (pageId.localeCompare(st[i].page) >= 0) {
								idx = i;
								break;
							}
						}
						st.splice(idx, 0, d);
					} else if (Object.keys(sessions).length > 1) {
						var proposalName = 'Proposal - ' + proposalId;
						if (proposalId == 'Stand_Alone_Pages') {
							proposalName = 'Stand Alone Pages';
						}
						var part = '';
						part += '<li class="active has-sub"><a id="id_proposal_' + proposalId + '">&nbsp;-&nbsp;' + proposalName + '</a><ul id="' + proposalId + '_ul">';
						var sub = '';
						$.each(sessions, function(sessionId, pageId) {
							sub = '<li><a id="psort_' + sessionId + '" onclick="load(\'' + sessionId + '\', \''
								+ pageId + '\', \'' + proposalId + '\')">&nbsp;&nbsp;--&nbsp;' + pageId + '</a></li>' + sub;
							var d = {
									page : pageId,
									session : sessionId,
									proposal : proposalId
							};
							var idx = st.length;
							for (var i = 0; i < st.length; i ++) {
								if (pageId.localeCompare(st[i].page) >= 0) {
									idx = i;
									break;
								}
							}
							st.splice(idx, 0, d);
						});
						part += sub + '</ul></li>';
						if (proposalId == 'Stand_Alone_Pages') {
							html += part;
						} else {
							html = part + html;
						}
					}					
				});
				$("#id_ul_archiveList").append(html);
				$('#id_ul_archiveList li.has-sub>a').on('click', function(){
					$(this).removeAttr('href');
					var element = $(this).parent('li');
					if (element.hasClass('open')) {
						element.removeClass('open');
						element.find('li').removeClass('open');
						element.find('ul').slideUp();
					} else {
						element.addClass('open');
						element.children('ul').slideDown();
						element.siblings('li').children('ul').slideUp();
						element.siblings('li').removeClass('open');
						element.siblings('li').find('li').removeClass('open');
						element.siblings('li').find('ul').slideUp();
					}
				});
				html = "";
				if (st.length > 0 ){
					var g = '';
					for (var i = 0; i < st.length; i ++) {
						var d = st[i];
						var m = d.page.substr(5, 7);
						if (g != m) {
							if (g != '') {
								html += '</ul></li>';
							}
							g = m;
							var gv = monthNames[parseInt(g.substr(5, 2)) - 1] + ' ' + g.substr(0, 4);
							html += '<li class="active has-sub"><a id="id_month_' + g + '">&nbsp;-&nbsp;' + gv + '</a>' 
								+ '<ul id="' + g + '_ul">';
						}
						html += '<li><a id="tsort_' + d.session + '" onclick="load(\'' + d.session + '\', \''
							+ d.page + '\', \'' + d.proposal + '\')"><font color="#aaa">' + d.proposal + '</font>:' + d.page + '</a></li>';
					}
					if (html != '') {
						html += '</ul></li>';
					}
					$("#id_ul_timeSorted").append(html);
					$('#id_ul_timeSorted li.has-sub>a').on('click', function(){
						$(this).removeAttr('href');
						var element = $(this).parent('li');
						if (element.hasClass('open')) {
							element.removeClass('open');
							element.find('li').removeClass('open');
							element.find('ul').slideUp();
						} else {
							element.addClass('open');
							element.children('ul').slideDown();
							element.siblings('li').children('ul').slideUp();
							element.siblings('li').removeClass('open');
							element.siblings('li').find('li').removeClass('open');
							element.siblings('li').find('ul').slideUp();
						}
					});
				}
//				$('#id_ul_archiveList>li.has-sub>a').append('<span class="holder"></span>');
			}
			updateUserArea(true, 'notebookAdmin.html');
		}
	})
	.fail(function(e) {
		if (e.status == 401) {
//			window.location = "signin.html?redirect=notebookAdmin.html";
			console.log(e);
		}
	});
	
	getUrl = "notebook/currentpage?" + timeString;
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			var splitIndex = data.indexOf(":");
			var sessionId = data.substr(0, splitIndex);
			data = data.substr(splitIndex + 1);
			splitIndex = data.indexOf(":");
			var pageId = data.substr(0, splitIndex);
			data = data.substr(splitIndex + 1);
			splitIndex = data.indexOf(":");
			var proposalId = data.substr(0, splitIndex);
			if (proposalId == 'null') {
				proposalId = "N/A";
			}
			if (splitIndex < data.length) {
				data = data.substr(splitIndex + 1);
			} else {
				data = "";
			}
			$("#id_a_reviewCurrent").html('Current Page - P' + proposalId + '<span class="holder"></span>');
			$("#id_a_reviewCurrent").click(function() {
				load(sessionId, pageId, proposalId);
			});
			$("#id_ul_currentpage").append('<li><a id="' + sessionId + '" onclick="loadCurrent(\'' + sessionId + '\', \'' 
					+ pageId + '\', \'' + proposalId + '\')">&nbsp;&nbsp;--&nbsp;' + pageId + '</a></li>');
			if ($.isNumeric(proposalId)) {
				$('#id_content_header').html('<span>P' + proposalId + ' - Current Notebook Page</span><a class="class_div_button" onclick="edit(null)">Edit</a>'
						+ '&nbsp;<a class="class_div_button" onclick="getPdf(\'' + pageId 
						+ '\')">PDF</a>&nbsp;<a class="class_div_button" onclick="getWord(\'' + pageId 
						+ '\')">WORD</a><div class="id_div_busyIndicator">&nbsp;</div>');
			} else {
				$('#id_content_header').html('<span>Current Notebook Page</span><a class="class_div_button" onclick="edit(null)">Edit</a>'
						+ '&nbsp;<a class="class_div_button" onclick="getPdf(\'' + pageId + '\', \'' + sessionId 
						+ '\')">PDF</a>&nbsp;<a class="class_div_button" onclick="getWord(\'' + pageId + '\', \'' + sessionId 
						+ '\')">WORD</a><div class="id_div_busyIndicator">&nbsp;</div>');
			}
			if (data.trim().length == 0) {
				$('#id_div_content').html("<p><br></p>");
			} else {
				$('#id_div_content').html(data);
			}
		}
	})
	.fail(function(e) {
		if (e.status == 401) {
//			window.location = "signin.html?redirect=notebookAdmin.html";
			console.log(e);
		}
	});


	$('#cssmenu li.has-sub>a').on('click', function(){
		$(this).removeAttr('href');
		var element = $(this).parent('li');
		if (element.hasClass('open')) {
			element.removeClass('open');
			element.find('li').removeClass('open');
			element.find('ul').slideUp();
		} else {
			element.addClass('open');
			element.children('ul').slideDown();
			element.siblings('li').children('ul').slideUp();
			element.siblings('li').removeClass('open');
			element.siblings('li').find('li').removeClass('open');
			element.siblings('li').find('ul').slideUp();
		}
	});

	$('#cssmenu>ul>li.has-sub>a').append('<span class="holder"></span>');

	getColor();


});