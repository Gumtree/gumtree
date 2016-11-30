var checkUserIntervalSeconds = 5;
var checkUserIntervalId;
var COLUMN_NAMES;
var CURRENT_PROPOSALID;
var LOADED_PROPOSALID;
var HIDDEN_DIV_ID = "_hiddenCopyText_";
var TABLE_SIZE = 0;
var CLASS_HIDDEN_COLUMN = "class_column_disable";
var CLASS_SEARCH_MADE = "class_search_made";
var CLASS_DISABLE_ITEM = 'class_a_disable';
var COOKIE_PREFIX = 'catalog_column_';
var checkNewFileIntervalId = null;
var checkNewFileIntervalSeconds = 10;
var NAME_COLUMN_TIMESTAMP = "_update_timestamp_";
var update_timestamp = "0";
var search_pattern;
var CLASS_SPAN_FOUND = "class_span_highlight";


function startCheckNewFile() {
	checkNewFileIntervalId = setInterval(function(){
		try {
			updateCatalogTable();
		} catch (e) {
		}
		}, checkNewFileIntervalSeconds * 1000);
}

function stopCheckNewFile() {
	if (checkNewFileIntervalId != null) {
		clearInterval(checkNewFileIntervalId);
	}
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

var download = function() {
	for(var i=0; i<arguments.length; i++) {
		var iframe = $('<iframe style="visibility: collapse;"></iframe>');
		$('body').append(iframe);
		var content = iframe[0].contentDocument;
		var form = '<form action="' + arguments[i] + '" method="GET"></form>';
		content.write(form);
		$('form', content).submit();
		setTimeout((function(iframe) {
			return function() { 
				iframe.remove(); 
			}
		})(iframe), 2000);
	}
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

function copyToHiddenTable(items) {
	// create hidden text element, if it doesn't already exist
//	var isInput = elem.tagName === "INPUT" || elem.tagName === "TEXTAREA";
//	var origSelectionStart, origSelectionEnd;
//	if (isInput) {
//		// can just use the original source element for the selection and copy
//		target = elem;
//		origSelectionStart = elem.selectionStart;
//		origSelectionEnd = elem.selectionEnd;
//	} else {
		// must use a temporary form element for the selection and copy
	
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
//	}
	// select the content
//	var currentFocus = document.activeElement;
//	$('#' + HIDDEN_DIV_ID).focus();
//	$('#tableTemp').select();
    
	selectElementContents( document.getElementById(HIDDEN_DIV_ID) );

	return true;
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

/**
 * detect IE
 * returns version of IE or false, if browser is not Internet Explorer
 */
function detectIE() {
	var ua = window.navigator.userAgent;

	// Test values; Uncomment to check result бн

	// IE 10
	// ua = 'Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0)';

	// IE 11
	// ua = 'Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko';

	// Edge 12 (Spartan)
	// ua = 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36 Edge/12.0';

	// Edge 13
	// ua = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586';

	var msie = ua.indexOf('MSIE ');
	if (msie > 0) {
		// IE 10 or older => return version number
		return parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
	}

	var trident = ua.indexOf('Trident/');
	if (trident > 0) {
		// IE 11 => return version number
		var rv = ua.indexOf('rv:');
		return parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
	}

	var edge = ua.indexOf('Edge/');
	if (edge > 0) {
		// Edge (IE 12+) => return version number
		return parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10);
	}

	// other browser
	return false;
}

function exportTableToCSV($table, filename) {

	var $rows = $table.find('tr:has(td,th)').not('.class_search_none'),

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

//	if (navigator.userAgent.indexOf('MSIE') !== -1 || navigator.appVersion.indexOf('Trident/') > 0) {
//		console.log("MSIE");
//		if(window.navigator.msSaveOrOpenBlob){
//			blobObject = new Blob([csv]);
//			window.navigator.msSaveOrOpenBlob(blobObject, filename);
//		}
//	} else {
//		console.log("other browser");
//		$(this)
//		.attr({
//			'download': filename,
//			'href': csvData,
//			'target': '_blank'
//		});
//	}

	var version = detectIE();

	if (version === false || version >= 12) {
		$(this)
		.attr({
			'download': filename,
			'href': csvData,
			'target': '_blank'
		});
	} else {
		console.log("IE " + version);
		if(window.navigator.msSaveOrOpenBlob){
			blobObject = new Blob([csv]);
			window.navigator.msSaveOrOpenBlob(blobObject, filename);
		}
	} 


}

function refreshPageToNewProposalId() {
	$('<div class="no_print"></div>').appendTo('body')
	  .html('<div class="class_confirm_dialog"><p>The current '
			  + 'catalog page is expired because a new proposal was created by the administrator. '
			  + '</p><p>You must reload to the new page. Do you want to print the current page before leaving?</p></div>')
	  .dialog({
	      modal: true, title: 'Confirm Refreshing Page to New Proposal', zIndex: 10000, autoOpen: true,
	      width: 'auto', resizable: false,
	      buttons: {
	    	  Print: function() {
	    		  try {
	    			  window.print();
	    		  } finally {
	    			  location.reload();
	    		  }
	    	  },
	          Leave: function () {
	        	  location.reload();
	          }
	      },
	      close: function (event, ui) {
	    	  location.reload();
	          $(this).remove();
	      }
	});
}

function updateCatalogTable() {
	var getUrl = "catalog/read?proposal=" + CURRENT_PROPOSALID + "&start=" + TABLE_SIZE + "&timestamp=" + update_timestamp + "&" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		var currentProposal = data["current_proposal"];
		if (currentProposal != null && currentProposal != CURRENT_PROPOSALID) {
			stopCheckNewFile();
			refreshPageToNewProposalId();
		}
		var size = data["size"];
		if (size > 0) {
			$('.class_tr_new').removeClass('class_tr_new');
			var items = $('<div/>').html(makeTableBody(COLUMN_NAMES, data["body"])).children();
			$.each(items, function(idx, val) {
				$(this).find("th").addClass('class_tr_new');
			});
			$("#id_table_catalog > tbody").prepend(items);
//			$("#id_table_catalog > tbody").prepend(data["body"]);
			TABLE_SIZE += size;
		}
		registerListeners(data["body"]);
		update_timestamp = data["timestamp"];
		updateUserArea(true);
	}).fail(function(e) {
	}).always(function() {
	});
}

