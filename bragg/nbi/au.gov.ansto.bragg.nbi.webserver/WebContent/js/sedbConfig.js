var TITLE_TEXT = "Sample Environment Device Configuration Database"
var KEY_DEVICE_DESC = "desc";
var KEY_DEVICE_DRIVER = "driver";
var KEY_DRIVER_NAME = "";
var URL_PREFIX = "seyaml/"
//var TABLE_TIER1_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="25%">Key</th><th width="25%">Type</th><th width="50%">Value</th></tr></thead><tbody>';
var TABLE_TIER1_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="34%">Key</th><th width="66%">Value</th></tr></thead><tbody>';
var TABLE_TIER2_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="40%">Key</th><th width="40%">Value</th><th width="20%"></th></tr></thead><tbody>';
var EMPTY_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" value="';
var EMPTY_ROW_PART2 = '"></td><td class="pair_value"><input type="text" class="form-control" value="';
var EMPTY_ROW_PART3 = '"></td><td class="pair_control input-group-btn"><button type="button" class="btn btn-outline-primary button_plus">+</button><button type="button" class="btn btn-outline-primary button_minus">-</button></td></tr>';
var DISABLED_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" disabled value="';
var KEY_DATYPE = "datype";
var PROPERTY_KEYWORDS = [
	KEY_DATYPE
];
var DATYPE_NAME_DICT = {
	"T" : "Temperature",
	"B" : "Magnet",
	"V" : "Voltage",
	"P" : "Pressure"
}
var DATYPE_ICON = {
	"T" : "thermometer-half",
	"B"	: "magnet",
	"V" : "bolt",
	"P" : "gauge"
}
var DEFAULT_SUB_DEVICE_NAME_PREFIX = {
	"T" : "tc",
	"B" : "ma",
	"V"	: "vo",
	"P" : "pc"
}
var JSON_TEMP_COMPOSITE_DEVICE = {
	"datype" : "C"
}
var ID_PROP_DRIVER = "driver";
var ID_PROP_CONFIGID = "config_id";
var ID_PROP_IP = "ip";
var ID_PROP_ID = "id";
var ID_PROP_NAME = "name";
var ID_PROP_PORT = "port";
var JSON_TEMP_SUB_DEVICE = {
	"config_id" : "--",
	"datype" 	: "NA",
	"driver" 	: "--",
	"id"		: "1",
	"ip"		: "--",
	"name"		: "--",
	"port"		: "--"
}
var _model = null;
var _configs;
var _firstConfig;
var _editorModel;
var _saveObj;
var _curDeviceModel;
//var _curModel;
var _curDevice;
var _initNewDevice = false;
var _dirtyFlag = false;
var _curCid = null;
var _curDid;
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
var _option_prop = ["config_id", "ip", "port"];

class DeviceModel {
	constructor(did, model) {
		this.did = did;
		this.model = model;
		this.cid = null;
	}
	
	getValue(cid, key) {
		var config = this.model[cid];
		if (config != null) {
			return config[key];
		} else {
			return null;
		}
	}
	
	getConfig(cid) {
		return this.model[cid];
	}
	
	updateModel(newModel) {
		$.extend(true, this.model, newModel);
	}
	
	setCurrentCid(cid) {
		this.cid = cid;
	}
	
	fromHistory(wholeModel) {
		console.log(wholeModel);
		$.extend(true, this.model, wholeModel[this.did]);
	}
}

class PropertyTable {
	
	constructor(cid, config)
	{
		this.cid = cid;
		this.cModel = config;
		this.driverId = config[ID_PROP_DRIVER];
		this.dModel = _model[this.driverId];
		this.configRow = null;
		this.ipRow = null;
		this.portRow = null;
	}
	
	getUI() {
		var $table = $(TABLE_TIER1_HEADER + '</tbody></table>');
		var $tbody = $table.find('tbody');
		var object = this;
//		var cid = this.cid;
//		var did = this.driverId;
		var subConfigId = this.cModel[ID_PROP_CONFIGID];
		if (typeof this.dModel === 'undefined') {
			$.each(this.cModel, function(key, val){
				if (_option_prop.includes(key)) {
					var pRow = createRow(object.cid, key, val);
					$tbody.append(pRow.getUI());
				} else {
					var pRow = createRow(object.cid, key, val, null, true);
					$tbody.append(pRow.getUI());
				}
			});
		} else {
			if (subConfigId in object.dModel) {
				$.each(this.cModel, function(key, val){
					if (key == ID_PROP_CONFIGID) {
						var options = getConfigArray(object.driverId);
						object.configRow = createRow(object.cid, key, val, options, true);
						$tbody.append(object.configRow.getUI());
					} else if (key == ID_PROP_IP) {
						var options = object.dModel[subConfigId][ID_PROP_IP];
						object.ipRow = createRow(object.cid, key, val, options);
						$tbody.append(object.ipRow.getUI());
					} else if (key == ID_PROP_PORT) {
						var options = object.dModel[subConfigId][ID_PROP_PORT];
						object.portRow = createRow(object.cid, key, val, options);
						$tbody.append(object.portRow.getUI());
					} else {
						var pRow = createRow(object.cid, key, val, null, true);
						$tbody.append(pRow.getUI());
					}
				});
			} else {
				$.each(this.cModel, function(key, val){
					if (key == ID_PROP_CONFIGID) {
						var options = getConfigArray(object.driverId);
						object.configRow = createRow(object.cid, key, val, options);
						$tbody.append(object.configRow.getUI());
					} else if (_option_prop.includes(key)) {
						var pRow = createRow(object.cid, key, val);
						$tbody.append(pRow.getUI());
					} else {
						var pRow = createRow(object.cid, key, val, null, true);
						$tbody.append(pRow.getUI());
					}
				});
			}
		}
		if (object.configRow != null) {
			object.configRow.addValueSelectListener(function(value){
				var newConfig = object.dModel[value];
				if (newConfig) {
					var ips = newConfig[ID_PROP_IP];
					if (object.ipRow) {
						object.ipRow.updateValueOptions(ips);
					}
					var ports = newConfig[ID_PROP_PORT];
					if (object.portRow) {
						object.portRow.updateValueOptions(ports);
					}
				}
			});
		}
		return $table;
	}
}

