var topDbIndex = -1;
var bottomDbIndex = -1;
var isAppending = false;
var dbFilter = null;
var session = null;

jQuery.fn.outerHTML = function() {
	return jQuery('<div />').append(this.eq(0).clone()).html();
};
	
(function($) {
	function img(url) {
		var i = new Image;
		i.src = url;
		return i;
	}

	if ('naturalWidth' in (new Image)) {
		$.fn.naturalWidth  = function() { return this[0].naturalWidth; };
		$.fn.naturalHeight = function() { return this[0].naturalHeight; };
		return;
	}
	$.fn.naturalWidth  = function() { return img(this.src).width; };
	$.fn.naturalHeight = function() { return img(this.src).height; };
})(jQuery);

jQuery.fn.convertDbToEditor = function() {
	var element = this.clone();
	element.removeClass('class_db_object');
	element.addClass('class_editor_object');
	var found = element.find('span.class_span_search_highlight');
//	found.removeClass('class_span_search_highlight');
	found.replaceWith(found.html());
	found = element.find('.class_db_insert');
	found.remove();
	found = element.find('img');
	found.each(function() {
		var width = $(this).naturalWidth();
		if (width > 680) {
			$(this).css("width", 680);
		}
	});
	return jQuery('<div />').append(element).html();
};

jQuery.fn.convertTemplateToEditor = function() {
	var element = this.clone();
	element.removeClass('class_template_object');
	element.addClass('class_editor_object');
	found = element.find('.class_template_insert');
	found.remove();
	return jQuery('<div />').append(element).html();
};

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

