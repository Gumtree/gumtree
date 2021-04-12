var PAR_INSTRUMENT_ID = "inst";
var _inst = null;
var _model = null;
var GALIL_CONTROLLERS_NODE = "GALIL_CONTROLLERS";
//var SICS_MOTORS_NODE = "SICS_MOTORS";
var _galil;
var _sics;
var _message;
var _editorModel;
var _curModel;
var _curName = null;
var _curPath;
var _title;
var _editorTitle;
var _tabs;
var _propertyTitle
var _editor;
var _property;
var _motors;
var _dirtyFlag = false;
var _versionId = "";
var _timestamp = "";
var KEY_MOTOR_NAME = "motor_name";
var KEY_MOTOR_DESC = "description";
var KEY_CONTROLLER_NAME = "asyncqueue";
var KEY_AXIS_NAME = "encoderaxis";
var KEY_AXIS_NAME = "axis";
var TABLE_TIER1_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="25%">Key</th><th width="25%">Type</th><th width="50%">Value</th></tr></thead><tbody>';
var TABLE_TIER2_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="40%">Key</th><th width="40%">Value</th><th width="20%"></th></tr></thead><tbody>';
var EMPTY_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" value="';
var EMPTY_ROW_PART2 = '"></td><td class="pair_value"><input type="text" class="form-control" value="';
var EMPTY_ROW_PART3 = '"></td><td class="pair_control input-group-btn"><button type="button" class="btn btn-outline-primary button_plus">+</button><button type="button" class="btn btn-outline-primary button_minus">-</button></td></tr>';

