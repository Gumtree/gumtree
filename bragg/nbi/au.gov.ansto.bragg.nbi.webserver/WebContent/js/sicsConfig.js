var _model = null;
var GALIL_CONTROLLERS_NODE = "GALIL_CONTROLLERS";
//var SICS_MOTORS_NODE = "SICS_MOTORS";
var _galil;
var _sics;
var _message;
var _editorModel;
var _curModel;
var _curPath;
var _title;
var _editorTitle;
var _propertyTitle
var _editor;
var _property;
var _motors;
var _dirtyFlag;
var KEY_MOTOR_NAME = "motor_name";
var KEY_CONTROLLER_NAME = "asyncqueue";
var KEY_AXIS_NAME = "encoderaxis";

var IntValidator = function($ip) {
	var $input = $ip;
	var key = $ip.attr('key');
	
	var validate = function() {
		var val = $input.val();
		val = val.replace(/^\s+|\s+$/gm,'');
		$input.val(val);
		if (val == "") {
			showMsg(key + ' can not be empty.', 'danger');
			$input.parent().parent().addClass("warning");
		} else if (!$.isNumeric(val)) {
			showMsg(key + ' has to be a number.', 'danger');
			$input.parent().parent().addClass("warning");
		} else {
			showMsg('');
			$input.parent().parent().removeClass("warning");
		}
	};
	
	$input.blur(function() {
		validate();
	});
};

var showMsg = function(msg, type, timeLast) {
	if (typeof type === 'undefined') {
		type = 'info';
	}
	if (typeof timeLast === 'undefined') {
		timeLast = 10000;
	}
	_message.html('<span class="badge badge-' + type + '">' + msg + '</span>');
	setTimeout(function () {
		_message.html('');
    }, timeLast);
};

var _title_property_list = [KEY_MOTOR_NAME, 
	KEY_CONTROLLER_NAME,
	KEY_AXIS_NAME];

var _editable_tier_1 = ["absenchome", "home"];
var _editable_tier_2 = ["maxspeed", "stepsperx", "cntsperx"];

var isEditable = function(name) {
	if (_title_property_list.includes(name)) {
		return false;
	}
	if (_editable_tier_1.includes(name)) {
		return false;
	}
	if (_editable_tier_2.includes(name)) {
		return false;
	}
	return true;
};

var keysOf = function(obj) {
	var keys = [];
	$.each(obj, function(key, val) {
		keys.push(key);
	});
	return keys;
};

var getGalilItem = function(path) {
	if (_galil) {
		var segs = path.split('/');
		var obj = _galil;
		$.each(segs, function(i, val) {
			if (val.trim().length > 0) {
				obj = obj[val];
			}
		});
		return obj;
	} else {
		return null;
	}
};
 var updateNode = function(key, val) {
	var subMotor = _editorModel[keysOf(_editorModel)[0]];
	subMotor[key] = val;
 };
 
var loadGalilMotor = function(path) {
	_curModel = getGalilItem(path);
	if (_curModel) {
		_editorModel = $.extend(true, {}, _curModel);
		_curPath = path;
//		_message.html(JSON.stringify(_curModel));
		var subMotor = _editorModel[keysOf(_editorModel)[0]];
		var motorName = subMotor[KEY_MOTOR_NAME];
		var controller = subMotor[KEY_CONTROLLER_NAME];
		var axis = subMotor[KEY_AXIS_NAME];
		_title.text(controller + ":" + axis + "(SICS name:" + motorName + ")");
		var html = '<table class="table table-striped table-sm"><thead><tr><th width="40%">Key</th><th width="60%">Value</th></tr></thead><tbody>';
//		$.each(subMotor, function(key, val) {
//			html += '<tr><td>' + key + '</td><td>' + val + "</td></tr>";
//		});
		$.each(_editable_tier_1, function(i, key) {
			if (subMotor.hasOwnProperty(key)){
				html += '<tr><td>' + key + '</td><td class="editable"><input type="text" key="' + key + '" class="form-control" value="' + subMotor[key] + '"></td></tr>';
			}
		});
		$.each(_editable_tier_2, function(i, key) {
			if (subMotor.hasOwnProperty(key)){
				html += '<tr><td>' + key + '</td><td class="editable"><input type="text" key="' + key + '" class="form-control" value="' + subMotor[key] + '"></td></tr>';
			}
		});
		html += '</tbody></table>';
		_editorTitle.text('Encoder editable');
		_editor.html(html);
		var node = _editor.find('input[type="text"]');
		node.focus(function() {
			$(this).parent().parent().addClass("active");
		}).blur(function() {
			$(this).parent().parent().removeClass("active");
			updateNode($(this).attr('key'), $(this).val());
			$(this).addClass('changed');
		}).keypress(function( event ) {
			if ( event.which == 13 ) {
				$(this).blur();
			}
		});
		$.each(node, function(idx, n) {
			var iv = new IntValidator($(this));
		});


		_propertyTitle.text('Other properties');
		html = '<table class="table table-striped table-sm"><thead><tr><th width="40%">Key</th><th width="60%">Value</th></tr></thead><tbody>';
		$.each(subMotor, function(key, val) {
			if (typeof val === 'object') {
				html += '<tr><td>' + key + '</td><td>' + 
						'<table class="table table-striped table-sm"><thead><tr><th width="40%">Key</th><th width="60%">Value</th></tr></thead><tbody>';
				$.each(val, function(subKey, subVal){
					html += '<tr><td>' + subKey + '</td><td>' + subVal + "</td></tr>";
				});
				html += '</tbody></table></td></tr>';
			} else {
				html += '<tr><td>' + key + '</td><td>' + val + "</td></tr>";
			}
		});
		html += '</tbody></table>';
		_property.html(html);
	}
};

