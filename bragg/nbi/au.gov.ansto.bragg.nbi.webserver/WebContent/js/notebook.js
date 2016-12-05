var topDbIndex = -1;
var bottomDbIndex = -1;
var isAppending = false;
var dbFilter = null;
var session = null;
var pageId = null;
var proposal = null;
var updateIntervalId = null;
var checkNewIntervalId = null;
var validateUserIntervalId = null;
var updateIntervalSeconds = 60;
var checkNewIntervalSeconds = 6;
var isLoggedIn = false;

jQuery.fn.outerHTML = function() {
	return jQuery('<div />').append(this.eq(0).clone()).html();
};

function formatDate(d) {
	var HH = d.getHours();
	if (HH < 10) HH = '0' + HH;
	var MM = d.getMinutes();
	if (MM < 10) MM = '0' + MM;
	var ss = d.getSeconds();
	if (ss < 10) ss = '0' + ss;
	return HH + ':' + MM + ":" + ss;
}

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

function validateUser() {
	var getUrl = "notebook/user";
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			if (data == 'NONE') {
				updateUserArea(null);
			} else {
				updateUserArea(true, 'notebook.html');
			}
		}
	})
	.fail(function(e) {
		updateUserArea(false);
	});
}

function checkNewPage() {
	var getUrl = "notebook/currentpage";
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			try {
				var newPageId = data.split(':')[1];
				if (pageId != newPageId) {
					stopCheckNewPage();
					$('<div></div>').appendTo('body')
					  .html('<div class="class_confirm_dialog"><p>The current '
							  + 'notebook page is expired because a new page was created by the administrator. '
							  + '</p><p>You must reload to the new page. Do you want to save the change before '
							  + 'reloading the page?</p></div>')
					  .dialog({
					      modal: true, title: 'Confirm Saving Expired Page', zIndex: 10000, autoOpen: true,
					      width: 'auto', resizable: false,
					      buttons: {
					          Yes: function () {
					        	  var postUrl = 'notebook/save' + (session != null ? '?session=' + session : '?pageid=' + pageId);
					        	  $.post( postUrl, CKEDITOR.instances.id_editable_inner.getData(), function(data, status) {
					        		  if (status == "success") {
					        			  var notification = new CKEDITOR.plugins.notification( CKEDITOR.instances.id_editable_inner, { message: 'Saved', type: 'success' } );
					        			  notification.show();
					        			  CKEDITOR.instances.id_editable_inner.resetDirty();
					        		  }
					        		  location.reload();
					        	  }).fail(function(e) {
					        		  var notification = new CKEDITOR.plugins.notification( CKEDITOR.instances.id_editable_inner, { message: 'Failed to save the page.', type: 'warning' } );
					        		  notification.show();
					        	  }).always(function() {
//						        	  $(this).dialog("close");
					        	  });
					          },
					          No: function () {
//					              $(this).dialog("close");
					        	  location.reload();
					          }
					      },
					      close: function (event, ui) {
					    	  location.reload();
					          $(this).remove();
					      }
					});
				}	
			} catch (e) {
				console.log(e);
			}			
		}
	})
	.fail(function(e) {
	}).always(function() {
	});
	
}

function startCheckNewPage() {
	checkNewIntervalId = setInterval(function(){
		checkNewPage();
		}, checkNewIntervalSeconds * 1000);
}

function startValidateUser() {
	validateUserIntervalId = setInterval(function(){
		validateUser();
		}, checkNewIntervalSeconds * 1000);
}

function stopCheckNewPage() {
	if (checkNewIntervalId != null) {
		clearInterval(checkNewIntervalId);
	}
}

function startUpdateInterval(){
	updateIntervalId = setInterval(function(){
		dbScrollTop();
		}, updateIntervalSeconds * 1000);
}

function stopUpdateInterval(){
	if (updateIntervalId != null) {
		clearInterval(updateIntervalId);
	}
}

