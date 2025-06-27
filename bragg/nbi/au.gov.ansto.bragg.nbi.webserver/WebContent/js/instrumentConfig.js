const TITLE_TEXT = "Instrument Configuration for ";
const PAR_INSTRUMENT_ID = "inst";

const URL_PREFIX = "instyaml/";
const KEY_STATIC = "STATIC";
const KEY_DATYPE = "datype";
const PROPERTY_KEYWORDS = [
	KEY_STATIC
];
const DATYPE_NAME_DICT = {
		"T" : "Temperature",
		"B" : "Magnet",
		"V" : "Voltage",
		"P" : "Pressure"
}
const DATYPE_ICON = {
	"T" : "thermometer-half",
	"B"	: "magnet",
	"V" : "bolt",
	"P" : "gauge"
}
const DEFAULT_SUB_DEVICE_NAME_PREFIX = {
	"T" : "tc",
	"B" : "ma",
	"V"	: "vo",
	"P" : "pc"
}
//const _option_prop = ["config_id", "ip", "port"];
const TABLE_TIER1_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="34%">Key</th><th width="66%">Value</th></tr></thead><tbody>';
const TABLE_TIER2_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="40%">Key</th><th width="40%">Value</th><th width="20%"></th></tr></thead><tbody>';
//const EMPTY_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" value="';
//const EMPTY_ROW_PART2 = '"></td><td class="pair_value"><input type="text" class="form-control" value="';
//const EMPTY_ROW_PART3 = '"></td><td class="pair_control input-group-btn"><button type="button" class="btn btn-outline-primary button_plus">+</button><button type="button" class="btn btn-outline-primary button_minus">-</button></td></tr>';
//const DISABLED_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" disabled value="';
const HTML_TABLE = '<table class="table table-striped table-sm"><thead><tr><th width="34%">Error entry</th><th width="66%">Message</th></tr></thead><tbody></tbody></table>';

const ID_PROP_DRIVER = "driver";
const ID_PROP_CONFIGID = "config_id";
const ID_PROP_IP = "ip";
const ID_PROP_ID = "id";
const ID_PROP_NAME = "name";
const ID_PROP_PORT = "port";
const ID_PROP_ENABLED = "enabled";
const ID_PROP_SAMPLESTAGE = "sample_stage";

const PROP_GALIL_CONTROLLERS = "GALIL_CONTROLLERS";
const KEY_DEVICE_DESC = "desc";
const KEY_DEVICE_DRIVER = "driver";
const KEY_MOTOR_DESC = "description";
const KEY_SAMPLE_STAGE = "SAMPLE_STAGE";
const KEY_SAMPLE_STAGE_MOTORS = "SAMPLE_STAGE_MOTORS";
const KEY_STAGE_GROUP = "SE_MOTOR_GROUPS";
const KEY_SELECTED_GROUP = "SELECTED_GROUP";
const KEY_DEVICE_MOTOR_GROUP = "DEVICE_MOTOR_GROUP_LINKS";
const HTML_HIDDEN_FILL_DIV = '<div class="div_fill div_hidden"/>';
//const HTML_STATIC_MAIN_DIV = '<div class="div_fill div_static div_hidden"/>';
const HTML_NAV_DIV = '<div class="nav"/>';
const _inst = getParam(PAR_INSTRUMENT_ID);
const _message = $('#id_div_info');
const _title = $('#id_device_title');
const _editorTitle = $('#id_editor_subtitle');
const _tabs = $('#id_ul_tabs');
const _editor = $('#id_div_editor_table');
const _propertyTitle = $('#id_property_subtitle');
const _property = $('#id_div_property_table');

const TYPE_INST = 'INST';
const TYPE_DB = 'DB';
const PHYSICAL_DB = "PD"
const COMPOSITE_DB = "CD"

var _curDevice;
var _saveButton;
var _resetButton;
var _errorReport;
var _instModel;
var _pdbModel;
var _cdbModel;
var _motorDBModel;
var _motorConficModel;
var _curVersionName;
var _additionalModel;
//var _sampleStage;
var _motorGroups;

class StaticUtils {
	
	static addPageTitle() {
		$(document).attr("title", TITLE_TEXT + _inst.toUpperCase());
		$('#id_span_side_title').html('<h5>' + _inst.toUpperCase() + '</h5>');
	}

	static showMsg(msg, type, timeLast) {
		if (typeof type === 'undefined') {
			type = 'info';
		}
		if (typeof timeLast === 'undefined') {
			timeLast = 10000;
		} 
		if (type == 'warning') {
			timeLast = 20000;
		}
		if (type == 'danger') {
			timeLast = 30000;
		}
		_message.html('<span class="badge badge-' + type + '">' + msg + '</span>');
		setTimeout(function () {
			_message.html('');
	    }, timeLast);
	}

	static showError(errorMsg, timeLast) {
		StaticUtils.showMsg(errorMsg, 'danger', timeLast);
	}

	static showWarning(warnMsg, timeLast) {
		StaticUtils.showMsg(warnMsg, 'warning', timeLast);
	}
	
	static getTimeString(timestamp) {
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
	}

	static clearMsg() {
		_message.html('');
	}
}

//var showMsg = function(msg, type, timeLast) {
//	if (typeof type === 'undefined') {
//		type = 'info';
//	}
//	if (typeof timeLast === 'undefined') {
//		timeLast = 10000;
//	} 
//	if (type == 'danger') {
//		timeLast = 100000;
//	}
//	_message.html('<span class="badge badge-' + type + '">' + msg + '</span>');
//	setTimeout(function () {
//		_message.html('');
//    }, timeLast);
//};
//
//var showError = function(errorMsg, timeLast) {
//	showMsg(errorMsg, 'danger', timeLast);
//}
//
//var showWarning = function(warnMsg, timeLast) {
//	showMsg(warnMsg, 'warning', timeLast);
//}

class AbstractModel {
	
	load() {}
	
	afterLoad() {}
}

//class AdditionalModel extends AbstractModel {
//	
//	constructor() {
//		super();
//		this.url = URL_PREFIX + 'adit?inst=' + _inst;
//		this.model;
//	}
//	
//	load() {
//		const obj = this;
//		$.get(obj.url, function(data) {
//			obj.model = data;
//			obj.afterLoad();
//		}).fail(function(e) {
//			if (e.status == 401) {
//				window.location = 'signin.html?redirect=instrumentConfig.html?inst=' + _inst;
//			} else {
//				StaticUtils.showError('failed to load additional model file.');
//			}
//
//		});
//	}
//	
//	createMenuUi() {
//		const obj = this;
//		var groupModel = {};
//		var groupEditorModel = {};
//		if (KEY_STAGE_GROUP in obj.model) {
//			groupModel = obj.model[KEY_STAGE_GROUP];
//			groupEditorModel = obj.editorModel[KEY_STAGE_GROUP];
//		}
////		obj.motorGroupsModel = KEY_STAGE_GROUP in obj.editorModel ? obj.editorModel[KEY_STAGE_GROUP] : {};
//		_motorGroups = new MotorGroups(KEY_STAGE_GROUP, groupModel, groupEditorModel, $('#id_div_sidebar'));
//		_motorGroups.createMenuUi();
//	}
//	
//	getSelectedGroup() {
//		var selected = null;
//		if (KEY_SELECTED_GROUP in this.editorModel) {
//			const group = this.editorModel[KEY_SELECTED_GROUP];
//			if (group !== 'None') {
//				selected = group;
//			}
//		}
//		return selected;
//	}
//	
//	setSelectedGroup(gid) {
//		if (gid) {
//			this.editorModel[KEY_SELECTED_GROUP] = gid;
//		} else {
//			delete this.editorModel[KEY_SELECTED_GROUP]
//		}
//	}
//	
//	getMotorForDevice(did) {
//		if (KEY_DEVICE_MOTOR in this.model) {
//			if (did in this.model[KEY_DEVICE_MOTOR]) {
//				return this.model[KEY_DEVICE_MOTOR][did];
//			}
//		}
//		return null;
//	}
//	
//}
//
//class MotorDBModel extends AbstractModel {
//	
//	constructor() {
//		super();
//		this.model;
//		this.devices;
//	}
//	
//	load() {
//		var obj = this;
//		$.get('yaml/cachedModel?inst=' + _inst, function(data) {
//			obj.model = data[PROP_GALIL_CONTROLLERS];
//			obj.devices = {};
//			$.each(obj.model, function(mcid, mc) {
//				$.each(mc, function(aid, axis) {
//					$.each(axis, function(mid, motor) {
//						if (ID_PROP_SAMPLESTAGE in motor) {
//							const val = motor[ID_PROP_SAMPLESTAGE];
//							if (Boolean(val)) {
//								obj.devices[mid] = motor;
//							}
//						}
//					});
//				});
//			});
//			obj.createMenuUi();
//			obj.afterLoad();
//		}).fail(function(e) {
//			if (e.status == 401) {
//				window.location = 'signin.html?redirect=instrumentConfig.html?inst=' + _inst;
//			} else {
//				alert(e.statusText);
//			}
//		});
//	}
//	
//	createMenuUi() {
//		const obj = this;
//		var faIcon;
//		var html = '<div class="class_a_motor_group" href="#"><h6 class="sidebar-subheading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' 
//				+ '<span class="class_span_mc_name" >Sample Stage Motors</span>';
//				+ '</h6></div><ul class="nav flex-column ">';
//		obj.$menuHeader = $(html);
//		
//		obj.$menuUl = $('<ul class="nav flex-column class_ul_folder"></ul>');
//		_sampleStage = new SampleStage(obj.$menuUl);
//		const selectedMotor = _additionalModel.getSelectedMotor();
//		$.each(Object.keys(this.devices), function(idx, mid) {
//			const motor = obj.devices[mid];
//			const checkedText = mid == selectedMotor ? "checked" : "";
//			html = '<li class="nav-item class_li_subitem"><div class="menu_left"><input type="checkbox" id="id_motor_' + mid + '" class="class_SE_checkbox" ' + checkedText + '>'
//				+ '<a class="nav-link class_a_motor" href="#" data-toggle="tooltip" data-placement="top" title="' + motor[KEY_MOTOR_DESC] + '">' 
//				+ mid + '</a></div></li>';
//			const $li = $(html);
//			_sampleStage.addMotor(mid, $li.find('input:checkbox'), $li.find('a'));
//			obj.$menuUl.append($li);
//		});
//		obj.$menuHeader.append(obj.$menuUl);
//		$('#id_div_sidebar').append(obj.$menuHeader);
//	}
//	
//	afterLoad() {}
//}

class AdditionalModel extends AbstractModel {
	
	constructor() {
		super();
		this.url = URL_PREFIX + 'adit?inst=' + _inst;
		this.model;
		this.editorModel;
//		this.motorGroupsModel;
//		this.motorGroups;
	}
	
	load() {
		const obj = this;
		$.get(obj.url, function(data) {
			obj.model = data;
			obj.editorModel = $.extend(true, {}, data);
			obj.createMenuUi();
			obj.afterLoad();
		}).fail(function(e) {
			if (e.status == 401) {
				window.location = 'signin.html?redirect=seConfig.html?inst=' + _inst;
			} else {
				StaticUtils.showError('failed to load additional model file.');
			}

		});
	}
	
	createMenuUi() {
		const obj = this;
		var groupModel = {};
		var groupEditorModel = {};
		if (KEY_STAGE_GROUP in obj.model) {
			groupModel = obj.model[KEY_STAGE_GROUP];
			groupEditorModel = obj.editorModel[KEY_STAGE_GROUP];
		}
//		obj.motorGroupsModel = KEY_STAGE_GROUP in obj.editorModel ? obj.editorModel[KEY_STAGE_GROUP] : {};
		_motorGroups = new MotorGroups(KEY_STAGE_GROUP, groupModel, groupEditorModel, $('#id_div_sidebar'));
		_motorGroups.createMenuUi();
	}
	
	getSelectedGroup() {
		var selected = null;
		if (KEY_SELECTED_GROUP in this.editorModel) {
			const group = this.editorModel[KEY_SELECTED_GROUP];
			if (group !== 'None') {
				selected = group;
			}
		}
		return selected;
	}
	
	setSelectedGroup(gid) {
		if (gid) {
			this.editorModel[KEY_SELECTED_GROUP] = gid;
		} else {
			delete this.editorModel[KEY_SELECTED_GROUP]
		}
	}
	
	applyGroupSelection() {
		const gid = this.getSelectedGroup();
		if (gid) {
			const motors = _motorGroups.listMotorsForGroup(gid);
			this.editorModel[KEY_SAMPLE_STAGE_MOTORS] = motors;
		} else {
			delete this.editorModel[KEY_SAMPLE_STAGE_MOTORS];
		}
	}
	
//	getSelectedMotor() {
//		var selected = null;
//		if (KEY_SAMPLE_STAGE in this.editorModel) {
//			const motor = this.editorModel[KEY_SAMPLE_STAGE];
//			if (motor !== 'None') {
//				selected = motor;
//			}
//		}
//		return selected;
//	}
	
	getMotorGroupForDevice(did) {
		if (KEY_DEVICE_MOTOR_GROUP in this.editorModel) {
			if (did in this.editorModel[KEY_DEVICE_MOTOR_GROUP]) {
				return this.editorModel[KEY_DEVICE_MOTOR_GROUP][did];
			}
		}
		return null;
	}
	