var _historyBar;

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
	if (type == 'danger') {
		timeLast = 100000;
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

var updateModel = function(path, idx, newModel){
	if (_galil) {
		console.log(path);
		var segs = path.split('/');
		var newObj = newModel[GALIL_CONTROLLERS_NODE];
		var obj = _galil;
		var l = segs.length;
		if (l == 1) {
			obj[segs[0]] = newObj[segs[0]];
		} else {
			for (var i = 0; i < l - 1; i++) {
				if (segs[i].trim().length > 0) {
					console.log(obj);
					obj = obj[segs[i]];
					newObj = newObj[segs[i]];
				}
			}
			obj[segs[l - 1]] = newObj[segs[l - 1]]; 
		}
	}
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

var updateNode = function($node, key, val) {
//	var subMotor = _editorModel[keysOf(_editorModel)[0]];
	var subMotor = _editorModel;
	var curVal = subMotor[key];
	if (curVal.toString() != val.toString()) {
		$node.addClass('changed');
		_dirtyFlag = true;
	} else {
		$node.removeClass('changed');
	}
	subMotor[key] = val;
};

var updateT2Pair = function($node, key, tbody, oldVal) {
	var tr = $node.parent().parent();
	var $key = tr.find("td.pair_key > input");
	var $value = tr.find("td.pair_value > input");
	var isValid = true;
	var kv = $key.val().trim();
	if (!kv || !/^[a-z0-9_]+$/i.test(kv)) {
		$key.parent().addClass("warning");
		isValid = false;
	} else {
		$key.parent().removeClass("warning");
	}
	var vv = $value.val().trim();
	if (!vv) {
		$value.parent().addClass("warning");
		isValid = false;
	} else {
		$value.parent().removeClass("warning");
	}
	
	if (isValid) {
		if ($node.val() != oldVal) {
			$node.addClass("changed");
		} else {
			$node.removeClass("changed");
		}
		
		var subMotor = _editorModel;
		var curVal = subMotor[key];
		var isChanged = true;
		if (typeof curVal === 'object') {
			if (curVal.hasOwnProperty(kv)) {
				var cv = curVal[kv];
				if (cv == vv) {
					isChanged = false;
				}
			}
		}
		if (isChanged) {
			_dirtyFlag = true;
			var newVal = {};
			var trs = tbody.find('tr');
			trs.each(function() {
				var rk = $(this).find('td.pair_key > input').val();
				var rv = $(this).find('td.pair_value > input').val();
				newVal[rk] = rv;
				console.log('add ' + rk + ":" + rv);
			});
			subMotor[key] = newVal;
		} 
	}
};

var addRow = function(bt, key, t2Body) {
	var nRow = $(EMPTY_ROW_PART1 + EMPTY_ROW_PART2 + EMPTY_ROW_PART3);
	var cRow = bt.parent().parent();
	nRow.insertAfter(cRow);
	
	var t2Key = nRow.find("td.pair_key > input");
	var oldKV = t2Key.val();
	
	t2Key.focus(function() {
		nRow.addClass("active");
	}).blur(function() {
		nRow.removeClass("active");
		updateT2Pair($(this), key, t2Body, oldKV);
	}).keypress(function( event ) {
		if ( event.which == 13 ) {
			$(this).blur();
		}
	});

	var t2Value = nRow.find("td.pair_value > input");
	var oldVV = t2Value.val();
	t2Value.focus(function() {
		nRow.addClass("active");
	}).blur(function() {
		nRow.removeClass("active");
		updateT2Pair($(this), key, t2Body, oldVV);
	}).keypress(function( event ) {
		if ( event.which == 13 ) {
			$(this).blur();
		}
	});
	
	var t2Add = nRow.find("td.pair_control > button.button_plus");
	t2Add.click(function() {
		addRow($(this), key, t2Body);
	});
	var t2Remove = nRow.find("td.pair_control > button.button_minus");
	t2Remove.click(function() {
		removeRow($(this), key, t2Body);
	});

	t2Key.focus();
};

var removeRow = function(bt, key, tbody) {
	var cRow = bt.parent().parent();
	cRow.remove();
	
	_dirtyFlag = true;
	var subMotor = _editorModel;
	var newVal = {};
	var trs = tbody.find('tr');
	trs.each(function() {
		var rk = $(this).find('td.pair_key > input').val();
		var rv = $(this).find('td.pair_value > input').val();
		newVal[rk] = rv;
	});
	subMotor[key] = newVal;
};

var PropertyRow = function(tr) {
	var colKey = tr.find('.editable_key');
	var colType = tr.find('.editable_type');
	var colValue = tr.find('.editable_value');
	var sel = colType.find('select');
	var key = tr.attr('key');
	var oldVal = _curModel[key];
	var newTextHtml;
	var newPairHtml;
	if (typeof oldVal === 'object') {
		newPairHtml = TABLE_TIER2_HEADER;
		$.each(oldVal, function(subKey, subVal){
			newPairHtml += EMPTY_ROW_PART1 + subKey + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
		});
		newPairHtml += '</tbody></table>';
		newTextHtml = '<input type="text" key="' + key + '" class="form-control" value="">';
	} else {
		newPairHtml = TABLE_TIER2_HEADER + EMPTY_ROW_PART1 + EMPTY_ROW_PART2 + EMPTY_ROW_PART3 + '</tbody></table>';
		newTextHtml = '<input type="text" key="' + key + '" class="form-control" value="' + oldVal + '">';
	}
	sel.change(function() {
		var selVal = sel.val();
		if (selVal === 'text') {
			colValue.html(newTextHtml);
		} else {
			colValue.html(newPairHtml);
		}
		addEventHandler();
	});
	
	var addEventHandler = function() {
		var t1Value = colValue.find("> input");
		t1Value.focus(function() {
			tr.addClass("active");
		}).blur(function() {
			tr.removeClass("active");
			updateNode($(this), key, $(this).val());
		}).keypress(function( event ) {
			if ( event.which == 13 ) {
				$(this).blur();
			}
		});
		
		var t2Body = colValue.find("tbody");
		if (t2Body) {
			var t2Key = t2Body.find("td.pair_key > input");
			var oldKV = t2Key.val();
			t2Key.focus(function() {
				tr.addClass("active");
			}).blur(function() {
				tr.removeClass("active");
				updateT2Pair($(this), key, t2Body, oldKV);
			}).keypress(function( event ) {
				if ( event.which == 13 ) {
					$(this).blur();
				}
			});

			var t2Value = t2Body.find("td.pair_value > input");
			var oldVV = t2Value.val();
			t2Value.focus(function() {
				tr.addClass("active");
			}).blur(function() {
				tr.removeClass("active");
				updateT2Pair($(this), key, t2Body, oldVV);
			}).keypress(function( event ) {
				if ( event.which == 13 ) {
					$(this).blur();
				}
			});
			
			var t2Add = t2Body.find("td.pair_control > button.button_plus");
			t2Add.click(function() {
				addRow($(this), key, t2Body);
			});
			var t2Remove = t2Body.find("td.pair_control > button.button_minus");
			t2Remove.click(function() {
				removeRow($(this), key, t2Body);
			});
		}
	}

	addEventHandler();
};

var createRow = function(key, val) {
	var html;
	if (typeof val === 'object') {
		html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>' 
		+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair" selected>name-value pair</option></select></td>'
		+ '<td class="editable_value">' + TABLE_TIER2_HEADER;
		$.each(val, function(subKey, subVal){
			html += EMPTY_ROW_PART1 + subKey + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
		});
		html += '</tbody></table></td></tr>';
	} else {
		html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>'
		+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair">name-value pair</option></select></td>'
		+ '<td class="editable_value"><input type="text" class="form-control" value="' + val + '"></td></tr>';
	}
	return html;
};

var loadGalilMotor = function(path, name) {
	var okToGo = true;
	if (_curModel != null && _editorModel != null) {
		if (_dirtyFlag) {
			okToGo = confirm('You have unsaved changes in the current axis. If you load another axis, your change will be lost. Do you want to continue?');
		}
	}
	if (okToGo) {
		_dirtyFlag = false;
		if (path == null) {
			_curModel = null;
			_curPath = null;
			_curName = null
			_title.text('Home');
			_editor.empty();
			_tabs.empty();
			_editorTitle.empty();
			_propertyTitle.empty();
			_property.empty();
			$('.class_ul_folder > li').removeClass('active');
			_historyBar.reload();
			return;
		}
		var modelGroup = getGalilItem(path);
		if (modelGroup) {
//			console.log(modelGroup);
//			var motors = modelGroup[keysOf(modelGroup)[0]];
			var motors = modelGroup;
//			var motors = _editorModel[keysOf(_editorModel)[0]];
			if (name) {
				if (!name in keysOf(modelGroup)) {
					alert(name + "doesn't exist");
					return false;
				}
				_curName = name;
			} else {
				_curName = keysOf(modelGroup)[0];
				name = _curName;
			}
			_curModel = motors[_curName];
			_tabs.empty();
			$.each(motors, function(motorName, motor) {
//				var motorName = motor[KEY_MOTOR_NAME];
				var $li = $('<li class="nav-item"><a class="nav-link" href="#">' + motorName + '</a></li>');
//				console.log(_curName);
//				console.log(motorName == _curName);
				if (motorName == _curName) {
					$li.find('a').addClass("active");
				} else {
					$li.click(function() {
						loadGalilMotor(path, motorName);
					});					
				}
				_tabs.append($li);
			});
			_curPath = path;
			_editorModel = $.extend(true, {}, _curModel);
			
			
	//		_message.html(JSON.stringify(_curModel));
//			var ulTab = $('<ul class="nav nav-tabs"/>');
//			$.each(motors, function() {
//				var motorName = motors[i][KEY_MOTOR_NAME];
//				var li = $('<li class="nav-item"><a class="nav-link ' + motorName + '" href="#">Active</a></li>');
//				ulTab.append(li);
//			});
			
			
			var subMotor = _curModel;
//			var motorName = subMotor[KEY_MOTOR_NAME];
			var motorName = name;
			var controller = subMotor[KEY_CONTROLLER_NAME];
			var axis = subMotor[KEY_AXIS_NAME];
			_title.text(controller + ":" + axis + " (SICS name:" + motorName + ")");
//			var html = TABLE_TIER1_HEADER;
//			//		$.each(subMotor, function(key, val) {
//			//			html += '<tr><td>' + key + '</td><td>' + val + "</td></tr>";
//			//		});
//			$.each(_editable_tier_1, function(i, key) {
//				if (subMotor.hasOwnProperty(key)){
//					var val = subMotor[key];
//					html += createRow(key, val);
//				}
//			});
//			$.each(_editable_tier_2, function(i, key) {
//				if (subMotor.hasOwnProperty(key)){
//					var val = subMotor[key];
//					html += createRow(key, val);
//				}
//			});
//			html += '</tbody></table>';
//			_editorTitle.text('Frequently changed');
//			_editor.html(html);
//			Below code disabled for allowing math expression 
//			$.each(node, function(idx, n) {
//				var iv = new IntValidator($(this));
//			});


			_propertyTitle.text('Axis properties');
			var html = TABLE_TIER1_HEADER;
			$.each(subMotor, function(key, val) {
				if (!(key in _editable_tier_1 || key in _editable_tier_2)) {
					html += createRow(key, val);
				}
			});
			html += '</tbody></table>';
			_property.html(html);

			var trs = $("#id_div_main_area").find('tr.editable_row');
			trs.each(function() {
				var pr = new PropertyRow($(this));				
			})
			
			_historyBar.reload();
			return true;
		}
	}
	return false;
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
				var s = loadGalilMotor(path);
				if (s) {
					$('.class_ul_folder > li').removeClass('active');
					$(this).parent().addClass('active');
					$(this).blur();
				}
			} else {
				showMsg('axis not found: ' + path, 'danger');
			}
		});
		$("#id_div_sidebar").append($div.children());
	});
};