var showModelInSidebar = function() {
	var html = "";
	$.each(_galil, function(key, mc) {
		html = '<a class="class_a_mc" href="#"><h6 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' + 
				'<span class="class_span_mc_name"><i class="fas fa-caret-down"></i> ' + key + '</span></h6></a>' + 
				'<ul id="ul_mc_'+ key + '" class="nav flex-column class_ul_folder class_ul_hide">';
		$.each(mc, function(id, encoder) {
			html += '<li id="li_' + key + '_' + id + '" class="nav-item class_li_subitem"><a class="nav-link class_a_axis" path="/' + key + '/' + id + '" href="#">' +
              		id + '<span class="sr-only">(current)</span></a></li>';
		});
		html += "</ul>";
		var $div = $('<div/>').append(html);
		$div.find('a.class_a_mc').click(function() {
			$(this).next('ul').toggleClass('class_ul_hide');
		});
		$div.find('a.class_a_axis').click(function() {
			var path = $(this).attr('path');
			if (path) {
				loadGalilMotor(path);
			}
			$('.class_ul_folder > li').removeClass('active');
			$(this).parent().addClass('active');
			$(this).blur();
		});
		$("#id_div_sidebar").append($div.children());
	});
};

var getModel = function() {
	var url = 'yaml/model?inst=bilby';
	$.get(url, function(data) {
		_model = data;
		_galil = _model[GALIL_CONTROLLERS_NODE];
		_motors = {}
		$.each(_galil, function(key, mc) {
			$.each(mc, function(id, encoder) {
				var path = "/" + key + "/" + id;
				var motor = encoder[keysOf(encoder)[0]];
				var name = motor[KEY_MOTOR_NAME];
				_motors[name] = path;
			});
		});
//		_sics = _model[SICS_MOTORS_NODE];
		showModelInSidebar();
	}).fail(function(e) {
		if (e.status == 401) {
			alert("sign in required");
		} else {
			alert(e.statusText);
		}
	});
};

var saveModel = function() {
//	$('#id_modal_saveDialog').modal('hide');
	_curModel = $.extend(true, _curModel, _editorModel);
	var url = 'yaml/save?inst=bilby&msg=';
	var saveMsg = $('#id_input_saveMessage').val().replace(/^\s+|\s+$/gm,'');
	if (saveMsg.length > 0) {
		url += encodeURI(saveMsg);
	}
	var text = JSON.stringify(_editorModel);
	$.post(url,  {path:_curPath, model:text}, function(data) {
//		data = $.parseJSON(data);
		try {
			if (data["status"] == "OK") {
				showMsg("Saved in the server.");
				$('td.editable input.changed').removeClass('changed');
			} else {
				console.log(data);
				showMsg(data["reason"], 'danger');
			}
		} catch (e) {
			showMsg("Failed to save: " + e.statusText, 'danger');
		}
	}).fail(function(e) {
		console.log(e);
		showMsg("Faied to save: " + e.statusText, "danger");
	}).always(function() {
		$('#id_modal_saveDialog').modal('hide');
	});
};

var resetEditor = function($bt) {
	if (_curPath) {
		loadGalilMotor(_curPath);
		showMsg('Reset successfully');
	} else {
		showMsg('Model not found!', 'danger');
	}
};

