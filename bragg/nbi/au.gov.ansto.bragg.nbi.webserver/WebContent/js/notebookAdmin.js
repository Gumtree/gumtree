var timeString = (new Date()).getTime();
var getUrl;

function load(id) {
	getUrl = "../notebook/load?file=" + id + "&" + timeString;
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			$('#id_div_content').html(data);
		}
	})
	.fail(function(e) {
		alert( "error loading notebook file " + id + ".");
	});
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
				      		var getUrl = "../notebook/new?" + timeString;
				    		$.get(getUrl, function(data, status) {
				    			if (status == "success") {
				    				$('#id_div_content').html("<p><br></p>");
				    				$("#id_ul_archiveList").prepend('<li><a id="' + data + '" onclick="load(\'' + data + '\')">&nbsp;-&nbsp;' + data + '</a></li>');
				    			}
				    		})
				    		.fail(function(e) {
				    			alert( "error creating new notebook file.");
				    		});

				      		getUrl = "../db/new?" + timeString;
				    		$.get(getUrl, function(data, status) {
				    			if (status == "success") {
//				    				$('#id_div_content').html("<p><br></p>");
//				    				$("#id_ul_archiveList").prepend('<li><a id="' + data + '" onclick="load(\'' + data + '\')">&nbsp;-&nbsp;' + data + '</a></li>');
				    			}
				    		})
				    		.fail(function(e) {
				    			alert( "error creating new database file.");
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
		
	});
});

$(function() {
	$("#id_a_reviewCurrent").click(function(e) {
		var getUrl = "../notebook/load?" + timeString;
		$.get(getUrl, function(data, status) {
			if (status == "success") {
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
	
	getUrl = "../notebook/archive?" + timeString;
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			if (data.trim().length > 0){
				var files = data.split(":");
				$.each(files, function(idx, val) {
					$("#id_ul_archiveList").append('<li><a id="' + val + '" onclick="load(\'' + val + '\')">&nbsp;-&nbsp;' + val + '</a></li>');
				});
			}
		}
	})
	.fail(function(e) {
		alert( "error loading archive notebook files.");
	});
	
	getUrl = "../notebook/load?file=ManagerUsersGuide&" + timeString;
	$.get(getUrl, function(data, status) {
		if (status == "success") {
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