function getPdf() {
	if (CKEDITOR.instances.id_editable_inner.checkDirty()) {
		$('<div></div>').appendTo('body')
		  .html('<div class="class_confirm_dialog"><p>You have unsaved changes. You need to save the page before converting it to PDF format. '
				  + 'Do you want to save the change?</p></div>')
		  .dialog({
		      modal: true, title: 'Confirm Saving The Page', zIndex: 10000, autoOpen: true,
		      width: 'auto', resizable: false,
		      buttons: {
		          Yes: function () {
		        	  var postUrl = 'notebook/save' + (session != null ? '?session=' + session : '');
		        	  $.post( postUrl, CKEDITOR.instances.id_editable_inner.getData(), function(data, status) {
		        		  if (status == "success") {
		        			  var notification = new CKEDITOR.plugins.notification( CKEDITOR.instances.id_editable_inner, { message: 'Saved', type: 'success' } );
		        			  notification.show();
		        			  CKEDITOR.instances.id_editable_inner.resetDirty();
		        			  
				        	  var getUrl = "notebook/pdf";
				        	  var session = getParam("session");
				        	  if (session != null) { 
				        		  getUrl += "?session=" + session;
				        	  }
//				        	  window.location.href = getUrl;
				        	  $.get(getUrl, function(data, status) {
				        		  if (status == "success") {
				        			  var pair = data.split(":");
				        			  var fileUrl = "notebook/download/" + pair[0] + ".pdf?ext=" + pair[1];
				        			  if (session != null) { 
				        				  fileUrl += "&session=" + session;
				        			  }
				        			  setTimeout(function() {
				        				  $.fileDownload(fileUrl)
				        				  .done(function () {})
				        				  .fail(function () { alert('File download failed!'); });				
				        			  }, 1000);
				        		  }
				        	  })
				        	  .fail(function(e) {
				        		  alert( "error creating PDF file.");
				        	  }).always(function() {
					        	  $(this).dialog("close");
				        	  });

		        		  }
		        	  })
		        	  .fail(function(e) {
		        		  var notification = new CKEDITOR.plugins.notification( CKEDITOR.instances.id_editable_inner, { message: 'Failed to save the page.', type: 'warning' } );
		        		  notification.show();
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
	} else {
		var getUrl = "notebook/pdf";
		var session = getParam("session");
		if (session != null) { 
			getUrl += "?session=" + session;
		}
//		window.location.href = getUrl;
		$.get(getUrl, function(data, status) {
			if (status == "success") {
				var pair = data.split(":");
				var fileUrl = "notebook/download/" + pair[0] + ".pdf?ext=" + pair[1];
				if (session != null) { 
					fileUrl += "&session=" + session;
				}
				setTimeout(function() {
					$.fileDownload(fileUrl)
					.done(function () {})
					.fail(function () { alert('File download failed!'); });				
				}, 1000);
			}
		})
		.fail(function(e) {
			alert( "error creating PDF file.");
		});
	}
}

function getWord(){
	var data = CKEDITOR.instances.id_editable_inner.getData();
	jQuery('<div />').append(data).wordExport();
//	$("#id_editable_page").wordExport();
}

function drag(ev) {
//	var html = ev.target.outerHTML;
	var element = $('<div />').append(ev.target.outerHTML);
	element.removeClass('class_db_object');
	var found = element.find('span.class_span_search_highlight');
	found.replaceWith(found.html());
	found = element.find('.class_db_insert');
	found.remove();
	found = element.find('.class_template_insert');
	found.remove();
    ev.dataTransfer.setData("text/html", "<p/>" + element.html() + "<p/>");
} 

function dbApplyFilter() {
	if (dbFilter != null){
		$('.class_db_object').hide();
		$(dbFilter).show();
	} else {
		$('.class_db_object').show();
	}
}

//$(window).keydown(function(event) {
//	if(event.ctrlKey && event.keyCode == 83) { 
//		alert("Hey! Ctrl+S event captured!");
//		event.preventDefault(); 
//	}
//});

function searchDatabase() {
	var searchPattern = encodeURIComponent($("#id_input_search_db").val());
	getUrl = "db/search?pattern=" + searchPattern
	if (session != null) {
		getUrl += "&session=" + session;
	}
	getUrl += "&" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			$('#id_sidebar_inner_2').html(data);
			$('#id_sidebar_inner').hide();
			$('#id_sidebar_inner_2').show();
			$('#id_input_search_close').show();
			
			dbApplyFilter();
			
			$('.class_db_object').hover(function() {
				$(this).append('<div class="class_db_insert"><img alt="insert" src="images/nav_backward.gif"><span>INSERT</span></div>');
				var h = $(this).height();
				var w = $(this).width();
				$('.class_db_insert').css({
					'top': h / 2 - 10,
					'left': w / 2 - 30
				});
				$('.class_db_insert').click(function(e) {
					CKEDITOR.instances.id_editable_inner.insertHtml('<p>' + $(this).parent().convertDbToEditor() + '</p>');
					$("div.class_db_insert").remove();
				});
			}, function() {
				$('div').remove('.class_db_insert');
			});
			
			$('.class_db_object').unbind('dblclick');
			$('.class_db_object').dblclick(function() {
				CKEDITOR.instances.id_editable_inner.insertHtml('<p>' + $(this).convertDbToEditor() + '</p>');
			});
			
			$('.class_db_object').each(function(i, obj) {
			    $(this).attr("draggable", true);
			    $(this).attr("ondragstart", "drag(event)");
			});

		}
	})
	.fail(function(e) {
		alert( "error searching notebook files.");
	});	
}

function closeSearch() {
	$('#id_sidebar_inner').show();
	$('#id_sidebar_inner_2').hide();
	$('#id_input_search_close').hide();
}

function dbScrollBottom() {
	isAppending = true;
    $('#id_sidebar_inner').append('<div class="class_inner_loading"><img src="images/loading.gif"></div>');
    $('#id_sidebar_inner').scrollTop = $('#id_sidebar_inner').scrollHeight;
    var getUrl;
    if (session == null) {
    	getUrl = "notebook/db?start=" + (bottomDbIndex - 1) + "&length=10";
    } else {
    	getUrl = "notebook/db?session=" + session + "&start=" + (bottomDbIndex - 1) + "&length=10";
    }
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			if (data.trim().length == 0) {
				$('#id_sidebar_inner').append("<p>End of Database</p>");
//				$('#id_sidebar_inner').unbind('scroll');
				return;
			}
			var brk = data.indexOf(";");
			var pair = data.substring(0, brk);
			bottomDbIndex = parseInt(pair.substring(pair.indexOf(":") + 1));
			
			$('.class_db_new').removeClass('class_db_new');
			var items = $('<div/>').html(data.substring(brk + 1)).children();
			$.each(items, function(idx, val) {
				$(this).addClass('class_db_new');
			});
			$('#id_sidebar_inner').append(items);

			dbApplyFilter();
			
			$('.class_db_object').hover(function() {
				$(this).append('<div class="class_db_insert"><img alt="insert" src="images/nav_backward.gif"><span>INSERT</span></div>');
				var h = $(this).height();
				var w = $(this).width();
				$('.class_db_insert').css({
					'top': h / 2 - 10,
					'left': w / 2 - 30
				});
				$('.class_db_insert').click(function(e) {
					CKEDITOR.instances.id_editable_inner.insertHtml('<p>' + $(this).parent().convertDbToEditor() + '</p>');
					$("div.class_db_insert").remove();
				});
			}, function() {
				$('div').remove('.class_db_insert');
			});
			
			$('.class_db_object').unbind('dblclick');
			$('.class_db_object').dblclick(function() {
				CKEDITOR.instances.id_editable_inner.insertHtml('<p>' + $(this).convertDbToEditor() + '</p>');
			});
			
			$('.class_db_object').each(function(i, obj) {
			    $(this).attr("draggable", true);
			    $(this).attr("ondragstart", "drag(event)");
			});

			if (bottomDbIndex <= 0){
				$('#id_sidebar_inner').append('<div class="class_inner_message">End of Database</div>');
			}

		}
	})
	.fail(function(e) {
		alert( "error loading db xml file.");
	})
	.always(function() {
	    isAppending = false;
	    $(".class_inner_loading").remove();
	});
}

