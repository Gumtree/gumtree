var timeString = (new Date()).getTime();
var getUrl;

//'<img src="../images/edit.png" onclick="edit(\'' + pair[1] + '\')"/>

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
}

function load(id, name, pattern) {
	getUrl = "../notebook/load?session=" + id;
	if (typeof(pattern) !== "undefined") { 
		 getUrl += "&pattern=" + pattern;
	}
	getUrl += "&" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			$('#id_content_header').html('<span>' + name + '</span><a class="class_div_button" onclick="edit(\'' + id + '\')">Edit</a>');
			$('#id_div_content').html(data);
		}
	})
	.fail(function(e) {
		alert( "error loading notebook file " + id + ".");
	});
}

function edit(id) {
	if (id == null) {
		getUrl = "../notebook.html";
		var win = window.open(getUrl, '_blank');
		win.focus();
	} else {
		getUrl = "../notebook.html?session=" + id;
		var win = window.open(getUrl, '_blank');
		win.focus();
	}
}

function searchNotebook() {
	var searchPattern = encodeURIComponent($("#id_input_search").val());
	getUrl = "../notebook/search?pattern=" + searchPattern + "&" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			$('#id_search_inner').html(data);
			$('.class_div_search_file').click(function(e) {
				load($(this).attr('session'), $(this).attr('name'), searchPattern);
			});
		}
	})
	.fail(function(e) {
		alert( "error searching notebook files.");
	});	
}

function searchDatabase() {
	var searchPattern = encodeURIComponent($("#id_input_search_db").val());
	getUrl = "../db/searchAll?pattern=" + searchPattern + "&" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			$('#id_search_db_inner').html(data);
//			$('.class_div_search_file').click(function(e) {
//				load($(this).attr('session'), $(this).attr('name'), searchPattern);
//			});
		}
	})
	.fail(function(e) {
		alert( "error searching notebook files.");
	});	
}

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
        tabHandleSize: 200,
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
		  .html('<div><h6>Please confirm creating new notebook file. The current notebook file will be put into archive list.</h6></div>')
		  .dialog({
		      modal: true, title: 'Confirm Creating New Notebook', zIndex: 10000, autoOpen: true,
		      width: 'auto', resizable: false,
		      buttons: {
		          Yes: function () {
			      		var getUrl = "../notebook/new?" + (new Date()).getTime();
			    		$.get(getUrl, function(data, status) {
			    			if (status == "success") {
			    				$('#id_content_header').html('<span>Current Notebook Page</span><a class="class_div_button" onclick="edit(null)">Edit</a>');
			    				$('#id_div_content').html("<p><br></p>");
			    				var pair = data.split(";");
			    				pair = pair[0].split(":");
			    				$("#id_ul_archiveList").prepend('<li><a id="' + pair[0] + '" onclick="load(\'' + pair[1] + '\', \'' 
			    						+ pair[0] + '\')">&nbsp;-&nbsp;' + pair[0] + '</a></li>');
			    			}
			    		})
			    		.fail(function(e) {
			    			alert( "error creating new notebook file.");
			    		});
//				      		getUrl = "../db/close?" + (new Date()).getTime();
//				    		$.get(getUrl, function(data, status) {
//				    			if (status == "success") {
//	//			    				$('#id_div_content').html("<p><br></p>");
//	//			    				$("#id_ul_archiveList").prepend('<li><a id="' + data + '" onclick="load(\'' + data + '\')">&nbsp;-&nbsp;' + data + '</a></li>');
//				    			}
//				    		})
//				    		.fail(function(e) {
//				    			alert( "error close database file.");
//				    		})
//				    		.always(function(e) {
//				    		})
				    		
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
		
	});
});

$(function() {
	$("#id_a_manageGuide").click(function(e) {
		var getUrl = "../notebook/manageguide";
		$.get(getUrl, function(data, status) {
			if (status == "success") {
				$('#id_content_header').html("<span>User's Guide</span>");
				if (data.trim().length == 0) {
					$('#id_div_content').html("<p><br></p>");
				} else {
					$('#id_div_content').html(data);
				}
			}
		})
		.fail(function(e) {
			alert( "error loading current notebook file.");
		});

	});
	
	$("#id_a_reviewCurrent").click(function(e) {
		var getUrl = "../notebook/load?" + (new Date()).getTime();
		$.get(getUrl, function(data, status) {
			if (status == "success") {
				$('#id_content_header').html('<span>Current Notebook Page</span><a class="class_div_button" onclick="edit(null)">Edit</a>');
				if (data.trim().length == 0) {
					$('#id_div_content').html("<p><br></p>");
				} else {
					$('#id_div_content').html(data);
				}
			}
		})
		.fail(function(e) {
			alert( "error loading current notebook file.");
		});
	});

});

jQuery(document).ready(function() {
	
	var notebookTitle = 'Manage Notebook - ' + title;
	$(document).attr("title", notebookTitle);
	$('#id_div_header').html("<span>" + notebookTitle + "</span>");
	$('#id_div_print_header').html("<h1>Instrument Notebook - " + title + "</h1>");
	
	var bodyheight = $(window).height();
	$(".slide-out-div").height(bodyheight - 80);
	$(".div_sidebar_inner").height(bodyheight - 118);

	var getUrl = "../notebook/archive?" + timeString;
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			if (data.trim().length > 0){
				var files = data.split(";");
				$.each(files, function(idx, val) {
					if (val.trim().length > 0) {
						var pair = val.split(":");
						$("#id_ul_archiveList").append('<li><a id="' + pair[0] + '" onclick="load(\'' + pair[1] + '\', \'' 
								+ pair[0] + '\')">&nbsp;-&nbsp;' + pair[0] + '</a></li>');
					}
				});
			}
		}
	})
	.fail(function(e) {
		alert( "error loading archive notebook files.");
	});
	
	getUrl = "../notebook/load?" + timeString;
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			$('#id_content_header').html('<span>Current Notebook Page</span><a class="class_div_button" onclick="edit(null)">Edit</a>');
			if (data.trim().length == 0) {
				$('#id_div_content').html("<p><br></p>");
			} else {
				$('#id_div_content').html(data);
			}
		}
	})
	.fail(function(e) {
		alert( "error loading current notebook file.");
	});

});