	getGroupDescription(gid) {
		if (KEY_STAGE_GROUP in this.editorModel) {
			if (gid in this.editorModel[KEY_STAGE_GROUP]) {
				return this.editorModel[KEY_STAGE_GROUP][gid]["desc"];
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	setMotorGroupForDevice(did, motor) {
		var pairs;
		if (KEY_DEVICE_MOTOR_GROUP in this.editorModel) {
			pairs = this.editorModel[KEY_DEVICE_MOTOR_GROUP];
		} else {
			pairs = {};
			this.editorModel[KEY_DEVICE_MOTOR_GROUP] = pairs;
		}
		if (did in pairs) {
			if (motor != "NONE") {
				pairs[did] = motor;
			} else {
				delete pairs[did];
			}
		} else {
			if (motor != "NONE") {
				pairs[did] = motor;
			}
		}
	}
	
	getDeviceMotorModel() {
		if (KEY_DEVICE_MOTOR_GROUP in this.editorModel) {
			return this.editorModel[KEY_DEVICE_MOTOR_GROUP];
		} else {
			return {};
		}
	}
	
	getChangeMsg() {
		const obj = this;
		var msg = "";
		if (KEY_DEVICE_MOTOR_GROUP in this.model) {
			const cModel = obj.model[KEY_DEVICE_MOTOR_GROUP];
			$.each(Object.keys(cModel), function(idx, did) {
				const cMotor = cModel[did];
				const nMotor = obj.editorModel[KEY_DEVICE_MOTOR_GROUP][did];
				if (cMotor != nMotor) {
					if (nMotor) {
						msg += "link " + did + " to " + nMotor + "; ";
					} else {
						msg += "remove link for " + did + "; ";
					}
				}
			});
		} 
		if (KEY_DEVICE_MOTOR_GROUP in obj.editorModel) {
			const nModel = obj.editorModel[KEY_DEVICE_MOTOR_GROUP];
			$.each(Object.keys(nModel), function(idx, did) {
				const nMotor = nModel[did];
				const cMotor = (KEY_DEVICE_MOTOR_GROUP in obj.model ? obj.model[KEY_DEVICE_MOTOR_GROUP][did] : null);
				if (!cMotor) {
					msg += "link " + did + " to " + nMotor + "; ";
				}
			});
		}
		return msg;
	}
	
	applyChange() {
		this.model = $.extend(true, {}, this.editorModel);
	}
	
	reset() {
		this.editorModel = $.extend(true, {}, this.model);
//		_sampleStage.select(this.getSelectedMotor());
		_motorGroups.select(this.getSelectedGroup());
	}
	
	verify() {
		const obj = this;
		console.log("additional verify");
		if (KEY_DEVICE_MOTOR_GROUP in obj.editorModel) {
			const deviceLinks = obj.editorModel[KEY_DEVICE_MOTOR_GROUP];
			$.each(deviceLinks, function(did, gid) {
				if (_instModel.getDevice(did)) {
					const groups = obj.editorModel[KEY_STAGE_GROUP];
					if (groups == null || !(gid in groups)) {
						_errorReport.addError(did, null, 'motor group ' + gid + ' not found in SE Motor Groups.');
					}
				}
			});
		} 
	}
}

class MotorDBModel extends AbstractModel {
	
	constructor() {
		super();
		this.model;
		this.devices;
	}
	
	getMotorList() {
		return Object.keys(this.devices);
	}
	
	load() {
		var obj = this;
		$.get('yaml/cachedModel?inst=' + _inst, function(data) {
			obj.model = data[PROP_GALIL_CONTROLLERS];
			obj.devices = {};
			$.each(obj.model, function(mcid, mc) {
				$.each(mc, function(aid, axis) {
					$.each(axis, function(mid, motor) {
						if (ID_PROP_SAMPLESTAGE in motor) {
							const val = motor[ID_PROP_SAMPLESTAGE];
							if (Boolean(val)) {
								obj.devices[mid] = motor;
							}
						}
					});
				});
			});
//			obj.createMenuUi();
			obj.afterLoad();
		}).fail(function(e) {
			if (e.status == 401) {
				window.location = 'signin.html?redirect=seConfig.html?inst=' + _inst;
			} else {
				alert(e.statusText);
			}
		});
	}
	
	createMenuUi() {
		const obj = this;
		var faIcon;
		var html = '<div class="class_a_motor_group" href="#"><h6 class="sidebar-subheading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' 
				+ '<span class="class_span_mc_name" >Sample Stage Motors</span>';
				+ '</h6></div><ul class="nav flex-column ">';
		obj.$menuHeader = $(html);
		
		obj.$menuUl = $('<ul class="nav flex-column class_ul_folder"></ul>');
//		_sampleStage = new SampleStage(obj.$menuUl);
//		const selectedMotor = _additionalModel.getSelectedMotor();
		var selectedMotor = null;
		$.each(Object.keys(this.devices), function(idx, mid) {
			const motor = obj.devices[mid];
			const checkedText = mid == selectedMotor ? "checked" : "";
			html = '<li class="nav-item class_li_subitem"><div class="menu_left"><input type="checkbox" id="id_motor_' + mid + '" class="class_SE_checkbox" ' + checkedText + '>'
				+ '<a class="nav-link class_a_motor" href="#" data-toggle="tooltip" data-placement="top" title="' + motor[KEY_MOTOR_DESC] + '">' 
				+ mid + '</a></div></li>';
			const $li = $(html);
//			_sampleStage.addMotor(mid, $li.find('input:checkbox'), $li.find('a'));
			obj.$menuUl.append($li);
		});
		obj.$menuHeader.append(obj.$menuUl);
		$('#id_div_sidebar').append(obj.$menuHeader);
	}
	
	afterLoad() {}
}

//class SampleStage {
//	
//	constructor($ul) {
//		this.$ul = $ul;
//		this.checkboxes = {};
//		this.links = {};
//		this.oldSelected = null;
////		this.model = {};
//	}
//	
//	addMotor(mid, $checkbox, $a) {
//		if ($checkbox.is(':checked')) {
//			this.oldSelected = mid;
//		}
//		this.checkboxes[mid] = $checkbox;
//		this.links[mid] = $a;
//		const obj = this;
//		$a.click(function() {
//			if ($checkbox.is(':checked')) {
//				$checkbox.prop('checked', false);
//			} else {
//				$checkbox.prop('checked', true);
//				$.each(Object.keys(obj.checkboxes), function(idx, key) {
//					if (key != mid) {
//						obj.checkboxes[key].prop('checked', false);
//					}
//				});
//			}
//		});
//		$checkbox.change(function() {
//			if ($checkbox.is(':checked')) {
//				$.each(Object.keys(obj.checkboxes), function(idx, key) {
//					if (key != mid) {
//						obj.checkboxes[key].prop('checked', false);
//					}
//				});
//			} 
//		});
//	}
//	
//	getSelected() {
//		const obj = this;
//		var found = null;
//		$.each(Object.keys(obj.checkboxes), function(idx, key) {
//			const $box = obj.checkboxes[key];
//			if ($box.is(':checked')) {
//				found = key;
//			}
//		});
//		return found;
//	}
//	
//	getMotors() {
//		return Object.keys(this.checkboxes);
//	}
//	
//	select(mid) {
//		const obj = this;
//		$.each(Object.keys(obj.checkboxes), function(idx, key) {
//			const $box = obj.checkboxes[key];
//			$box.prop('checked', key == mid);
//		});
//	}
//	
//	getChangeMsg() {
//		const newSelected = this.getSelected();
//		var msg = '';
//		if (this.oldSelected) {
//			if (newSelected) {
//				if (this.oldSelected != newSelected) {
//					msg = 'change sample stage motor to: ' + newSelected + '; ';
//				}
//			} else {
//				msg = 'deselect sample stage motor: ' + this.oldSelected + "; ";
//			}
//		} else {
//			if (newSelected) {
//				msg = 'select sample stage motor: ' + newSelected + '; ';
//			}
//		}
//		return msg;
//	}
//}

_additionalModel = new AdditionalModel();
_additionalModel.afterLoad = function() {
	_motorDBModel.load();
}

_motorDBModel = new MotorDBModel();
_motorDBModel.afterLoad = function() {
	_pdbModel.load();
}

class AbstractDeviceModel extends AbstractModel {
	#model;
	#configs;
	#firstConfigs;
	#configNamesOfDevice;
	#devices = {};

	constructor() {
		super();
	}
	
	get $menuBar() {}
	
	get type() {
		return '';
	}
	
	get model() {
		return this.#model;
	}
	
	set model(model) {
		this.#model = model;
	}
	
	get configs() {
		return this.#configs;
	}
	
	set configs(configs) {
		this.#configs = configs;
	}
	
	get firstConfigs() {
		return this.#firstConfigs;
	}
	
	set firstConfigs(dict) {
		this.#firstConfigs = dict;
	}

	getConfigModel(did, cid) {
		if (!(did in this.model)) {
			return {};
		} 
		if (!(cid in this.model[did])) {
			return {};
		}
		return this.model[did][cid];
	}
	
	get devices() {
		return this.#devices;
	}
	
	get deviceNames() {
		return this.#devices.keys();
	}
	
	get url() {}
	
	getConfigNamesOfDevice(did) {
		return this.configNamesOfDevice[did];
	}
	
	getDeviceModel(did) {
		if (!(did in this.model)) {
			return {};
		}
		return this.model[did];
	}
	
	getDevice(did) {
		return this.devices[did];
	}
	
	setDevice(did, device) {
		this.devices[did] = device;
	}
	
	afterLoad() {}
	
	verify() {}

	load() {
		var obj = this;
		$.get(this.url, function(data) {
			obj.model = data;
			obj.configs = {};
			obj.firstConfigs = {};
			obj.configNamesOfDevice = {};
//			var this_model = this._model;
//			var this_configs = this._configs;
//			var this_firstConfigs = this._firstConfigs;
			$.each(data, function(did, dev) {
				const names = [];
				$.each(dev, function(cid, cfg) {
					if (!(PROPERTY_KEYWORDS.includes(cid))) {
						var path = obj.type + "/" + did + "/" + cid;
						var desc = "";
						if (cfg.hasOwnProperty(KEY_DEVICE_DESC)) {
							desc = cfg[KEY_DEVICE_DESC];
						}
						var name = did + ":" + cid
						obj.configs[name] = [path, did, cid, desc];
						if (!(did in obj.firstConfigs)) {
							obj.firstConfigs[did] = cid;
						}
						names.push(cid);
					}
				});
				obj.configNamesOfDevice[did] = names;
			});
			obj.createUi();
			obj.afterLoad();
//			$("#id_div_sidebar").empty();
//			showModelInSidebar();
		}).fail(function(e) {
			if (e.status == 401) {
				alert("sign in required");
				window.location = 'signin.html?redirect=instrumentConfig.html';
			} else {
				alert(e.statusText);
			}
		});
	}
		
	createUi() {}
	
	addDevice(did, device) {}
	
	removeDevice(did) {}
	
	reset() {}
}

class InstrumentModel extends AbstractDeviceModel {

	constructor(){
		super();
	}

	get $menuBar() {
//		return $('#id_div_sidebar');
		return $('#id_div_composite_devices');
	}
	
	get type() {
		return TYPE_INST;
	}
	
	get url() {
		return URL_PREFIX + 'seconfig?inst=' + _inst;
	}

	createUi() {
		const obj = this;
		$.each(this.model, function(did, deviceModel){
			const device = new InstrumentDevice(did, deviceModel, obj.$menuBar);
			device.createMenuUi();
			obj.setDevice(did, device);
		});
	}

	afterLoad() {
		this.verify();
		_errorReport.show();
	}
	
	addDevice(did, deviceModel) {
//		if (did in this.model) {
//			throw new Error('device already exists: ' + did);
//		}
//		const obj = this;
//		const names = [];
//		
//		const newModel = {};
////		newModel[KEY_STATIC] = $.extend(true, {}, deviceModel[KEY_STATIC]);
//		$.each(deviceModel, function(cid, cfg) {
//			if (!(PROPERTY_KEYWORDS.includes(cid))) {
//				const newConfig = $.extend(true, {}, INST_DEVICE_INIT);
//				const datype = cfg[KEY_DATYPE];
//				newConfig[KEY_DATYPE] = datype;
//				newConfig[ID_PROP_ID] = cfg[ID_PROP_ID];
//				newConfig[ID_PROP_NAME] = cfg[ID_PROP_NAME];
////				var typeId = obj.getNextTypeId(datype, 1);
////				$.each(newModel, (subCid, subModel) => {
////					if (subModel[KEY_DATYPE] == datype) {
////						if (typeId == subModel[ID_PROP_ID]) {
////							typeId += 1;
////						}
////					}
////				});
////				console.log("add " + datype + " " + typeId);
////				newConfig[ID_PROP_ID] = typeId;
////				var prefix = datype;
////				if (datype in DEFAULT_SUB_DEVICE_NAME_PREFIX) {
////					prefix = DEFAULT_SUB_DEVICE_NAME_PREFIX[datype];
////				}
////				newConfig[ID_PROP_NAME] = prefix + typeId;
//				$.each(INST_DEVICE_COPY, (idx, key) => {
//					newConfig[key] = cfg[key];
//				});
//				newModel[cid] = newConfig;
//			}
//		});
//		this.model[did] = newModel;
//		
//		$.each(newModel, function(cid, cfg) {
//			if (!(PROPERTY_KEYWORDS.includes(cid))) {
//				var path = obj.type + "/" + did + "/" + cid;
//				var desc = "";
//				if (cfg.hasOwnProperty(KEY_DEVICE_DESC)) {
//					desc = cfg[KEY_DEVICE_DESC];
//				}
//				var name = did + ":" + cid
//				obj.configs[name] = [path, did, cid, desc];
//				if (!(did in obj.firstConfigs)) {
//					obj.firstConfigs[did] = cid;
//				}
//				names.push(cid);
//			}
//		});
//		obj.configNamesOfDevice[did] = names;
//			
//		const device = new InstrumentDevice(did, newModel, obj.$menuBar);
//		device.createMenuUi();
//		obj.setDevice(did, device);
//		
//		obj.reassignTypeNumber();
//		device.load();
//		device.setDirtyFlag();
	}
	
	reassignTypeNumber() {
//		const typeNumbers = {};
//		const obj = this;
//		$.each(obj.model, (did, dModel) => {
//			$.each(dModel, (cid, cModel) => {
//				const datype = cModel[KEY_DATYPE];
//				var typeNum = 1;
//				if (datype in typeNumbers) {
//					typeNum = typeNumbers[datype] + 1;
//				}
//				cModel[ID_PROP_ID] = typeNum;
//				var prefix = datype;
//				if (datype in DEFAULT_SUB_DEVICE_NAME_PREFIX) {
//					prefix = DEFAULT_SUB_DEVICE_NAME_PREFIX[datype];
//				}
//				cModel[ID_PROP_NAME] = prefix + typeNum;
//				typeNumbers[datype] = typeNum;
//				console.log(did + " - " + cid + ": " + prefix + typeNum);
//			});
//		});
	}
	
//	getNextTypeId(datype, startIdx) {
//		var idx = startIdx;
//		const obj = this;
//		$.each(obj.model, (did, dModel) => {
////			if (did != KEY_STATIC) {
//				$.each(dModel, (cid, cModel) => {
//					if (cModel[KEY_DATYPE] == datype) {
//						if (idx == cModel[ID_PROP_ID]) {
//							idx += 1;
//							return obj.getNextTypeId(datype, idx);
//						}
//					}
//				});
////			}
//		});
//		return idx;
//	}
	
	save() {
		const obj = this;
//		const selectedMotor = _sampleStage.getSelected();
//		console.log(selectedMotor);
		var url = URL_PREFIX + 'configsave?inst=' + _inst + '&msg=';
		var saveMsg = "";
		$.each(obj.devices, function(did, device) {
			saveMsg += device.getChangeMsg();
			device.applyChange();
		});
		saveMsg += _motorGroups.getChangeMsg();

		if (saveMsg.length == 0) {
			StaticUtils.showError('no change has been made.');
			return;
		} else {
			url += encodeURI(saveMsg);
		}
		url += "&" + Date.now();

		_saveButton.close();
		const rootModel = {
				"instrument_model" : obj.model,
				"additional_model" : _additionalModel.editorModel
		}

		var text = JSON.stringify(rootModel);
//		$.post(url,  {model:text}, function(data) {
		$.post(url,  text, function(data) {
			try {
				if (data["status"] == "OK") {
					StaticUtils.showMsg("Saved in the server.");
					$.each(obj.devices, (did, device) => {
						device.clearDirtyFlag();
					});
					_motorGroups.clearDirtyFlag();
					_historyBar.reload();
					_saveButton.reset();
//					setTimeout(_historyBar.reload, 3000)
				} else {
					StaticUtils.showError(data["reason"]);
				}
			} catch (e) {
				StaticUtils.showError("Failed to save: " + e.statusText);
			}
//			_errorReport.clearError();
			_instModel.verify();
		}).fail(function(e) {
			console.log(e);
			StaticUtils.showError("Faied to save: " + e.statusText);
		}).always(function() {
			_saveButton.close();
		});
	}
	
	removeDevice(did) {
//		const obj = this;
//		if (did in this.model) {
//			const deviceModel = this.model[did];
//			$.each(deviceModel, function(cid, cfg) {
//				if (!(PROPERTY_KEYWORDS.includes(cid))) {
//					var name = did + ":" + cid
//					delete obj.configs[name];
//				}
//			});
//			delete this.model[did];
//			delete this.firstConfigs[did];
//			delete this.configNamesOfDevice[did];
//			delete this.devices[did];
//		} else {
//			throw new Error('device not found:' + did);
//		}
	}

	selectionChanged() {
		var groupToEnable;
		$.each(this.devices, function(did, device) {
			if (device.isEnabled()) {
//				motorToEnable = _additionalModel.getMotorForDevice(did);
				groupToEnable = _additionalModel.getMotorGroupForDevice(did);
				if (groupToEnable) {
					return false;
				}
			}
		});
		_motorGroups.select(groupToEnable);
	}
	
	reset() {
		$.each(this.devices, function(did, device) {
			device.reset();
		});
	}
	
	verify() {
		const obj = this;
		_errorReport.clearError();
		$.each(Object.keys(obj.devices), function(idx, key) {
			obj.devices[key].verify();
		});
	}
}
_instModel = new InstrumentModel();

class DBModel extends AbstractDeviceModel {

	constructor(dbType){
		super();
		this.dbType = dbType;
	}

	get $menuBar() {
		return $('#id_div_sidebuttom');
	}
	
	get type() {
		return TYPE_DB;
	}
	
	get url() {
		return URL_PREFIX + 'sedb?dbtype=' + this.dbType;
	}

	afterLoad() {
//		_instModel.load();
	}
	
	createUi() {
//		const obj = this;
//		$.each(this.model, function(did, deviceModel){
//			const device = new DBDevice(did, deviceModel, obj.$menuBar);
//			device.createMenuUi();
//			obj.setDevice(did, device);
//		});
	}

}
_pdbModel = new DBModel(PHYSICAL_DB);
_cdbModel = new DBModel(COMPOSITE_DB);
_pdbModel.afterLoad = function() {
	_cdbModel.load();
}
_cdbModel.afterLoad = function() {
	_instModel.load();
}

class AbstractDevice {
	
	did;
	model;
	#dirtyFlag = false;
	$parentUi;
	$menuHeader;
	$menuUl;
	tabUi;
	rootEditor;
	configEditors;
	
	constructor(did, model, $parentUi)
	{
		this.did = did;
		this.model = model;
		this.editorModel = $.extend(true, {}, model);
		this.$parentUi = $parentUi;
		this.configEditors = {};
	}

	get type() {
		return '';
	}
	
	get datype() {
		if (KEY_STATIC in this.model && KEY_DATYPE in this.model[KEY_STATIC]) {
			return this.model[KEY_STATIC][KEY_DATYPE];
		} else {
			return "C";
		}
	}
	
	get id() {
		return this.type + ":" + this.did;
	}
	
	setDirtyFlag() {
		this.#dirtyFlag = true;
	}
	
	clearDirtyFlag() {
		this.#dirtyFlag = false;
	}
	
	isDirty() {
		return this.#dirtyFlag;
	}
	
	createMenuUi() {}
	
	checkDirtyFlag() {
//		var okToGo = true;
//		if (_curDevice != null) {
//			if (_curDevice.id != this.id && _curDevice.isDirty()) {
//				okToGo = confirm('You have unsaved changes in the current device. If you load another device, your change will be lost. Do you want to continue?');
//			}
//		}
//		return okToGo;
		return true;
	}
	
	load() {
		_errorReport.hide();
	}

	hide() {
//		this.dirtyFlag = false;
		this.setMenuActive(false);
		if (this.tabUi) {
			this.tabUi.hide();
		}
		if (this.rootEditor) {
			this.rootEditor.hide();
		}
		$.each(Object.values(this.configEditors), function(idx, editor) {
			editor.hide();
		});
		_title.text('');
		_editorTitle.text('');
	}
	
	setMenuItemActive(cid){}
	
	setMenuActive(isActive, cid) {
		if (isActive) {
			this.$menuHeader.find(".class_span_mc_name").addClass("span_highlight");
			if (this.$menuUl) {
				this.$menuUl.removeClass("class_ul_hide");
			}
		} else {
			this.$menuHeader.find(".class_span_mc_name").removeClass("span_highlight");
			if (this.$menuUl) {
				this.$menuUl.addClass("class_ul_hide");
			}
		}
		this.setMenuItemActive(cid);
	}
	
	loadConfig(){}
	
	getConfigArray() {
		const arr = [];
		$.each(this.model, function(cid, config){
			if (! (PROPERTY_KEYWORDS.includes(cid))){
				arr.push(cid);
			}
		});
		return arr;
	}
	
	getValue(cid, key) {
		return this.editorModel[cid][key];
	}
	
	setValue(cid, key, val) {
		this.editorModel[cid][key] = val;
	}
	
	save() {}
	
	reset() {}
	
	remove() {}
	
	fromHistory() {}
	
	verify() {}
}

class MotorGroups extends AbstractDevice {
	constructor(did, model, editorModel, $parentUi) {
//	constructor(model, $ul) {
		super(did, model, $parentUi);
		this.editorModel = editorModel;
//		this.model = model;
//		this.$ul = $ul;
		this.groupTitles = {};
		this.checkboxes = {};
		this.links = {};
		this.oldSelected = null;
		this.configMenuDict = {};
//		this.groupEditor = {};
	}
	
	createMenuUi() {
		const obj = this;
		var faIcon;
		var html = '<div class="class_a_motor_group" href="#"><h6 class="sidebar-subheading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' 
				+ '<span class="class_span_mc_name" >SE Motor Groups</span>'
				+ '</h6></div>';
		obj.$menuHeader = $(html);
		
		obj.$menuUl = $('<ul class="nav flex-column class_ul_folder"></ul>');
		const selectedGroup = _additionalModel.getSelectedGroup();
		$.each(Object.keys(obj.editorModel), function(idx, gid) {
			const group = obj.editorModel[gid];
			const checkedText = gid == selectedGroup ? "checked" : "";
			const title = "desc" in group ? group["desc"] : gid;
			obj.groupTitles[gid] = title;
			html = '<li class="nav-item class_li_subitem"><div class="menu_left"><input type="checkbox" id="id_group_' + gid + '" class="class_SE_checkbox" ' + checkedText + '>'
				+ '<a class="nav-link class_a_group" href="#" data-toggle="tooltip" data-placement="top" title="' + gid + '">' 
				+ title + '</a></div></li>';
			const $li = $(html);
			obj.addGroup(gid, $li.find('input:checkbox'), $li.find('a'));
			obj.$menuUl.append($li);
			obj.configMenuDict[gid] = $li;
		});
		obj.$menuHeader.append(obj.$menuUl);
		obj.$parentUi.append(obj.$menuHeader);
	}
	
	addGroup(gid, $checkbox, $a) {
		if ($checkbox.is(':checked')) {
			this.oldSelected = gid;
		}
		this.checkboxes[gid] = $checkbox;
		this.links[gid] = $a;
		const obj = this;
		$a.click(function() {
//			if ($checkbox.is(':checked')) {
//				$checkbox.prop('checked', false);
//			} else {
//				$checkbox.prop('checked', true);
//				$.each(Object.keys(obj.checkboxes), function(idx, key) {
//					if (key != mid) {
//						obj.checkboxes[key].prop('checked', false);
//					}
//				});
//			}
			obj.loadGroup(gid);
		});
		$checkbox.change(function() {
			if ($checkbox.is(':checked')) {
				$.each(Object.keys(obj.checkboxes), function(idx, key) {
					if (key != gid) {
						obj.checkboxes[key].prop('checked', false);
					}
				});
			}
			
			var found = null;
			$.each(obj.checkboxes, function(key, $box) {
				if ($box.is(':checked')) {
					found = key;
				}
			});
			obj.select(found);
		});
		
	}
	
	getGids() {
		return Object.keys(this.checkboxes);
	}
	
	getSelected() {
		const obj = this;
		var found = null;
		$.each(Object.keys(obj.checkboxes), function(idx, key) {
			const $box = obj.checkboxes[key];
			if ($box.is(':checked')) {
				found = key;
			}
		});
		return found;
	}
	
	listMotorsForGroup(gid) {
		return this.editorModel[gid]["motors"];
	}
	
	select(gid) {
		const obj = this;
		$.each(Object.keys(obj.checkboxes), function(idx, key) {
			const $box = obj.checkboxes[key];
			$box.prop('checked', key == gid);
		});
		_additionalModel.setSelectedGroup(gid);
		obj.setDirtyFlag();
	}

	loadGroup(gid) {
		if (_curDevice != null && _curDevice.id != this.id) {
			_curDevice.hide();
		}
		_curDevice = this;
		this.curGid = gid;
		const obj = this;
		const curConfig = obj.editorModel[gid];
		const stageMotors = Object.keys(_motorDBModel.devices);
//		const dbConfig = _cdbModel.getConfigModel(obj.did, cid);
//		const config = $.extend(true, {}, dbConfig, curConfig);
//			_tabs.empty();
//			var datype = obj.editorModel[KEY_STATIC][KEY_DATYPE];
		
		
		var desc = curConfig[KEY_DEVICE_DESC];
		if (desc == null) {
			desc = gid;
		}
		_title.text(gid + " (" + desc + ")");
		StaticUtils.clearMsg();

		_editorTitle.text('Details of the motor group');
		
		$.each(Object.values(this.configEditors), function(idx, groupEditor) {
			if (groupEditor.gid != gid) {
				groupEditor.hide();
			}
		});
//		this.rootEditor.hide();
		var editor = this.configEditors[gid];
		if (!editor) {
			editor = new MotorGroupEditor(obj, gid, curConfig);
			this.configEditors[gid] = editor;
		}
		editor.show();
		this.setMenuActive(true);
		this.setMenuItemActive(gid);
		_errorReport.hide();
	}
	
	setMenuItemActive(cid) {
		this.$menuUl.find('li.class_li_subitem').removeClass('active');
		if (cid) {
			this.configMenuDict[cid].addClass("active");
		}
//		this.tabUi.setActive(cid);
	}
	
	setMenuActive(isActive, cid) {
		if (isActive) {
			this.$menuHeader.find(".class_span_mc_name").addClass("span_highlight");
		} else {
			this.$menuHeader.find(".class_span_mc_name").removeClass("span_highlight");
		}
		this.setMenuItemActive(cid);
	}

	getChangeMsg() {
		const obj = this;
		var msg = '';
		
		const newSelected = this.getSelected();
		if (obj.oldSelected) {
			if (newSelected) {
				if (obj.oldSelected != newSelected) {
					msg = 'change SE motor group to: ' + newSelected + '; ';
				}
			} else {
				msg = 'deselect SE motor group: ' + obj.oldSelected + "; ";
			}
		} else {
			if (newSelected) {
				msg = 'select SE motor group: ' + newSelected + '; ';
			}
		}
		return msg;
	}

	isChanged() {
		return this.oldSelected != this.getSelected();
	}
	
	applyChange() {
		this.oldSelected = this.getSelected();
		this.model = _additionalModel.model[KEY_STAGE_GROUP];
		this.clearDirtyFlag();
	}
	
	setDirtyFlag() {
		const isDirty = this.isChanged();
		if (isDirty) {
			if (!this.isDirty()) {
				this.$menuHeader.find('span.class_span_mc_name').append('<i class="fas fa-asterisk i_changed"> </i>');
			}
			super.setDirtyFlag();
		} else {
			this.clearDirtyFlag();
		}
	}
	
	clearDirtyFlag() {
		super.clearDirtyFlag();
		this.$menuHeader.find('i.i_changed').remove();
	}

}

class DBDevice extends AbstractDevice {
	
	constructor(did, model, $parentUi)
	{
//		this.#did = did;
//		this.#model = model;
//		this.#parentUi = parentUi;
		super(did, model, $parentUi);
		this.rootEditor = new ImmutableRootUi(this);
	}
	
	get type() {
		return TYPE_DB;
	}
	
	get datype() {
		if (KEY_STATIC in this.model && KEY_DATYPE in this.model[KEY_STATIC]) {
			return this.model[KEY_STATIC][KEY_DATYPE];
		} else {
			return "C";
		}
	}

	createMenuUi() {
//		var datype = this.model["datype"];
//		if (datype != "C") {
//			return;
//		}
		if (this.datype != "C") {
			return;
		}
		
		const obj = this;
		var faIcon;
		var html = '<div class="class_a_mc" href="#"><h6 class="sidebar-subheading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' 
				+ '<span class="class_span_mc_name" did="' + this.did + '" datype="' + this.datype + '">' + this.did + '</span>';
//		if (datype != null && datype in DATYPE_ICON) {
//			faIcon = DATYPE_ICON[datype];
//			html += '<span class="class_span_mc_icon"><i class="fas fa-' + faIcon + '"></i> </span>';
//		}
		html += '<span class="class_span_mc_icon"><i class="fa fa-plus" aria-hidden="true"></i> </span>';
//		if (datype == "C") {
//			html += '<span class="class_span_mc_input class_span_hide"><input type="text" class="form-control class_mc_input" value="' + this.did + '"></span>';
//			html += '<span class="class_span_mc_control"><i class="fas fa-edit"></i> </span>';
//		}
		html += '</h6></div><ul id="ul_mc_'+ this.did + '" class="nav flex-column class_ul_hide ">';
		
		$.each(this.model, function(cid, config) {
			if (! (PROPERTY_KEYWORDS.includes(cid)) ) {
				html += '<li id="li_menu_' + obj.did + '_' + cid + '" class="nav-item class_li_subitem"><a class="nav-link class_a_axis" did="' 
					+ obj.did + '" cid="' + cid + '" href="#">' 
					+ cid + '<span class="sr-only">(current)</span></a></li>';
			}
		});
//		if (datype != "C") {
//			html += '<li id="li_menu_' + obj.did + '_NEW_DEVICE' + '" class="nav-item class_li_subitem">'
//				+ '<a class="nav-link class_a_add_axis" did="' + obj.did + '" href="#"><i class="fas fa-plus"> </a></li>';
//		}
		html += "</ul>";
		this.$menuHeader = $(html);
		this.$menuHeader.find('span.class_span_mc_name').click(function() {
			obj.load();
		});
		
		this.$menuHeader.find('span.class_span_mc_icon').click(function() {
			try {
				_instModel.addDevice(obj.did, _cdbModel.getDeviceModel(obj.did));
			} catch (e) {
				StaticUtils.showError(e);
			}
		});

		this.$parentUi.append(this.$menuHeader);
	}

	load() {
//		var okToGo = true;
//		if (_curDevice != null) {
//			if (_curDevice.did != this.did && _curDevice.dirtyFlag) {
//				okToGo = confirm('You have unsaved changes in the current axis. If you load another axis, your change will be lost. Do you want to continue?');
//			}
//		}
		if (this.checkDirtyFlag()) {
			if (_curDevice != null && _curDevice.id != this.id) {
				_curDevice.hide();
			}
			_curDevice = this;
			this.setMenuActive(true);
			
			StaticUtils.clearMsg();
			
			const obj = this;
			_title.text(obj.did + " (Database device preview)");

			_editorTitle.text('If you need to change the DB device configuration, use the Device Database page.');

			var ct = 0;
			
//			_tabs.empty();
			_errorReport.hide();

			this.rootEditor.show();
		}
	}
	
	save() {
		_saveButton.close();
		StaticUtils.showError("Can not change the DB device. Please use SE Database page to edit DB devices. ");
	}
}

class InstrumentDevice extends AbstractDevice {
	
	constructor(did, model, $parentUi)
	{
//		this.#did = did;
//		this.#model = model;
//		this.#parentUi = parentUi;
		super(did, model, $parentUi);
		this.tabUi = new TabUi(this);
		this.rootEditor = new MutableRootUi(this);
		this.configMenuDict = {};
		this.curCid = null;
	}

	get type() {
		return TYPE_INST;
	}
	
	isEnabled() {
		if (KEY_STATIC in this.editorModel) {
			if (ID_PROP_ENABLED in this.editorModel[KEY_STATIC]) {
				return this.editorModel[KEY_STATIC][ID_PROP_ENABLED];
			}
		}
		return true;
	}
		
	getChangeMsg() {
		const isEnabledNew = this.isEnabled();
		var isEnabledOld = true;
		if (KEY_STATIC in this.model) {
			if (ID_PROP_ENABLED in this.model[KEY_STATIC]) {
				isEnabledOld = this.model[KEY_STATIC][ID_PROP_ENABLED];
			}
		}
		if (isEnabledNew) {
			if (!isEnabledOld) {
				return 'enable ' + this.did + '; ';
			}
		} else {
			if (isEnabledOld) {
				return 'disable ' + this.did + '; ';
			}
		}
		return '';
	}

	applyChange() {
		const isEnabledNew = this.isEnabled();
		var isEnabledOld = true;
		if (KEY_STATIC in this.model) {
			if (ID_PROP_ENABLED in this.model[KEY_STATIC]) {
				isEnabledOld = this.model[KEY_STATIC][ID_PROP_ENABLED];
			}
		}
		if (isEnabledNew) {
			if (!isEnabledOld) {
				if (KEY_STATIC in this.model) {
					this.model[KEY_STATIC][ID_PROP_ENABLED] = true;
				} else {
					this.model[KEY_STATIC] = {"enabled" : true};
				}
			}
		} else {
			if (isEnabledOld) {
				if (KEY_STATIC in this.model) {
					this.model[KEY_STATIC][ID_PROP_ENABLED] = false;
				} else {
					this.model[KEY_STATIC] = {"enabled" : false};
				}
			}
		}
	}

	hasChanged() {
		const isEnabledNew = this.isEnabled();
		var isEnabledOld = true;
		if (KEY_STATIC in this.model) {
			if (ID_PROP_ENABLED in this.model[KEY_STATIC]) {
				isEnabledOld = this.model[KEY_STATIC][ID_PROP_ENABLED];
			}
		}
		return isEnabledNew ? !isEnabledOld : isEnabledOld;
	}
	
	createMenuUi() {
		const obj = this;
//		var datype = this.model["datype"];
//		if (datype == null) {
//			return;
//		}
		var faIcon;
		var html = '<div class="class_a_mc" href="#"><h6 class="sidebar-subheading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' 
			+ '<div class="menu_left">'
			+ '<span class="class_span_mc_enabled"><input type="checkbox" name="' + this.did + '_enabled" ' + (obj.isEnabled() ? 'checked' : '')+ '/> </span>'
			+ '<span class="class_span_mc_name" did="' + this.did + '" datype="' + this.datype + '"><i class="fas fa-caret-down"></i> ' + this.did + '</span>'
			+ '</div>';
//		if (this.datype != null && this.datype in DATYPE_ICON) {
//			faIcon = DATYPE_ICON[this.datype];
//			html += '<span class="class_span_mc_icon"><i class="fas fa-' + faIcon + '"></i> </span>';
//		}
//		if (this.datype == "C") {
//			html += '<span class="class_span_mc_control"><i class="fas fa-minus"></i> </span>';
//		}
		html += '</h6></div>';
		
		this.$menuHeader = $(html);
		this.$menuHeader.find('span.class_span_mc_name').click(function() {
			obj.load();
		});
		
		this.$menuHeader.find('span.class_span_mc_enabled > input').change(function() {
	    	if (KEY_STATIC in obj.editorModel) {
	    		obj.editorModel[KEY_STATIC][ID_PROP_ENABLED] = $(this).is(':checked');
	    	} else {
	    		obj.editorModel[KEY_STATIC] = {"enabled" : $(this).is(':checked')};
	    	}
	    	_instModel.selectionChanged();
	    	if (obj.hasChanged()) {
	    		obj.setDirtyFlag();
	    	} else {
	    		obj.clearDirtyFlag();
	    	}
		});
		
//		this.$menuHeader.find('span.class_span_mc_control').click(function() {
////			_removeModal.toRemove = obj.did;
////			_removeModal.open();
//			obj.remove();
//		});
		
		const $menuUl = $('<ul id="ul_mc_'+ this.did + '" class="nav flex-column class_ul_hide class_ul_folder"></ul>');
		$.each(this.model, function(cid, config) {
			if (! (PROPERTY_KEYWORDS.includes(cid))){
				const subItem = new ConfigMenuItem(obj, cid);
				subItem.createUi($menuUl);
				obj.configMenuDict[cid] = subItem;
			}
		});
		this.$menuUl = $menuUl;
		
		this.$parentUi.append(this.$menuHeader);
		this.$parentUi.append(this.$menuUl);
	}

	setDirtyFlag() {
		if (!this.isDirty()) {
			this.$menuHeader.find('span.class_span_mc_name').append('<i class="fas fa-asterisk i_changed"> </i>');
		}
		super.setDirtyFlag();
	}
	
	clearDirtyFlag() {
		super.clearDirtyFlag();
		this.$menuHeader.find('i.i_changed').remove();
	}
	
	load() {
		if (this.checkDirtyFlag()) {
			const obj = this;
			if (_curDevice != null && _curDevice.id != this.id) {
				_curDevice.hide();
			}
			_curDevice = this;
			this.setMenuActive(true);
			
			_title.text(obj.did);
			StaticUtils.clearMsg();

			if (!(obj.did in _cdbModel.model)) {
				StaticUtils.showWarning("device " + obj.did + " not found in the Device Database.");
			}
			
			_editorTitle.text('Use the tab menu to load configuration of sub-device');
			this.curCid = null;
			$.each(Object.values(this.configEditors), function(idx, propTable) {
				propTable.hide();
			});
			this.tabUi.show();
			_errorReport.hide();
			this.rootEditor.show();
//			_historyBar.reload();
		} else {
			return;
		}
	}
	
	loadConfig(cid) {
		if (this.checkDirtyFlag()) {
			
			if (_curDevice != null && _curDevice.id != this.id) {
				_curDevice.hide();
			}
			_curDevice = this;
//
//			_curDeviceModel = new DeviceModel(did, modelGroup);
//			if (cid) {
//				if (!cid in keysOf(modelGroup)) {
//					alert(cid + "doesn't exist");
//					return false;
//				}
//			} else {
//				this.cid = keysOf(modelGroup)[0];
//			}
//			_curDid = did;
//			_curCid = cid;
			this.curCid = cid;
			const obj = this;
			const curConfig = obj.editorModel[cid];
			const dbConfig = _cdbModel.getConfigModel(obj.did, cid);
			const config = $.extend(true, {}, dbConfig, curConfig);
//			_tabs.empty();
//			var datype = obj.editorModel[KEY_STATIC][KEY_DATYPE];
			
			
			var desc = config[KEY_DEVICE_DESC];
			if (desc == null) {
				desc = config[KEY_DEVICE_DRIVER];
			}
			_title.text(obj.did + ":" + cid + " (" + desc + ")");
			StaticUtils.clearMsg();

//			_editor.empty();
			_editorTitle.text('Device properties');
			
			$.each(Object.values(this.configEditors), function(idx, propTable) {
				if (propTable.cid != cid) {
					propTable.hide();
				}
			});
			this.rootEditor.hide();
			var table = this.configEditors[cid];
			if (!table) {
				table = new PropertyTable(obj, cid, config);
				this.configEditors[cid] = table;
			}
			table.show();
			this.setMenuActive(true);
			this.setMenuItemActive(cid);
			_errorReport.hide();
//			_historyBar.reload();
		}
	}
	
	setMenuItemActive(cid) {
		this.$menuUl.find('li.class_li_subitem').removeClass('active');
		if (cid) {
			this.configMenuDict[cid].setActive();
		}
		this.tabUi.setActive(cid);
	}
	
	getDriverName(cid) {
		return this.model[cid][ID_PROP_DRIVER];
	}
	
	changeConfig(cid, newConfigId) {
		const obj = this;
		const driverId = this.getDriverName(cid);
		if (driverId == null) {
//			throw new Error('device not found in this configuration');
			StaticUtils.showError('deviceId property not found in this configuration');
			return;
		}
		const dModel = _pdbModel.getDeviceModel(driverId);
		if (Object.keys(dModel).length == 0) {
//			throw new Error('device not found in the Device Database: ' + driverId);
			StaticUtils.showError('device not found in the Device Database: ' + driverId);
			return;
		}
		const cModel = dModel[newConfigId];
		if (cModel == null) {
//			throw new Error('configuration not found in database device: ' + newConfigId);
			StaticUtils.showError('configuration not found in database device: ' + newConfigId);
			return;
		}
		const editorConfig = this.editorModel[cid];
		$.each(cModel, function(key, val) {
			if (!PROP_TO_AVOID.includes(key) && key in editorConfig) {
				editorConfig[key] = val;
				if (cid in obj.configEditors) {
					const editor = obj.configEditors[cid];
					editor.updateValue(key, val);
				}
			}
		});
		
	}
	
	save() {
//		const obj = this;
//		var url = URL_PREFIX + 'configsave?inst=' + _inst + '&msg=';
//		var saveMsg = $('#id_input_saveMessage').val().replace(/^\s+|\s+$/gm,'');
//		if (saveMsg.length > 0) {
//			url += encodeURI(saveMsg);
//		}
//		url += "&" + Date.now();
//		_saveButton.close();
//		var text = JSON.stringify(this.editorModel);
//		$.post(url,  {did:obj.did, model:text}, function(data) {
////			data = $.parseJSON(data);
//			try {
//				if (data["status"] == "OK") {
//					StaticUtils.showMsg("Saved in the server.");
//					$('td.editable_value input.changed').removeClass('changed');
////					if (_deviceItem != null) {
////						_deviceItem.init = false;
////						_deviceItem = null;
////					}
//					$.extend(true, obj.model, obj.editorModel);
//					obj.clearDirtyFlag();
//					_historyBar.reload();
//					_saveButton.reset();
////					setTimeout(_historyBar.reload, 3000)
//				} else {
//					StaticUtils.showError(data["reason"]);
//				}
//			} catch (e) {
//				StaticUtils.showError("Failed to save: " + e.statusText);
//			}
////			_errorReport.clearError();
//			_instModel.verify();
//		}).fail(function(e) {
//			console.log(e);
//			StaticUtils.showError("Faied to save: " + e.statusText);
//		}).always(function() {
//			_saveButton.close();
//		});
	}
	
	reset() {
		this.clearDirtyFlag();
		const obj = this;
		$.extend(true, this.editorModel, this.model);
//		$.each(Object.values(this.configEditors), function(idx, editor) {
//			editor.dispose();
//		});
//		this.configEditors = {};
//		if (this.curCid) {
//			this.loadConfig(this.curCid);
//		} else {
//			this.load();
//		}
		
		this.$menuHeader.find('span.class_span_mc_enabled > input').prop('checked', this.isEnabled()); 
	}
	
	fromHistory(commitModel) {
		const deviceModel = commitModel[this.did];
		if (!deviceModel) {
			throw new Error('device not found in history model: ' + this.did);
		}
		$.extend(true, this.editorModel, deviceModel);
		$.each(Object.values(this.configEditors), function(idx, editor) {
			editor.dispose();
		});
		this.configEditors = {};
		if (this.curCid) {
			this.loadConfig(this.curCid);
		} else {
			this.load();
		}
		this.setDirtyFlag();
	}
	
//	remove(saveMsg) {
	remove() {
		const obj = this;
		try {
			if (_curDevice != null && _curDevice.id == obj.id) {
				_curDevice.hide();
				_title.text('Please use the side bar to select a device configuration.');
			}
			
			obj.$menuHeader.remove();
			$.each(obj.configMenuDict, function(cid, configMenu) {
				configMenu.dispose();
				delete obj.configMenuDict[cid];
			});
			if (obj.$menuUl) {
				obj.$menuUl.remove();
			}
			if (obj.tabUi) {
				obj.tabUi.dispose();
			}
			if (obj.rootEditor) {
				obj.rootEditor.dispose();						
			}
			$.each(obj.configEditors, function(cid, editor) {
				editor.dispose();
				delete obj.configEditors[cid];
			});
			
			_instModel.removeDevice(obj.did);
			
			obj.clearDirtyFlag();
			StaticUtils.showWarning("Make sure to use the 'Save' button to save the change to the server.");
		} catch (e) {
			StaticUtils.showError("Failed to remove: " + e.statusText);
		}

//		var url = URL_PREFIX + 'configremove?inst=' + _inst + '&did=' + obj.did + '&msg=';
//		if (saveMsg.length > 0) {
//			url += encodeURI(saveMsg);
//		}
//		url += "&" + Date.now();
//		$.get(url, function(data) {
//			try {
//				if (data["status"] == "OK") {
//					obj.clearDirtyFlag();
//					StaticUtils.showMsg("Removed successfully in the server.");
////					var $ul = $('#ul_mc_' + did);
////					$ul.prev().remove();
////					$ul.remove();
////					loadDeviceConfig(null, null);
//					if (_curDevice != null && _curDevice.id == obj.id) {
//						_curDevice.hide();
//						_title.text('Please use the side bar to select a device configuration.');
//					}
//					
//					obj.$menuHeader.remove();
//					$.each(obj.configMenuDict, function(cid, configMenu) {
//						configMenu.dispose();
//						delete obj.configMenuDict[cid];
//					});
//					if (obj.$menuUl) {
//						obj.$menuUl.remove();
//					}
//					if (obj.tabUi) {
//						obj.tabUi.dispose();
//					}
//					if (obj.rootEditor) {
//						obj.rootEditor.dispose();						
//					}
//					$.each(obj.configEditors, function(cid, editor) {
//						editor.dispose();
//						delete obj.configEditors[cid];
//					});
//					
//					_instModel.removeDevice(obj.did);
////					setTimeout(_historyBar.reload, 3000)
//				} else {
//					StaticUtils.showError(data["reason"]);
//				}
//			} catch (e) {
//				StaticUtils.showError("Failed to remove: " + e.statusText);
//			}
//		}).fail(function(e) {
//			StaticUtils.showError("Faied to remove: " + e.statusText);
//		}).always(function() {
//			$('#id_modal_deleteDialog').modal('hide');
//		});
	}
	
	verify() {
		const obj = this;
		$.each(this.getConfigArray(), function(idx, cid) {
			const configModel = obj.model[cid];
			const driverId = obj.getDriverName(cid);
			if (!(obj.did in _cdbModel.model)) {
				_errorReport.addError(obj.did, cid, "device " + obj.did + " not found in the Composite Database");
				return false;
			}
			const configSelection = _cdbModel.model[obj.did][cid][ID_PROP_CONFIGID];
			if (driverId == null) {
//				StaticUtils.showError('device ID not found in configuration');
				_errorReport.addError(obj.did, cid, "configuration doesn't have a deviceId property");
				return false;
			}
			const dModel = _pdbModel.getDeviceModel(driverId);
			if (Object.keys(dModel).length == 0) {
				_errorReport.addError(obj.did, cid, 'device ' + driverId + ' not found in the Physical Database');
//				StaticUtils.showError('device not found in the Device Database: ' + driverId);
				return false;
			}
			const cModel = dModel[configSelection];
			if (cModel == null) {
//				StaticUtils.showError('configuration not found in database device: ' + newConfigId);
				_errorReport.addError(obj.did, cid, 'configuration ' + configSelection + ' not found in physical device: ' + driverId);
				return false;
			}
		});

	}
}

class ConfigMenuItem {
	
	$ui;
	$parentUi;

	constructor(device, cid)
	{
		this.device = device;
		this.cid = cid;
	}
	
	createUi($parentUi) {
		var obj = this;
		this.$parentUi = $parentUi;
		var html = '<li id="li_menu_' + this.device.did + '_' + this.cid + '" class="nav-item class_li_subitem"><a class="nav-link class_a_axis" did="' 
			+ this.device.did + '" cid="' + this.cid + '" href="#">' 
			+ this.cid + '<span class="sr-only">(current)</span></a></li>';
		this.$ui = $(html);
		this.$ui.click(function(){
			obj.load(obj.cid);
		});
		this.$parentUi.append(this.$ui);
	}
	
	load() {
		this.device.loadConfig(this.cid);
	}
	
	setActive() {
		this.$ui.addClass("active");
	}
	
	dispose() {
		if (this.$ui) {
			this.$ui.remove();
		}
	}
}

class TabUi {
	device;
	init = false;
	
	$tabUi = $(HTML_NAV_DIV).addClass("div_hidden");
	
	constructor(device)
	{
		this.device = device;
	}
	
	createUi() {
		var obj = this;
		var $li = $('<li class="nav-item active"><a class="nav-link tab_root active" href="#">Root view</a></li>');
		$li.click(function() {
			obj.device.load();
		});

		obj.$tabUi.append($li);
		$.each(this.device.model, function(cid, config) {
			if (! (PROPERTY_KEYWORDS.includes(cid))){
				var $li = $('<li class="nav-item" id="li_tab_' + obj.device.did + '_' + cid + '"><a class="nav-link tab_item" href="#">' + cid + '</a></li>');
				$li.click(function() {
					obj.device.loadConfig(cid);
				});					
				obj.$tabUi.append($li);
//				ct++;
			}
		});
		_tabs.append(this.$tabUi);
		this.init = true;
	}
	
	show() {
		if (!this.init) {
			this.createUi();
		}
		this.$tabUi.removeClass("div_hidden");
//		_tabs.empty();
//		_tabs.append(this.$tabUi);
	}
	
	hide() {
		if (!this.$tabUi.hasClass("div_hidden")) {
			this.$tabUi.addClass("div_hidden");
		}
	}
	
	setActive(cid) {
		this.$tabUi.find('a').removeClass("active");
		if (cid) {
			this.$tabUi.find('#li_tab_' + this.device.did + '_' + cid + '>a').addClass("active");
		} else {
			this.$tabUi.find('a.tab_root').addClass("active");
		}
	}
	
	dispose() {
		this.$tabUi.remove();
	}
}

class AbstractMainUi {
	device;
	init = false;
	
	$editorUi = $(HTML_HIDDEN_FILL_DIV);
	
	constructor(device)
	{
		this.device = device;
	}
	
	createUi() {}
	
	show() {
		if (!this.init) {
			this.createUi();
		}
//		_editor.empty();
		this.$editorUi.removeClass("div_hidden");
//		_editor.append(this.$editorUi);
	}
	
	hide() {
		if (!this.$editorUi.hasClass("div_hidden")) {
			this.$editorUi.addClass("div_hidden");
		}
	}
	
	dispose() {
		this.$editorUi.remove();
	}
}

class ErrorReportUi extends AbstractMainUi {
	constructor()
	{
		super(null);
	}
	
	createUi() {
		this.$table = $(HTML_TABLE);
		this.$body = this.$table.find('tbody');
		this.$editorUi.append(this.$table);
		_editor.append(this.$editorUi);
		this.init = true;
	}
	
	addError(did, cid, msg) {
		this.$body.find('.class_no_entry').remove();
		const eid = did + " / " + cid;
		const $tr = $('<tr id="' + did + "_" + cid + '"><td><span class="span_error_id" did="' + did + '" cid="' + cid + '">' + eid 
				+ '</span></td><td><span class="span_error_msg">' + msg + '</span></td></tr>');
		$tr.find('span.span_error_id').click(function() {
			console.log("load " + eid);
			const device = _instModel.getDevice(did);
			device.loadConfig(cid);
		});
		this.$body.append($tr);
	}
	
	removeError(did, cid) {
		this.$body.find('#' + id).remove();
	}
	
	clearError() {
		this.$body.empty();
		this.addNoEntry();
	}
	
	addNoEntry() {
		const $tr = $('<tr class="class_no_entry"><td colspan="2">No error found.</td></tr>');
		this.$body.append($tr);
	}
}

class MotorGroupEditor extends AbstractMainUi {
	constructor(device, gid, groupModel) {
		super(device);
		this.gid = gid;
		this.model = groupModel;
		this.addedMotors = {};
		this.availMotors = {};
		this.$gAdded;
		this.$gAvail;
	}
	
	createUi() {
		const obj = this;
		const $div = $('<div class="class_div_motor_group"/>');
		const $desc = $('<div class="class_div_desc_editor"><span>Description</span><span><input type="text" class="form-control" disabled value="' + obj.model["desc"] + '"></span></div>');
		$div.append($desc);
		$desc.find('input').focus(function() {
		}).blur(function() {
			obj.setDesc($(this).val());
		}).keypress(function( event ) {
			if ( event.which == 13 ) {
				$desc.blur();
			}
		});
		obj.$gAdded = $('<div class="class_div_motor_box" id="id_div_added_box"><span class="class_span_box_title">list of motors</span></div>');
		$div.append(obj.$gAdded);
		
		$.each(obj.model["motors"], function(idx, motorName) {
			const $motorItem = $('<div class="class_div_motor_item">' + motorName 
			+ '</div>');
			obj.$gAdded.append($motorItem);
			obj.addedMotors[motorName] = $motorItem;
			$motorItem.find(".class_span_motor_control").click(function() {
				obj.removeMotor(motorName);
			});
		});
		
//		$div.append($del);
		this.$editorUi.append($div);
		_editor.append(this.$editorUi);
//		_historyBar.reload();
		this.init = true;
	}

}

class MutableRootUi extends AbstractMainUi {
	constructor(device) {
		super(device);
	}
	
	createUi() {
		const obj = this;
		var html = '';
		$.each(obj.device.model, function(cid, config) {
			if (! (PROPERTY_KEYWORDS.includes(cid))) {
				html += '<div class="class_div_device_page" id="div_page_' + obj.device.did + '_' + cid + '">'
					+ '<div class="class_div_device_item"><a href="#" class="class_a_cid_label" did="' 
					+ obj.device.did + '" cid="' + cid + '">' + cid + '<br>(' + config[KEY_DEVICE_DRIVER] + ')' + '</a></div></div>';
			}
		});
		
		const selected = _additionalModel.getMotorGroupForDevice(obj.device.did);
		const desc = _additionalModel.getGroupDescription(selected);
		
		if (selected) {
			html += '<div class="class_div_device_page" id="div_page_' + obj.device.did + '_motor">'
				+ '<div class="class_div_device_item"><span class="class_a_cid_label" did="' 
				+ obj.device.did + '" cid="motor_selection"><a href="#">Sample Stage</a><br>' + desc + '</span></div></div>';
		}
		
		var $div = $('<div class="class_div_device_canvas"/>').append(html);
		$div.find('a.class_a_cid_label').click(function(){
			var acid = $(this).attr('cid');
			obj.device.loadConfig(acid);
		});
		
		this.$editorUi.append($div);
		
		_editor.append(this.$editorUi);
//		_historyBar.reload();
		this.init = true;
	}
	
}

class ImmutableRootUi extends AbstractMainUi {
	constructor(device) {
		super(device);
	}
	
	createUi() {
		const obj = this;
		var html = '';
		$.each(obj.device.model, function(cid, config) {
			if (! (PROPERTY_KEYWORDS.includes(cid))) {
				html += '<div class="class_div_device_page" id="div_page_' + obj.device.did + '_' + cid + '">'
					+ '<div class="class_div_device_item"><span class="class_a_cid_label" did="' 
					+ obj.device.did + '" cid="' + cid + '">' + cid + '<br>(' + config[KEY_DEVICE_DRIVER] + ')' + '</span></div></div>';
			}
		});
		var $div = $('<div class="class_div_device_canvas div_static"/>').append(html);

		this.$editorUi.append($div);
		_editor.append(this.$editorUi);
//		_historyBar.reload();
		this.init = true;
	}
}

class PropertyTable extends AbstractMainUi {
	
	constructor(device, cid, config) {
		super(device);
//		this.device = device;
		this.cid = cid;
		this.cModel = config;
		this.driverId = config[KEY_DEVICE_DRIVER];
		this.dModel = _pdbModel.getDeviceModel(this.driverId);
		this.rowEditor = {};
		this.configRow = null;
		this.ipRow = null;
		this.portRow = null;
	}
	
	createUi() {
		var $table = $(TABLE_TIER1_HEADER + '</tbody></table>');
		var $tbody = $table.find('tbody');
		var obj = this;
//		var cid = this.cid;
//		var did = this.driverId;
		var subConfigId = this.cModel[ID_PROP_CONFIGID];
//		if (typeof this.dModel === 'undefined') {
//			StaticUtils.showWarning('device ' + obj.driverId + ' not found in the Database');
//			$.each(this.cModel, function(key, val){
//				if (!FIXED_PROPS.includes(key)) {
//					var pRow = obj.createRow(key, val);
//					$tbody.append(pRow.getUI());
//				} else {
//					var pRow = obj.createRow(key, val, null, true);
//					$tbody.append(pRow.getUI());
//				}
//			});
//		} else {
//			if (subConfigId in obj.dModel) {
//				$.each(this.cModel, function(key, val){
//					if (key == ID_PROP_CONFIGID) {
//						var options = _dbModel.getConfigNamesOfDevice(obj.driverId);
//						obj.configRow = obj.createRow(key, val, options, true);
//						$tbody.append(obj.configRow.getUI());
//					} else if (key == ID_PROP_IP) {
//						var options = obj.dModel[KEY_STATIC][ID_PROP_IP];
//						obj.ipRow = obj.createRow(key, val, options);
//						$tbody.append(obj.ipRow.getUI());
//					} else if (key == ID_PROP_PORT) {
//						var options = obj.dModel[subConfigId][ID_PROP_PORT];
//						obj.portRow = obj.createRow(key, val, options);
//						$tbody.append(obj.portRow.getUI());
//					} else if (!FIXED_PROPS.includes(key)) {
//						var pRow = obj.createRow(key, val);
//						$tbody.append(pRow.getUI());
//					} else {
//						var pRow = obj.createRow(key, val, null, true);
//						$tbody.append(pRow.getUI());
//					}
//				});
//			} else {
//				const obj = this;
//				$.each(this.cModel, function(key, val){
//					if (key == ID_PROP_CONFIGID) {
////						var options = obj.device.getConfigArray();
//						StaticUtils.showWarning("configuration " + subConfigId + " not found in physical device: " + obj.driverId);
//						var options = _dbModel.getConfigNamesOfDevice(obj.driverId);
//						options.push(val);
//						obj.configRow = obj.createRow(key, val, options);
//						$tbody.append(obj.configRow.getUI());
//					} else if (!FIXED_PROPS.includes(key)) {
//						var pRow = obj.createRow(key, val);
//						$tbody.append(pRow.getUI());
//					} else {
//						var pRow = obj.createRow(key, val, null, true);
//						$tbody.append(pRow.getUI());
//					}
//				});
//			}
//		}
		$.each(this.cModel, function(key, val){
			var pRow = obj.createRow(key, val, null, true);
			$tbody.append(pRow.getUI());
		});
		if (obj.configRow != null) {
			obj.configRow.addValueSelectListener(function(value){
				var newConfig = obj.dModel[value];
//				const staticConfig = obj.dModel[KEY_STATIC];
				if (newConfig) {
//					var ips = staticConfig[ID_PROP_IP];
//					if (obj.ipRow) {
//						obj.ipRow.updateValueOptions(ips);
//					}
					var ports = newConfig[ID_PROP_PORT];
					if (obj.portRow) {
						obj.portRow.updateValueOptions(ports);
					}
				}
			});
		}
		this.$editorUi.append($table);
		_editor.append(this.$editorUi);
		this.init = true;
	}
	
//	createRow(key, val, options, editingDisabled) {
//		const cid = this.cid;
//		if (typeof options === 'undefined') {
//			options = null;
//		} else if (!(typeof options === 'object')) {
//			options = [options];
//		}
//		if (typeof editingDisabled === 'undefined') {
//			editingDisabled = false;
//		}
//		var html;
//		if (typeof val === 'object') {
//			html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>' 
////			+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair" selected>name-value pair</option></select></td>'
//			+ '<td class="editable_value">' + TABLE_TIER2_HEADER;
//			$.each(val, function(subKey, subVal){
//				if ('key' in val) {
//					html += EMPTY_ROW_PART1 + subKey + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
//				} else {
//					html += DISABLED_ROW_PART1 + '-' + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
//				}
//			});
//			html += '</tbody></table></td></tr>';
//		} else {
//			if (options != null) {
//				var ot = "";
//				$.each(options, function(idx, op){
//					var sl = op == val ? " selected" : "";
//					ot += '<option value="' + op + '"' + sl + '>'+ op + '</option>';
//				});
//				html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>'
////				+ '<td class="editable_type"></td>' 
//				+ '<td class="editable_value">';
//				if (editingDisabled) {
//					html += '<select name="option_value" class="form-control">' + ot + '</select>';
//				} else {
//					html += '<input name="option_value" class="form-control" value="' + val + '" list="' + this.device.did + '_' + key + '"/><datalist id="' + this.device.did + '_' + key + '">' + ot + '</datalist>';
//				}
//				html += '</td></tr>';
//			} else {
//				html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>'
////				+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair">name-value pair</option></select></td>'
//				+ '<td class="editable_value"><input type="text" class="form-control" value="' + val + '"' + (editingDisabled ? ' disabled' : '') + '></td></tr>';
//			}
//		}
//		var $row = $(html);
//		return new PropertyRow(this.device, cid, $row);
//	};

	updateValue(key, val) {
		const propertyRow = this.rowEditor[key];
		if (propertyRow != null) {
			propertyRow.setValue(val);
		}
	}
	
	createRow(key, val, options, editingDisabled) {
		const cid = this.cid;
		if (typeof options === 'undefined') {
			options = null;
		} else if (!(typeof options === 'object')) {
			options = [options];
		}
		if (typeof editingDisabled === 'undefined') {
			editingDisabled = false;
		}
		var propertyRow;
		if (key == ID_PROP_CONFIGID && options != null) {
			propertyRow = new ConfigIDPropertyRow(this.device, cid, key, val, options);
		} else {
			if (typeof val === 'object') {
				if (Array.isArray(val)) {
					propertyRow = new ArrayPropertyRow(this.device, cid, key, val);
				} else {
					propertyRow = new DictPropertyRow(this.device, cid, key, val);
				}
			} else {
				if (options != null) {
					propertyRow = new OptionsPropertyRow(this.device, cid, key, val, options);
				} else {
					propertyRow = new PropertyRow(this.device, cid, key, val);				
				}
			}
		}
		propertyRow.isEditingDisabled = editingDisabled;
		propertyRow.init();
		this.rowEditor[key] = propertyRow;
		return propertyRow;
	}
	
}

//class PropertyRow {
//	constructor(device, cid, tr) {
//		this.device = device;
//		this.row = tr;
//		this.cid = cid;
////		var colKey = tr.find('.editable_key');
//		this.colValue = tr.find('.editable_value');
//		this.key = tr.attr('key');
//		this.addEventHandler();
//	}
//	
//	addEventHandler() {
//		const obj = this;
//		const colType = this.row.find('.editable_type');
//		const sel = colType.find('select');
//		const oldVal = this.device.model[this.cid][this.key];
//		var newTextHtml;
//		var newPairHtml;
//		if (typeof oldVal === 'object') {
//			newPairHtml = TABLE_TIER2_HEADER;
//			$.each(oldVal, function(subKey, subVal){
//				newPairHtml += EMPTY_ROW_PART1 + subKey + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
//			});
//			newPairHtml += '</tbody></table>';
//			newTextHtml = '<input type="text" key="' + this.key + '" class="form-control" value="">';
//		} else {
//			newPairHtml = TABLE_TIER2_HEADER + EMPTY_ROW_PART1 + EMPTY_ROW_PART2 + EMPTY_ROW_PART3 + '</tbody></table>';
//			newTextHtml = '<input type="text" key="' + this.key + '" class="form-control" value="' + oldVal + '">';
//		}
//		sel.change(function() {
//			var selVal = sel.val();
//			if (selVal === 'text') {
//				obj.colValue.html(newTextHtml);
//			} else {
//				obj.colValue.html(newPairHtml);
//			}
//		});
//		var t1Value = this.colValue.find("> input");
//		t1Value.focus(function() {
//			obj.row.addClass("active");
//		}).blur(function() {
//			obj.row.removeClass("active");
//			obj.updateNode(t1Value, obj.key, t1Value.val());
//		}).keypress(function( event ) {
//			if ( event.which == 13 ) {
//				t1Value.blur();
//			}
//		});
//		
//		var t1Select = this.colValue.find("> select");
//		if (t1Select) {
//			t1Select.change(function() {
//				obj.updateNode(t1Select, obj.key, t1Select.val());
//			});
//		}
//		
//		var t2Body = obj.colValue.find("tbody");
//		if (t2Body) {
//			var t2Key = t2Body.find("td.pair_key > input");
//			var oldKV = t2Key.val();
//			var isPair = !t2Key.prop('disabled');
//			if (isPair) {
//				t2Key.focus(function() {
//					this.row.addClass("active");
//				}).blur(function() {
//					this.row.removeClass("active");
//					updateT2Pair(t2Key, obj.key, t2Body, oldKV, isPair);
//				}).keypress(function( event ) {
//					if ( event.which == 13 ) {
//						t2Key.blur();
//					}
//				});
//			}
//
//			var t2Value = t2Body.find("td.pair_value > input");
//			var oldVV = t2Value.val();
//			t2Value.focus(function() {
//				obj.row.addClass("active");
//			}).blur(function() {
//				obj.row.removeClass("active");
//				updateT2Pair(t2Value, obj.key, t2Body, oldVV, isPair);
//			}).keypress(function( event ) {
//				if ( event.which == 13 ) {
//					t2Value.blur();
//				}
//			});
//			
//			var t2Add = t2Body.find("td.pair_control > button.button_plus");
//			t2Add.click(function() {
//				addRow(t2Add, obj.key, t2Body, isPair);
//			});
//			var t2Remove = t2Body.find("td.pair_control > button.button_minus");
//			t2Remove.click(function() {
//				removeRow(t2Remove, obj.key, t2Body, isPair);
//			});
//		}
//	}
//
//	getUI() {
//		return this.row;
//	}
//	
//	addValueSelectListener(f) {
//		var t1ValueSelect = this.colValue.find("> select");
//		t1ValueSelect.change(function() {
//			f(t1ValueSelect.val());
//		});
//	}
//	
//	updateValueOptions(options) {
//		if (typeof options === 'undefined') {
//			options = [];
//		} else if (!(typeof options === 'object')) {
//			options = [options];
//		}
//		var ot = "";
//		$.each(options, function(idx, op) {
//			ot += '<option value="' + op + '">'+ op + '</option>';
//		});
//		var t1ValueDatalist = this.colValue.find("> datalist");
//		if (t1ValueDatalist) {
//			t1ValueDatalist.html(ot);
//		}
//		if (options.length > 0) {
//			this.setValue(options[0]);			
//		} else {
//			this.setValue("");
//		}
//	}
//	
//	setValue(val) {
//		var t1Value = this.colValue.find("> input");
//		if (t1Value) {
//			t1Value.val(val);
//			this.updateNode(t1Value, this.key, val);
//		}
//	}
//	
//	updateNode($node, key, val) {
////		var curVal = _editorModel[_curCid][key];
//		const curVal = this.device.getValue(this.cid, key);
//		if (curVal.toString() != val.toString()) {
//			$node.addClass('changed');
//			this.device.setDirtyFlag();
//		} else {
//			$node.removeClass('changed');
//		}
////		_editorModel[_curCid][key] = val;
//		this.device.setValue(this.cid, key, val);
//	}
//}

class PropertyRow {
	
	isEditingDisabled = false;
	
	constructor(device, cid, key, val) {
//		this.device = device;
//		this.$row = $tr;
//		this.cid = cid;
//		this.configModel = device.model[cid];
////		var colKey = tr.find('.editable_key');
//		this.$colValue = $tr.find('.editable_value');
//		this.$t1Value = this.$colValue.find("> input");
//		this.$t1Select = this.$colValue.find("> select");
//		this.$t2Body = this.$colValue.find("tbody");
//		this.key = $tr.attr('key');
		this.device = device;
		this.cid = cid;
		this.key = key;
		if (typeof val === 'string' && val.includes("\"")) {
			this.val = escape(val);
		} else {
			this.val = val;
		}
		this.configModel = device.editorModel[cid];
	}
	
	init() {
		const html = '<tr class="editable_row" key="' + this.key + '"><td class="editable_key">' + this.key + '</td>'
//	+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair">name-value pair</option></select></td>'
			+ '<td class="editable_value"><input type="text" class="form-control" value="' + this.val + '"' + (this.isEditingDisabled ? ' disabled="true"' : '') + '></td></tr>';
//			+ '<td class="editable_value"><input type="text" class="form-control" value="' + this.val + '"></td></tr>';
		this.$row = $(html);
		this.$colValue = this.$row.find('.editable_value');
		this.$t1Value = this.$colValue.find("> input");
		this.$t1Select = this.$colValue.find("> select");
		this.$t2Body = this.$colValue.find("tbody");
		this.key = this.$row.attr('key');

		this.addEventHandler();
	}
	
	addEventHandler() {
		const obj = this;
		const colType = this.$row.find('.editable_type');
//		const sel = colType.find('select');
		const oldVal = obj.configModel[this.key];
		if (obj.$t1Value) {
			obj.$t1Value.focus(function() {
				obj.$row.addClass("active");
			}).blur(function() {
				obj.$row.removeClass("active");
				obj.updateNode(obj.$t1Value);
			}).keypress(function( event ) {
				if ( event.which == 13 ) {
					obj.$t1Value.blur();
				}
			});
		}
		
		if (obj.$t1Select) {
			obj.$t1Select.change(function() {
				obj.updateNode(obj.$t1Select);
			});
		}
		
	}

	getUI() {
		return this.$row;
	}
	
	addValueSelectListener(f) {
		var $t1ValueSelect = this.$colValue.find("> select");
		$t1ValueSelect.change(function() {
			f($t1ValueSelect.val());
		});
	}
	
	updateValueOptions(options) {
		if (typeof options === 'undefined') {
			options = [];
		} else if (!(typeof options === 'object')) {
			options = [options];
		}
		var ot = "";
		$.each(options, function(idx, op) {
			ot += '<option value="' + op + '">'+ op + '</option>';
		});
		const $t1ValueDatalist = this.$colValue.find("> datalist");
		if ($t1ValueDatalist) {
			$t1ValueDatalist.html(ot);
		}
		if (options.length > 0) {
			this.setValue(options[0]);			
		} else {
			this.setValue("");
		}
	}
	
	setValue(val) {
		if (this.$t1Value) {
			this.$t1Value.val(val);
			this.updateNode(this.$t1Value);
		}
	}
	
	updateNode($node) {
		const curVal = this.device.getValue(this.cid, this.key);
		const newVal = unescape($node.val());
		if (curVal.toString() != newVal.toString()) {
			$node.addClass('changed');
			this.device.setDirtyFlag();
		} else {
			$node.removeClass('changed');
		}
		this.device.setValue(this.cid, this.key, newVal);
	}
	
}

class OptionsPropertyRow extends PropertyRow {
	
	isEditingDisabled = false;
	
	constructor(device, cid, key, val, options) {
		super(device, cid, key, val);
		this.options = options;
	}
	
	init() {
		const obj = this;
		var ot = "";
		$.each(this.options, function(idx, op){
			var sl = op == obj.val ? " selected" : "";
			ot += '<option value="' + op + '"' + sl + '>'+ op + '</option>';
		});
		var html = '<tr class="editable_row" key="' + obj.key + '"><td class="editable_key">' + obj.key + '</td>'
			+ '<td class="editable_value">';
		html += '<input name="option_value" class="form-control" value="' + obj.val + '" list="' + obj.device.did + '_' + obj.cid + '_' + obj.key 
			+ '"/><datalist id="' + obj.device.did + '_' + obj.cid + '_' + obj.key + '">' + ot + '</datalist>';
		html += '</td></tr>';

		this.$row = $(html);
		this.$colValue = this.$row.find('.editable_value');
		this.$t1Value = this.$colValue.find("> input");
		this.$t1Select = this.$colValue.find("> select");
		this.$t2Body = this.$colValue.find("tbody");
		this.key = this.$row.attr('key');

		this.addEventHandler();
	}
}

class ConfigIDPropertyRow extends PropertyRow {
	
	isEditingDisabled = false;
	
	constructor(device, cid, key, val, options) {
		super(device, cid, key, val);
		this.options = options;
	}
	
	init() {
		const obj = this;
		var ot = "";
		$.each(obj.options, function(idx, op){
			var sl = op == obj.val ? " selected" : "";
			ot += '<option value="' + op + '"' + sl + '>'+ op + '</option>';
		});
		var html = '<tr class="editable_row" key="' + obj.key + '"><td class="editable_key">' + obj.key + '</td>'
			+ '<td class="editable_value">';
		html += '<select name="option_value" class="form-control">' + ot + '</select>';
		html += '</td></tr>';

		this.$row = $(html);
		this.$colValue = this.$row.find('.editable_value');
		this.$t1Value = this.$colValue.find("> input");
		this.$t1Select = this.$colValue.find("> select");
		this.$t2Body = this.$colValue.find("tbody");
		this.key = this.$row.attr('key');

		this.addEventHandler();
	}
	
	addEventHandler() {
		const obj = this;
		const colType = this.$row.find('.editable_type');
//		const sel = colType.find('select');
		const oldVal = obj.configModel[this.key];
		if (obj.$t1Value) {
			obj.$t1Value.focus(function() {
				obj.$row.addClass("active");
			}).blur(function() {
				obj.$row.removeClass("active");
				obj.updateNode(obj.$t1Value);
			}).keypress(function( event ) {
				if ( event.which == 13 ) {
					obj.$t1Value.blur();
				}
			});
		}
		
		if (obj.$t1Select) {
			obj.$t1Select.change(function() {
				obj.updateNode(obj.$t1Select);
				obj.device.changeConfig(obj.cid, obj.$t1Select.val());
			});
		}
		
	}

}

class DictPropertyRow extends PropertyRow {
	constructor(device, cid, key, val) {
		super(device, cid, key, val);
	}
	
	init() {
		var html = '<tr class="editable_row" key="' + this.key + '"><td class="editable_key">' + this.key + '</td>' 
			+ '<td class="editable_value">' + TABLE_TIER2_HEADER;
		$.each(this.val, function(subKey, subVal){
//				if ('key' in val) {
				html += EMPTY_ROW_PART1 + subKey + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
//				} else {
//					html += DISABLED_ROW_PART1 + '-' + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
//				}
		});
		html += '</tbody></table></td></tr>';

		this.$row = $(html);
		this.$colValue = this.$row.find('.editable_value');
		this.$t1Value = this.$colValue.find("> input");
		this.$t1Select = this.$colValue.find("> select");
		this.$t2Body = this.$colValue.find("tbody");
		this.key = this.$row.attr('key');

		this.addEventHandler();
	}
	
	addEventHandler() {
		super.addEventHandler();
		const obj = this;
		const colType = this.$row.find('.editable_type');
//		const sel = colType.find('select');
		const oldVal = obj.configModel[this.key];
		
		const $t2Key = obj.$t2Body.find("td.pair_key > input");
		const oldKV = $t2Key.val();
		$t2Key.focus(function() {
			this.$row.addClass("active");
		}).blur(function() {
			this.$row.removeClass("active");
			obj.updateT2Pair($(this), oldKV, isPair);
		}).keypress(function( event ) {
			if ( event.which == 13 ) {
				$t2Key.blur();
			}
		});

		var $t2Value = obj.$t2Body.find("td.pair_value > input");
		var oldVV = $t2Value.val();
		$t2Value.focus(function() {
			obj.$row.addClass("active");
		}).blur(function() {
			obj.$row.removeClass("active");
			obj.updateT2Pair($(this), oldVV);
		}).keypress(function( event ) {
			if ( event.which == 13 ) {
				t2Value.blur();
			}
		});
		
		var $t2Add = obj.$t2Body.find("td.pair_control > button.button_plus");
		$t2Add.click(function() {
			obj.addRow($(this), obj.$t2Body);
		});
		var $t2Remove = obj.$t2Body.find("td.pair_control > button.button_minus");
		$t2Remove.click(function() {
			obj.removeRow($(this), obj.$t2Body);
		});
	}

//	updateValueOptions(options) {
//		if (typeof options === 'undefined') {
//			options = [];
//		} else if (!(typeof options === 'object')) {
//			options = [options];
//		}
//		var ot = "";
//		$.each(options, function(idx, op) {
//			ot += '<option value="' + op + '">'+ op + '</option>';
//		});
//		const $t1ValueDatalist = this.$colValue.find("> datalist");
//		if ($t1ValueDatalist) {
//			$t1ValueDatalist.html(ot);
//		}
//		if (options.length > 0) {
//			this.setValue(options[0]);			
//		} else {
//			this.setValue("");
//		}
//	}
	
	updateT2Pair($node, oldVal) {
		const obj = this;
		const $tr = $node.parent().parent();
		const $key = $tr.find("td.pair_key > input");
		const $value = $tr.find("td.pair_value > input");
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
		console.log($node.val() + " : " + oldVal + " : " + isPair + " : " + isValid);
		if (isValid) {
			if ($node.val() != oldVal) {
				$node.addClass("changed");
			} else {
				$node.removeClass("changed");
			}
			
//			var curVal = obj.device.editorModel[obj.cid][obj.key];
			const curVal = obj.device.getValue(obj.cid, obj.key);
			var isChanged = true;
			if (typeof curVal === 'object') {
				if (curVal.hasOwnProperty(kv)) {
					const cv = curVal[kv];
					if (cv == vv) {
						isChanged = false;
					}
				} else {
					isChanged = $node.val() != oldVal;
				}
			}
			if (isChanged) {
				obj.device.setDirtyFlag();
				const newVal = {};
				const $trs = tbody.find('tr');
				$trs.each(function() {
					const rk = $(this).find('td.pair_key > input').val();
					const rv = $(this).find('td.pair_value > input').val();
					newVal[rk] = rv;
					console.log('add ' + rk + ":" + rv);
				});
				obj.device.setValue(obj.cid, obj.key, newVal)
			} 
		}
	}

	addRow($bt) {
		const obj = this;
		const nRow = $(EMPTY_ROW_PART1 + EMPTY_ROW_PART2 + EMPTY_ROW_PART3);
		const cRow = $bt.parent().parent();
		nRow.insertAfter(cRow);
		
		const $t2Key = nRow.find("td.pair_key > input");
		const oldKV = $t2Key.val();

		$t2Key.focus(function() {
			nRow.addClass("active");
		}).blur(function() {
			nRow.removeClass("active");
			obj.updateT2Pair($(this), oldKV);
		}).keypress(function( event ) {
			if ( event.which == 13 ) {
				$(this).blur();
			}
		});

		const $t2Value = nRow.find("td.pair_value > input");
		const oldVV = $t2Value.val();
		$t2Value.focus(function() {
			nRow.addClass("active");
		}).blur(function() {
			nRow.removeClass("active");
			obj.updateT2Pair($(this), oldVV);
		}).keypress(function( event ) {
			if ( event.which == 13 ) {
				$(this).blur();
			}
		});
		
		const $t2Add = nRow.find("td.pair_control > button.button_plus");
		$t2Add.click(function() {
			obj.addRow($(this));
		});
		const $t2Remove = nRow.find("td.pair_control > button.button_minus");
		$t2Remove.click(function() {
			obj.removeRow($(this));
		});

		$t2Key.focus();
	}

	removeRow($bt) {
		const obj = this;
		
		const $cRow = $bt.parent().parent();
		$cRow.remove();
		
		var newVal = {};
		var $trs = obj.$t2Body.find('tr');
		$trs.each(function() {
			const rk = $(this).find('td.pair_key > input').val();
			const rv = $(this).find('td.pair_value > input').val();
			newVal[rk] = rv;
		});
		obj.device.setValue(obj.cid, obj.key, newVal);
	}

}

class ArrayPropertyRow extends PropertyRow {
	constructor(device, cid, key, val) {
		super(device, cid, key, val);
	}
	
	init() {
		var html = '<tr class="editable_row" key="' + this.key + '"><td class="editable_key">' + this.key + '</td>' 
			+ '<td class="editable_value">' + TABLE_TIER2_HEADER;
		$.each(this.val, function(subKey, subVal){
			html += DISABLED_ROW_PART1 + '-' + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
		});
		html += '</tbody></table></td></tr>';

		this.$row = $(html);
		this.$colValue = this.$row.find('.editable_value');
		this.$t1Value = this.$colValue.find("> input");
		this.$t1Select = this.$colValue.find("> select");
		this.$t2Body = this.$colValue.find("tbody");
		this.key = this.$row.attr('key');

		this.addEventHandler();
	}
	
	addEventHandler() {
		super.addEventHandler();
		const obj = this;
		const colType = this.$row.find('.editable_type');
//		const sel = colType.find('select');
		const oldVal = obj.configModel[this.key];
		
		const $t2Key = obj.$t2Body.find("td.pair_key > input");
		const oldKV = $t2Key.val();

		var $t2Value = obj.$t2Body.find("td.pair_value > input");
		var oldVV = $t2Value.val();
		$t2Value.focus(function() {
			obj.$row.addClass("active");
		}).blur(function() {
			obj.$row.removeClass("active");
			obj.updateT2Pair($(this), oldVV);
		}).keypress(function( event ) {
			if ( event.which == 13 ) {
				t2Value.blur();
			}
		});
		
		var $t2Add = obj.$t2Body.find("td.pair_control > button.button_plus");
		$t2Add.click(function() {
			obj.addRow($(this), obj.$t2Body);
		});
		var $t2Remove = obj.$t2Body.find("td.pair_control > button.button_minus");
		$t2Remove.click(function() {
			obj.removeRow($(this), obj.$t2Body);
		});
	}

//	updateValueOptions(options) {
//		if (typeof options === 'undefined') {
//			options = [];
//		} else if (!(typeof options === 'object')) {
//			options = [options];
//		}
//		var ot = "";
//		$.each(options, function(idx, op) {
//			ot += '<option value="' + op + '">'+ op + '</option>';
//		});
//		const $t1ValueDatalist = this.$colValue.find("> datalist");
//		if ($t1ValueDatalist) {
//			$t1ValueDatalist.html(ot);
//		}
//		if (options.length > 0) {
//			this.setValue(options[0]);			
//		} else {
//			this.setValue("");
//		}
//	}
	
	updateT2Pair($node, oldVal) {
		const obj = this;
		const $tr = $node.parent().parent();
		const $key = $tr.find("td.pair_key > input");
		const $value = $tr.find("td.pair_value > input");
		var isValid = true;
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
			
//			var curVal = obj.device.editorModel[obj.cid][obj.key];
			const curVal = obj.device.getValue(obj.cid, obj.key);
			var isChanged = $node.val() != oldVal;
			if (isChanged) {
				obj.device.setDirtyFlag();
				const newVal = [];
				const $trs = obj.$t2Body.find('tr');
				$trs.each(function() {
					const rv = $(this).find('td.pair_value > input').val();
					newVal.push(rv);
					console.log('push ' + rv);
				});
				obj.device.setValue(obj.cid, obj.key, newVal);
			} 
		}
	}

	addRow($bt) {
		const obj = this;
		const nRow = $(DISABLED_ROW_PART1 + '-' + EMPTY_ROW_PART2 + EMPTY_ROW_PART3);
		const cRow = $bt.parent().parent();
		nRow.insertAfter(cRow);
		
		const $t2Key = nRow.find("td.pair_key > input");
		const oldKV = $t2Key.val();

		const $t2Value = nRow.find("td.pair_value > input");
		const oldVV = $t2Value.val();
		$t2Value.focus(function() {
			nRow.addClass("active");
		}).blur(function() {
			nRow.removeClass("active");
			obj.updateT2Pair($(this), oldVV);
		}).keypress(function( event ) {
			if ( event.which == 13 ) {
				$(this).blur();
			}
		});
		
		const $t2Add = nRow.find("td.pair_control > button.button_plus");
		$t2Add.click(function() {
			obj.addRow($(this));
		});
		const $t2Remove = nRow.find("td.pair_control > button.button_minus");
		$t2Remove.click(function() {
			obj.removeRow($(this));
		});

		$t2Value.focus();
	}

	removeRow($bt) {
		const obj = this;
		
		const $cRow = $bt.parent().parent();
		$cRow.remove();
		
		newVal = [];
		var trs = obj.$t2Body.find('tr');
		trs.each(function() {
			const rv = $(this).find('td.pair_value > input').val();
			newVal.push(rv);
		});
		obj.device.setValue(obj.cid, obj.key, newVal);
	}

}

class SearchWidget {
	constructor($textInput) {
		this.$textInput = $textInput;
		this.$msgPop = $textInput.parent().find('.messagepop');
		this.curSel = -1;
		this.lastText = null;
	}
		
	show($li) {
		var name = $li.attr('name');
		var path = $li.attr('path');
//		var did = $li.attr('did');
		const parts = path.split('/');
		const type = parts[0];
		const did = parts[1];
		const cid = parts[2];
		this.$msgPop.hide();
//		$('.class_ul_folder').addClass('class_ul_hide');
//		$('#ul_mc_' + did).removeClass('class_ul_hide');
//		$('.class_ul_folder > li').removeClass('active');
//		$('#li_' + did + '_' + cid).addClass('active');
//		loadDeviceConfig(did, cid);
		if (type == TYPE_INST) {
			const device = _instModel.getDevice(did);
			device.loadConfig(cid);
		} 
		this.$textInput.blur();
	}
	
	
	doSearch() {
		var target = '';
		this.curSel = -1;

		var html = '';
//		var found = {};
		const obj = this;
		$.each(_instModel.configs, function(name, pair) {
			var word = obj.$textInput.val().toLowerCase();
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
						_inst + ': ' + name + ' => ' + desc + '</span></li>';
			} else {
				var desc = pair[3];
				if (desc && desc.toLowerCase().indexOf(word) >= 0) {
//					found[name] = path;
					var path = pair[0];
					var did = pair[1];
					desc = '(' + desc + ')';
					html += '<li class="messageitem" href="#" name="' + name + '" path="' + path + '" did="' + did + '"><span class="widget_text">' + 
						_inst + ': ' + name + ' => ' + desc + '</span></li>';
				} 
			}
		});
		

//		$.each(found, function(name, path) {
//			html += '<li class="messageitem" href="#" name="' + name + '"><span class="widget_text" title="' + path + '">' + name + '</span></li>';
//		});
//		$m.css('width', $t.width() + 16);
		this.$msgPop.html(html);
		this.$msgPop.show();
		this.$msgPop.find('li:not(.groupname)').mousedown(function() {
			obj.show($(this));
		});
	}
	
	init() {
		const obj = this;
		this.$textInput.off();
		this.$textInput.keyup(function(e) {
			if(e.keyCode == 13) {
				if (obj.curSel >= 0) {
					obj.show(obj.$msgPop.find('li:not(.groupname)').eq(obj.curSel));
				} 
				e.preventDefault();
				return false;
			} else if (e.keyCode == 38) {
				var lis = obj.$msgPop.find('li.messageitem');
				if (lis.length > 0) {
					if (obj.curSel >= lis.length) {
							lis.removeClass('selected');
							lis.eq(lis.length - 1).addClass('selected');
							obj.curSel = lis.length - 1;
					} else if (obj.curSel > 0) {
							lis.removeClass('selected');
							obj.curSel--;
							lis.eq(obj.curSel).addClass('selected');
					} else if (obj.curSel == 0) {
						lis.removeClass('selected');
						obj.curSel = -1;
					}
				}				
			} else if (e.keyCode == 40) {
				var lis = obj.$msgPop.find('li.messageitem');
				if (lis.length > 0) {
					if (obj.curSel < 0 || obj.curSel >= lis.length) {
							lis.removeClass('selected');
							lis.eq(0).addClass('selected');
							obj.curSel = 0;
					} else {
						if (obj.curSel < lis.length - 1) {
							lis.removeClass('selected');
							obj.curSel++;
							lis.eq(obj.curSel).addClass('selected');
						}
					}
				}
			} else if (e.keyCode == 27) {
				obj.$msgPop.hide();
				obj.curSel = -1;
			} else {
				var newText = obj.$textInput.val().replace(/^\s+|\s+$/gm,'');
				if (newText != obj.lastText) {
					if (newText.length >= 2) {
						obj.doSearch();
					} else {
						obj.$msgPop.html('');
						obj.$msgPop.hide();
						obj.curSel = -1;
					}
					obj.lastText = newText;
				}
			}
		});
		this.$textInput.blur(function() {
			var lis = obj.$msgPop.find('li.messageitem');
			lis.removeClass('selected');
			obj.$msgPop.hide();
			obj.curSel = -1;
		});
		this.$textInput.focus(function() {
			if (obj.$msgPop.find('li').length > 0) {
				obj.$msgPop.show();
			} else {
				var newText = obj.$textInput.val().replace(/^\s+|\s+$/gm,'');
				if (newText.length >= 3) {
					obj.doSearch();
				}
			} 
		});
	};
};

class LoadingLabel {
	