function dbScrollTop() {
	isAppending = true;
	$(".class_inner_topmessage").remove();
    $('#id_sidebar_inner').prepend('<div class="class_inner_loading"><img src="images/loading.gif"></div>');
    $('#id_sidebar_inner').scrollTop = 0;
    var getUrl;
    if (session == null) {
    	getUrl = "notebook/db?start=" + (topDbIndex + 10) + "&length=10";
    } else {
    	getUrl = "notebook/db?session=" + session + "&start=" + (topDbIndex + 10) + "&length=10";
    }
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			if (data.trim().length == 0) {
				$('#id_sidebar_inner').prepend('<div class="class_inner_topmessage">No new entry was found. Please try again later. </div>');
				return;
			}
			var brk = data.indexOf(";");
			var pair = data.substring(0, brk);
			topDbIndex = parseInt(pair.substring(0, pair.indexOf(":")));
			var tempBottomDbIndex = parseInt(pair.substring(pair.indexOf(":") + 1));

			if (topDbIndex - tempBottomDbIndex < 0){
				$('#id_sidebar_inner').prepend('<div class="class_inner_topmessage">No new entry was found. Please try again later. </div>');
				return;
			}
			
			$('.class_db_new').removeClass('class_db_new');
			var items = $('<div/>').html(data.substring(brk + 1)).children();
			$.each(items, function(idx, val) {
				$(this).addClass('class_db_new');
			});
			$('#id_sidebar_inner').prepend(items);

			dbApplyFilter();
			
			$('.class_db_object').hover(function() {
				$(this).append('<div class="class_db_insert"><img alt="insert" src="images/nav_backward.gif"><span>INSERT</span></div>');
				var h = $(this).height();
				var w = $(this).width();
				$('.class_db_insert').css({
					'top': h / 2 - 10,
					'left': w / 2 - 30
				});
				$('.class_db_insert').click(function(e) {
					CKEDITOR.instances.id_editable_inner.insertHtml('<p>' + $(this).parent().convertDbToEditor() + '</p>');
					$("div.class_db_insert").remove();
				});
			}, function() {
				$('div').remove('.class_db_insert');
			});
			
			$('.class_db_object').unbind('dblclick');
			$('.class_db_object').dblclick(function() {
				CKEDITOR.instances.id_editable_inner.insertHtml('<p>' + $(this).convertDbToEditor() + '</p>');
			});
			
			$('.class_db_object').each(function(i, obj) {
			    $(this).attr("draggable", true);
			    $(this).attr("ondragstart", "drag(event)");
			});

		}
	})
	.fail(function(e) {
		alert( "error loading db xml file.");
	})
	.always(function() {
	    isAppending = false;
	    $(".class_inner_loading").remove();
	});
}

