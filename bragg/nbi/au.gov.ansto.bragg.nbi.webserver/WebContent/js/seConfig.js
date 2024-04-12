const TITLE_TEXT = "Sample Environment Device Configuration for ";
const PAR_INSTRUMENT_ID = "inst";

const URL_PREFIX = "seyaml/";
const KEY_DATYPE = "datype";
const PROPERTY_KEYWORDS = [
	KEY_DATYPE
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
const KEYS_COPIED_FROM_DB = [
	"ip",
	"port",
	"desc",
];
const _option_prop = ["config_id", "ip", "port"];
const TABLE_TIER1_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="34%">Key</th><th width="66%">Value</th></tr></thead><tbody>';
const TABLE_TIER2_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="40%">Key</th><th width="40%">Value</th><th width="20%"></th></tr></thead><tbody>';
const EMPTY_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" value="';
const EMPTY_ROW_PART2 = '"></td><td class="pair_value"><input type="text" class="form-control" value="';
const EMPTY_ROW_PART3 = '"></td><td class="pair_control input-group-btn"><button type="button" class="btn btn-outline-primary button_plus">+</button><button type="button" class="btn btn-outline-primary button_minus">-</button></td></tr>';
const DISABLED_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" disabled value="';

const ID_PROP_DRIVER = "driver";
const ID_PROP_CONFIGID = "config_id";
const ID_PROP_IP = "ip";
const ID_PROP_ID = "id";
const ID_PROP_NAME = "name";
const ID_PROP_PORT = "port";

const KEY_DEVICE_DESC = "desc";
const KEY_DEVICE_DRIVER = "driver";
const HTML_HIDDEN_FILL_DIV = '<div class="div_fill div_hidden"/>';
const HTML_STATIC_MAIN_DIV = '<div class="div_fill div_static div_hidden"/>';
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

var _curDevice;
var _saveButton;
var _resetButton;

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

var showError = function(errorMsg, timeLast) {
	showMsg(errorMsg, 'danger', timeLast);
}

var showWarning = function(warnMsg, timeLast) {
	showMsg(warnMsg, 'warning', timeLast);
}

class AbstractDeviceModel {
	#model;
	#configs;
	#firstConfigs;
	#configNamesOfDevice;
	#devices = {};

	constructor() {
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
		return this.model[did];
	}
	
	getDevice(did) {
		return this.devices[did];
	}
	
	setDevice(did, device) {
		this.devices[did] = device;
	}
	
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
//			$("#id_div_sidebar").empty();
//			showModelInSidebar();
		}).fail(function(e) {
			if (e.status == 401) {
				alert("sign in required");
				window.location = 'signin.html?redirect=seConfig.html';
			} else {
				alert(e.statusText);
			}
		});
	}
		
	createUi() {}
	
	addDevice(did, device) {}
	
	removeDevice(did) {
		const obj = this;
		if (did in this.model) {
			const deviceModel = this.model[did];
			$.each(deviceModel, function(cid, cfg) {
				if (!(PROPERTY_KEYWORDS.includes(cid))) {
					var name = did + ":" + cid
					delete obj.configs[name];
				}
			});
			delete this.model[did];
			delete this.firstConfigs[did];
			delete this.configNamesOfDevice[did];
			delete this.devices[did];
		} else {
			throw new Error('device not found:' + did);
		}
	}
}

class InstrumentModel extends AbstractDeviceModel {

	constructor(){
		super();
	}