	constructor($label)
	{
		this.label = $label;
		$label.hide();
	}
	
	init() {
		const obj = this;
		$(document).ajaxStart(function () {
			obj.label.show();
		}).ajaxStop(function () {
			obj.label.hide();
		});
	}
}

class SaveButton {
	constructor($button)
	{
		this.$button = $button;
	}
	
	init() {
		const obj = this;
		this.$button.click(function() {
//			if (_curDevice) {
//				_curDevice.save();
//			} else {
//				StaticUtils.showWarning('Please select a device for saving. Saving is per-device based.')
//				obj.close();
//			}
			obj.run();
		});
	
//		this.$textInput.keypress(function(event) {
//			if ( event.which == 13 ) {
//				obj.run();
//			}
//		});
	}
	
	run() {
		_instModel.save();
	}
	
	reset() {
//		this.$textInput.val("");
	}
	
	close() {
//		this.dialog.modal('hide');
	}
}

class RemoveModal {

	toRemove;

	constructor(id)
	{
		this.id = id;
		this.$modelUi = $('#' + id);
		this.$confirm = this.$modelUi.find('#' + this.id + '_confirm');
		this.$textInput = this.$modelUi.find('input#' + this.id + '_text');
	}
	
	init() {
		const obj = this;
		this.$confirm.click(function() {
			obj.remove();
		});
	
		this.$textInput.keypress(function(event) {
			if ( event.which == 13 ) {
				obj.remove();
			}
		});
	}
	