$(function(){
	
	$(document).click(function(e) {
		if (e.target.tagName.toLowerCase() == 'body') {
//			$('#id_editable_page').focus();
			var editor = CKEDITOR.instances.id_editable_inner;
			if (editor) {
				editor.focus();
			}
		}
	});
	
    $('#id_input_search_db').keyup(function(e){
        if(e.keyCode == 13) {
            searchDatabase();
        }
    });
    
    $('#id_input_search_close').click(function(e) {
    	closeSearch();
	});

//	define scroll div with auto height
	$(window).resize(function() {
	    var bodyheight = $(window).height();
		$(".slide-out-div").height(bodyheight - 20);
		$(".div_sidebar_inner").height(bodyheight - 44);
		
		$(".div_canvas_slideout").height(bodyheight - 20);
		$("#id_editable_page").height(bodyheight - 180);
//		$(".div_canvas_inner").height(bodyheight - 80);
	});

// define slide out side bar
    $('.slide-out-div').tabSlideOut({
    	tabHandleClass: '.a_sidebar_handle',
    	tabBlockClass: '.div_sidebar_block',
        tabHandles: ['#a_sidebar_database', '#a_sidebar_template', '#a_sidebar_canvas'],                     //class of the element that will become your tab
        tabBlocks: ['#div_sidebar_database', '#div_sidebar_template', '#div_sidebar_canvas'],
        tabHandleSize: 200,
        pathToTabImage: $('html').hasClass('ie9') ? ['images/Database.GIF', 'images/Canvas.GIF', 'images/Template.GIF'] : null, //path to the image for the tab //Optionally can be set using css
        imageHeight: '218px',                     //height of tab image           //Optionally can be set using css
        imageWidth: '33px',                       //width of tab image            //Optionally can be set using css
        tabLocation: 'right',                      //side of screen where tab lives, top, right, bottom, or left
        speed: 300,                               //speed of animation
        action: 'click',                          //options: 'click' or 'hover', action to trigger animation
        topPos: '40px',                          //position from the top/ use if tabLocation is left or right
        leftPos: '20px',                          //position from left/ use if tabLocation is bottom or top
        fixedPosition: true,                      //options: true makes it stick(fixed position) on scroll
        onSlideOut: function() {
			$('.div_shiftable').css({ marginLeft: "20px" });
			$('.class_editable_page').css({ marginLeft: "20px" });
		},
        onSlideIn: function() {
        	$('.div_shiftable').css({ marginLeft: "auto" });
        	$('.class_editable_page').css({ margin: "0px auto" });
		}
    });
    
// define drawing canvas
	var drawingBoard = new DrawingBoard.Board('id_canvas_inner', {
		controls: [
			'Color',
			{ Size: { type: 'dropdown' } },
			{ DrawingMode: { filler: false } },
			'Navigation',
			'Download'
		],
		size: 1,
		webStorage: 'session',
		enlargeYourContainer: true,
		droppable: true, //try dropping an image on the canvas!
		stretchImg: false //the dropped image can be automatically ugly resized to to take the canvas size
	});

// config inserting drawing picture
	drawingBoard.downloadImg = function() {
		var img = $('<img >'); 
		img.attr('src', drawingBoard.getImg());
		CKEDITOR.instances.id_editable_inner.insertHtml('<p/>' + $('<div>').append(img).html() + '<p/>');
	};
	
    $('#id_sidebar_inner').bind('scroll', function() {
        if(!isAppending && bottomDbIndex > 0 && $(this).scrollTop() + $(this).innerHeight() >= this.scrollHeight && bottomDbIndex > 0) {
        	dbScrollBottom();
        } else if(!isAppending && $(this).scrollTop() == 0) {
        	dbScrollTop();
        }
    });
    
    document.body.onbeforeunload = function() {
    	if (CKEDITOR.instances.id_editable_inner.checkDirty()) {
    		return 'You have unsaved changes in the editor.';
    	}
    };
});