	get $menuBar() {
		return $('#id_div_sidebar');
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

	addDevice(did, deviceModel) {
		if (did in this.model) {
			throw new Error('device already exists: ' + did);
		}
		const obj = this;
		const names = [];
		const newModel = $.extend(true, {}, deviceModel);
		this.model[did] = newModel;
		$.each(newModel, function(cid, cfg) {
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
		
		const device = new InstrumentDevice(did, newModel, obj.$menuBar);
		device.createMenuUi();
		obj.setDevice(did, device);
		device.load();
	}
	
}
const _instModel = new InstrumentModel();

class DBModel extends AbstractDeviceModel {

	constructor(){
		super();
	}

	get $menuBar() {
		return $('#id_div_sidebuttom');
	}
	
	get type() {
		return TYPE_DB;
	}
	
	get url() {
		return URL_PREFIX + 'sedb';
	}

	createUi() {
		const obj = this;
		$.each(this.model, function(did, deviceModel){
			const device = new DBDevice(did, deviceModel, obj.$menuBar);
			device.createMenuUi();
			obj.setDevice(did, device);
		});
	}

}
const _dbModel = new DBModel();

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
		var okToGo = true;
		if (_curDevice != null) {
			if (_curDevice.id != this.id && _curDevice.isDirty()) {
				okToGo = confirm('You have unsaved changes in the current device. If you load another device, your change will be lost. Do you want to continue?');
			}
		}
		return okToGo;
	}
	
	load() {}

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
	
	createMenuUi() {
		var datype = this.model["datype"];
		if (datype != "C") {
			return;
		}
		
		const obj = this;
		var faIcon;
		var html = '<div class="class_a_mc" href="#"><h6 class="sidebar-subheading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' 
				+ '<span class="class_span_mc_name" did="' + this.did + '" datype="' + datype + '">' + this.did + '</span>';
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
		if (datype != "C") {
			html += '<li id="li_menu_' + obj.did + '_NEW_DEVICE' + '" class="nav-item class_li_subitem">'
				+ '<a class="nav-link class_a_add_axis" did="' + obj.did + '" href="#"><i class="fas fa-plus"> </a></li>';
		}
		html += "</ul>";
		this.$menuHeader = $(html);
		this.$menuHeader.find('span.class_span_mc_name').click(function() {
			obj.load();
		});
		
		this.$menuHeader.find('span.class_span_mc_icon').click(function() {
			try {
				_instModel.addDevice(obj.did, _dbModel.getDeviceModel(obj.did));
			} catch (e) {
				console.error(e);
				showMsg(e, 'danger');
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
			
			const obj = this;
			_title.text(obj.did + " (Database device preview)");

			_editorTitle.text('If you need to change the DB device configuration, use the Device Database page.');

			var ct = 0;
			
//			_tabs.empty();
			this.rootEditor.show();
		}
	}
	
	save() {
		_saveButton.close();
		showMsg("Can not change the DB device. Please use SE Database page to edit DB devices. ", 'danger');
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
	
	createMenuUi() {
		const obj = this;
		var datype = this.model["datype"];
		if (datype == null) {
			return;
		}
		var faIcon;
		var html = '<div class="class_a_mc" href="#"><h6 class="sidebar-subheading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' 
				+ '<span class="class_span_mc_name" did="' + this.did + '" datype="' + datype + '"><i class="fas fa-caret-down"></i> ' + this.did + '</span>';
		if (datype != null && datype in DATYPE_ICON) {
			faIcon = DATYPE_ICON[datype];
			html += '<span class="class_span_mc_icon"><i class="fas fa-' + faIcon + '"></i> </span>';
		}
		if (datype == "C") {
			html += '<span class="class_span_mc_control"><i class="fas fa-minus"></i> </span>';
		}
		html += '</h6></div>';
		
		this.$menuHeader = $(html);
		this.$menuHeader.find('span.class_span_mc_name').click(function() {
			obj.load();
		});
		
		this.$menuHeader.find('span.class_span_mc_control').click(function() {
			_removeModal.toRemove = obj.did;
			_removeModal.open();
		});
		
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

			_editorTitle.text('Use the tab menu to load configuration of sub-device');
			this.curCid = null;
			$.each(Object.values(this.configEditors), function(idx, propTable) {
				propTable.hide();
			});
			this.tabUi.show();
			this.rootEditor.show();
			_historyBar.reload();
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
			var config = obj.editorModel[cid]; 
//			_tabs.empty();
			var datype = obj.editorModel[KEY_DATYPE];
			
			
			var desc = config[KEY_DEVICE_DESC];
			if (desc == null) {
				desc = config[KEY_DEVICE_DRIVER];
			}
			_title.text(obj.did + ":" + cid + " (" + desc + ")");

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
			
			_historyBar.reload();
		}
	}
	
	setMenuItemActive(cid) {
		this.$menuUl.find('li.class_li_subitem').removeClass('active');
		if (cid) {
			this.configMenuDict[cid].setActive();
		}
		this.tabUi.setActive(cid);
	}
	
	save() {
		const obj = this;
		var url = URL_PREFIX + 'configsave?inst=' + _inst + '&msg=';
		var saveMsg = $('#id_input_saveMessage').val().replace(/^\s+|\s+$/gm,'');
		if (saveMsg.length > 0) {
			url += encodeURI(saveMsg);
		}
		url += "&" + Date.now();
		_saveButton.close();
		var text = JSON.stringify(this.editorModel);
		$.post(url,  {did:obj.did, model:text}, function(data) {
//			data = $.parseJSON(data);
			try {
				if (data["status"] == "OK") {
					showMsg("Saved in the server.");
					$('td.editable_value input.changed').removeClass('changed');
//					if (_deviceItem != null) {
//						_deviceItem.init = false;
//						_deviceItem = null;
//					}
					$.extend(true, obj.model, obj.editorModel);
					obj.clearDirtyFlag();
					_historyBar.reload();
//					setTimeout(_historyBar.reload, 3000)
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
			_saveButton.close();
		});
	}
	
	reset() {
		this.clearDirtyFlag();
		$.extend(true, this.editorModel, this.model);
		$.each(Object.values(this.configEditors), function(idx, editor) {
			editor.dispose();
		});
		this.configEditors = {};
		if (this.curCid) {
			this.loadConfig(this.curCid);
		} else {
			this.load();
		}
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
	
	remove(saveMsg) {
		const obj = this;
		var url = URL_PREFIX + 'configremove?inst=' + _inst + '&did=' + obj.did + '&msg=';
		if (saveMsg.length > 0) {
			url += encodeURI(saveMsg);
		}
//		if (_versionId) {
//			url += "&version=" + encodeURI(_versionId);
//		}
		url += "&" + Date.now();
		$.get(url, function(data) {
			try {
				if (data["status"] == "OK") {
					obj.clearDirtyFlag();
					showMsg("Removed successfully in the server.");
//					var $ul = $('#ul_mc_' + did);
//					$ul.prev().remove();
//					$ul.remove();
//					loadDeviceConfig(null, null);
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
//					setTimeout(_historyBar.reload, 3000)
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
		this.dModel = _dbModel.getDeviceModel(this.driverId);
		this.configRow = null;
		this.ipRow = null;
		this.portRow = null;
	}
	
	createUi() {
		var $table = $(TABLE_TIER1_HEADER + '</tbody></table>');
		var $tbody = $table.find('tbody');
		var object = this;
//		var cid = this.cid;
//		var did = this.driverId;
		var subConfigId = this.cModel[ID_PROP_CONFIGID];
		if (typeof this.dModel === 'undefined') {
			$.each(this.cModel, function(key, val){
				if (_option_prop.includes(key)) {
					var pRow = object.createRow(key, val);
					$tbody.append(pRow.getUI());
				} else {
					var pRow = object.createRow(key, val, null, true);
					$tbody.append(pRow.getUI());
				}
			});
		} else {
			if (subConfigId in object.dModel) {
				$.each(this.cModel, function(key, val){
					if (key == ID_PROP_CONFIGID) {
						var options = _dbModel.getConfigNamesOfDevice(object.driverId);
						object.configRow = object.createRow(key, val, options, true);
						$tbody.append(object.configRow.getUI());
					} else if (key == ID_PROP_IP) {
						var options = object.dModel[subConfigId][ID_PROP_IP];
						object.ipRow = object.createRow(key, val, options);
						$tbody.append(object.ipRow.getUI());
					} else if (key == ID_PROP_PORT) {
						var options = object.dModel[subConfigId][ID_PROP_PORT];
						object.portRow = object.createRow(key, val, options);
						$tbody.append(object.portRow.getUI());
					} else {
						var pRow = object.createRow(key, val, null, true);
						$tbody.append(pRow.getUI());
					}
				});
			} else {
				const obj = this;
				$.each(this.cModel, function(key, val){
					if (key == ID_PROP_CONFIGID) {
						var options = obj.device.getConfigArray();
						object.configRow = obj.createRow(key, val, options);
						$tbody.append(object.configRow.getUI());
					} else if (_option_prop.includes(key)) {
						var pRow = obj.createRow(key, val);
						$tbody.append(pRow.getUI());
					} else {
						var pRow = obj.createRow(key, val, null, true);
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
		this.$editorUi.append($table);
		_editor.append(this.$editorUi);
		this.init = true;
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
		var html;
		if (typeof val === 'object') {
			html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>' 
//			+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair" selected>name-value pair</option></select></td>'
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
//				+ '<td class="editable_type"></td>' 
				+ '<td class="editable_value">';
				if (editingDisabled) {
					html += '<select name="option_value" class="form-control">' + ot + '</select>';
				} else {
					html += '<input name="option_value" class="form-control" value="' + val + '" list="' + key + '"/><datalist id="' + key + '">' + ot + '</datalist>';
				}
				html += '</td></tr>';
			} else {
				html = '<tr class="editable_row" key="' + key + '"><td class="editable_key">' + key + '</td>'
//				+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair">name-value pair</option></select></td>'
				+ '<td class="editable_value"><input type="text" class="form-control" value="' + val + '"' + (editingDisabled ? ' disabled' : '') + '></td></tr>';
			}
		}
		var $row = $(html);
		return new PropertyRow(this.device, cid, $row);
	};

}

class PropertyRow {
	constructor(device, cid, tr) {
		this.device = device;
		this.row = tr;
		this.cid = cid;
//		var colKey = tr.find('.editable_key');
		this.colValue = tr.find('.editable_value');
		this.key = tr.attr('key');
		this.addEventHandler();
	}
	
	addEventHandler() {
		const obj = this;
		const colType = this.row.find('.editable_type');
		const sel = colType.find('select');
		const oldVal = this.device.model[this.cid][this.key];
		var newTextHtml;
		var newPairHtml;
		if (typeof oldVal === 'object') {
			newPairHtml = TABLE_TIER2_HEADER;
			$.each(oldVal, function(subKey, subVal){
				newPairHtml += EMPTY_ROW_PART1 + subKey + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
			});
			newPairHtml += '</tbody></table>';
			newTextHtml = '<input type="text" key="' + this.key + '" class="form-control" value="">';
		} else {
			newPairHtml = TABLE_TIER2_HEADER + EMPTY_ROW_PART1 + EMPTY_ROW_PART2 + EMPTY_ROW_PART3 + '</tbody></table>';
			newTextHtml = '<input type="text" key="' + this.key + '" class="form-control" value="' + oldVal + '">';
		}
		sel.change(function() {
			var selVal = sel.val();
			if (selVal === 'text') {
				obj.colValue.html(newTextHtml);
			} else {
				obj.colValue.html(newPairHtml);
			}
		});
		var t1Value = this.colValue.find("> input");
		t1Value.focus(function() {
			obj.row.addClass("active");
		}).blur(function() {
			obj.row.removeClass("active");
			obj.updateNode(t1Value, obj.key, t1Value.val());
		}).keypress(function( event ) {
			if ( event.which == 13 ) {
				t1Value.blur();
			}
		});
		
		var t1Select = this.colValue.find("> select");
		if (t1Select) {
			t1Select.change(function() {
				obj.updateNode(t1Select, obj.key, t1Select.val());
			});
		}
		
		var t2Body = obj.colValue.find("tbody");
		if (t2Body) {
			var t2Key = t2Body.find("td.pair_key > input");
			var oldKV = t2Key.val();
			var isPair = !t2Key.prop('disabled');
			if (isPair) {
				t2Key.focus(function() {
					this.row.addClass("active");
				}).blur(function() {
					this.row.removeClass("active");
					updateT2Pair(t2Key, obj.key, t2Body, oldKV, isPair);
				}).keypress(function( event ) {
					if ( event.which == 13 ) {
						t2Key.blur();
					}
				});
			}

			var t2Value = t2Body.find("td.pair_value > input");
			var oldVV = t2Value.val();
			t2Value.focus(function() {
				obj.row.addClass("active");
			}).blur(function() {
				obj.row.removeClass("active");
				updateT2Pair(t2Value, obj.key, t2Body, oldVV, isPair);
			}).keypress(function( event ) {
				if ( event.which == 13 ) {
					t2Value.blur();
				}
			});
			
			var t2Add = t2Body.find("td.pair_control > button.button_plus");
			t2Add.click(function() {
				addRow(t2Add, obj.key, t2Body, isPair);
			});
			var t2Remove = t2Body.find("td.pair_control > button.button_minus");
			t2Remove.click(function() {
				removeRow(t2Remove, obj.key, t2Body, isPair);
			});
		}
	}

	getUI() {
		return this.row;
	}
	
	addValueSelectListener(f) {
		var t1ValueSelect = this.colValue.find("> select");
		t1ValueSelect.change(function() {
			f(t1ValueSelect.val());
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
		var t1ValueDatalist = this.colValue.find("> datalist");
		if (t1ValueDatalist) {
			t1ValueDatalist.html(ot);
		}
		if (options.length > 0) {
			this.setValue(options[0]);			
		} else {
			this.setValue("");
		}
	}
	
	setValue(val) {
		var t1Value = this.colValue.find("> input");
		if (t1Value) {
			t1Value.val(val);
			this.updateNode(t1Value, this.key, val);
		}
	}
	
	updateNode($node, key, val) {
//		var curVal = _editorModel[_curCid][key];
		const curVal = this.device.getValue(this.cid, key);
		if (curVal.toString() != val.toString()) {
			$node.addClass('changed');
			this.device.setDirtyFlag();
		} else {
			$node.removeClass('changed');
		}
//		_editorModel[_curCid][key] = val;
		this.device.setValue(this.cid, key, val);
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
		} else if (type == TYPE_DB) {
			console.log('load db device: ' + did);
			const device = _dbModel.getDevice(did);
			device.load();			
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
		
		$.each(_dbModel.configs, function(name, pair) {
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
						'Database: ' + name + ' => ' + desc + '</span></li>';
			} else {
				var desc = pair[3];
				if (desc && desc.toLowerCase().indexOf(word) >= 0) {
//					found[name] = path;
					var path = pair[0];
					var did = pair[1];
					desc = '(' + desc + ')';
					html += '<li class="messageitem" href="#" name="' + name + '" path="' + path + '" did="' + did + '"><span class="widget_text">' + 
						'Database: ' + name + ' => ' + desc + '</span></li>';
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
		this.$textInput = $('#id_input_saveMessage');
		this.dialog = $('#id_modal_saveDialog');
	}
	
	init() {
		const obj = this;
		this.$button.click(function() {
			if (_curDevice) {
				_curDevice.save();
			} else {
				showMsg('Please select a device for saving. Saving is per-device based.', 'warning')
				obj.close();
			}
		});
	
		this.$textInput.keypress(function(event) {
			if ( event.which == 13 ) {
				if (_curDevice) {
					_curDevice.save();
				} else {
					showMsg('Please select a device for saving. Saving is per-device based.', 'warning')
					obj.close();
				}
			}
		});
	}
	
	close() {
		this.dialog.modal('hide');
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
			showMsg("failed to remove: no device selected", 'danger');
			return;
		}

		const device = _instModel.getDevice(did);
		var saveMsg = obj.$textInput.val().replace(/^\s+|\s+$/gm,'');
		
		try {
			device.remove(saveMsg);
		} catch (e) {
			showMsg(e, 'danger');
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
			if (_curDevice) {
				_curDevice.reset();
			}
		});
	}
}

var addPageTitle = function(){
	var titleString = "SE Configuration - " + _inst.charAt(0).toUpperCase() + _inst.slice(1);
	$(document).attr("title", titleString);
	var subTitle = _inst.toUpperCase() + " SE Setup";
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
	}
	
	empty() {
		this.$side.find('.class_div_commit_item').remove();
	}
	
	reload() {
		if (this.enabled) {
			this.empty();
			const obj = this;
			var url = URL_PREFIX + 'confighistory?inst=' + _inst;
			if (_curDevice) {
				url += '&path=' + _curDevice.did;
			}
			url += '&' + Date.now();
			$.get(url, function(data) {
				data = $.parseJSON(data);
				$.each(data, function(index, version) {
					var commit = new CommitItem(version, index);
					commit.init(obj.$side);
//					.append(commit.getControl());
				});
			}).fail(function() {
				obj.$side.html('<span class="alert alert-danger">failed to get history.</span>');
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
			this.reload();
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
		this.$control.append('<span class="badge badge-secondary class_span_commit_timestamp">' + star + Utils.getTimeString(timestamp) + '</span>');
		this.$control.append('<span class="class_span_commit_message">' + message + '</span>');
		var button = $('<span class="class_span_commit_button"><button class="class_button_load_commit btn btn-sm btn-block btn-outline-primary">' + btText + '</button></span>');
		this.$control.append(button);
		button.find('button').click(function() {
			if (_curDevice == null) {
				alert('Please select a device first. Loading history version is only supported on a per-device base.')
				return;
			}
			var url = URL_PREFIX + 'configload?inst=' + _inst + '&version=' + encodeURI(name) + "&" + Date.now();
			$.get(url, function(data) {
				try{
					_curDevice.fromHistory(data);
				} catch (e) {
					alert('failed to load model from the commit, ' + e);
					return;
				}
//				$('.class_ul_folder').addClass('class_ul_hide');
//				$('#ul_mc_' + _curDid).removeClass('class_ul_hide');
//				$('.class_ul_folder > li').removeClass('active');
//				$('#li_' + _curDid + '_' + _curCid).addClass('active');
//				loadDeviceConfig(_curDid, _curCid);
//				_dirtyFlag = true;
				
//				if (_curDid != null) {
//					try{
//						_curDeviceModel.fromHistory(data);
//					} catch (e) {
//						alert('Device ' + _curDid + " doesn't exist.");
//						return;
//					}
//					$('.class_ul_folder').addClass('class_ul_hide');
//					$('#ul_mc_' + _curDid).removeClass('class_ul_hide');
//					$('.class_ul_folder > li').removeClass('active');
//					$('#li_' + _curDid + '_' + _curCid).addClass('active');
//					loadDeviceConfig(_curDid, _curCid);
//					_dirtyFlag = true;
//				} else {
//					_model = data;
//					_versionId = name;
//					_timestamp = getTimeString(timestamp);
//					_configs = {};
//					_firstConfig = {};
//					$.each(_model, function(did, device) {
//						$.each(device, function(cid, config) {
//							if (!(PROPERTY_KEYWORDS.includes(cid))) {
//								var path = "/" + did + "/" + cid;
//	//							var motor = encoder[keysOf(encoder)[0]];
//								var name = did + ":" + cid;
//								var desc = "";
//	//							if (KEY_MOTOR_DESC in motor){
//								if (motor.hasOwnProperty(KEY_MOTOR_DESC)) {
//									desc = motor[KEY_DEVICE_DESC];
//								}
//								_configs[name] = [path, did, cid, desc];
//								if (!(did in _firstConfig)) {
//									_firstConfig[did] = cid;
//								}
//							}
//						});
//					});
//		//			_sics = _model[SICS_MOTORS_NODE];
//	//				$("#id_div_sidebar").empty();
//					_curDeviceModel = null;
//					_curCid = null;
//					_curDid = null;
//					_editorModel = null;
//					showModelInSidebar();
//					showMsg('Successully loaded version: ' + message);
//					_editorTitle.empty();
//					_tabs.empty();
//					_editor.empty();
//					_propertyTitle.empty();
//					_property.empty();
//					_editorTitle.text("History version '" + message + "' has been loaded back.");
//					var $b = $('<button class="btn btn-outline-primary">Apply this version</button>');
//					$b.click(function() {
//						applyCurrentVersion($(this));
//					});
//					_editor.append($b);
//				}
			}).fail(function(e) {
				alert('failed to load the version, ' + e.statusText);
			});
		});
		$parentUi.append(this.$control);
	}
	
};

class Utils {
	
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
	
}

$(document).ready(function() {
	if (!_inst) {
		window.location = "seConfigMenu.html";
	}
	
	$('#id_a_signout').click(function() {
		signout("seConfig.html?inst=" + _inst);
	});
	
	addPageTitle();
	
	const homeButton = new HomeButton($('#id_span_side_home'));
	homeButton.init();

	const loadingLabel = new LoadingLabel($('#id_span_waiting'));
	loadingLabel.init();
	_saveButton = new SaveButton($('#id_button_saveConfirm'));
	_saveButton.init();
	
	_resetButton = new ResetButton($('#id_button_reset'));
	_resetButton.init();
	
	_removeModal = new RemoveModal('id_modal_deleteDialog');
	_removeModal.init();
	
	_instModel.load();
	_dbModel.load();
	
	_historyBar = new HistoryBlock($("#id_div_main_area"), $('#id_div_right_bar'));
	$('#id_button_history').click(function(){
		_historyBar.toggle();
	})
	
	$('#id_button_reload_history').click(function() {
		_historyBar.reload();
	});
	_historyBar.toggle();
	
	const searchWidget = new SearchWidget($('#id_input_search_text'));
	searchWidget.init();
});