	remove() {
		const obj = this;
		const did = this.toRemove;
		if (!did) {
			StaticUtils.showError("failed to remove: no device selected");
			return;
		}

		const device = _instModel.getDevice(did);
		var saveMsg = obj.$textInput.val().replace(/^\s+|\s+$/gm,'');
		
		try {
			device.remove(saveMsg);
		} catch (e) {
			StaticUtils.showError(e);
		} finally {
			obj.close();
		}
		
		
//		var deviceModel = _instModel.getDeviceModel(did);
//		if (deviceModel == null) {
//			showMsg("failed to remove: device does not found", 'danger');
//			return;
//		}
//		var datype = deviceModel[KEY_DATYPE];
//		if (datype != "C") {
//			showMsg("failed to remove: device is not a composite one", 'danger');
//			return;
//		}

	}
	
	close() {
		this.$modelUi.modal('hide');
	}
	
	open() {
		this.$modelUi.modal('show');
	}
	
	set toRemove(did) {
		this.toRemove = did;
	}
}

class ResetButton {
	constructor($button)
	{
		this.$button = $button;
	}
	
	init() {
		this.$button.click(function(){
			_instModel.reset();
		});
	}
}

var addPageTitle = function(){
	var titleString = "Instrument Configuration - " + _inst.charAt(0).toUpperCase() + _inst.slice(1);
	$(document).attr("title", titleString);
	var subTitle = _inst.toUpperCase() + " Configuration";
	$('#id_span_side_title').html('<h5>' + subTitle + '</h5>');
};

class HomeButton {
	constructor($button) {
		this.$button = $button;
	}
	