var addPageTitle = function(){
	if (typeof title !== 'undefined') {
		var titleString = "SICS Configuration - " + title;
		$(document).attr("title", titleString);
		$('#id_a_instrument_name').text(titleString);
	}

};

var SearchWidget = function($t) {
	var $m = $t.parent().find('.messagepop');
	var curSel = -1;
	var lastText = null;
	
	var show = function($li){
		var name = $li.attr('name');
		var path = $li.attr('path');
		var mc = path.split('/')[1];
		var axis = path.split('/')[2];
		$m.hide();
		$('.class_ul_folder').addClass('class_ul_hide');
		$('#ul_mc_' + mc).removeClass('class_ul_hide');
		$('.class_ul_folder > li').removeClass('active');
		$('#li_' + mc + '_' + axis).addClass('active');
		loadGalilMotor(path);
		$t.blur();
	}
	
	
	var doSearch = function() {
		var target = '';
		curSel = -1;

		var html = '';
//		var found = {};
		$.each(_motors, function(name, path) {
			if (name.indexOf($t.val()) >= 0) {
//				found[name] = path;
				html += '<li class="messageitem" href="#" name="' + name + '" path="' + path + '"><span class="widget_text">' + 
						name + ' => ' + path + '</span></li>';
			}
		});
//		$.each(found, function(name, path) {
//			html += '<li class="messageitem" href="#" name="' + name + '"><span class="widget_text" title="' + path + '">' + name + '</span></li>';
//		});
//		$m.css('width', $t.width() + 16);
		$m.html(html);
		$m.show();
		$m.find('li:not(.groupname)').mousedown(function() {
			show($(this));
		});
	};
	
	this.init = function() {
		$t.off();
		$t.keyup(function(e) {
			if(e.keyCode == 13) {
				if (curSel >= 0) {
					show($m.find('li:not(.groupname)').eq(curSel));
				} 
				e.preventDefault();
				return false;
			} else if (e.keyCode == 38) {
				var lis = $m.find('li.messageitem');
				if (lis.length > 0) {
					if (curSel >= lis.length) {
							lis.removeClass('selected');
							lis.eq(lis.length - 1).addClass('selected');
							curSel = lis.length - 1;
					} else if (curSel > 0) {
							lis.removeClass('selected');
							curSel--;
							lis.eq(curSel).addClass('selected');
					} else if (curSel == 0) {
						lis.removeClass('selected');
						curSel = -1;
					}
				}				
			} else if (e.keyCode == 40) {
				var lis = $m.find('li.messageitem');
				if (lis.length > 0) {
					if (curSel < 0 || curSel >= lis.length) {
							lis.removeClass('selected');
							lis.eq(0).addClass('selected');
							curSel = 0;
					} else {
						if (curSel < lis.length - 1) {
							lis.removeClass('selected');
							curSel++;
							lis.eq(curSel).addClass('selected');
						}
					}
				}
			} else if (e.keyCode == 27) {
				$m.hide();
				curSel = -1;
			} else {
				var newText = $t.val().replace(/^\s+|\s+$/gm,'');
				if (newText != lastText) {
					if (newText.length >= 2) {
						doSearch();
					} else {
						$m.html('');
						$m.hide();
						curSel = -1;
					}
					lastText = newText;
				}
			}
		});
		$t.blur(function() {
			var lis = $m.find('li.messageitem');
			lis.removeClass('selected');
			$m.hide();
			curSel = -1;
		});
		$t.focus(function() {
			if ($m.find('li').length > 0) {
				$m.show();
			} else {
				var newText = $t.val().replace(/^\s+|\s+$/gm,'');
				if (newText.length >= 3) {
					doSearch();
				}
			} 
		});
	};
};

$(document).ready(function() {
	_message = $('#id_div_info');
	_title = $('#id_device_title');
	_editorTitle = $('#id_editor_subtitle');
	_editor = $('#id_div_editor_table');
	_propertyTitle = $('#id_property_subtitle');
	_property = $('#id_div_property_table');
	
	addPageTitle();
	
	getModel();
	
	$('#id_button_saveConfirm').click(function() {
		saveModel();
	});
	
	$('#id_input_saveMessage').keypress(function(event) {
		if ( event.which == 13 ) {
			saveModel();
		}
	});
	
	$('#id_button_reset').click(function() {
		resetEditor($(this));
	});
	
	var search = new SearchWidget($('#id_input_search_text'));
	search.init();
});