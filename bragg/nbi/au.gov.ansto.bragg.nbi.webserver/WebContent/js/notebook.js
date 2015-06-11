var editorDocumentPage = null;
var editorPastePlugin = null;
var topDbIndex = 0;
var bottomDbIndex = 0;
var isAppending = false;
var dbFilter = null;

//alert($('html').hasClass('ie9'));

jQuery.fn.outerHTML = function() {
	return jQuery('<div />').append(this.eq(0).clone()).html();
};
	
jQuery.fn.convertDbToEditor = function() {
	var element = this.clone();
	element.removeClass('class_db_object');
	element.addClass('class_editor_object');
	return jQuery('<div />').append(element).html();
};

function drag(ev) {
	var html = ev.target.outerHTML;
	html = html.replace("class_db_object", "class_editor_object"); 
    ev.dataTransfer.setData("text/html", html);
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

$(function(){
	
//	define scroll div with auto height
	$(window).resize(function() {
	    var bodyheight = $(window).height();
		$(".slide-out-div").height(bodyheight - 80);
		$(".div_sidebar_inner").height(bodyheight - 104);
	});
	
//	overwrite save key shortcut	
	$(document).bind('keydown', 'Ctrl+s', function(event) {
		setTimeout(function() {
			try {
				$('.class_editable_page').raptor.Raptor.getInstances()[0].getPlugin('saveRest').save();
			} catch (e) {
				console.log("saving failed.");
			}
		}, 0);
		return false;
	});

    $('.slide-out-div').tabSlideOut({
        tabHandle: '.id_sidebar_handle',                     //class of the element that will become your tab
        pathToTabImage: $('html').hasClass('ie9') ? 'images/Database.GIF' : null, //path to the image for the tab //Optionally can be set using css
        imageHeight: '218px',                     //height of tab image           //Optionally can be set using css
        imageWidth: '33px',                       //width of tab image            //Optionally can be set using css
        tabLocation: 'right',                      //side of screen where tab lives, top, right, bottom, or left
        speed: 300,                               //speed of animation
        action: 'click',                          //options: 'click' or 'hover', action to trigger animation
        topPos: '80px',                          //position from the top/ use if tabLocation is left or right
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

    
    $('#id_sidebar_inner').bind('scroll', function() {
        if(!isAppending && bottomDbIndex > 0 && $(this).scrollTop() + $(this).innerHeight() >= this.scrollHeight) {
        	isAppending = true;
            $('#id_sidebar_inner').append('<div class="class_inner_loading"><img src="images/loading.gif"></div>');
            $('#id_sidebar_inner').scrollTop = $('#id_sidebar_inner').scrollHeight;
            var getUrl = "notebook/db?start=" + (bottomDbIndex - 1) + "&length=10";
        	$.get(getUrl, function(data, status) {
        		if (status == "success") {
        			if (data.trim().length = 0) {
        				$('#id_sidebar_inner').append("<p>End of Database</p>");
//        				$('#id_sidebar_inner').unbind('scroll');
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
        					var text = '';
        					$.each(editorPastePlugin, function(idx, val) {
        						text += idx + '\n';
        					});
        					if (!editorDocumentPage.isEditing()) {
        						editorDocumentPage.enableEditing();
        						editorPastePlugin.insertContent('<br>' + $(this).parent().convertDbToEditor() + '<br>');
        					} else {
        						editorPastePlugin.insertContent('<br>' + $(this).parent().convertDbToEditor() + '<br>');
        					}
        					$("div.class_db_insert").remove();
        				});
        			}, function() {
        				$('div').remove('.class_db_insert');
        			});
        			
        			$('.class_db_object').unbind('dblclick');
        			$('.class_db_object').dblclick(function() {
        				if (!editorDocumentPage.isEditing()) {
        					editorDocumentPage.enableEditing();
        					editorPastePlugin.insertContent('<br>' + $(this).convertDbToEditor() + '<br>');
        				} else {
        					editorPastePlugin.insertContent('<br>' + $(this).convertDbToEditor() + '<br>');
        				}
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
        } else if(!isAppending && $(this).scrollTop() == 0) {
        	isAppending = true;
        	$(".class_inner_topmessage").remove();
            $('#id_sidebar_inner').prepend('<div class="class_inner_loading"><img src="images/loading.gif"></div>');
            $('#id_sidebar_inner').scrollTop = 0;
            var getUrl = "notebook/db?start=" + (topDbIndex + 10) + "&length=10";
        	$.get(getUrl, function(data, status) {
        		if (status == "success") {
        			if (data.trim().length = 0) {
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
        					var text = '';
        					$.each(editorPastePlugin, function(idx, val) {
        						text += idx + '\n';
        					});
        					if (!editorDocumentPage.isEditing()) {
        						editorDocumentPage.enableEditing();
        						editorPastePlugin.insertContent('<br>' + $(this).parent().convertDbToEditor() + '<br>');
        					} else {
        						editorPastePlugin.insertContent('<br>' + $(this).parent().convertDbToEditor() + '<br>');
        					}
        					$("div.class_db_insert").remove();
        				});
        			}, function() {
        				$('div').remove('.class_db_insert');
        			});
        			
        			$('.class_db_object').unbind('dblclick');
        			$('.class_db_object').dblclick(function() {
        				if (!editorDocumentPage.isEditing()) {
        					editorDocumentPage.enableEditing();
        					editorPastePlugin.insertContent('<br>' + $(this).convertDbToEditor() + '<br>');
        				} else {
        					editorPastePlugin.insertContent('<br>' + $(this).convertDbToEditor() + '<br>');
        				}
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
    })
    
//	disabled for unexpected behavior
//	$(".class_editable_page").droppable({
//		accept: ".class_db_object",
//		tolerance: "pointer",
//		revert: "invalid",
//		drop: function(event,ui){
////				$(this).append($(ui.draggable).clone());
//				if (!editorDocumentPage.isEditing()) {
//					editorDocumentPage.enableEditing();
//					editorPastePlugin.dropContent('<br>' + $(ui.draggable).clone().convertDbToEditor());
//				} else {
//					editorPastePlugin.dropContent('<br>' + $(ui.draggable).clone().convertDbToEditor());
//				}
//			}
//	});
});
            

jQuery(document).ready(function(){

//	load current notebook content file
	var getUrl = "notebook/load";
	$.get(getUrl, function(data, status) {
		if (status == "success") {
//			$('#id_editable_page').html(decodeURIComponent(data.replace(/\+/g, ' ')));
			$('#id_editable_page').html(data);
			
//			make editable page
			jQuery(function($) {
				$('.class_editable_page').raptor({
					"plugins": {
						"cancel": true,
						"classMenu": false,
//						"classMenu": {
//							"classes": {
//								"Blue background": "cms-blue-bg",
//								"Round corners": "cms-round-corners",
//								"Indent and center": "cms-indent-center"
//							}
//						},
						"dockToScreen": false,
						"dockToElement": false,
						"dock": {
							"docked": false,
							"persist": false
						},
						"guides": false,
						"languageMenu": false,
						"logo": false,
//						"paste": false,
						"paste": {
							enabled: false
						},
						// The save UI plugin/button
						"save": {
							// Specifies the UI to call the saveRest plugin to do the actual saving
							plugin: 'saveRest'
						},
						"saveRest": {
							// The URI to send the content to
							url: 'notebook/save',
							// Returns an object containing the data to send to the server
							data: function(html) {
								return {
									id: this.raptor.getElement().data('id'),
									content: html
								};
							},
							retain: true
						},
//						"snippetMenu": {
//							"snippets": {
//								"Grey Box": "<div class=\"grey-box\"><h1>Grey Box<\/h1><ul><li>This is a list<\/li><\/ul><\/div>"
//							}
//						},
						"snippetMenu": false,
						"statistics": false, 
						"viewSource": true
					} 
					
//				    ,"bind": {
//				    	"disabled" : function() {
//				    		$(".class_editable_page").droppable({
//				    			accept: ".class_db_object",
//				    			drop: function(event,ui){
//				    					alert('drop detected');
////				    					$(this).append($(ui.draggable).clone());
//				    					if (!editorDocumentPage.isEditing()) {
//				    						editorDocumentPage.enableEditing();
//				    						editorPastePlugin.insertContent('<br>' + $(ui.draggable).clone().convertDbToEditor());
//				    					} else {
//				    						editorPastePlugin.insertContent('<br>' + $(ui.draggable).clone().convertDbToEditor());
//				    					}
//				    				}
//				    		});
//				        }
//				    }
				});
			});
			
			editorDocumentPage = $('.class_editable_page').raptor.Raptor.getInstances()[0];
			editorPastePlugin = editorDocumentPage.getPlugin('paste');

		}
	})
	.fail(function(e) {
		alert( "error loading current notebook file.");
	});

//	load db xml file
	var getUrl = "notebook/db?length=10";
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			if (data.trim().length = 0) {
				$('#id_sidebar_inner').append("<p>Empty Database</p>");
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
					var text = '';
					$.each(editorPastePlugin, function(idx, val) {
						text += idx + '\n';
					});
					if (!editorDocumentPage.isEditing()) {
						editorDocumentPage.enableEditing();
						editorPastePlugin.insertContent('<br>' + $(this).parent().convertDbToEditor() + '<br>');
//						$('html, body').animate({ 
//							   scrollTop: $(document).height()-$(window).height()}, 
//							   1400, 
//							   "easeOutQuint"
//						);
					} else {
						editorPastePlugin.insertContent('<br>' + $(this).parent().convertDbToEditor() + '<br>');
					}
					$("div.class_db_insert").remove();
				});
			}, function() {
				$('div').remove('.class_db_insert');
			});
			
			$('.class_db_object').dblclick(function() {
				if (!editorDocumentPage.isEditing()) {
					editorDocumentPage.enableEditing();
					editorPastePlugin.insertContent('<br>' + $(this).convertDbToEditor() + '<br>');
				} else {
					editorPastePlugin.insertContent('<br>' + $(this).convertDbToEditor() + '<br>');
				}
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

//	define scroll div with auto height
	var bodyheight = $(window).height();
	$(".slide-out-div").height(bodyheight - 80);
	$(".div_sidebar_inner").height(bodyheight - 104);
	
//	below code prevent body scroll together with the side bar innver div.
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

	    if (!up && -delta > scrollHeight - height - scrollTop && scrollHeight == height) {
	        // Scrolling down, but this will take us past the bottom.
		    console.log(scrollHeight + '; ' + height + ';' + scrollTop);
	        $this.scrollTop(scrollHeight);
        	isAppending = true;
            $('#id_sidebar_inner').append('<div class="class_inner_loading"><img src="images/loading.gif"></div>');
            $('#id_sidebar_inner').scrollTop = $('#id_sidebar_inner').scrollHeight;
            var getUrl = "notebook/db?start=" + (bottomDbIndex - 1) + "&length=10";
        	$.get(getUrl, function(data, status) {
        		if (status == "success") {
        			if (data.trim().length = 0) {
        				$('#id_sidebar_inner').append("<p>End of Database</p>");
//        				$('#id_sidebar_inner').unbind('scroll');
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
        					var text = '';
        					$.each(editorPastePlugin, function(idx, val) {
        						text += idx + '\n';
        					});
        					if (!editorDocumentPage.isEditing()) {
        						editorDocumentPage.enableEditing();
        						editorPastePlugin.insertContent('<br>' + $(this).parent().convertDbToEditor() + '<br>');
        					} else {
        						editorPastePlugin.insertContent('<br>' + $(this).parent().convertDbToEditor() + '<br>');
        					}
        					$("div.class_db_insert").remove();
        				});
        			}, function() {
        				$('div').remove('.class_db_insert');
        			});
        			
        			$('.class_db_object').unbind('dblclick');
        			$('.class_db_object').dblclick(function() {
        				if (!editorDocumentPage.isEditing()) {
        					editorDocumentPage.enableEditing();
        					editorPastePlugin.insertContent('<br>' + $(this).convertDbToEditor() + '<br>');
        				} else {
        					editorPastePlugin.insertContent('<br>' + $(this).convertDbToEditor() + '<br>');
        				}
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
	        return prevent();
	    } else if (up && delta > scrollTop && scrollHeight == height) {
	        // Scrolling up, but this will take us past the top.
		    console.log(scrollHeight + '; ' + height + ';' + scrollTop);
	        $this.scrollTop(0);
			
	        if(!isAppending && $(this).scrollTop() == 0) {
	        	isAppending = true;
	        	$(".class_inner_topmessage").remove();
	            $('#id_sidebar_inner').prepend('<div class="class_inner_loading"><img src="images/loading.gif"></div>');
	            $('#id_sidebar_inner').scrollTop = 0;
	            var getUrl = "notebook/db?start=" + (topDbIndex + 10) + "&length=10";
	        	$.get(getUrl, function(data, status) {
	        		if (status == "success") {
	        			if (data.trim().length = 0) {
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
	        					var text = '';
	        					$.each(editorPastePlugin, function(idx, val) {
	        						text += idx + '\n';
	        					});
	        					if (!editorDocumentPage.isEditing()) {
	        						editorDocumentPage.enableEditing();
	        						editorPastePlugin.insertContent('<br>' + $(this).parent().convertDbToEditor() + '<br>');
	        					} else {
	        						editorPastePlugin.insertContent('<br>' + $(this).parent().convertDbToEditor() + '<br>');
	        					}
	        					$("div.class_db_insert").remove();
	        				});
	        			}, function() {
	        				$('div').remove('.class_db_insert');
	        			});
	        			
	        			$('.class_db_object').unbind('dblclick');
	        			$('.class_db_object').dblclick(function() {
	        				if (!editorDocumentPage.isEditing()) {
	        					editorDocumentPage.enableEditing();
	        					editorPastePlugin.insertContent('<br>' + $(this).convertDbToEditor() + '<br>');
	        				} else {
	        					editorPastePlugin.insertContent('<br>' + $(this).convertDbToEditor() + '<br>');
	        				}
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
		$('#id_filter_menu span').text('MULTI-SAMPLE SCAN');
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