	init() {
		this.$button.click(function() {
			
			var okToGo = true;
			if (_curDevice != null) {
				if (_curDevice.isDirty()) {
					okToGo = confirm('You have unsaved changes in the current device. If you load another device, your change will be lost. Do you want to continue?');
				}
			}

			if (okToGo) {
				if (_curDevice != null) {
					_curDevice.hide();
					_curDevice = null;
				}
				_title.text('Please use the side bar to select a device configuration.');
				_errorReport.show();
				_historyBar.reload();
			} else {
				return;
			}
		});
	}
}

class HistoryBlock {
	constructor($main, $side) {
		this.$main = $main;
		this.$side = $side;
//		this.$holder = $('.class_div_commit_item');
		this.enabled = false;
		this.commits = [];
	}
	
	empty() {
		this.$side.find('.class_div_commit_item').remove();
	}
	
	reload() {
		if (this.enabled) {
			this.empty();
			const obj = this;
			var url = URL_PREFIX + 'confighistory?inst=' + _inst;
//			if (_curDevice) {
//				url += '&path=' + _curDevice.did;
//			}
			url += '&' + Date.now();
			$.get(url, function(data) {
				data = $.parseJSON(data);
				$.each(data, function(index, version) {
					const commit = new CommitItem(version, index);
					obj.commits.push(commit);
//					.append(commit.getControl());
				});
				url = URL_PREFIX + 'adithistory?inst=' + _inst + '&' + Date.now();
				$.get(url, function(data) {
					data = $.parseJSON(data);
					$.each(data, function(index, version) {
						const commit = new CommitItem(version, index);
						var exists = false;
						obj.commits.forEach((c) => {
							if (c.id == commit.id) {
								exists = true;
								return;
							}
						});
						if (!exists) {
							obj.commits.push(commit);
						}
					});
					
					obj.commits.sort((a, b) => b.ts - a.ts);
					obj.commits.forEach((commit) => {
						commit.init(obj.$side);
					});

				}).fail(function() {
					obj.$side.prepend($('<span class="alert alert-danger">failed to get additional config history.</span>'));
				});

			}).fail(function() {
				obj.$side.prepend($('<span class="alert alert-danger">failed to get SE configuration history.</span>'));
			});
		}
	};
	