function getHistoryPdf(session) {
	if (session == null || session.trim().length == 0) {
		return;
	}
	var getUrl = "notebook/pdf?session=" + session;
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
		alert( "error downloading PDF file.");
	}).always(function() {
		$(this).dialog("close");
	});
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
		        	  var postUrl = 'notebook/save' + (session != null ? '?session=' + session : '?pageid=' + pageId);
		        	  $.post( postUrl, CKEDITOR.instances.id_editable_inner.getData(), function(data, status) {
		        		  if (status == "success") {
		        			  var notification = new CKEDITOR.plugins.notification( CKEDITOR.instances.id_editable_inner, { message: 'Saved', type: 'success' } );
		        			  notification.show();
		        			  CKEDITOR.instances.id_editable_inner.resetDirty();
		        			  
		        			  setTimeout(function() {
		        				  var getUrl = "notebook/pdf";
		        				  var session = getParam("session");
		        				  if (session != null) { 
		        					  getUrl += "?session=" + session;
		        				  }
//		        				  window.location.href = getUrl;
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
		        			  }, 1000);
		        			  

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

// ********** below functions are for data catalogs ********** 

var CLASS_HIDDEN_COLUMN = "class_column_disable";
var CLASS_DISABLE_ITEM = 'class_a_disable';
var HIDDEN_DIV_ID = "_hiddenCopyText_";
var COLUMN_NAMES;
var TABLE_SIZE;
var CURRENT_PROPOSALID;
var LOADED_PROPOSALID;
var COOKIE_PREFIX = 'notebook_catalog_column_';
var checkNewFileIntervalId = null;
var checkNewFileIntervalSeconds = 10;
var NAME_COLUMN_TIMESTAMP = "_update_timestamp_";
var update_timestamp = "0";
var search_pattern;
var CLASS_SPAN_FOUND = "class_span_highlight";
var CLASS_HIDDEN_COLUMN = "class_column_disable";
var CLASS_SEARCH_MADE = "class_search_made";


function startCheckNewFile() {
	checkNewFileIntervalId = setInterval(function(){
		updateCatalogTable();
		}, checkNewFileIntervalSeconds * 1000);
}

function stopCheckNewFile() {
	if (checkNewFileIntervalId != null) {
		clearInterval(checkNewFileIntervalId);
	}
}

function escapeSpace(value) {
	if (value != null) {
		return value.replace(/([ /])|\./g, '_');
	}
	return null;
}

function updateCatalogTable() {
	
	var getUrl= "catalog/read?proposal=" + CURRENT_PROPOSALID + "&start=" + TABLE_SIZE + "&timestamp=" + update_timestamp + "&" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		var size = data["size"];
		if (size > 0) {
			$('.class_tr_new').removeClass('class_tr_new');
			var items = $('<div/>').html(makeTableBody(COLUMN_NAMES, data["body"])).children();
			$.each(items, function(idx, val) {
				$(this).find("th").addClass('class_tr_new');
			});
			$("#id_table_catalog > tbody").prepend(items);
			TABLE_SIZE += size;
		}
		update_timestamp = data["timestamp"];
	}).fail(function(e) {
	}).always(function() {
	});
	
//	var getUrl = "catalog/read?start=" + TABLE_SIZE + "&" + (new Date()).getTime();
//	$.get(getUrl, function(data, status) {
//		var size = data["size"];
//		if (size > 0) {
//			$('.class_tr_new').removeClass('class_tr_new');
//			var items = $('<div/>').html(makeTableBody(COLUMN_NAMES, data["body"])).children();
//			$.each(items, function(idx, val) {
//				$(this).find("th").addClass('class_tr_new');
//			});
//			$("#id_table_catalog > tbody").prepend(items);
////			$("#id_table_catalog > tbody").prepend(data["body"]);
//			TABLE_SIZE += size;
//		}
//	}).fail(function(e) {
//	}).always(function() {
//	});
}

function makeTableHeader(columnNames) {
	var html = '<tr class="class_tr_header"><th class="class_column_key">File Number</th>';
	for ( var i = 0; i < columnNames.length; i++) {
		var name = escapeSpace(columnNames[i]);
		var ck = Cookies.get(COOKIE_PREFIX + name);
		html += '<th class="class_column_' + name + (ck == 'disabled' ? ' ' + CLASS_HIDDEN_COLUMN : '') + '">' + columnNames[i] + '</th>';
	}
	html += '</tr>';
	return html;
}

function makeTableBody(columnNames, rowArray) {
	rowArray.sort(function(a, b){
		if(b['_key_'] < a['_key_']) return -1;
	    if(b['_key_'] > a['_key_']) return 1;
	    return 0;
	});
	var html = '';
	for ( var i = 0; i < rowArray.length; i++) {
		var row = rowArray[i];
		var innerHtml = '<th class="class_column_key class_tr_new">' + rowArray[i]['_key_'] + '</th>';
		for ( var j = 0; j < columnNames.length; j++) {
			var name = escapeSpace(columnNames[j]);
			var ck = Cookies.get(COOKIE_PREFIX + name);

			var cv = rowArray[i][columnNames[j]];
			if (cv == null) {
				cv = "";
			}
			innerHtml += '<td class="class_column_' + name + (ck == 'disabled' ? ' ' + CLASS_HIDDEN_COLUMN : '') + '">' + cv + '</td>';
		}

		var keyString = escapeSpace(rowArray[i]['_key_']);
		if ($('#id_tr_' + keyString).length > 0) {
			$('#id_tr_' + keyString).html(innerHtml);
		} else {
			html += '<tr class="class_tr_body" id="id_tr_' + keyString + '">' + innerHtml;
			html += '</tr>';
		}
	}
	return html;
}

function selectElementContents(el) {
	var body = document.body, range, sel;
	if (document.createRange && window.getSelection) {
		range = document.createRange();
		sel = window.getSelection();
		sel.removeAllRanges();
		try {
			range.selectNodeContents(el);
			sel.addRange(range);
		} catch (e) {
			range.selectNode(el);
			sel.addRange(range);
		}
	} else if (body.createTextRange) {
		range = body.createTextRange();
		range.moveToElementText(el);
		range.select();
	}
}

function exportTableToCSV($table, filename) {

	var $rows = $table.find('tr:has(td,th)'),

	// Temporary delimiter characters unlikely to be typed by keyboard
	// This is to avoid accidentally splitting the actual contents
	tmpColDelim = String.fromCharCode(11), // vertical tab character
	tmpRowDelim = String.fromCharCode(0), // null character

	// actual delimiter characters for CSV format
//	colDelim = '","',
	colDelim = ', ',
//	rowDelim = '"\r\n"',
	rowDelim = '\r\n',

// Grab text from table into CSV formatted string
//	csv = '"' + $rows.map(function (i, row) {
	csv = $rows.map(function (i, row) {
		var $row = $(row),
		$cols = $row.find('th,td').not('.' + CLASS_HIDDEN_COLUMN);

		return $cols.map(function (j, col) {
			var $col = $(col),
			text = $col.text();
			return text;
//			return text.replace(/"/g, '""'); // escape double quotes

		}).get().join(tmpColDelim);

	}).get().join(tmpRowDelim)
	.split(tmpRowDelim).join(rowDelim)
//	.split(tmpColDelim).join(colDelim) + '"',
	.split(tmpColDelim).join(colDelim) + '',

	// Data URI
	csvData = 'data:application/csv;charset=utf-8,' + encodeURIComponent(csv);

	$(this)
	.attr({
		'download': filename,
		'href': csvData,
		'target': '_blank'
	});
	
}

function copyToHiddenTable(items) {
	
	var html = '<table border="1" cellpadding="2" cellspacing="0" width="100%" class="scrollTable" id="tableTemp"><tbody class="scrollContent">';
	html += $("#table_thead_catalog").html();
	$.each(items, function(idx, val) {
		html += val.outerHTML;
	});
	html += '</tbody></table>';
	target = document.getElementById(HIDDEN_DIV_ID);
	if (!target) {
		var target = document.createElement("div");
		target.style.position = "absolute";
		target.style.left = "-9999px";
		target.style.top = "0";
		target.id = HIDDEN_DIV_ID;
		document.body.appendChild(target);
	}

	$('#' + HIDDEN_DIV_ID).html(html);
    
	selectElementContents( document.getElementById(HIDDEN_DIV_ID) );

	return true;
}

function toggleColumn(name) {
	name = escapeSpace(name);
	$('#id_a_' + name + ' >span').toggleClass(CLASS_DISABLE_ITEM);
	$('.class_column_' + name).toggleClass(CLASS_HIDDEN_COLUMN);
	if ($('#id_a_' + name + ' >span').hasClass(CLASS_DISABLE_ITEM)) {
		Cookies.set(COOKIE_PREFIX + name, 'disabled');
	} else {
		Cookies.set(COOKIE_PREFIX + name, 'enabled');
	}
}

function prepareColumnMenu() {
	for ( var i = 0; i < COLUMN_NAMES.length; i++) {
		var name = escapeSpace(COLUMN_NAMES[i]);
		var ck = Cookies.get(COOKIE_PREFIX + name);
		if (ck == 'disabled') {
			$('#id_a_' + name + ' >span').addClass(CLASS_DISABLE_ITEM);
		}
	}
}

function searchCatalog() {
	if ($('#id_input_search').val().trim().length == 0) {
		removeFilter();
	} else {
		filterPatter($('#id_input_search').val(), true);
	}
}

function updateSelectionTable() {
	$rows = $("#table_body_catalog >tr.ui-selected").not(".class_search_none");
	copyToHiddenTable($rows);
}

function hightlightText(text, pattern) {
	var re = new RegExp(pattern,"ig");
	return text.replace(re, '<span class="' + CLASS_SPAN_FOUND +'">$&</span>');
}

function filterPatter(pattern, force) {
	if (typeof force == 'undefined' || force != true) {
		if (search_pattern == pattern) {
			return;
		}
	}
	$("span." + CLASS_SPAN_FOUND).removeClass(CLASS_SPAN_FOUND);
	var $rows = $("#table_body_catalog").find('tr:has(td,th)');
	$.each($rows, function(itr, val) {
		var $row = $(this);
		var found = false;
		var $cols = $(this).find('th,td').not('.' + CLASS_HIDDEN_COLUMN);
		$.each($cols, function(itd, $col) {
			var text = $(this).text();
			var re = new RegExp(pattern,"i");
			if (text != null && text.match(re) != null) {
				found = true;
				$(this).html(hightlightText(text, pattern));
			}
		});
		if (found) {
			$row.removeClass("class_search_none");
			$row.addClass("class_search_found");
		} else {
			$row.removeClass("class_search_found");
			$row.addClass("class_search_none");
		}
	});

	search_pattern = pattern;
	$('#id_a_clearSearch').css('visibility', 'visible');
	$('#id_a_search').addClass(CLASS_SEARCH_MADE);
	updateSelectionTable();
}

function removeFilter() {
	if (search_pattern == null) {
		return;
	}
	$("span." + CLASS_SPAN_FOUND).removeClass(CLASS_SPAN_FOUND);
	$('.class_search_none').removeClass('class_search_none');
	$('.class_search_found').removeClass('class_search_found');
	$('#id_a_clearSearch').css('visibility', 'hidden');
	$('#id_a_search').removeClass(CLASS_SEARCH_MADE);
	search_pattern = null;
}

$(function(){
    $('#id_table_catalog > tbody').selectable({
//  	filter:'td,th',
    	filter:'tr',
    	selected: function(event, ui){
//  		console.log(event);
//  		console.log(ui);
    		var s=$(this).find('.ui-selected');
//  		$.each(s, function(idx, val){
//  		console.log(val.outerHTML);
//  		});
    		if (s.size() == 0) {
    			$('#id_li_printSelected').hide();
    			$('#id_li_exportSelected').hide();
    		} else {
    			$('#id_li_printSelected').show();
    			$('#id_li_exportSelected').show();
    		}
    		copyToHiddenTable(s);
    	},
    	unselected: function (event, ui) {
    		var s=$(this).find('.ui-selected');
    		if (s.size() == 0) {
    			$('#id_li_printSelected').hide();
    			$('#id_li_exportSelected').hide();
    		} else {
    			$('#id_li_printSelected').show();
    			$('#id_li_exportSelected').show();
    		}
    		copyToHiddenTable(s);
    	}
    });
    
    $('#id_div_catalogInsert').click(function(e) {
    	var s=$('#id_table_catalog').find('.ui-selected');
		if (s.size() > 0) {
			var cp = $('<div>').append($('#' + HIDDEN_DIV_ID).html());
			cp.find('.' + CLASS_HIDDEN_COLUMN).remove();
//			CKEDITOR.instances.id_editable_inner.insertHtml('<p>' + $('#' + HIDDEN_DIV_ID).html() + '</p>');
			CKEDITOR.instances.id_editable_inner.insertHtml('<p>' + cp.html() + '</p>');
		} else {
			alert('Please select at least one entry.');
		}
	});
    
//	$('#id_a_exportAll').on('click', function(e) {
//		exportTableToCSV.apply(this, [$("#id_table_catalog"), 'export.csv']);
//	});

	$('#id_a_catalogExport').on('click', function(e) {
		var s=$('#id_table_catalog').find('.ui-selected');
		if (s.size() > 0) {
			exportTableToCSV.apply(this, [$('#' + HIDDEN_DIV_ID + '>table'), 'export.csv']);
		} else {
			exportTableToCSV.apply(this, [$("#id_table_catalog"), 'export.csv']);
		}
	});
	
	$('#id_a_noColumns').on('click', function(e) {
		for ( var i = 0; i < COLUMN_NAMES.length; i++) {
			var name = escapeSpace(COLUMN_NAMES[i]);
			var columnItems = $('#id_a_' + name + ' >span');
			$.each(columnItems, function() {
				if (!$( this ).hasClass(CLASS_DISABLE_ITEM)) {
					$( this ).addClass(CLASS_DISABLE_ITEM);
				}
			});
			var columns = $('.class_column_' + name);
			$.each(columns, function() {
				if (!$( this ).hasClass(CLASS_HIDDEN_COLUMN)) {
					$( this ).addClass(CLASS_HIDDEN_COLUMN);
				}
			});
			Cookies.set(COOKIE_PREFIX + name, 'disabled');
		}
	});
	
	$('#id_a_allColumns').on('click', function(e) {
		for ( var i = 0; i < COLUMN_NAMES.length; i++) {
			var name = escapeSpace(COLUMN_NAMES[i]);
			var columnItems = $('#id_a_' + name + ' >span');
			$.each(columnItems, function() {
				if ($( this ).hasClass(CLASS_DISABLE_ITEM)) {
					$( this ).removeClass(CLASS_DISABLE_ITEM);
				}
			});
			var columns = $('.class_column_' + name);
			$.each(columns, function() {
				if ($( this ).hasClass(CLASS_HIDDEN_COLUMN)) {
					$( this ).removeClass(CLASS_HIDDEN_COLUMN);
				}
			});
			Cookies.set(COOKIE_PREFIX + name, 'enabled');
		}
	});
	
	$('#id_a_invertColumns').on('click', function(e) {
		for ( var i = 0; i < COLUMN_NAMES.length; i++) {
			var name = escapeSpace(COLUMN_NAMES[i]);
			$('#id_a_' + name + ' >span').toggleClass(CLASS_DISABLE_ITEM);
			$('.class_column_' + name).toggleClass(CLASS_HIDDEN_COLUMN);
			if ($('#id_a_' + name + ' >span').hasClass(CLASS_DISABLE_ITEM)) {
				Cookies.set(COOKIE_PREFIX + name, 'disabled');
			} else {
				Cookies.set(COOKIE_PREFIX + name, 'enabled');
			}
		}
	});
	
	$('#id_input_search').keyup(function (e) {
		if (typeof e.which == "undefined") {
	        return true;
	    }
		try {
			if ($('#id_input_search').val() == '') {
				removeFilter();
			} else {
				if (search_pattern != null || $('#id_input_search').val().trim().length > 2) {
					filterPatter($('#id_input_search').val());
				} else {
					if(e.which == 13) {
						filterPatter($('#id_input_search').val(), true);
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
		removeFilter();
	});

	$('#id_a_clearSearch').css('visibility', 'hidden');;
});

//********** end of functions for data catalogs ********** 

function makeNewPage(){
	$('<div></div>').appendTo('body')
	  .html('<div class="class_confirm_dialog"><p>Do you want to save the current page and make a new one? '
			  + '</p><p>You will not be able to edit this page again.</p></div>')
	  .dialog({
	      modal: true, title: 'Confirm Making New Page', zIndex: 10000, autoOpen: true,
	      width: 'auto', resizable: false,
	      buttons: {
	          Yes: function () {
	        	  var postUrl = 'notebook/save' + (session != null ? '?session=' + session : '?pageid=' + pageId);
	        	  $.post( postUrl, CKEDITOR.instances.id_editable_inner.getData(), function(data, status) {
	        		  if (status == "success") {
	        			  var notification = new CKEDITOR.plugins.notification( CKEDITOR.instances.id_editable_inner, { message: 'Saved', type: 'success' } );
	        			  notification.show();
	        			  CKEDITOR.instances.id_editable_inner.resetDirty();
	    	        	  var getUrl = "notebook/new?proposal_id=" + proposal + "&" + (new Date()).getTime();
	    	        	  $.get(getUrl, function(data, status) {
	    	        		  if (status == "success") {
	    		        		  location.reload();
	    	        		  }
	    	        	  }).fail(function(e) {
	    	        		  alert( "error creating new notebook file.");
	    	        	  });
	        		  } 
	        	  }).fail(function(e) {
	        		  if (e.status == 401) {
	        			  var notification = new CKEDITOR.plugins.notification( CKEDITOR.instances.id_editable_inner, { message: 'Failed to save, loggin required.', type: 'warning' } );
	        		  } else {
	        			  var notification = new CKEDITOR.plugins.notification( CKEDITOR.instances.id_editable_inner, { message: 'Failed to save the page.', type: 'warning' } );
	        		  }
        			  notification.show();
	        	  }).always(function() {
//		        	  $(this).dialog("close");
	        	  });
	          },
	          No: function () {
	              $(this).dialog("close");
	          }
	      },
	      close: function (event, ui) {
	          $(this).remove();
	      }
	});
}

function getHistoryWord(session, pageId) {
	if (session == null || session.trim().length == 0) {
		return;
	}
	var getUrl = "notebook/load?session=" + session;
	$.get(getUrl, function(data, status) {
		if (status == "success") {
//			var data = CKEDITOR.instances.id_editable_inner.getData();
		    var converted = htmlDocx.asBlob(data);
		    var fn = "Quokka_Notebook";
		    if (pageId != null) {
		    	fn = pageId;
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

function getWord(){
	var data = CKEDITOR.instances.id_editable_inner.getData();
//	jQuery('<div />').append(data).wordExport();
	
    var converted = htmlDocx.asBlob(data);
    var fn = title + "_Notebook";
    if (pageId != null) {
    	fn = pageId;
    }
    fn += ".docx";
    saveAs(converted, fn);
    
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

function showLogoutMessage(msg) {
	var notification = new CKEDITOR.plugins.notification( CKEDITOR.instances.id_editable_inner, { message: msg, type: 'info' } );
	notification.show();
}

//function signout(){
//    var getUrl = "signin/LOGOUT";
//    $.get(getUrl, function(data, status) {
//        if (status == "success") {
//            if (data['result'] == "OK") {
//            	$("#id_div_main").html("<div class=\"id_span_infoText\">You have successfully signed out. Now jump to the sign in page. "
//            			+ "If the browser doesn't redirect automatically, please click <a href=\"signin.html\">here</a>.</div>");
//                setTimeout(function() {
//                	window.location = "signin.html?redirect=notebook.html";
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
////		alert( "User session is expired. Please sign in again.");
//		var notification = new CKEDITOR.plugins.notification( CKEDITOR.instances.id_editable_inner, { message: 'User session is expired. Please sign in again.', type: 'info' } );
//		notification.show();
//	}
//	isLoggedIn = loggedIn;
//}

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
		if (e.status == 401) {
			updateUserArea(false);
		} else {
			alert( "error loading db xml file.");
		}
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
				$('#id_sidebar_inner').prepend('<div class="class_inner_topmessage">No new entry was found. Last checked at ' + formatDate(new Date()) + '.</div>');
				return;
			}
			var brk = data.indexOf(";");
			var pair = data.substring(0, brk);
			topDbIndex = parseInt(pair.substring(0, pair.indexOf(":")));
			var tempBottomDbIndex = parseInt(pair.substring(pair.indexOf(":") + 1));

			if (topDbIndex - tempBottomDbIndex < 0){
				$('#id_sidebar_inner').prepend('<div class="class_inner_topmessage">No new entry was found. Last checked at ' + formatDate(new Date()) + '.</div>');
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
		if (e.status == 401) {
			updateUserArea(false);
		} else {
			alert( "error loading db xml file.");
		}
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
		$("#id_editable_page").height(bodyheight - 120);
//		$(".div_canvas_inner").height(bodyheight - 80);
	});

// define slide out side bar
    $('.slide-out-div').tabSlideOut({
    	tabHandleClass: '.a_sidebar_handle',
    	tabBlockClass: '.div_sidebar_block',
        tabHandles: ['#a_sidebar_database', '#a_sidebar_template', '#a_sidebar_canvas', '#a_sidebar_catalog'],                     //class of the element that will become your tab
        tabBlocks: ['#div_sidebar_database', '#div_sidebar_template', '#div_sidebar_canvas', '#div_sidebar_catalog'],
        tabHandleSize: 180,
        pathToTabImage: $('html').hasClass('ie9') ? ['images/Database.GIF', 'images/Template.GIF', 'images/Canvas.GIF', 'images/Catalog.GIF'] : null, //path to the image for the tab //Optionally can be set using css
        imageHeight: '198px',                     //height of tab image           //Optionally can be set using css
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
        	stopUpdateInterval();
        	startUpdateInterval();
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
	if (typeof title !== 'undefined') {
		var titleString = "Instrument Notebook - " + title;
		$(document).attr("title", titleString);
		$('#titleString').text(titleString);
	}
	
	var bodyheight = $(window).height();
	$(".slide-out-div").height(bodyheight - 20);
	$(".div_sidebar_inner").height(bodyheight - 44);
	$(".div_canvas_slideout").height(bodyheight - 20);
	$("#id_editable_page").height(bodyheight - 120);

	session = getParam('session');
	
//	load current notebook content file
	var getUrl = "notebook/load";
	var pageIdUrl = "notebook/pageid";
	var historyUrl = "notebook/history";
	if (session != null && session.trim().length > 0) {
		getUrl += "?session=" + session;
		pageIdUrl += "?session=" + session;
		historyUrl += "?session=" + session;
	}
	$.get(getUrl, function(data, status) {
		if (status == "success") {
//			$('#id_editable_page').html(decodeURIComponent(data.replace(/\+/g, ' ')));
			if (data.trim().length == 0) {
				$('#id_editable_inner').html("<p><br></p>");
			} else {
				$('#id_editable_inner').html(data);
			}
			
			$.get(pageIdUrl, function(data, status) {
				if (status == "success") {
					pageId = data;
				}
			}) 
			.fail(function(e) {
			});

			$.get(historyUrl, function(data, status) {
				if (status == "success") {
					var brk = data.indexOf(";");
					var proposalId = data.substring(0, brk);
					var sessions = data.substring(brk + 1);
					if (brk != "Unknown") {
						$('#id_span_proposalId').text("History pages of Proposal " + proposalId);
					}
					proposal = proposalId;
					if (sessions != "None") {
						var sessionList = sessions.split(",");
						for ( var i = 0; i < sessionList.length; i++) {
							var sessionPair = sessionList[i].split(":");
							var sessionId = sessionPair[0];
							var subPageId = sessionPair[1];
							if ((session != null && sessionId != session) || (session == null && subPageId != pageId)) {
								var html = '<li class="active has-sub"><a id="history_' + sessionId + '"><span>' + subPageId + '</span></a>';
								html += '<ul><li><a onclick="getHistoryPdf(\'' + sessionId + '\')"><img src="images/pdf.png"><span class="class_span_historyIcons">&nbsp;&nbsp;--&nbsp;Download PDF</span></a></li>'
								+ '<li><a onclick="getHistoryWord(\'' + sessionId + '\', \'' + subPageId + '\')"><img src="images/word.png"><span class="class_span_historyIcons">&nbsp;&nbsp;--&nbsp;Download Word</span></a></li></ul>';
								html += '</li>';
								$('#id_ul_historyitems').append(html);
							}
						}
					}
				}
			}) 
			.fail(function(e) {
			});
			
//			make editable page
			CKEDITOR.replace( 'id_editable_inner' );
			CKEDITOR.instances.id_editable_inner.on('save', function(event, editor, data) {
//				alert(CKEDITOR.instances.id_editable_inner.getData());
		        var postUrl = 'notebook/save' + (session != null ? '?session=' + session : '?pageid=' + pageId);
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
        		    if (e.status == 401) {
        			    updateUserArea(false);
        		    }
		        });
		    });
			
			if (session == null) {
				startCheckNewPage();				
			}
			CKEDITOR.instances.id_editable_inner.on("instanceReady", function(event) {
				var sessionPar = getParam("session");
				if (sessionPar != null) {
					var newpageCommand = CKEDITOR.instances.id_editable_inner.getCommand('newpage');
					if (newpageCommand != null) {
						newpageCommand.disable();
					}
				}
			});

//			$('#id_editable_inner').ckeditor().on('save', function(event, editor, data) {
//				alert("save");
//			});
		}
	})
	.fail(function(e) {
		if (e.status == 401) {
			window.location = "../user/signin.html?redirect=notebook.html";
		}
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
				startUpdateInterval();
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

			startUpdateInterval();
			
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
	});

	
    var getUrl;
    if (session == null) {
    	getUrl = "catalog/read?" + (new Date()).getTime();;
    } else {
    	getUrl = "catalog/read?session=" + session + "&" + (new Date()).getTime();;
    }
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			var re = data["status"];
			if (re == "OK") {
	        	COLUMN_NAMES = data["header"];
	        	CURRENT_PROPOSALID = data["proposal"];
	        	LOADED_PROPOSALID = CURRENT_PROPOSALID;
	        	
	        	$("#id_table_catalog > thead").append(makeTableHeader(COLUMN_NAMES));
	        	$("#id_table_catalog > tbody").append(makeTableBody(COLUMN_NAMES, data["body"]));
	        	TABLE_SIZE = data["size"];
	        	
	        	for ( var i = 0; i < COLUMN_NAMES.length; i++) {
		        	$("#id_div_catalogDropdown").append('<a id="id_a_' + escapeSpace(COLUMN_NAMES[i]) 
		        			+ '" onclick="toggleColumn(\'' + escapeSpace(COLUMN_NAMES[i]) 
		        			+ '\')"' + '>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="class_a_columns">[' 
		        			+ COLUMN_NAMES[i] + ']</span></a>');
				}
	        	update_timestamp = data["timestamp"];
	        	prepareColumnMenu();
			}
		}
	}).fail(function(e) {
	}).always(function() {
	});
	
	
	getUrl = "notebook/user";
	$.get(getUrl, function(data, status) {
		if (status == "success") {
//			$('#id_a_signout').html("<img src=\"images/signout_blue.png\"/>Sign Out ");
//			$("#id_a_signout").click(function() {
//				console.log('click');
//				signout();
//			});
			if (data == 'NONE') {
				updateUserArea(null);
			} else {
				updateUserArea(true, 'notebook.html');
			}
		}
	})
	.fail(function(e) {
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
	        	stopUpdateInterval();
	        	startUpdateInterval();
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
	
	startValidateUser();
	startCheckNewFile();
});