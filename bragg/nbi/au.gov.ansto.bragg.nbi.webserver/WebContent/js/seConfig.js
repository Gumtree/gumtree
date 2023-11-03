var TITLE_TEXT = "Sample Environment Device Configuration for "
var PAR_INSTRUMENT_ID = "inst";
var KEY_DEVICE_DESC = "desc";
var KEY_DRIVER_NAME = "";
var URL_PREFIX = "seyaml/"
var TABLE_TIER1_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="25%">Key</th><th width="25%">Type</th><th width="50%">Value</th></tr></thead><tbody>';
var TABLE_TIER2_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="40%">Key</th><th width="40%">Value</th><th width="20%"></th></tr></thead><tbody>';
var EMPTY_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" value="';
var EMPTY_ROW_PART2 = '"></td><td class="pair_value"><input type="text" class="form-control" value="';
var EMPTY_ROW_PART3 = '"></td><td class="pair_control input-group-btn"><button type="button" class="btn btn-outline-primary button_plus">+</button><button type="button" class="btn btn-outline-primary button_minus">-</button></td></tr>';
var CHECK_MARK = '<span class="float_right padding_right_4"><i class="fa fa-check" aria-hidden="true"></i> </span>';

var PROP_NAME_ENABLED = "enabled";
var PROP_VALUE_ENABLED = 1;
var PROP_VALUE_DESELECTED = 0;
var PROP_NAME_OPTIONS = "options";
var PROP_NAME_VALUE = "value";

var _inst = null;
var _model = null;
var _configs;
var _editorModel;
var _curModel;
var _dirtyFlag = false;
var _curName = null;
var _curPath;
var _title;
var _editorTitle;
var _tabs;
var _propertyTitle
var _editor;
var _property;
var _dirtyFlag = false;
var _versionId = "";
var _timestamp = "";
var _editable_tier_1 = [];
var _editable_tier_2 = [];
var _DB;
var _DBItems;