	toggle() {
		this.enabled = !this.enabled;
		if (this.enabled) {
			this.$main.css('width', '70%');
			this.$side.show();
			var bodyHeight = $(window).height() - 150;
			var mainHeight = $("#id_div_main_area").height();
			var newHeight = bodyHeight > mainHeight ? bodyHeight : mainHeight;
			this.$side.height(newHeight);
//			this.reload();
		} else {
			this.$side.hide();
			this.$main.css('width', '100%');
		}
	};
};

class CommitItem {
	
	$control;
	
	constructor(commit, index) {
		this.commit = commit;
		this.index = index;
		this.ts = Number(commit["timestamp"]);
		this.id = commit["id"];
	}
	
	init($parentUi) {
		var id = this.commit["id"];
		var name = this.commit["name"];
		var message = this.commit["message"];
		var timestamp = this.commit["timestamp"];
		var btText;
		var star;
		if (this.index == 0) {
			btText = "Current version";
			star = "* ";
		} else {
			btText = "Load this version";
			star = "";
		}
		this.$control = $('<div/>').addClass("class_div_commit_item");
		this.$control.append('<span class="badge badge-secondary class_span_commit_timestamp">' + star + StaticUtils.getTimeString(timestamp) + '</span>');
		this.$control.append('<span class="class_span_commit_message">' + message + '</span>');
//		var button = $('<span class="class_span_commit_button"><button class="class_button_load_commit btn btn-sm btn-block btn-outline-primary">' + btText + '</button></span>');
//		this.$control.append(button);
//		button.find('button').click(function() {
//			if (_curDevice == null) {
//				alert('Please select a device first. Loading history version is only supported on a per-device base.')
//				return;
//			}
//			var url = URL_PREFIX + 'configload?inst=' + _inst + '&version=' + encodeURI(name) + "&" + Date.now();
//			$.get(url, function(data) {
//				try{
//					_curDevice.fromHistory(data);
//				} catch (e) {
//					alert('failed to load model from the commit, ' + e);
//					return;
//				}
//			}).fail(function(e) {
//				alert('failed to load the version, ' + e.statusText);
//			});
//		});
		$parentUi.append(this.$control);
	}
	
};

//class Utils {
//	
//	static getTimeString(timestamp) {
//		var a = new Date(timestamp * 1000);
//		  var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
//		  var year = a.getFullYear();
//		  var month = months[a.getMonth()];
//		  var date = a.getDate();
//		  var hour = a.getHours();
//		  var min = a.getMinutes();
//		  var sec = a.getSeconds();
//		  var time = date + ' ' + month + ' ' + year + ' ' + hour + ':' + min + ':' + sec ;
//		  return time;
//	}
//	
//}

$(document).ready(function() {
	if (!_inst) {
		window.location = "home.html";
	}
	
	$('#id_a_signout').click(function() {
		signout("instrumentConfig.html?inst=" + _inst);
	});
	
	StaticUtils.addPageTitle();
	
	_errorReport = new ErrorReportUi();
	_errorReport.createUi();
	
	const homeButton = new HomeButton($('#id_span_side_home'));
	homeButton.init();

	const loadingLabel = new LoadingLabel($('#id_span_waiting'));
	loadingLabel.init();
	_saveButton = new SaveButton($('#id_button_save'));
	_saveButton.init();
	
	_resetButton = new ResetButton($('#id_button_reset'));
	_resetButton.init();
	
	_removeModal = new RemoveModal('id_modal_deleteDialog');
	_removeModal.init();
	
	_additionalModel.load();
//	_pdbModel.load();
//	_cdbModel.load();
//	_instModel.load();
	
	_historyBar = new HistoryBlock($("#id_div_main_area"), $('#id_div_right_bar'));
	$('#id_button_history').click(function(){
		_historyBar.toggle();
	})
	
	$('#id_button_reload_history').click(function() {
		_historyBar.reload();
	});
	_historyBar.toggle();
	_historyBar.reload();
	
	const searchWidget = new SearchWidget($('#id_input_search_text'));
	searchWidget.init();
});