function registerListeners(rows) {
	for ( var i = 0; i < rows.length; i++) {
		var key = escapeSpace(rows[i]["_key_"]);
		var $row = $('#id_tr_' + key);
		$row.find('td.class_column_Comments').on('click', function(e) {
			$(this).children('input:first').show();
			$(this).children('span:first').hide();
			$(this).children('input:first').focus();
			return false;
		});
		$row.find('input.class_input_comments').keypress(function(e) {
			if(e.which === 13) {
				var span = $(this).parent().children('span:first');
				if (span.text().trim() != $(this).val().trim()) {
					updateEntry($(this).closest('tr').children('th:first').text(), 'Comments', $(this).val(), $(this));
				} else {
					$(this).hide();
					span.show();
				}
			}
			if(e.keyCode === 27) {
				$(this).hide();
				var span = $(this).parent().children('span:first');
				$(this).val(span.text());
				span.show();
			}
		});
		$row.find('input.class_input_comments').blur(function(e) {
			var span = $(this).parent().children('span:first');
			if (span.text().trim() != $(this).val().trim()) {
				updateEntry($(this).closest('tr').children('th:first').text(), 'Comments', $(this).val(), $(this));
			} else {
				$(this).hide();
				span.show();
			}
		});
	}
}

//function loadColumnConfig() {
//	for ( var i = 0; i < COLUMN_NAMES.length; i++) {
//		var name = escapeSpace(COLUMN_NAMES[i]);
//		var ck = Cookies.get(COOKIE_PREFIX + name);
//		console.log(name + ' ' + ck);
//		if (ck == 'disabled') {
//			$('#id_a_' + name + ' >span').addClass(CLASS_DISABLE_ITEM);
//			$('.class_column_' + name).addClass(CLASS_HIDDEN_COLUMN);
//		}
//	}
//}