var HistoryBlock = function(main, side) {
	var enabled = false;
	
	this.reload = function() {
		if (enabled) {
			$('.class_div_commit_item').remove();
			var url = URL_PREFIX + 'confighistory?inst=' + _inst;
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
		var url = URL_PREFIX + 'configload?inst=' + _inst + '&version=' + encodeURI(name) + "&" + Date.now();
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
				loadDeviceConfig(_curPath, _curName);
			} else {
				_model = data;
				_versionId = name;
				_timestamp = getTimeString(timestamp);
				_configs = {}
				$.each(_model, function(did, device) {
					$.each(device, function(cid, config) {
							var path = "/" + did + "/" + cid;
//							var motor = encoder[keysOf(encoder)[0]];
							var name = did + ":" + cid;
							var desc = "";
//							if (KEY_MOTOR_DESC in motor){
							if (motor.hasOwnProperty(KEY_MOTOR_DESC)) {
								desc = motor[KEY_DEVICE_DESC];
							}
							_configs[name] = [path, did, cid, desc];
						});
//						var path = "/" + key + "/" + id;
//						var motor = encoder[keysOf(encoder)[0]];
//						var name = motor[KEY_MOTOR_NAME];
//						_motors[name] = path;
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
	var subConfig = _editorModel;
	var newVal = {};
	var trs = tbody.find('tr');
	trs.each(function() {
		var rk = $(this).find('td.pair_key > input').val();
		var rv = $(this).find('td.pair_value > input').val();
		newVal[rk] = rv;
	});
	subConfig[key] = newVal;
};

var updateNode = function($node, key, val) {
	var config = _editorModel;
	var curVal = config[key];
	if (curVal.toString() != val.toString()) {
		$node.addClass('changed');
		_dirtyFlag = true;
	} else {
		$node.removeClass('changed');
	}
	config[key] = val;
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
		
		var config = _editorModel;
		var curVal = config[key];
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
			config[key] = newVal;
		} 
	}
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

var SearchWidget = function($t) {
	var $m = $t.parent().find('.messagepop');
	var curSel = -1;
	var lastText = null;
	
	var show = function($li){
		var name = $li.attr('name');
		var path = $li.attr('path');
//		var did = $li.attr('did');
		var did = path.split('/')[1];
		var cid = path.split('/')[2];
		$m.hide();
		$('.class_ul_folder').addClass('class_ul_hide');
		$('#ul_mc_' + did).removeClass('class_ul_hide');
		$('.class_ul_folder > li').removeClass('active');
		$('#li_' + did + '_' + cid).addClass('active');
		loadDeviceConfig(did, cid);
		$t.blur();
	}
	
	
	var doSearch = function() {
		var target = '';
		curSel = -1;

		var html = '';
//		var found = {};
		$.each(_configs, function(name, pair) {
			var word = $t.val().toLowerCase();
			if (name.toLowerCase().indexOf(word) >= 0) {
//				found[name] = path;
				var path = pair[0];
				var did = pair[1];
				var cid = pair[2];
				var desc = pair[3];
				if (desc) {
					desc = '(' + desc + ')';
				}
				html += '<li class="messageitem" href="#" name="' + name + '" path="' + path + '" did="' + did + '"><span class="widget_text">' + 
						name + ' => ' + desc + '</span></li>';
			} else {
				var desc = pair[3];
				if (desc && desc.toLowerCase().indexOf(word) >= 0) {
//					found[name] = path;
					var path = pair[0];
					var did = pair[1];
					desc = '(' + desc + ')';
					html += '<li class="messageitem" href="#" name="' + name + '" path="' + path + '" did="' + did + '"><span class="widget_text">' + 
							name + ' => ' + desc + '</span></li>';
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

var getModel = function() {
	var url = URL_PREFIX + 'seconfig?inst=' + _inst;
	$.get(url, function(data) {
		_model = data;
		_configs = {}
		$.each(_model, function(did, dev) {
			$.each(dev, function(cid, cfg) {
				var path = "/" + did + "/" + cid;
				var desc = "";
				if (cfg.hasOwnProperty(KEY_DEVICE_DESC)) {
					desc = cfg[KEY_DEVICE_DESC];
				}
				name = did + ":" + cid
				_configs[name] = [path, did, cid, desc];
			});
		});
		$("#id_div_sidebar").empty();
		showModelInSidebar();
		getDB();
	}).fail(function(e) {
		if (e.status == 401) {
			alert("sign in required");
			window.location = 'signin.html?redirect=seConfig.html?inst=' + _inst;
		} else {
			alert(e.statusText);
		}
	});
};

var showModelInSidebar = function() {
	var html = "";
	$.each(_model, function(did, device) {
		var fold = "class_ul_hide";
		$.each(device, function(cid, config) {
			if (config.hasOwnProperty(PROP_NAME_ENABLED)) {
				var isEnabled = config[PROP_NAME_ENABLED];
				if (isEnabled == PROP_VALUE_ENABLED){
					fold = "";
				}
			}
		});
		html = '<a class="class_a_mc" href="#"><h6 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' + 
				'<span class="class_span_mc_name"><i class="fas fa-caret-down"></i> ' + did + '</span></h6></a>' + 
				'<ul id="ul_mc_'+ did + '" class="nav flex-column class_ul_folder ' + fold + '">';
		$.each(device, function(cid, config) {
			var cb = "";
			if (config.hasOwnProperty(PROP_NAME_ENABLED)){
				var isEnabled = config[PROP_NAME_ENABLED];
				if (isEnabled == PROP_VALUE_ENABLED){
//					cb = '<span class="float_right padding_right_4">&check;</span>';
					cb = '<span class="float_right padding_right_4"><i class="fa fa-check" aria-hidden="true"></i> </span>';
				}
			}
			html += '<li id="li_' + did + '_' + cid + '" class="nav-item class_li_subitem"><a class="nav-link class_a_axis" did="' + did + '" cid="' + cid + '" href="#">' +
              		cid + cb + '</a></li>';
		});
		html += "</ul>";
		var $div = $('<div/>').append(html);
		$div.find('a.class_a_mc').click(function() {
			$(this).next('ul').toggleClass('class_ul_hide');
		});
		$div.find('a.class_a_axis').click(function() {
			var adid = $(this).attr('did');
			var acid = $(this).attr('cid');
			if (adid) {
				var s = loadDeviceConfig(adid, acid);
				if (s) {
					$('.class_ul_folder > li').removeClass('active');
					$(this).parent().addClass('active');
					$(this).blur();
				}
			} else {
				showMsg('device not found: ' + did, 'danger');
			}
		});
		$("#id_div_sidebar").append($div.children());
	});
};

var loadDeviceConfig = function(did, cid) {
	var okToGo = true;
	if (_curModel != null && _editorModel != null) {
		if (_dirtyFlag) {
			okToGo = confirm('You have unsaved changes in the current axis. ' 
					+ 'If you load another axis, your change will be lost. Do you want to continue?');
		}
	}
	if (okToGo) {
		_dirtyFlag = false;
		if (did == null) {
			_curModel = null;
			_curPath = null;
			_curName = null
			_title.text(TITLE_TEXT + _inst);
			_editor.empty();
			_tabs.empty();
			_editorTitle.empty();
			_propertyTitle.empty();
			_property.empty();
			$('.class_ul_folder > li').removeClass('active');
			_historyBar.reload();
			return;
		}
		var modelGroup = getDeviceItem(did);
		if (modelGroup) {
			var device = modelGroup;
			if (cid) {
				if (!cid in keysOf(modelGroup)) {
					alert(cid + "doesn't exist");
					return false;
				}
			} else {
				cid = keysOf(modelGroup)[0];
			}
			_curName = did + ":" + cid;
			_curModel = device[cid];
			_tabs.empty();
			$.each(device, function(cid, config) {
				var $li = $('<li class="nav-item"><a class="nav-link" href="#">' + cid + '</a></li>');
				if (did + ":" + cid == _curName) {
					$li.find('a').addClass("active");
				} else {
					$li.click(function() {
						loadDeviceConfig(did, cid);
						var devItem = $('#ul_mc_' + did).find('li');
						$.each(devItem, function(){
							$(this).removeClass('active');
							$('#li_' + did + '_' + cid).addClass('active');
						});
					});					
				}
				_tabs.append($li);
			});
			_curPath = did + "/" + cid;
			_editorModel = $.extend(true, {}, _curModel);
			
			
			var desc = _curModel[KEY_DEVICE_DESC];
			_title.text(did + ":" + cid + " (" + desc + ")");

			_propertyTitle.text('Device properties');
			var html = TABLE_TIER1_HEADER;
			$.each(_curModel, function(key, val) {
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
			
			var cbox = $("#id_div_main_area").find('input.enable_checkbox:checkbox');
			cbox.change(function() {
		        if(this.checked) {
		        	$(this).val(1);
		        } else {
		        	$(this).val(0);
		        }
	        	console.log($(this).val());
	        	setCurrentConfigEnabled($(this).val());
		    });
			_historyBar.reload();
			return true;
		}
	}
	return false;
};

var setCurrentConfigEnabled = function(isEnabled) {
	var pair = _curName.split(":");
	var did = pair[0];
	var cid = pair[1];
	var configs = $('#ul_mc_' + did).find('.class_a_axis');
	if (isEnabled == 1) {
		console.log("enabled");
		$.each(configs, function() {
			if ($(this).attr('cid') == cid) {
				$(this).html(cid + CHECK_MARK);
			} else {
				$(this).html($(this).attr('cid'));
			}
		});
	} else {
		console.log("not enabled");
		$.each(configs, function() {
			$(this).html($(this).attr('cid'));
		});
	}
}

var createRow = function(key, val) {
	var html;
	if (typeof val === 'object') {
		if (val.hasOwnProperty(PROP_NAME_OPTIONS)){
			var ops = val[PROP_NAME_OPTIONS]
			var ot = "";
			var sv = val[PROP_NAME_VALUE];
			$.each(ops, function(idx, op){
				var sl = sv == op ? "selected" : "";
				ot += '<option value="text" ' + sl + '>'+ op + '</option>';
			});
			html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>'
			+ '<td class="editable_type"></td>'
			+ '<td class="editable_value"><select name="option_value" class="form-control">' + ot + '</select></td>'
			+ '</tr>';
		} else {
			html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>' 
			+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair" selected>name-value pair</option></select></td>'
			+ '<td class="editable_value">' + TABLE_TIER2_HEADER;
			$.each(val, function(subKey, subVal){
				html += EMPTY_ROW_PART1 + subKey + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
			});
			html += '</tbody></table></td></tr>';
		}
	} else {
		if (key == PROP_NAME_ENABLED){
			var checked = "";
			if (val == PROP_VALUE_ENABLED){
				checked = "checked";
			}
			html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">enabled</td>'
			+ '<td class="editable_type"></td>'
			+ '<td class="editable_value"><input type="checkbox" class="form-control enable_checkbox" ' + checked + '></td></tr>';
		} else {
			html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>'
			+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair">name-value pair</option></select></td>'
			+ '<td class="editable_value"><input type="text" class="form-control" value="' + val + '"></td></tr>';
		}
	}
	return html;
};

var getDeviceItem = function(did) {
	if (_model) {
//		var segs = path.split('/');
//		var obj = _model;
//		$.each(segs, function(i, val) {
//			if (val.trim().length > 0) {
//				obj = obj[val];
//			}
//		});
//		return obj;
		return _model[did];
	} else {
		return null;
	}
};

var keysOf = function(obj) {
	var keys = [];
	$.each(obj, function(key, val) {
		keys.push(key);
	});
	return keys;
};

var saveModel = function() {
//	$('#id_modal_saveDialog').modal('hide');
	_curModel = $.extend(true, _curModel, _editorModel);
	_dirtyFlag = false;
	var url = URL_PREFIX + 'configsave?inst=' + _inst + '&msg=';
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
	$.post(url,  {path:_curPath, model:text}, function(data) {
		console.log(data);
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

var goHome = function() {
	loadDeviceConfig(null);
}

var getDB = function() {
	var url = URL_PREFIX + 'sedb';
	$.get(url, function(data) {
		_DB = data;
		_DBItems = {}
		$.each(_DB, function(did, dev) {
			_DBItems[did] = dev;
		});
		addDBItemHtml();
	}).fail(function(e) {
		if (e.status == 401) {
		} else {
			alert(e.statusText);
		}
	});
}

var addDBItemHtml = function() {
	var html = "";
	html = '<a class="class_a_mc" href="#"><h6 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' + 
	'<span class="class_span_mc_name"><i class="fas fa-caret-down"></i> Available Devices </span></h6></a>' + 
	'<ul id="ul_mc_all" class="nav flex-column">';
	$.each(_DB, function(did, device) {
		var cb = "";
		if (!_model.hasOwnProperty(did)) {
			cb = '<a class="float_right padding_right_4 nav-link class_a_plus" did="' + did + '" href="#"><i class="fa fa-plus" aria-hidden="true"></i> </a>';
		}
		html += '<li id="li_db_' + did + '" class="nav-item class_li_subitem">' + did + cb + '</li>';
	});

	html += "</ul>";
	var $div = $('<div/>').append(html);
	$div.find('a.class_a_plus').click(function() {
		var deviceId = $(this).attr('did');
		addDevice(deviceId);
	});
	$("#id_div_sidebuttom").append($div.children());
}

var addDevice = function(did) {
	var device = _DB[did];
	if (device) {
		_model[did] = device;
	}
	$("#id_div_sidebar").empty();
	showModelInSidebar();
}

$(document).ready(function() {
	_inst = getParam(PAR_INSTRUMENT_ID);
	
	$('#id_span_home').text(_inst.toUpperCase());
	
	$('#id_a_signout').click(function() {
		signout("seConfigMenu.html");
	});
	
	_message = $('#id_div_info');
	_title = $('#id_device_title');
	_editorTitle = $('#id_editor_subtitle');
	_tabs = $('#id_ul_tabs');
	_editor = $('#id_div_editor_table');
	_propertyTitle = $('#id_property_subtitle');
	_property = $('#id_div_property_table');

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