var getConfigArray = function(did) {
	var device = _model[did];
	if (device != null) {
		var keys = [];
		for (key in device) {
			if (! (PROPERTY_KEYWORDS.includes(key))) {
				keys.push(key);
			}
		}
		return keys;
	} else {
		return [];
	}
}

var HistoryBlock = function(main, side) {
	var enabled = false;
	
	this.reload = function() {
		if (enabled) {
			$('.class_div_commit_item').remove();
			var url = URL_PREFIX + 'dbhistory?';
			if (_curDid) {
				url += 'did=' + _curDid;
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
		var url = URL_PREFIX + 'dbload?version=' + encodeURI(name) + "&" + Date.now();
		$.get(url, function(data) {
			_dirtyFlag = false;
			if (_curDid != null) {
				try{
					_curDeviceModel.fromHistory(data);
				} catch (e) {
					alert('Device ' + _curDid + " doesn't exist.");
					return;
				}
				$('.class_ul_folder').addClass('class_ul_hide');
				$('#ul_mc_' + _curDid).removeClass('class_ul_hide');
				$('.class_ul_folder > li').removeClass('active');
				$('#li_' + _curDid + '_' + _curCid).addClass('active');
				loadDeviceConfig(_curDid, _curCid);
				_dirtyFlag = true;
			} else {
				_model = data;
				_versionId = name;
				_timestamp = getTimeString(timestamp);
				_configs = {};
				_firstConfig = {};
				$.each(_model, function(did, device) {
					$.each(device, function(cid, config) {
						if (!(PROPERTY_KEYWORDS.includes(cid))) {
							var path = "/" + did + "/" + cid;
//							var motor = encoder[keysOf(encoder)[0]];
							var name = did + ":" + cid;
							var desc = "";
//							if (KEY_MOTOR_DESC in motor){
							if (motor.hasOwnProperty(KEY_MOTOR_DESC)) {
								desc = motor[KEY_DEVICE_DESC];
							}
							_configs[name] = [path, did, cid, desc];
							if (!(did in _firstConfig)) {
								_firstConfig[did] = cid;
							}
						}
					});
				});
	//			_sics = _model[SICS_MOTORS_NODE];
//				$("#id_div_sidebar").empty();
				_curDeviceModel = null;
				_curCid = null;
				_curDid = null;
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

var PropertyRow = function(cid, tr) {
	var colKey = tr.find('.editable_key');
	var colType = tr.find('.editable_type');
	var colValue = tr.find('.editable_value');
	var sel = colType.find('select');
	var key = tr.attr('key');
	var oldVal = _curDeviceModel.getValue(cid, key);
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
		
		var t1Select = colValue.find("> select");
		if (t1Select) {
			t1Select.change(function() {
				updateNode($(this), key, $(this).val());
			});
		}
		
		var t2Body = colValue.find("tbody");
		if (t2Body) {
			var t2Key = t2Body.find("td.pair_key > input");
			var oldKV = t2Key.val();
			var isPair = !t2Key.prop('disabled');
			if (isPair) {
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
			}

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
				addRow($(this), key, t2Body, isPair);
			});
			var t2Remove = t2Body.find("td.pair_control > button.button_minus");
			t2Remove.click(function() {
				removeRow($(this), key, t2Body);
			});
		}
	}

	this.getUI = function() {
		return tr;
	}
	
	this.addValueSelectListener = function(f) {
		var t1ValueSelect = colValue.find("> select");
		t1ValueSelect.change(function() {
			f(t1ValueSelect.val());
		});
	}
	
	this.updateValueOptions = function(options) {
		if (typeof options === 'undefined') {
			options = [];
		} else if (!(typeof options === 'object')) {
			options = [options];
		}
		var ot = "";
		$.each(options, function(idx, op) {
			ot += '<option value="' + op + '">'+ op + '</option>';
		});
		var t1ValueDatalist = colValue.find("> datalist");
		if (t1ValueDatalist) {
			t1ValueDatalist.html(ot);
		}
		if (options.length > 0) {
			this.setValue(options[0]);			
		} else {
			this.setValue("");
		}
	}
	
	this.setValue = function(val) {
		var t1Value = colValue.find("> input");
		if (t1Value) {
			t1Value.val(val);
			updateNode(t1Value, key, val);
		}
	}
	
	addEventHandler();
};

var addRow = function(bt, key, t2Body, isPair) {
	var nRow;
	if (isPair) {
		nRow = $(EMPTY_ROW_PART1 + EMPTY_ROW_PART2 + EMPTY_ROW_PART3);
	} else {
		nRow = $(DISABLED_ROW_PART1 + '-' + EMPTY_ROW_PART2 + EMPTY_ROW_PART3);	
	}
	var cRow = bt.parent().parent();
	nRow.insertAfter(cRow);
	
	if (isPair) {
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
	}

	var t2Value = nRow.find("td.pair_value > input");
	var oldVV = t2Value.val();
	t2Value.focus(function() {
		nRow.addClass("active");
	}).blur(function() {
		nRow.removeClass("active");
		updateT2Pair($(this), key, t2Body, oldVV, isPair);
	}).keypress(function( event ) {
		if ( event.which == 13 ) {
			$(this).blur();
		}
	});
	
	var t2Add = nRow.find("td.pair_control > button.button_plus");
	t2Add.click(function() {
		addRow($(this), key, t2Body, isPair);
	});
	var t2Remove = nRow.find("td.pair_control > button.button_minus");
	t2Remove.click(function() {
		removeRow($(this), key, t2Body);
	});

	if (isPair) {
		t2Key.focus();
	} else {
		t2Value.focus();
	}
};

var removeRow = function(bt, key, tbody) {
	var cRow = bt.parent().parent();
	cRow.remove();
	
	_dirtyFlag = true;
	var newVal = {};
	var trs = tbody.find('tr');
	trs.each(function() {
		var rk = $(this).find('td.pair_key > input').val();
		var rv = $(this).find('td.pair_value > input').val();
		newVal[rk] = rv;
	});
	_editorModel[_curCid][key] = newVal;
};

var updateNode = function($node, key, val) {
	var curVal = _editorModel[_curCid][key];
	if (curVal.toString() != val.toString()) {
		$node.addClass('changed');
		_dirtyFlag = true;
	} else {
		$node.removeClass('changed');
	}
	_editorModel[_curCid][key] = val;
};

var updateT2Pair = function($node, key, tbody, oldVal) {
	var tr = $node.parent().parent();
	var $key = tr.find("td.pair_key > input");
	var $value = tr.find("td.pair_value > input");
	var isPair = !$key.prop('disabled');
	var isValid = true;
	if (isPair) {
		var kv = $key.val().trim();
		if (!kv || !/^[a-z0-9_]+$/i.test(kv)) {
			$key.parent().addClass("warning");
			isValid = false;
		} else {
			$key.parent().removeClass("warning");
		}
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
		
		var curVal = _editorModel[_curCid][key];
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
			var newVal;
			if (isPair) {
				newVal = {};
				var trs = tbody.find('tr');
				trs.each(function() {
					var rk = $(this).find('td.pair_key > input').val();
					var rv = $(this).find('td.pair_value > input').val();
					newVal[rk] = rv;
					console.log('add ' + rk + ":" + rv);
				});
				_editorModel[_curCid][key] = newVal;
			} else {
				newVal = [];
				var trs = tbody.find('tr');
				trs.each(function() {
					var rv = $(this).find('td.pair_value > input').val();
					newVal.push(rv);
					console.log('add ' + rv);
				});
				_editorModel[_curCid][key] = newVal;
			}
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
	var url = URL_PREFIX + 'sedb';
	$.get(url, function(data) {
		_model = data;
		_configs = {};
		_firstConfig = {};
		$.each(_model, function(did, dev) {
			$.each(dev, function(cid, cfg) {
				if (!(PROPERTY_KEYWORDS.includes(cid))) {
					var path = "/" + did + "/" + cid;
					var desc = "";
					if (cfg.hasOwnProperty(KEY_DEVICE_DESC)) {
						desc = cfg[KEY_DEVICE_DESC];
					}
					name = did + ":" + cid
					_configs[name] = [path, did, cid, desc];
					if (!(did in _firstConfig)) {
						_firstConfig[did] = cid;
					}
				}
			});
		});
//		$("#id_div_sidebar").empty();
		showModelInSidebar();
	}).fail(function(e) {
		if (e.status == 401) {
			alert("sign in required");
			window.location = 'signin.html?redirect=sedbConfig.html';
		} else {
			alert(e.statusText);
		}
	});
};

function drag(ev) {
//	var html = ev.target.outerHTML;
//	var did = ev.target;
	
	var element = $(ev.target.outerHTML);
	var did = element.attr('did');
//	element.removeClass('class_db_object');
//	var found = element.find('span.class_span_search_highlight');
//	found.replaceWith(found.html());
//	found = element.find('.class_db_insert');
//	found.remove();
//	found = element.find('.class_template_insert');
//	found.remove();
//    ev.dataTransfer.setData("text/html", "<p/>" + element.html() + "<p/>");
	ev.dataTransfer.setData("text", did);
} 

function allowDrop(ev) {
	ev.preventDefault();
}

function drop(ev) {
	ev.preventDefault();
	var did = ev.dataTransfer.getData("text");
	ev.target.appendChild(document.getElementById(data));
}

var DeviceGroup = function($div) {
	var deviceItem = this;
	var label = $div.find('span.class_span_mc_name');
	var input = $div.find('span.class_span_mc_input');
	var control = $div.find('span.class_span_mc_control');
	var ulList = $div.next('ul');
	var textbox = input.find('input');
	var did = label.attr('did');
	var datype = label.attr('datype');
	
	if (datype != 'C') {
		label.attr("draggable", true);
	    label.attr("ondragstart", "drag(event)");
	}
	
	label.click(function() {
		var $ul = $div.next('ul');
//		$ul.toggleClass('class_ul_hide');
//		if (!($ul.hasClass('class_ul_hide'))){
//		}
		if (datype == 'C') {
			loadCompositeDevice(did);
		} else {
			loadDevice(did);
		}
	});
	
	control.click(function() {
		control.addClass('class_span_hide');
		label.addClass('class_span_hide');
		input.removeClass('class_span_hide');
		textbox.focus();
		var range = textbox.val().length;
		textbox.get(0).setSelectionRange(range, range);
	});
	
	this.activateEditing = function() {
		control.trigger('click');
	}
	
	textbox.keypress(function(event) {
		if ( event.which == 13 ) {
			newDid = textbox.val();
//			if (saveDeviceNameChange(did, val)) {
//				saveDeviceNameChange(did, val, deviceItem);
			label.attr('did', newDid);
			label.html('<i class="fas fa-caret-down"></i> ' + newDid);
			input.addClass('class_span_hide');
			label.removeClass('class_span_hide');
			control.removeClass('class_span_hide');
			if (_initNewDevice) {
				_initNewDevice = false;
				_saveObj = null;
				var device = _model[did];
				delete _model[did];
				_model[newDid] = device;
				if (_curDeviceModel.did == did) {
					_curDeviceModel.did = newDid;
					_curDeviceModel.model = device;
				}
				_curDid = newDid;
				did = newDid;
			} else {
				_saveObj = deviceItem;
				$('#id_modal_saveDialog').modal('show');
			}
//			} else {
//				textbox.val(did);
//				input.addClass('class_span_hide');
//				label.removeClass('class_span_hide');
//				control.removeClass('class_span_hide');
//			}
		} else if ( event.which == 27) {
			_initNewDevice = false;
			textbox.val(label.attr(did));
			input.addClass('class_span_hide');
			label.removeClass('class_span_hide');
			control.removeClass('class_span_hide');
		}
	}).blur(function() {
		_initNewDevice = false;
		textbox.val(label.attr(did));
		input.addClass('class_span_hide');
		label.removeClass('class_span_hide');
		control.removeClass('class_span_hide');
	});
	
	this.resetChange = function() {
		textbox.val(did);
		label.attr('did', did);
		label.html('<i class="fas fa-caret-down"></i> ' + did);
	}
	
	this.save = function() {
//		_curModel = $.extend(true, _curModel, _editorModel);
//		_dirtyFlag = false;
//		var url = URL_PREFIX + 'changeName?oldName=' + encodeURI(oldName) + '&newName=' + encodeURI(newName) + '&msg=';
		var url = URL_PREFIX + 'changeName?msg=';
		var saveMsg = $('#id_input_saveMessage').val().replace(/^\s+|\s+$/gm,'');
		if (saveMsg.length > 0) {
			url += encodeURI(saveMsg);
		}
//		if (_versionId) {
//			url += "&version=" + encodeURI(_versionId);
//		}
		url += "&" + Date.now();
		$('#id_modal_saveDialog').modal('hide');
		var text = JSON.stringify(_model[newDid]);
		$.post(url,  {oldDid:did, newDid:newDid, model:text}, function(data) {
			console.log(data);
//			data = $.parseJSON(data);
			try {
				if (data["status"] == "OK") {
					_saveObj = null;
					var device = _model[did];
					delete _model[did];
					_model[newDid] = device;
					if (_curDeviceModel.did == did) {
						_curDeviceModel.did = newDid;
					}
					did = newDid;
					showMsg("Device name saved in the server.");
					$('td.editable input.changed').removeClass('changed');
					setTimeout(_historyBar.reload, 3000)
				} else {
					showMsg("Failed to rename the device: " + data["reason"], 'danger');
//					deviceItem.resetChange();
				}
			} catch (e) {
				showMsg("Failed to rename the device: " + e.statusText, 'danger');
//				deviceItem.resetChange();
			}
		}).fail(function(e) {
			showMsg("Failed to talk to the server: " + e.statusText, "danger");
//			deviceItem.resetChange();
		}).always(function() {
			$('#id_modal_saveDialog').modal('hide');
		});
	}
	
}

var showModelInSidebar = function() {
	var html = "";
	$.each(_model, function(did, device) {
		var datype = device["datype"];
		if (datype == null) {
			return true;
		}
		var faIcon;
		html = '<div class="class_a_mc" href="#"><h6 class="sidebar-subheading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' 
				+ '<span class="class_span_mc_name" did="' + did + '" datype="' + datype + '"><i class="fas fa-caret-down"></i> ' + did + '</span>';
		if (datype != null && datype in DATYPE_ICON) {
			faIcon = DATYPE_ICON[datype];
			html += '<span class="class_span_mc_icon"><i class="fas fa-' + faIcon + '"></i> </span>';
		}
		if (datype == "C") {
			html += '<span class="class_span_mc_input class_span_hide"><input type="text" class="form-control class_mc_input" value="' + did + '"></span>';
			html += '<span class="class_span_mc_control"><i class="fas fa-edit"></i> </span>';
		}
		html += '</h6></div><ul id="ul_mc_'+ did + '" class="nav flex-column class_ul_folder class_ul_hide ">';
		
		$.each(device, function(cid, config) {
			if (! (PROPERTY_KEYWORDS.includes(cid)) ) {
				html += '<li id="li_menu_' + did + '_' + cid + '" class="nav-item class_li_subitem"><a class="nav-link class_a_axis" did="' + did + '" cid="' + cid + '" href="#">' +
          		cid + '<span class="sr-only">(current)</span></a></li>';
			}
		});
		if (datype != "C") {
			html += '<li id="li_menu_' + did + '_NEW_DEVICE' + '" class="nav-item class_li_subitem">'
				+ '<a class="nav-link class_a_add_axis" did="' + did + '" href="#"><i class="fas fa-plus"> </a></li>';
		}
		html += "</ul>";
		var $div = $('<div/>').append(html);
		new DeviceGroup($div.find('div.class_a_mc'));
//		$div.find('div.class_a_mc span.class_span_mc_name').click(function() {
//			var $ul = $('#ul_mc_' + did);
//			$ul.toggleClass('class_ul_hide');
//			if (!($ul.hasClass('class_ul_hide'))){
//				loadDevice(did);
//			}
//		});
//		$div.find('div.class_a_mc span.class_span_mc_control').click(function() {
//			var control = $(this);
//			var input = control.prev('span.class_span_mc_input');
//			var label = input.prev('span.class_span_mc_name');
//			control.addClass('class_span_hide');
//			label.addClass('class_span_hide');
//			input.removeClass('class_span_hide');
//			var textbox = input.find('input');
//			textbox.focus();
//			var range = textbox.val().length;
//			textbox.get(0).setSelectionRange(range, range);
//		});
//		$div.find('div.class_a_mc input').keypress(function(event) {
//			if ( event.which == 13 ) {
////				saveChange();
//				var val = $(this).val();
//				var input = $(this).parent();
//				var control = input.next('span.class_span_mc_control');
//				var label = input.prev('span.class_span_mc_name');
//				var oldDid = label.attr('did');
//				if (saveDeviceNameChange(oldDid, val)) {
//					label.attr('did', val);
//					label.html('<i class="fas fa-caret-down"></i> ' + val);
//					input.addClass('class_span_hide');
//					label.removeClass('class_span_hide');
//					control.removeClass('class_span_hide');
//				} else {
//					$(this).val(oldDid);
//					input.addClass('class_span_hide');
//					label.removeClass('class_span_hide');
//					control.removeClass('class_span_hide');
//				}
//			} else if ( event.which == 27) {
//				var input = $(this).parent();
//				var control = input.next('span.class_span_mc_control');
//				var label = input.prev('span.class_span_mc_name');
////				label.html('<i class="fas fa-caret-down"></i> ' + $(this).val());
//				$(this).val(label.attr('did'));
//				input.addClass('class_span_hide');
//				label.removeClass('class_span_hide');
//				control.removeClass('class_span_hide');
//			}
//		}).blur(function() {
//			var input = $(this).parent();
//			var control = input.next('span.class_span_mc_control');
//			var label = input.prev('span.class_span_mc_name');
//			$(this).val(label.attr('did'));
//			input.addClass('class_span_hide');
//			label.removeClass('class_span_hide');
//			control.removeClass('class_span_hide');
//		});
		
//		$div.find('a.class_a_mc').on( "dblclick", function() {
//			  alert( "Handler for `dblclick` called." );
//		} );
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
		$div.find('a.class_a_add_axis').click(function() {
			var btn = $(this);
			var adid = btn.attr('did');
			var lastEnt = btn.prev().find('a.class_a_axis');
			if (lastEnt == null) {
				showMsg("Failed to create new configuration: no existing one to be copied", "danger");
			}
			var lastCid = lastEnt.attr('cid');
			
		});
		
		if (datype != 'C'){
//			var ulName = 'ul_device_' + datype;
//			var folderName;
//			if (datype in DATYPE_NAME_DICT) {
//				folderName = DATYPE_NAME_DICT[datype] + " devices";
//			} else {
//				folderName = "Other devices";
//			}
//			var block = $('#id_div_sidebar');
//			var ulBlock = block.find('#' + ulName);
//			if (ulBlock.length == 0){
//				ulBlock = addFolder(ulName, folderName);
//			}
//			ulBlock.append($div.children());
			$('#id_div_sidebar').append($div.children());
		} else {
			$('#id_div_sidebuttom').append($div.children());
		}
	});
};

var loadDevice = function(did) {
	var device = _model[did];
	if (device != null) {
		var datype = device['datype'];
		if (datype != null) {
			if (datype == "C") {
				loadCompositeDevice(did);
			} else {
				var cid = _firstConfig[did];
				if (cid != null) {
					loadDeviceConfig(did, cid)
					$('#id_div_sidebar>.class_ul_folder').addClass('class_ul_hide');
					$('#ul_mc_' + did).removeClass('class_ul_hide');
//					$('.class_ul_folder > li').removeClass('active');
//					$('#li_' + did + '_' + cid).addClass('active');
				} else {
					showMsg('no configuration found for device: ' + did, 'danger');
				}
			}
		}
	} else {
		showMsg('device not found: ' + did, 'danger');
	}
}

var loadCompositeDevice = function(did) {
	var okToGo = true;
	if (_curDeviceModel != null && _editorModel != null) {
		if (_curDid != did && _dirtyFlag) {
			okToGo = confirm('You have unsaved changes in the current axis. If you load another axis, your change will be lost. Do you want to continue?');
		}
	}

	if (okToGo) {
		if (_curDid != did) {
			_dirtyFlag = false;
		}
		_tabs.empty();
		var device = _model[did];
		var ct = 0;
		_curDeviceModel = new DeviceModel(did, device);
		_curDid = did;
		_editorModel = device;
		
		var $li = $('<li class="nav-item active"><a class="nav-link active" href="#">Root view</a></li>');
		$li.click(function() {
			loadCompositeDevice(_curDeviceModel.did);
		});
		_tabs.append($li);
		$.each(device, function(cid, config) {
			if (! (PROPERTY_KEYWORDS.includes(cid))){
				var $li = $('<li class="nav-item" id="li_tab_' + did + '_' + cid + '"><a class="nav-link" href="#">' + cid + '</a></li>');
				$li.click(function() {
					loadDeviceConfig(_curDeviceModel.did, cid);
				});					
				_tabs.append($li);
				ct++;
			}
		});
//		var $li_plus = $('<li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-plus"></i> </a></li>');
//		$li_plus.click(function() {
//			addDeviceToComposite(did);
//		});
//		_tabs.append($li_plus);
		
		_title.text(did + " (Composite Device)");

		_propertyTitle.text('Drag and drop devices from the left menu to this area');
		var html = '';
		$.each(device, function(subDid, config) {
			if (! (PROPERTY_KEYWORDS.includes(subDid))) {
				html += '<div class="class_div_device_page" id="div_page_' + did + '_' + subDid + '"><a href="#" class="class_a_cid_delete" did="' 
					+ did + '" cid="' + subDid + '"><i class="fas fa-square-minus"></i> </a>'
					+ '<div class="class_div_device_item"><a href="#" class="class_a_cid_label" did="' 
					+ did + '" cid="' + subDid + '">' + subDid + '</a></div></div>';
			}
		});
		var $div = $('<div ondragover="allowDrop(event)" />').append(html);
		$div.find('a.class_a_cid_label').click(function(){
			var adid = $(this).attr('did');
			var acid = $(this).attr('cid');
			loadDeviceConfig(adid, acid);
		});
		$div.find('a.class_a_cid_delete').click(function(){
			var adid = $(this).attr('did');
			var acid = $(this).attr('cid');
			deleteSubDevice(adid, acid);
		});
		
		$div.addClass('class_div_device_canvas');
		$div.on("drop", function(ev){
			ev.preventDefault();
			var subDid = ev.originalEvent.dataTransfer.getData("text");
			var newSubDevice = makeSubDevice(_curDeviceModel.did, subDid);
			var item = $(newSubDevice["html"]);
			item.find('a.class_a_cid_label').click(function(){
				var adid = $(this).attr('did');
				var acid = $(this).attr('cid');
				loadDeviceConfig(adid, acid);
			});
			item.find('a.class_a_cid_delete').click(function(){
				var adid = $(this).attr('did');
				var acid = $(this).attr('cid');
				deleteSubDevice(adid, acid);
			});
			$div.append(item);
			_dirtyFlag = true;
//			loadDeviceConfig(did, newSubDevice["cid"]);
		});
		_property.empty();
		_property.append($div);
		
		$del = $('<div class="main_footer"><span id="id_button_del" class="btn btn-outline-primary btn-block " href="#"><i class="fas fa-remove"></i> Remove This Device</span></div>');
		$del.find('span').click(function() {
			$('#id_modal_deleteDialog').modal('show');
		});
		_property.append($del);
		$('#id_div_sidebuttom>.class_ul_folder').addClass('class_ul_hide');
		$('#ul_mc_' + did).removeClass('class_ul_hide');

//		var trs = $("#id_div_main_area").find('tr.editable_row');
//		trs.each(function() {
//			var pr = new PropertyRow($(this));				
//		});				
	} else {
		return;
	}
}

var removeCurrentDevice = function() {
//	_curDeviceModel.updateModel(_editorModel);
	if (_curDeviceModel == null) {
		showMsg("failed to remove: no device selected", 'danger');
		return;
	}
	var did = _curDeviceModel.did;
	var device = _model[did];
	if (device == null) {
		showMsg("failed to remove: device does not exist", 'danger');
		return;
	}
	var datype = device[KEY_DATYPE];
	if (datype != "C") {
		showMsg("failed to remove: device is not a composite one", 'danger');
		return;
	}
	var url = URL_PREFIX + 'dbremove?did=' + did + '&msg=';
	var saveMsg = $('#id_input_saveMessage').val().replace(/^\s+|\s+$/gm,'');
	if (saveMsg.length > 0) {
		url += encodeURI(saveMsg);
	}
//	if (_versionId) {
//		url += "&version=" + encodeURI(_versionId);
//	}
	url += "&" + Date.now();
	$('#id_modal_deleteDialog').modal('hide');
	$.get(url, function(data) {
		console.log(data);
		try {
			if (data["status"] == "OK") {
				_dirtyFlag = false;
				showMsg("Removed successfully in the server.");
				var $ul = $('#ul_mc_' + did);
				$ul.prev().remove();
				$ul.remove();
				loadDeviceConfig(null, null);
//				setTimeout(_historyBar.reload, 3000)
			} else {
				showMsg(data["reason"], 'danger');
			}
		} catch (e) {
			showMsg("Failed to remove: " + e.statusText, 'danger');
		}
	}).fail(function(e) {
		console.log(e);
		showMsg("Faied to remove: " + e.statusText, "danger");
	}).always(function() {
		$('#id_modal_deleteDialog').modal('hide');
	});
}

var deleteSubDevice = function(did, subDid) {
	_dirtyFlag = true;
	var cDevice = _model[did];
	if (cDevice == null) {
		showMsg('Fatal error, invalid device: ' + did + ', please reload this page', 'danger');
	}
	
	var subDevice = cDevice[subDid];
	if (subDevice == null) {
		showMsg('sub device not found: ' + subDid, 'danger');
	}
	var rmType = subDevice[KEY_DATYPE];
	var rmId = subDevice[ID_PROP_ID];
	
	delete cDevice[subDid];
	
	var liMenuItem = $('#li_menu_' + did + '_' + subDid);
	liMenuItem.remove();
	
	var liTabItem = $('#li_tab_' + did + '_'  + subDid);
	liTabItem.remove();
	
	var divItem = $('#div_page_' + did + '_'  + subDid);
	divItem.remove();
	
	$.each(cDevice, function(key, config) {
		if (key != KEY_DATYPE) {
			var iType = config[KEY_DATYPE];
			if (iType == rmType) {
				var iId = config[ID_PROP_ID];
				if (iId > rmId) {
					iId--;
					_editorModel[key][ID_PROP_ID] = iId;
					_editorModel[key][ID_PROP_NAME] = DEFAULT_SUB_DEVICE_NAME_PREFIX[iType] + iId;
				}
			}
		}
	});
}

var makeSubDevice = function(did, subDid) {
	var cDevice = _model[did];
	if (cDevice == null) {
		showMsg('Fatal error, invalid device: ' + did + ', please reload this page', 'danger');
	}
	var device = _model[subDid];
	if (device == null) {
		showMsg('device not found: ' + subDid, 'danger');
	}
	var subType = device['datype'].toUpperCase();
	var idx = 1;
	var newName;
	while(true) {
		newName = subDid + '_' + idx;
		if (!(newName in cDevice)) {
			break;
		}
		idx++;
	}
	var newId = 1;
	$.each(cDevice, function(key, config) {
		if (config[KEY_DATYPE] == subType && config[ID_PROP_ID] == newId) {
			newId++;
		}
	});
	
	var cid = newName;
	var menuItem = $('<li id="li_menu_' + did + '_' + cid + '" class="nav-item class_li_subitem"><a class="nav-link class_a_axis" did="' 
			+ did + '" cid="' + cid + '" href="#">' + cid + '<span class="sr-only">(current)</span></a></li>');
	menuItem.click(function() {
		loadDeviceConfig(did, cid);
	});
	var ulList = $('#ul_mc_' + did);
	ulList.append(menuItem);
	
	var tabItem = $('<li class="nav-item" id="li_tab_' + did + '_' + cid + '"><a class="nav-link" href="#">' + cid + '</a></li>');
	tabItem.click(function() {
		loadDeviceConfig(did, cid);
	});
	_tabs.append(tabItem);
	
	var subDevice = $.extend({}, JSON_TEMP_SUB_DEVICE);
	var configs = getConfigArray(subDid);
	if (configs.length > 0) {
		subDevice["config_id"] = configs[0];
	}
	var source = _model[subDid][configs[0]];
	subDevice["datype"] = subType;
	subDevice["driver"] = subDid;
	subDevice["id"] = newId;
	var ips = source["ip"];
	if (typeof ips === 'object') {
		subDevice["ip"] = ips[0];
	} else {
		subDevice["ip"] = ips;
	}
	subDevice["name"] = DEFAULT_SUB_DEVICE_NAME_PREFIX[subType] + newId;
	subDevice["port"] = source["port"];
	cDevice[newName] = subDevice;
	var newSubDevice = {};
	var subHtml = '<div class="class_div_device_page" id="div_page_' + did + '_' + newName + '"><a href="#" class="class_a_cid_delete"><i class="fas fa-square-minus"></i> </a>'
		+ '<div class="class_div_device_item"><a href="#" class="class_a_cid_label" did="' 
		+ did + '" cid="' + newName + '">' + newName + '</a></div></div>';
	newSubDevice["html"] = subHtml;
//	newSubDevice["html"] = '<div class="class_div_device_item">' + newName + '</div>';
	newSubDevice["cid"] = newName;
	return newSubDevice;
}

var addFolder = function(folderId, folderName) {
	var html = '<a class="class_a_folder" href="#"><h5 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 "'
	           + '><span class="class_span_folder_name">' + folderName + '</span>'
	           + '<span class="class_span_folder_icon"><i class="fas fa-caret-up"></i></span></h5></a>'
	           + '<ul id="' + folderId + '" class="nav flex-column class_device_folder "></ul>';
	var $div = $('<div/>').append(html);
	var $ul = $div.find('#' + folderId);
	var $a = $div.find('a');
	var folder = new DeviceFolder($a);
	$('#id_div_sidebar').append($div.children());
	return $ul;
}

class DeviceConfig {
	constructor(did, cid) {
		this.did = did;
		this.cid = cid;
	}
	
	load() {
		var cid = this.cid;
		var did = this.did;
		var modelGroup = getDeviceItem(did);
		if (modelGroup) {
			if (_curDeviceModel == null || _curDeviceModel.did != did) {
				_editorModel = $.extend(true, {}, modelGroup);
			}

			_curDeviceModel = new DeviceModel(did, modelGroup);
			if (cid) {
				if (!cid in keysOf(modelGroup)) {
					alert(cid + "doesn't exist");
					return false;
				}
			} else {
				this.cid = keysOf(modelGroup)[0];
			}
			_curDid = did;
			_curCid = cid;
			var config = _curDeviceModel.getConfig(cid); 
			_tabs.empty();
			var datype = modelGroup['datype'];
			if (datype == 'C') {
				var $li = $('<li class="nav-item"><a class="nav-link" href="#">Root view</a></li>');
				$li.click(function() {
					loadCompositeDevice(did);
				});
				_tabs.append($li);
			}
			$.each(modelGroup, function(key, config) {
				if (! (PROPERTY_KEYWORDS.includes(key))){
					var $li = $('<li class="nav-item" id="li_tab_"' + did + '_' + key + '><a class="nav-link" href="#">' + key + '</a></li>');
					if (key == _curCid) {
						$li.find('a').addClass("active");
					} else {
						$li.click(function() {
							loadDeviceConfig(did, key);
						});					
					}
					_tabs.append($li);
				}
			});
			
			
			var desc = config[KEY_DEVICE_DESC];
			if (desc == null) {
				desc = config[KEY_DEVICE_DRIVER];
			}
			_title.text(did + ":" + cid + " (" + desc + ")");

			_property.empty();
			if (datype == 'C') {
				_propertyTitle.text('Device properties');
//				var pdid = config[ID_PROP_DRIVER];
//				var device = _model[pdid];
				var table = new PropertyTable(cid, config);
//				html = TABLE_TIER1_HEADER + table.getHtml() + '</tbody></table>';
				_property.append(table.getUI());
			} else {
				_propertyTitle.text('Device properties');
				var $table = $(TABLE_TIER1_HEADER + '</tbody></table>')
				var $tbody = $table.find('tbody');
				$.each(config, function(key, val) {
					 $tbody.append(createRow(cid, key, val).getUI());
				});
				_property.append($table);
			}

//			var trs = $("#id_div_main_area").find('tr.editable_row');
//			trs.each(function() {
//				var pr = new PropertyRow(cid, $(this));				
//			});
			
			$('#ul_mc_' + did + '>li').removeClass('active');
			$('#li_menu_' + did + '_' + cid).addClass("active");
			_historyBar.reload();
			return true;
		}
	}
}

var loadDeviceConfig = function(did, cid) {
	var okToGo = true;
	if (_curDeviceModel != null && _editorModel != null) {
		if (_curDid != did && _dirtyFlag) {
			okToGo = confirm('You have unsaved changes in the current axis. If you load another axis, your change will be lost. Do you want to continue?');
		}
	}
	if (okToGo) {
		if (_curDeviceModel == null || _curDeviceModel.did != did) {
			_dirtyFlag = false;
		}
		if (did == null) {
			_curDeviceModel = null;
			_curDid = null;
			_curCid = null
			_title.text(TITLE_TEXT);
			_editor.empty();
			_tabs.empty();
			_editorTitle.empty();
			_propertyTitle.empty();
			_property.empty();
			$('.class_ul_folder > li').removeClass('active');
			_historyBar.reload();
			return;
		}

		var deviceConfig = new DeviceConfig(did, cid);
		deviceConfig.load();
	}
	return false;
};

var createRow = function(cid, key, val, options, editingDisabled) {
	if (typeof options === 'undefined') {
		options = null;
	} else if (!(typeof options === 'object')) {
		options = [options];
	}
	if (typeof editingDisabled === 'undefined') {
		editingDisabled = false;
	}
	var html;
	if (typeof val === 'object') {
		html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>' 
//		+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair" selected>name-value pair</option></select></td>'
		+ '<td class="editable_value">' + TABLE_TIER2_HEADER;
		$.each(val, function(subKey, subVal){
			if ('key' in val) {
				html += EMPTY_ROW_PART1 + subKey + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
			} else {
				html += DISABLED_ROW_PART1 + '-' + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
			}
		});
		html += '</tbody></table></td></tr>';
	} else {
		if (options != null) {
			var ot = "";
			$.each(options, function(idx, op){
				var sl = op == val ? " selected" : "";
				ot += '<option value="' + op + '"' + sl + '>'+ op + '</option>';
			});
			html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>'
//			+ '<td class="editable_type"></td>' 
			+ '<td class="editable_value">';
			if (editingDisabled) {
				html += '<select name="option_value" class="form-control">' + ot + '</select>';
			} else {
				html += '<input name="option_value" class="form-control" value="' + val + '" list="' + key + '"/><datalist id="' + key + '">' + ot + '</datalist>';
			}
			html += '</td></tr>';
		} else {
			html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>'
//			+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair">name-value pair</option></select></td>'
			+ '<td class="editable_value"><input type="text" class="form-control" value="' + val + '"' + (editingDisabled ? ' disabled' : '') + '></td></tr>';
		}
	}
	var $row = $(html);
	var pr = new PropertyRow(cid, $row);
	return pr;
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
//	_curDeviceModel = $.extend(true, _curModel, _editorModel);
	_curDeviceModel.updateModel(_editorModel);
	_dirtyFlag = false;
	var url = URL_PREFIX + 'dbsave?msg=';
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
	$.post(url,  {did:_curDeviceModel.did, model:text}, function(data) {
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

//var saveDeviceNameChange = function(oldName, newName, deviceItem) {
//	$('#id_modal_saveDialog').modal('show');
//	_curModel = $.extend(true, _curModel, _editorModel);
//	_dirtyFlag = false;
////	var url = URL_PREFIX + 'changeName?oldName=' + encodeURI(oldName) + '&newName=' + encodeURI(newName) + '&msg=';
//	var url = URL_PREFIX + 'changeName?msg=';
//	var saveMsg = $('#id_input_saveMessage').val().replace(/^\s+|\s+$/gm,'');
//	if (saveMsg.length > 0) {
//		url += encodeURI(saveMsg);
//	}
////	if (_versionId) {
////		url += "&version=" + encodeURI(_versionId);
////	}
//	url += "&" + Date.now();
////	$('#id_modal_saveDialog').modal('hide');
//	var text = JSON.stringify(_editorModel);
//	$.post(url,  {oldName:oldName, newName:newName}, function(data) {
//		console.log(data);
////		data = $.parseJSON(data);
//		try {
//			if (data["status"] == "OK") {
//				showMsg("Device name saved in the server.");
//				$('td.editable input.changed').removeClass('changed');
//				setTimeout(_historyBar.reload, 3000)
//			} else {
//				showMsg("Failed to rename the device: " + data["reason"], 'danger');
//				deviceItem.resetChange(oldName, newName);
//			}
//		} catch (e) {
//			showMsg("Failed to rename the device: " + e.statusText, 'danger');
//			deviceItem.resetChange(oldName, newName);
//		}
//	}).fail(function(e) {
//		showMsg("Failed to talk to the server: " + e.statusText, "danger");
//		deviceItem.resetChange(oldName, newName);
//	}).always(function() {
////		$('#id_modal_saveDialog').modal('hide');
//	});
//};

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

var DeviceFolder = function(folder){
	var $a = folder;
	var $ul = $a.next('ul');
	var $icon = $a.find('.class_span_folder_icon');
	
	$a.click(function() {
		$ul.toggleClass('class_ul_hide');
		if ($ul.hasClass('class_ul_hide')){
			$icon.html('<i class="fas fa-caret-down"></i>');
		} else {
			$icon.html('<i class="fas fa-caret-up"></i>');
		}
	});
}

$(document).ready(function() {
	
	$('#id_span_home').text("SE Database");
	
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

	var $loading = $('#id_span_waiting').hide();
	$(document).ajaxStart(function () {
		$loading.show();
	}).ajaxStop(function () {
		$loading.hide();
	});

	getModel();
	
	$('a.class_a_folder').each(function() {
		var folder = new DeviceFolder($(this));
	});
	
	$('#id_span_add').click(function(){
		var okToGo = true;
		if (_curDeviceModel != null && _editorModel != null) {
			if (_curDid != did && _dirtyFlag) {
				okToGo = confirm('You have unsaved changes in the current device. If you leave this device, your change will be lost. Do you want to continue?');
			}
		}
		if (okToGo) {
			_dirtyFlag = true;
//				_title.text(TITLE_TEXT);
			_editor.empty();
			_tabs.empty();
			_editorTitle.empty();
			_propertyTitle.empty();
			_property.empty();
			$('.class_ul_folder > li').removeClass('active');
			_historyBar.reload();

			var did = 'New_device';
			_initNewDevice = true;
			var datype = 'C';
			var html = '<div class="class_a_mc" href="#"><h6 class="sidebar-subheading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' + 
				'<span class="class_span_mc_name" did="' + did + '" datype="' + datype + '"><i class="fas fa-caret-down"></i> ' + did + '</span>';
			html += '<span class="class_span_mc_input class_span_hide"><input type="text" class="form-control class_mc_input" value="' + did + '"></span>';
			html += '<span class="class_span_mc_control"><i class="fas fa-edit"></i> </span>';
			html += '</h6></div><ul id="ul_mc_'+ did + '" class="nav flex-column class_ul_folder class_ul_hide ">';
	
			html += "</ul>";
			var $div = $(html);
			var devGroup = new DeviceGroup($div);
			var newDev = $.extend({}, JSON_TEMP_COMPOSITE_DEVICE);
			_model[did] = newDev;
			_curDeviceModel = new DeviceModel(did, newDev);
			_curDid = did;
			_curCid = null;
			$('#id_div_sidebuttom').append($div);
//			devGroup.load();
			loadCompositeDevice(did)
			devGroup.activateEditing();
		} else {
			return;
		}
	});
	
	$('#id_button_saveConfirm').click(function() {
		if (_saveObj != null) {
			_saveObj.save();
		} else {
			saveModel();
		}
//		if (_dirtyFlag) {
//			saveModel();
//		}
	});
	
	$('#id_input_saveMessage').keypress(function(event) {
		if ( event.which == 13 ) {
			if (_saveObj != null) {
				_saveObj.save();
			} 
			if (_dirtyFlag) {
				saveModel();
			}
		}
	});
	
	$('#id_button_deleteConfirm').click(function() {
		removeCurrentDevice();
	});

	$('#id_input_deleteMessage').keypress(function(event) {
		if ( event.which == 13 ) {
			removeCurrentDevice();
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