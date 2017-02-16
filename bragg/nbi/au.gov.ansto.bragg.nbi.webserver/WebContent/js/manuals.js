var CLASS_SEARCH_MADE = "class_search_made";
var search_pattern;

function loadManual(path) {
	window.open('manuals/load?path=' + encodeURI(path), '_blank');
}

function hideSearchResult() {
	if (search_pattern == null) {
		return;
	}
	$('#id_div_floatingRight').hide();
//	$('#id_a_clearSearch >img').css('visibility', 'hidden');
	$('#id_a_search').removeClass(CLASS_SEARCH_MADE);
	search_pattern = null;
}

function removeSearchResult() {
	$('#id_div_searchResult').empty();
}

function searchForPattern(pattern, force) {
	if (typeof force == 'undefined' || force != true) {
		if (search_pattern == pattern) {
			return;
		}
	}
	$('#id_div_floatingRight').show();
	
	searchPattern = pattern;
	var getUrl = "manuals/search?pattern=" + encodeURIComponent(searchPattern) + "&" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			var length = data["length"];
			if (length > 0) {
				$('#id_div_searchResult').empty();
				var arr = data["list"];
				for (var i = 0; i < arr.length; i++) {
					$('#id_div_searchResult').append(arr[i]["found"]);
				}
				$('.class_div_search_file').hover(function() {
					$(this).append('<div class="class_div_openPage"><img alt="open" src="images/new-tabx16.png"><span>OPEN in New Tab</span></div>');
					var h = $(this).height();
					var w = $(this).width();
					$('.class_div_openPage').css({
						'top': 4,
						'left': w - 140
					});
					$('.class_div_openPage').click(function(e) {
						loadManual($(this).parent().attr("path"));
					});
				}, function() {
					$('div').remove('.class_div_openPage');
				});
				
				$('.class_template_object').dblclick(function() {
//					if (!editorDocumentPage.isEditing()) {
//						editorDocumentPage.enableEditing();
//						editorPastePlugin.insertContent('<br>' + $(this).convertTemplateToEditor() + '<br>');
//					} else {
//						editorPastePlugin.insertContent('<br>' + $(this).convertTemplateToEditor() + '<br>');
//					}
					CKEDITOR.instances.id_editable_inner.insertHtml('<p>' + $(this).convertTemplateToEditor() + '</p>');
//					var element = CKEDITOR.dom.element.createFromHtml($(this).convertTemplateToEditor());
//					CKEDITOR.instances.id_editable_inner.insertElement(element);
				});
			} else {
				$('#id_div_searchResult').text("Not found.");
			}
		}
	})
	.fail(function(e) {
		if (e.status == 401) {
			updateUserArea(false);
		} else {
			alert( "error searching the manuals: " + e.message);
		}
	});	

	search_pattern = pattern;
	$('#id_a_clearSearch >img').css('visibility', 'visible');
	$('#id_a_search').addClass(CLASS_SEARCH_MADE);
}

$(function () {
	$('#id_a_search').on('click', function(e) {
		if ($('#id_input_search').val().trim().length == 0) {
			hideSearchResult();
		} else {
			searchForPattern($('#id_input_search').val(), true);
		}
	});

	$('#id_input_search').keyup(function (e) {
		if (typeof e.which == "undefined") {
	        return true;
	    }
		try {
			if ($('#id_input_search').val() == '') {
				removeSearchResult();
			} else {
				if (search_pattern != null || $('#id_input_search').val().trim().length > 2) {
					searchForPattern($('#id_input_search').val());
				} else {
					if(e.which == 13) {
						searchForPattern($('#id_input_search').val(), true);
					}
				}
			}
		} catch (e) {
			console.log(e);
		} finally {
			return false;  
		}
	});   

	$('#id_a_clearSearch').on('click', function(e) {
		hideSearchResult();
	});

	$('#id_a_clearSearch >img').css('visibility', 'hidden');
	$('#id_div_floatingRight').hide();
});

jQuery(document).ready(function(){
	
	var titleText = title + " Instrument Manuals";
	$(document).attr("title", titleText);
	$('#titleString').text(titleText);
	
	var getUrl = "manuals/list?" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
        	console.log(data);
			$.each(data, function(key, val) {
//				console.log(key + ':' + val);
				var lis = "";
				var itemCount = 0;
				$.each(val, function(k, v) {
					var buttonHtml = "";
					$.each(v["resource"], function(rk, rv) {
						lis += '<li><a onclick="loadManual(\'' + rv + '\')">&nbsp;&nbsp;<span class="class_span_menuItem">' 
							+ k + ' - ' + rk + '</span></a></li>';
						buttonHtml += '<button class="w3-btn w3-round w3-dark-grey" onclick="loadManual(\'' + rv + '\')">' + rk + '</button>&nbsp;'
					});
					if (itemCount == 0) {
						$("#id_div_main").loadTemplate("templates/manualCard.html",
							    {
							        title: v["full_title"],
							        buttons: buttonHtml
							    }, 
							    {
							    	append: true,
							    	beforeInsert: function() {
							    		$("#id_div_main").append('<div style="clear:both;"></div>');
									}
							    }
						);
					} else {
						$("#id_div_main").loadTemplate("templates/manualCard.html",
							    {
							        title: v["full_title"],
							        buttons: buttonHtml
							    }, 
							    {
							    	append: true
							    }
						);
					}
					itemCount ++;
				});
				$("#id_ul_menuList").append('<li class="active has-sub"><a id="id_a_menu' + key 
						+ '">' + key + '</a><ul id="id_ul_' + key + '">' + lis
						+ '</a></li>');
			});
		}

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
		
	}).fail(function(e) {
//		window.location = "../user/signin.html?redirect=catalog.html";
	}).always(function() {
	});
	
});