var getModel = function() {
	var url = 'yaml/model?inst=' + _inst + '&' + Date.now();
	$.get(url, function(data) {
		_model = data;
		_galil = _model[GALIL_CONTROLLERS_NODE];
		_motors = {}
		$.each(_galil, function(key, mc) {
			$.each(mc, function(id, axis) {
//				var motors = encoder[keysOf(encoder)[0]];
				$.each(axis, function(motorName, motor){
					var path = "/" + key + "/" + id;
//					var motor = encoder[keysOf(encoder)[0]];
//					var name = motor[KEY_MOTOR_NAME];
					var name = motorName;
					var desc = "";
//					if (KEY_MOTOR_DESC in motor){
					if (motor.hasOwnProperty(KEY_MOTOR_DESC)) {
						desc = motor[KEY_MOTOR_DESC];
					}
					_motors[name] = [path, motorName, desc];
					console.log('add ' + name);
				});
			});
		});
//		_sics = _model[SICS_MOTORS_NODE];
		$("#id_div_sidebar").empty();
		showModelInSidebar();
	}).fail(function(e) {
		if (e.status == 401) {
			alert("sign in required");
			window.location = 'signin.html?redirect=sicsConfig.html?inst=' + _inst;
		} else {
			alert(e.statusText);
		}
	});
};