function printSelected() {
    var myPrintContent = document.getElementById(HIDDEN_DIV_ID);
    var myPrintWindow = window.open(null, 'Data Catalog - ' + title, 'left=300,top=100,width=400,height=400');
    myPrintWindow.document.write('<html><head><title>Data Catalog - ' + title 
    		+ '</title><link rel="stylesheet" type="text/css" href="css/catalog.css"></head><body>');
    myPrintWindow.document.write(document.getElementById("titleString").outerHTML);
    myPrintWindow.document.write(myPrintContent.innerHTML);
//    myPrintWindow.document.getElementById(HIDDEN_DIV_ID).style.display='block'
    myPrintWindow.document.close();
    myPrintWindow.focus();
    myPrintWindow.print();
    myPrintWindow.close();    
    return false;
}

function escapeSpace(value) {
	if (value != null) {
		return value.replace(/([ /])|\./g, '_');
	}
	return null;
}

function makeTableHeader(columnNames) {
	var html = '<tr class="class_tr_header"><th class="class_column_key">File Name</th>';
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
			} else {
				cv = $("<div>").text(cv).html();
			}
			if (name != "Comments") {
				innerHtml += '<td class="class_column_' + name + (ck == 'disabled' ? ' ' + CLASS_HIDDEN_COLUMN : '') + '">' + cv + '</td>';
			} else {
				innerHtml += '<td class="class_column_' + name + (ck == 'disabled' ? ' ' + CLASS_HIDDEN_COLUMN : '') + '">' 
						+ '<input type="text" class="class_input_comments" style="display:none;" value="' + cv + '"><span>' + cv + '</span>'
						+ '</td>';
			}
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
	$('#id_a_clearSearch >img').css('visibility', 'visible');
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
	$('#id_a_clearSearch >img').css('visibility', 'hidden');
	$('#id_a_search').removeClass(CLASS_SEARCH_MADE);
	search_pattern = null;
}

