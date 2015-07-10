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

function load(id, name) {
	getUrl = "../notebook/load?session=" + id + "&" + timeString;
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

$(function() {
	$("#id_a_newbook").click(function(e) {
		
		$('<div></div>').appendTo('body')
		  .html('<div><h6>Please confirm creating new notebook file. The current notebook file will be put into archive list.</h6></div>')
		  .dialog({
		      modal: true, title: 'Confirm Creating New Notebook', zIndex: 10000, autoOpen: true,
		      width: 'auto', resizable: false,
		      buttons: {
		          Yes: function () {
				      		getUrl = "../db/close?" + timeString;
				    		$.get(getUrl, function(data, status) {
				    			if (status == "success") {
	//			    				$('#id_div_content').html("<p><br></p>");
	//			    				$("#id_ul_archiveList").prepend('<li><a id="' + data + '" onclick="load(\'' + data + '\')">&nbsp;-&nbsp;' + data + '</a></li>');
				    			}
				    		})
				    		.fail(function(e) {
				    			alert( "error close database file.");
				    		})
				    		.always(function(e) {
					      		var getUrl = "../notebook/new?" + timeString;
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
				    		})
				    		
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
		var getUrl = "../notebook/manageguide?" + timeString;
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
		var getUrl = "../notebook/load?" + timeString;
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