var saveModel = function() {
//	$('#id_modal_saveDialog').modal('hide');
	_curModel = $.extend(true, _curModel, _editorModel);
	_dirtyFlag = false;
	var url = 'yaml/save?inst=' + _inst + '&msg=';
	var saveMsg = $('#id_input_saveMessage').val().replace(/^\s+|\s+$/gm,'');
	if (saveMsg.length > 0) {
		url += encodeURI(saveMsg);
	}
//	if (_versionId) {
//		url += "&version=" + encodeURI(_versionId);
//	}
	url += "&" + Date.now();
	$('#id_modal_saveDialog').modal('hide');
	var text = JSON.stringify(_editorModel);
	$.post(url,  {path:_curPath, name:_curName, model:text}, function(data) {
//		data = $.parseJSON(data);
		try {
			if (data["status"] == "OK") {
				showMsg("Saved in the server.");
				$('td.editable input.changed').removeClass('changed');
				setTimeout(_historyBar.reload, 3000)
			} else {
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

var goHome = function() {
	loadGalilMotor(null);
}

var resetEditor = function($bt) {
	if (_curPath) {
		_dirtyFlag = false;
		loadGalilMotor(_curPath, _curName);
		showMsg('Reset successfully');
	} else {
		showMsg('Model not found!', 'danger');
	}
};

var addPageTitle = function(){
//	if (typeof title !== 'undefined') {
//		var titleString = "SICS Configuration - " + title;
//		$(document).attr("title", titleString);
//		$('#id_a_instrument_name').text(titleString);
//	}
	var titleString = "SICS Configuration - " + _inst.charAt(0).toUpperCase() + _inst.slice(1);
	$(document).attr("title", titleString);
	var subTitle = _inst.toUpperCase() + " configuration";
	$('#id_span_side_title').html('<h5>' + subTitle + '</h5>');
};

var SearchWidget = function($t) {
	var $m = $t.parent().find('.messagepop');
	var curSel = -1;
	var lastText = null;
	
	var show = function($li){
		var name = $li.attr('name');
		var path = $li.attr('path');
		var motorName = $li.attr('motorName');
		var mc = path.split('/')[1];
		var axis = path.split('/')[2];
		$m.hide();
		$('.class_ul_folder').addClass('class_ul_hide');
		$('#ul_mc_' + mc).removeClass('class_ul_hide');
		$('.class_ul_folder > li').removeClass('active');
		$('#li_' + mc + '_' + axis).addClass('active');
		loadGalilMotor(path, name);
		$t.blur();
	}
	
	
	var doSearch = function() {
		var target = '';
		curSel = -1;

		var html = '';
//		var found = {};
		$.each(_motors, function(name, pair) {
			var word = $t.val().toLowerCase();
			if (name.toLowerCase().indexOf(word) >= 0) {
//				found[name] = path;
				var path = pair[0];
				var motorName = pair[1];
				var desc = pair[2];
				if (desc) {
					desc = '(' + desc + ')';
				}
				html += '<li class="messageitem" href="#" name="' + name + '" path="' + path + '" motorName="' + motorName + '"><span class="widget_text">' + 
						name + ' => ' + path + ' ' + desc + '</span></li>';
			} else {
				var desc = pair[2];
				if (desc && desc.toLowerCase().indexOf(word) >= 0) {
//					found[name] = path;
					var path = pair[0];
					var idx = pair[1];
					desc = '(' + desc + ')';
					html += '<li class="messageitem" href="#" name="' + name + '" path="' + path + '" motorName="' + motorName + '"><span class="widget_text">' + 
							name + ' => ' + path + ' ' + desc + '</span></li>';
				} 
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

var getTimeString = function(timestamp) {
	var a = new Date(timestamp * 1000);
	  var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
	  var year = a.getFullYear();
	  var month = months[a.getMonth()];
	  var date = a.getDate();
	  var hour = a.getHours();
	  var min = a.getMinutes();
	  var sec = a.getSeconds();
	  var time = date + ' ' + month + ' ' + year + ' ' + hour + ':' + min + ':' + sec ;
	  return time;
};

var CommitItem = function(commit, index) {
	var id = commit["id"];
	var name = commit["name"];
	var message = commit["message"];
	var timestamp = commit["timestamp"];
	var btText;
	var star;
	if (index == 0) {
		btText = "Current version";
		star = "* ";
	} else {
		btText = "Load this version";
		star = "";
	}
	
	var control = $('<div/>').addClass("class_div_commit_item");
	control.append('<span class="badge badge-secondary class_span_commit_timestamp">' + star + getTimeString(timestamp) + '</span>');
	control.append('<span class="class_span_commit_message">' + message + '</span>');
	var button = $('<span class="class_span_commit_button"><button class="class_button_load_commit btn btn-sm btn-block btn-outline-primary">' + btText + '</button></span>');
	control.append(button);
	button.find('button').click(function() {
		var url = 'yaml/load?inst=' + _inst + '&version=' + encodeURI(name) + "&" + Date.now();
		$.get(url, function(data) {
			_dirtyFlag = false;
			if (_curPath != null) {
				try{
					updateModel(_curPath, _curName, data);
				} catch (e) {
					alert('Axis ' + _curPath + " doesn't exist.");
					return;
				}
				var mc = _curPath.split('/')[1];
				var axis = _curPath.split('/')[2];
				$('.class_ul_folder').addClass('class_ul_hide');
				$('#ul_mc_' + mc).removeClass('class_ul_hide');
				$('.class_ul_folder > li').removeClass('active');
				$('#li_' + mc + '_' + axis).addClass('active');
				loadGalilMotor(_curPath, _curName);
			} else {
				_model = data;
				_versionId = name;
				_timestamp = getTimeString(timestamp);
				_galil = _model[GALIL_CONTROLLERS_NODE];
				_motors = {}
				$.each(_galil, function(key, mc) {
					$.each(mc, function(id, encoder) {
						var motors = encoder[keysOf(encoder)[0]];
						$.each(motors, function(idx, motor){
							var path = "/" + key + "/" + id;
//							var motor = encoder[keysOf(encoder)[0]];
							var name = motor[KEY_MOTOR_NAME];
							var desc = "";
//							if (KEY_MOTOR_DESC in motor){
							if (motor.hasOwnProperty(KEY_MOTOR_DESC)) {
								desc = motor[KEY_MOTOR_DESC];
							}
							_motors[name] = [path, idx, desc];
						});
//						var path = "/" + key + "/" + id;
//						var motor = encoder[keysOf(encoder)[0]];
//						var name = motor[KEY_MOTOR_NAME];
//						_motors[name] = path;
					});
				});
	//			_sics = _model[SICS_MOTORS_NODE];
				$("#id_div_sidebar").empty();
				_curModel = null;
				_curName = null;
	//			_curPath = null;
				_editorModel = null;
				showModelInSidebar();
				showMsg('Successully loaded version: ' + message);
				_editorTitle.empty();
				_tabs.empty();
				_editor.empty();
				_propertyTitle.empty();
				_property.empty();
				_editorTitle.text("History version '" + message + "' has been loaded back.");
				var $b = $('<button class="btn btn-outline-primary">Apply this version</button>');
				$b.click(function() {
					applyCurrentVersion($(this));
				});
				_editor.append($b);
			}
		}).fail(function() {
			alert('failed to load the version');
		});
	});
	
	this.getControl = function() {
		return control;
	};
};

var applyCurrentVersion = function($b) {
	var c = confirm('Do you want to overwrite the current configuration file with the history version?');
	if (c && _inst && _versionId) {
		var url = 'yaml/apply?inst=' + _inst + '&version=' + encodeURI(_versionId);
		url += '&timestamp=' + encodeURI(_timestamp)  + '&' + Date.now();
		$.get(url, function(data) {
			if (data["status"] == "OK") {
				showMsg("Successfully applied the history version. A new version ID has been created.");
				$b.remove();
				_historyBar.reload();
			} else {
				showMsg("failed to apply the history version.", 'danger');
				alert(data["reason"]);
			}
		}).fail(function() {
			showMsg("failed to apply the history version.", 'danger')
			alert("Connection failed.");
		});
	}
};

var HistoryBlock = function(main, side) {
	var enabled = false;
	
	this.reload = function() {
		if (enabled) {
			$('.class_div_commit_item').remove();
			var url = 'yaml/history?inst=' + _inst;
			if (_curPath) {
				url += '&path=' + _curPath;
			}
			url += '&' + Date.now();
			$.get(url, function(data) {
				data = $.parseJSON(data);
				$.each(data, function(i, v) {
					var commit = new CommitItem(v, i);
					side.append(commit.getControl());
				});
			}).fail(function() {
				side.html('<span class="alert alert-danger">failed to get history.</span>');
			});
		}
	};
	
	this.toggle = function() {
		enabled = !enabled;
		if (enabled) {
			main.css('width', '70%');
			side.show();
			var bodyHeight = $(window).height() - 150;
			var mainHeight = $("#id_div_main_area").height();
			var newHeight = bodyHeight > mainHeight ? bodyHeight : mainHeight;
			side.height(newHeight);
			this.reload();
		} else {
			side.hide();
			main.css('width', '100%');
		}
	};
};


$(document).ready(function() {
	_inst = getParam(PAR_INSTRUMENT_ID);
	if (_inst == null) {
		window.location = "sicsConfigMenu.html";
	}
	
	$('#id_a_signout').click(function() {
		signout("sicsConfigMenu.html");
	});
	
	_message = $('#id_div_info');
	_title = $('#id_device_title');
	_editorTitle = $('#id_editor_subtitle');
	_tabs = $('#id_ul_tabs');
	_editor = $('#id_div_editor_table');
	_propertyTitle = $('#id_property_subtitle');
	_property = $('#id_div_property_table');
	
	addPageTitle();
	
	var $loading = $('#id_span_waiting').hide();
	$(document).ajaxStart(function () {
		$loading.show();
	}).ajaxStop(function () {
		$loading.hide();
	});

	getModel();
	
	$('#id_button_saveConfirm').click(function() {
		saveModel();
	});
	
	$('#id_input_saveMessage').keypress(function(event) {
		if ( event.which == 13 ) {
			saveModel();
		}
	});
	
	$(window).resize(function() {
		var bodyHeight = $(window).height() - 150;
		var mainHeight = $("#id_div_main_area").height();
		var newHeight = bodyHeight > mainHeight ? bodyHeight : mainHeight;
		$('#id_div_right_bar').height(newHeight);
	});
	
	$('#id_button_reset').click(function() {
		resetEditor($(this));
	});
	
	$('#id_span_side_home').click(function() {
		goHome();
	});
	
	_historyBar = new HistoryBlock($("#id_div_main_area"), $('#id_div_right_bar'));
	
	$('#id_button_history').click(function(){
		_historyBar.toggle();
	})
	
	$('#id_button_reload_history').click(function() {
		_historyBar.reload();
	});
	var search = new SearchWidget($('#id_input_search_text'));
	search.init();
	_historyBar.toggle();
	
});