$(function() {
	$('#id_table_catalog > tbody').selectable({
//        filter:'td,th',
        filter:'tr',
        selected: function(event, ui){
//            console.log(event);
//            console.log(ui);
            var s=$(this).find('.ui-selected');
//            $.each(s, function(idx, val){
//                console.log(val.outerHTML);
//            });
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

	$('#id_a_exportAll').on('click', function(e) {
		exportTableToCSV.apply(this, [$("#id_table_catalog"), title + '_catalog_' + LOADED_PROPOSALID + '.csv']);
	});

	$('#id_a_exportSelected').on('click', function(e) {
		var s=$('#id_table_catalog').find('.ui-selected');
		if (s.size() > 0) {
			exportTableToCSV.apply(this, [$('#' + HIDDEN_DIV_ID + '>table'), title + '_catalog_' + LOADED_PROPOSALID + '.csv']);
		} else {
			alert('Please select at least one file.');
		}
	});

	$('#id_a_printAll').on('click', function(e) {
		window.print();
	});

	$('#id_a_printSelected').on('click', function(e) {
		printSelected();
	});

	$('#id_a_update').on('click', function(e) {
		updateCatalogTable();
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
	
	$('#id_a_downloadAll').on('click', function(e) {
		download('flot/QKK0115497.nx.hdf', 'flot/QKK0115498.nx.hdf');
	})
	
	$('#id_a_search').on('click', function(e) {
		if ($('#id_input_search').val().trim().length == 0) {
			removeFilter();
		} else {
			filterPatter($('#id_input_search').val(), true);
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

	$('#id_a_clearSearch >img').css('visibility', 'hidden');
	
});

function updateEntry(key, column, val, $item) {
	var obj = {};
	obj[column] = val;
	var form = {key: key, columns: JSON.stringify(obj)};
	var postUrl = "catalog/update";
	$.post(postUrl, form, function(data, status) {
		if (status == "success") {
			var span = $item.parent().children('span:first');
			span.text($item.val());
			span.show();
			$item.hide();
		} else {
			var span = $item.parent().children('span:first');
			span.append('error submitting changes, please try again.');
			span.show();
			$item.hide();
		}
	}).fail(function(e) {
		var span = $item.parent().children('span:first');
		span.text('error submitting changes, please try again.');
		span.show();
		$item.hide();
	}).always(function() {
	});
}
//$(document).keydown(function(e) {
//    if (e.keyCode == 67 && e.ctrlKey) {
//        console.log('copy');
//    }
//});

jQuery(document).ready(function(){
	
	var titleText = "Data Catalog - " + title;
	$(document).attr("title", titleText);
	$('#titleString').text(titleText);
	
	$('#id_li_printSelected').hide();
	$('#id_li_exportSelected').hide();

	var getUrl = "catalog/read?" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			var re = data["status"];
			if (re != "OK") {
				$("#id_div_main").html("<div class=\"id_span_infoText\">Status: <span style=\"color:red\">" 
					+ re + "</span>. Now jump to the sign in page. "
	        		+ "If the browser doesn't redirect automatically, please click "
	        		+ "<a href=\"../signin.html\">here</a>.</div>");
	            setTimeout(function() {
	            	window.location = "../signin.html?redirect=doc/catalog.html";
				}, 2000);
	        } else {
//				$.each(data["menu"], function(link, text) {
//					$("#cssmenu").append('<ul><li><a href="' + link + '">' + text + '</a></li></ul>');
//				});
//				$.each(data["info"], function(link, text) {
//					$("#id_div_main").append("<div class=\"id_span_infoText\">" + text + "</div>");
//				});

	        	COLUMN_NAMES = data["header"];
	        	CURRENT_PROPOSALID = data["proposal"];
	        	LOADED_PROPOSALID = CURRENT_PROPOSALID;
	        	
	        	$("#id_table_catalog > thead").append(makeTableHeader(COLUMN_NAMES));
	        	$("#id_table_catalog > tbody").append(makeTableBody(COLUMN_NAMES, data["body"]));
	        	$('#titleString').text(titleText + ' Proposal ' + data["proposal"]);
	        	TABLE_SIZE = data["size"];
	        	
	        	for ( var i = 0; i < COLUMN_NAMES.length; i++) {
		        	$("#id_ul_configure").append('<li><a id="id_a_' + escapeSpace(COLUMN_NAMES[i]) 
		        			+ '" onclick="toggleColumn(\'' + escapeSpace(COLUMN_NAMES[i]) 
		        			+ '\')"' + '>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="class_a_columns">[' 
		        			+ COLUMN_NAMES[i] + ']</span></a></li>');
				}
	        	prepareColumnMenu();
	        	update_timestamp = data["timestamp"];
//	        	loadColumnConfig();
				updateUserArea(true);
				startCheckUser();
				$('.class_column_Comments').on('click', function(e) {
					$(this).children('input:first').show();
					$(this).children('span:first').hide();
					$(this).children('input:first').focus();
					return false;
				});
				$('.class_input_comments').keypress(function(e) {
					if(e.which === 13) {
						var span = $(this).parent().children('span:first');
						if (span.text().trim() != $(this).val().trim()) {
							updateEntry($(this).closest('tr').children('th:first').text(), 'Comments', $(this).val(), $(this));
						} else {
							$(this).hide();
							span.show();
						}
					}
					if(e.keyCode === 27) {
						$(this).hide();
						var span = $(this).parent().children('span:first');
						$(this).val(span.text());
						span.show();
					}
				});
				$('.class_input_comments').blur(function(e) {
					var span = $(this).parent().children('span:first');
					if (span.text().trim() != $(this).val().trim()) {
						updateEntry($(this).closest('tr').children('th:first').text(), 'Comments', $(this).val(), $(this));
					} else {
						$(this).hide();
						span.show();
					}
				});
			}
		}
	}).fail(function(e) {
		window.location = "../signin.html?redirect=doc/catalog.html";
	}).always(function() {
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
	
	startCheckNewFile();
});