jQuery(document).ready(function() {
//	define scroll div with auto height
	var bodyheight = $(window).height();
	$(".slide-out-div").height(bodyheight - 20);
	$(".div_sidebar_inner").height(bodyheight - 44);
	$(".div_canvas_slideout").height(bodyheight - 20);
	$("#id_editable_page").height(bodyheight - 180);

	session = getParam('session');
	
//	load current notebook content file
	var getUrl = "notebook/load";
	if (session != null && session.trim().length > 0) {
		getUrl += "?session=" + session;
	}
	$.get(getUrl, function(data, status) {
		if (status == "success") {
//			$('#id_editable_page').html(decodeURIComponent(data.replace(/\+/g, ' ')));
			if (data.trim().length == 0) {
				$('#id_editable_inner').html("<p><br></p>");
			} else {
				$('#id_editable_inner').html(data);
			}
			
//			make editable page
			CKEDITOR.replace( 'id_editable_inner' );
			CKEDITOR.instances.id_editable_inner.on('save', function(event, editor, data) {
//				alert(CKEDITOR.instances.id_editable_inner.getData());
		        var postUrl = 'notebook/save' + (session != null ? '?session=' + session : '');
		        $.post( postUrl, CKEDITOR.instances.id_editable_inner.getData(), function(data, status) {
		            if (status == "success") {
			        	var notification = new CKEDITOR.plugins.notification( CKEDITOR.instances.id_editable_inner, { message: 'Saved', type: 'success' } );
			            notification.show();
			            CKEDITOR.instances.id_editable_inner.resetDirty();
			        }
		        })
		        .fail(function(e) {
		        	var notification = new CKEDITOR.plugins.notification( CKEDITOR.instances.id_editable_inner, { message: 'Failed to save the page.', type: 'warning' } );
		            notification.show();
		        });
		    });
//			$('#id_editable_inner').ckeditor().on('save', function(event, editor, data) {
//				alert("save");
//			});
		}
	})
	.fail(function(e) {
		alert( "error loading current notebook file.");
	});

//	load db entries
    var getUrl;
    if (session == null) {
    	getUrl = "notebook/db?length=20";
    } else {
    	getUrl = "notebook/db?session=" + session + "&length=20";
    }

	$.get(getUrl, function(data, status) {
		if (status == "success") {
			if (data.trim().length == 0) {
				$('#id_sidebar_inner').append("<p>End of Database</p>");
//				$('#id_sidebar_inner').unbind('scroll');
				return;
			}
			var brk = data.indexOf(";");
			var pair = data.substring(0, brk);
			topDbIndex = parseInt(pair.substring(0, pair.indexOf(":")));
			bottomDbIndex = parseInt(pair.substring(pair.indexOf(":") + 1));
			
			$('#id_sidebar_inner').html(data.substring(brk + 1));

			if (bottomDbIndex <= 0){
				$('#id_sidebar_inner').append('<div class="class_inner_message">End of Database</div>');
//				$('#id_sidebar_inner').unbind('scroll');
			}

//			add insert button to db div on mouse over
			$('.class_db_object').hover(function() {
				$(this).append('<div class="class_db_insert"><img alt="insert" src="images/nav_backward.gif"><span>INSERT</span></div>');
				var h = $(this).height();
				var w = $(this).width();
				$('.class_db_insert').css({
					'top': h / 2 - 10,
					'left': w / 2 - 30
				});
				$('.class_db_insert').click(function(e) {
					CKEDITOR.instances.id_editable_inner.insertHtml('<p>' + $(this).parent().convertDbToEditor() + '</p>');
					$("div.class_db_insert").remove();
				});
			}, function() {
				$('div').remove('.class_db_insert');
			});
			
			$('.class_db_object').dblclick(function() {
				CKEDITOR.instances.id_editable_inner.insertHtml('<p>' + $(this).convertDbToEditor() + '</p>');
			});

			$('.class_db_object').each(function(i, obj) {
			    $(this).attr("draggable", true);
			    $(this).attr("ondragstart", "drag(event)");
			});

//			disabled for unexpected behavior
//			$(".class_db_object").draggable({
//				helper : 'clone', 
//				cursor: 'pointer' 
//			});
			
//			$(".class_db_object").mousedown(function (e) {
//				e.dataTransfer.setData("text", e.target.id);
//			    return false;
//			});
		}
	})
	.fail(function(e) {
		alert( "error loading db xml file.");
	});

	// load templates
	getUrl = "notebook/template";
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			if (data.trim().length == 0) {
				return;
			}
			
			$('#id_template_inner').html(data);

//			add insert button to db div on mouse over
			$('.class_template_object').hover(function() {
				$(this).append('<div class="class_template_insert"><img alt="insert" src="images/nav_backward.gif"><span>INSERT</span></div>');
				var h = $(this).height();
				var w = $(this).width();
				$('.class_template_insert').css({
					'top': h / 2 - 10,
					'left': w / 2 - 30
				});
				$('.class_template_insert').click(function(e) {
//					CKEDITOR.instances.id_editable_inner.insertHtml('<br>' + $(this).parent().convertTemplateToEditor() + '<br>');
//					CKEDITOR.instances.id_editable_inner.focus();
//					var element = CKEDITOR.dom.element.createFromHtml($(this).parent().convertTemplateToEditor());
//					CKEDITOR.instances.id_editable_inner.insertElement(element);
					CKEDITOR.instances.id_editable_inner.insertHtml("<p>" + $(this).parent().convertTemplateToEditor() + "</p>");
//					$("div.class_template_insert").remove();
				});
			}, function() {
				$('div').remove('.class_template_insert');
			});
			
			$('.class_template_object').dblclick(function() {
//				if (!editorDocumentPage.isEditing()) {
//					editorDocumentPage.enableEditing();
//					editorPastePlugin.insertContent('<br>' + $(this).convertTemplateToEditor() + '<br>');
//				} else {
//					editorPastePlugin.insertContent('<br>' + $(this).convertTemplateToEditor() + '<br>');
//				}
				CKEDITOR.instances.id_editable_inner.insertHtml('<p>' + $(this).convertTemplateToEditor() + '</p>');
//				var element = CKEDITOR.dom.element.createFromHtml($(this).convertTemplateToEditor());
//				CKEDITOR.instances.id_editable_inner.insertElement(element);
			});

			$('.class_template_object').each(function(i, obj) {
			    $(this).attr("draggable", true);
			    $(this).attr("ondragstart", "drag(event)");
			});

		}
	})
	.fail(function(e) {
		alert( "error loading db xml file.");
	});
	
	$('#id_sidebar_inner').on('DOMMouseScroll mousewheel', function(ev) {
	    var $this = $(this),
	        scrollTop = this.scrollTop,
	        scrollHeight = this.scrollHeight,
	        height = $this.height(),
	        delta = (ev.type == 'DOMMouseScroll' ?
	            ev.originalEvent.detail * -40 :
	            ev.originalEvent.wheelDelta),
	        up = delta > 0;

	    var prevent = function() {
	        ev.stopPropagation();
	        ev.preventDefault();
	        ev.returnValue = false;
	        return false;
	    }

	    if (!up && -delta > scrollHeight - height - scrollTop && scrollHeight == height && bottomDbIndex > 0) {
	        // Scrolling down, but this will take us past the bottom.
	        $this.scrollTop(scrollHeight);
	        dbScrollBottom();
	        return prevent();
	    } else if (up && delta > scrollTop && scrollHeight == height) {
	        // Scrolling up, but this will take us past the top.
	        $this.scrollTop(0);
	        if(!isAppending && $(this).scrollTop() == 0) {
	        	dbScrollTop();
	        }
	        return prevent();
	    }
	});
	
	$('#id_sidebar_header').prepend('<div id="indicatorContainer"><div id="pIndicator"><div id="cIndicator"></div></div></div>');
    var activeElement = $('#id_sidebar_header>ul>li:first');

    $('#id_sidebar_header>ul>li').each(function() {
        if ($(this).hasClass('active')) {
            activeElement = $(this);
        }
    });


	var posLeft = activeElement.position().left;
	var elementWidth = activeElement.width();
	posLeft = posLeft + elementWidth/2 -6;
	if (activeElement.hasClass('has-sub')) {
		posLeft -= 6;
	}

	$('#id_sidebar_header #pIndicator').css('left', posLeft);
	var element, leftPos, indicator = $('#id_sidebar_header pIndicator');
	
	$("#id_sidebar_header>ul>li").hover(function() {
        element = $(this);
        var w = element.width();
        if ($(this).hasClass('has-sub'))
        {
        	leftPos = element.position().left + w/2 - 12;
        }
        else {
        	leftPos = element.position().left + w/2 - 6;
        }

        $('#id_sidebar_header #pIndicator').css('left', leftPos);
    }
    , function() {
    	$('#id_sidebar_header #pIndicator').css('left', posLeft);
    });

	$('#id_sidebar_header>ul').prepend('<li id="menu-button"><a>Menu</a></li>');
	$( "#menu-button" ).click(function(){
		if ($(this).parent().hasClass('open')) {
			$(this).parent().removeClass('open');
		}
		else {
			$(this).parent().addClass('open');
		}
	});

	$('#id_sidebar_inner').hover(function() {
        $('#id_sidebar_inner').focus();
	}, function() {
		$('#id_sidebar_inner').blur();
	});
	
	$('#id_filter_mss').click(function(e) {
		$('.class_db_object').hide();
		$('.class_db_table').show();
		$('#id_filter_menu span').text('SAMPLE SCAN');
		dbFilter = '.class_db_table';
	});

	$('#id_filter_als').click(function(e) {
		$('.class_db_object').hide();
		$('.class_db_image').show();
		$('#id_filter_menu span').text('ALIGNMENT SCAN');
		dbFilter = '.class_db_image';
	});
	
	$('#id_filter_tbo').click(function(e) {
		$('.class_db_object').hide();
		$('.class_db_table').show();
		$('#id_filter_menu span').text('TABLE ONLY');
		dbFilter = '.class_db_table';
	});

	$('#id_filter_plo').click(function(e) {
		$('.class_db_object').hide();
		$('.class_db_image').show();
		$('#id_filter_menu span').text('PLOT ONLY');
		dbFilter = '.class_db_image';
	});

	$('#id_filter_all').click(function(e) {
		$('.class_db_object').show();
		$('#id_filter_menu span').text('ALL ITEMS');
		dbFilter = null;
	});
	
});