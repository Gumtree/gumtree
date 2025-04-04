const TITLE_TEXT = "Sample Environment Device Configuration Database"
const URL_PREFIX = "seyaml/"
const KEY_DEVICE_DRIVER = "driver";
const KEY_DEVICE_DESC = "desc";
const HTML_ID_PHYSICALBAR = '#id_div_sidebar';
const HTML_ID_COMPOSITEBAR = '#id_div_sidebuttom';
const HTML_HIDDEN_FILL_DIV = '<div class="div_fill div_hidden"/>';
const HTML_NAV_DIV = '<div class="nav"/>';

const PHYSICAL_DB = "PD"
const COMPOSITE_DB = "CD"
	
const TABLE_TIER1_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="34%">Key</th><th width="66%">Value</th></tr></thead><tbody>';
const TABLE_TIER2_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="40%">Key</th><th width="40%">Value</th><th width="20%"></th></tr></thead><tbody>';
const EMPTY_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" value="';
const EMPTY_ROW_PART2 = '"></td><td class="pair_value"><input type="text" class="form-control" value="';
const EMPTY_ROW_PART3 = '"></td><td class="pair_control input-group-btn"><button type="button" class="btn btn-outline-primary button_plus">+</button><button type="button" class="btn btn-outline-primary button_minus">-</button></td></tr>';
const DISABLED_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" disabled value="';
const HTML_TABLE = '<table class="table table-striped table-sm"><thead><tr><th width="34%">Error entry</th><th width="66%">Message</th></tr></thead><tbody/></table>';

const KEY_DATYPE = "datype";
const KEY_STATIC = "STATIC";
const TYPE_COMPOSITE = "C";
const PROPERTY_KEYWORDS = [
	KEY_STATIC,
];
const DATYPE_ICON = {
		"T" : "thermometer-half",
		"B"	: "magnet",
		"V" : "bolt",
		"P" : "gauge",
		"rheometry" : "rotate",
		"robot" : "hand-holding",
		"X" : "compass",
		"pump" : "gas-pump",
		"valve" : "gear",
		"viscosity" : "droplet",
}
const DEFAULT_SUB_DEVICE_NAME_PREFIX = {
		"T" : "tc",
		"B" : "ma",
		"V"	: "volts",
		"P" : "pc"
}
const _option_prop = ["config_id", "ip", "port"];
//const JSON_TEMP_COMPOSITE_DEVICE = {
//		"STATIC" :
//		{
//			"datype" : "C"
//		}
//}
const JSON_TEMP_COMPOSITE_DEVICE = {};
const JSON_TEMP_SUB_DEVICE = {
		"config_id" : "--",
		"datype" 	: "NA",
		"desc" 		: "--",
		"driver" 	: "--",
		"id"		: "1",
		"ip"		: "--",
		"name"		: "--",
		"port"		: "--"
}

const ID_PROP_DRIVER = "driver";
const ID_PROP_CONFIGID = "config_id";
const ID_PROP_IP = "ip";
const ID_PROP_ID = "id";
const ID_PROP_NAME = "name";
const ID_PROP_PORT = "port";
const ID_PROP_NATIVETYPE = "type";
const JSON_SKIP_NAME = [
	ID_PROP_NAME,
	ID_PROP_NATIVETYPE,
	ID_PROP_DRIVER,
];

const _message = $('#id_div_info');
const _title = $('#id_device_title');
const _editorTitle = $('#id_editor_subtitle');
const _tabs = $('#id_ul_tabs');
const _editor = $('#id_div_editor_table');
const _propertyTitle = $('#id_property_subtitle');
const _property = $('#id_div_property_table');
var _errorReport;
var _removeModal;
var _homeButton;

const _allDevices = {};
var _curDevice;
//var _curDid;

function allowDrop(ev) {
	ev.preventDefault();
}

function drop(ev) {
	ev.preventDefault();
	var did = ev.dataTransfer.getData("text");
	ev.target.appendChild(document.getElementById(data));
}

class StaticUtils {
	
	static addPageTitle() {
		$(document).attr("title", TITLE_TEXT);
		$('#id_span_side_title').html('<h5>' + TITLE_TEXT + '</h5>');
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

}

class DBModel {
	
	#model;

	constructor(dbType) {
		this.dbType = dbType;
		this.$physicalMenuBar = $(HTML_ID_PHYSICALBAR);
		this.$compositeMenuBar = $(HTML_ID_COMPOSITEBAR);
		this.url = URL_PREFIX + 'sedb';
		this.devices = {};
		this.physicalDevices = {};
		this.compositeDevices = {};
		this.configsByPath = {};
		this.firstConfigs = {};
		this.configNamesOfDevice = {};
	}
	
	get model() {
		return this.#model;
	}
	
	set model(model) {
		this.#model = model;
	}
	
	get physicalDids() {
		return Object.keys(this.physicalDevices);
	}
	
	get compositeDids() {
		return Object.keys(this.compositeDevices);
	}
	
	getCidsOfDevice(did) {
		return this.configNamesOfDevice[did];
	}
	
	getDeviceModel(did) {
		return this.model[did];
	}
	
	setDevice(did, device) {
		this.devices[did] = device;
	}
	
	getDevice(did) {
		return this.devices[did];
	}
	
	setController(did, device) {
		this.devices[did] = device;
	}
	
	load(callback) {
		var obj = this;
		const loadUrl = this.url + "?dbtype=" + this.dbType;
		$.get(loadUrl, function(data) {
			obj.model = data;
//			obj.configs = {};
			obj.firstConfigs = {};
			obj.configNamesOfDevice = {};
			$.each(data, function(did, deviceModel) {
				const names = [];
				$.each(deviceModel, function(cid, cfg) {
					if (!(PROPERTY_KEYWORDS.includes(cid))) {
//						var path = obj.getType(cid) + "/" + did + "/" + cid;
						var path = "/" + did + "/" + cid;
						var desc = "";
						if (cfg.hasOwnProperty(KEY_DEVICE_DESC)) {
							desc = cfg[KEY_DEVICE_DESC];
						}
						var name = did + ":" + cid
						obj.configsByPath[name] = [path, did, cid, desc];
						if (!(did in obj.firstConfigs)) {
							obj.firstConfigs[did] = cid;
						}
						names.push(cid);
					}
				});
				obj.configNamesOfDevice[did] = names;
			});
			obj.createUi();
			obj.verify();
			if (callback) {
				callback();
			}
			_errorReport.show();
		}).fail(function(e) {
			if (e.status == 401) {
				alert("sign in required");
				window.location = 'signin.html?redirect=sedbConfig.html';
			} else {
				alert(e.statusText);
			}
		});
	}
		
	createUi() {
		const obj = this;
		$.each(Object.keys(this.model).sort(), (idx, did) => {
			const deviceModel = obj.model[did];
//			const datype = deviceModel[KEY_STATIC][KEY_DATYPE];
			var datype;
			if (KEY_STATIC in deviceModel) {
				datype = deviceModel[KEY_STATIC][KEY_DATYPE];
			} else {
				datype = "C";
			}
			if (datype == "C") {
				const device = new CompositeDevice(did, deviceModel, obj.$compositeMenuBar);
				device.createMenuUi();
				obj.setDevice(did, device);
				obj.compositeDevices[did] = device;
				_allDevices[did] = device;
			} else {
				const device = new PhysicalDevice(did, deviceModel, obj.$physicalMenuBar);
				device.createMenuUi();
				obj.setDevice(did, device);
				obj.physicalDevices[did] = device;
				_allDevices[did] = device;
			}
		});
	}

	verify() {
		const obj = this;
		_errorReport.clearError();
		$.each(this.compositeDids, function(idx, key) {
			obj.compositeDevices[key].verify();
		});
	}
	
	addNewCompositeDevice(did, deviceModel) {
		const device = new CompositeDevice(did, deviceModel, this.$compositeMenuBar);
		device.init = true;
		device.createMenuUi();
		this.setDevice(did, device);
		this.compositeDevices[did] = device;
		device.setDirtyFlag();
		device.load();
	}
	
	updatePathTable(newMcid, newAid, newMid, desc) {
		const obj = this;
		const path = "/" + newMcid + "/" + newAid;
		const names = [];
		const name = newMcid + ":" + newAid + ":" + newMid
		obj.motors[name] = [path, newMid, desc];
		obj.firstMotor[path] = newMid;
		names.push(newMid);
		obj.motorIdsOfAxis[path] = names;
		obj.axes[path] = obj.getControllerModel(newMcid)[newAid];
	}
	
	removePathTableItem(mcid, aid, mid) {
		const obj = this;
		const path = "/" + mcid + "/" + aid;
		const names = [];
		const fullName = mcid + ":" + aid + ":" + mid
		delete obj.motors[fullName];
		const axisModel = obj.model[mcid][aid];
		var isFirst = true;
		$.each(axisModel, function(motorId, motorModel) {
			var desc = "";
			if (motorModel.hasOwnProperty(KEY_DEVICE_DESC)) {
				desc = motorModel[KEY_DEVICE_DESC];
			}
			const name = mcid + ":" + aid + ":" + motorId;
			obj.motors[name] = [path, mid, desc];
			if (isFirst) {
				obj.firstMotor[path] = motorId;
				isFirst = false;
			}
			names.push(motorId);
		});
		obj.motorIdsOfAxis[path] = names;
		delete this.axes[path];
	}
	
	removeDevice(did) {
		const obj = this;
		if (did in this.model) {
			const deviceModel = this.model[did];
			$.each(deviceModel, function(cid, cfg) {
				if (cid != KEY_STATIC) {
					var name = did + ":" + cid;
					delete obj.configsByPath[name];
				}
			});
			delete this.model[did];
			delete this.devices[did];
			delete this.compositeDevices[did];
			delete this.firstConfigs[did];
			delete this.configNamesOfDevice[did];
		} else {
			throw new Error('device not found:' + did);
		}
	}
	
	getConfigNamesOfDevice(did) {
		const device = this.getDevice(did);
		if (device == null) {
			throw new Error('device not found: ' + did);
		}
		return device.getConfigArray();
	}
	
}
const _pdModel = new DBModel(PHYSICAL_DB);
const _cdModel = new DBModel(COMPOSITE_DB);

class AbstractDevice {
	
	#did;
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
		this.STATIC = model[KEY_STATIC];
		this.editorModel = $.extend(true, {}, model);
		this.$parentUi = $parentUi;
		this.tabUi = new TabUi(this);
		this.configEditors = {};
		this.configMenuDict = {};
		this.curCid = null;
		this.firstConfigId = this.getConfigArray()[0];
	}
	
	get firstConfig() {
		return this.model[this.firstConfigId];
	}

	get datype() {
//		return this.model["datype"];
//		return this.firstConfig[KEY_DATYPE];
		return this.model[KEY_STATIC][KEY_DATYPE];
	}
	
	set did(did) {
		this.#did = did;
	}
	
	get did() {
		return this.#did;
	}
	
	get type() {
		return '';
	}
	
	get id() {
//		return this.getType() + ":" + this.did;
		return this.did;
	}
	
	get configs() {
		return this.getConfigArray();
	}
	
	verify() {}
	
	setDirtyFlag() {
//		if (!this.isDirty()) {
//			this.$menuHeader.find('span.class_span_mc_name').append('<i class="fas fa-asterisk i_changed"> </i>');
//		}
		if (this.$menuHeader.find('i.fa-asterisk').length == 0) {
			this.$menuHeader.find('span.class_span_mc_name').append('<i class="fas fa-asterisk i_changed"> </i>');
		}
		this.#dirtyFlag = true;
	}
	
	clearDirtyFlag() {
		this.#dirtyFlag = false;
		this.$menuHeader.find('i.i_changed').remove();
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
		$.each(Object.values(this.configEditors), (idx, editor) => {
			editor.hide();
		});
		_title.text('');
		_editorTitle.text('');
	}
	
	setMenuItemActive(cid) {
		this.$menuUl.find('li.class_li_subitem').removeClass('active');
		if (cid) {
			this.configMenuDict[cid].setActive();
		}
		this.tabUi.setActive(cid);
	}
		
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
	
	save() {
		const obj = this;
		var url = URL_PREFIX + 'dbsave?dbtype=' + obj.dbType + '&msg=';
		var saveMsg = $('#id_input_saveMessage').val().replace(/^\s+|\s+$/gm,'');
		if (saveMsg.length > 0) {
			url += encodeURI(saveMsg);
		}
		url += "&" + Date.now();
		_saveButton.close();
		var text = JSON.stringify(this.editorModel);
		$.post(url,  {did:obj.did, model:text}, data => {
//			data = $.parseJSON(data);
			try {
				if (data["status"] == "OK") {
					StaticUtils.showMsg("Saved in the server.");
					$('td.editable_value input.changed').removeClass('changed');
//					if (_deviceItem != null) {
//						_deviceItem.init = false;
//						_deviceItem = null;
//					}
					$.extend(true, obj.model, obj.editorModel);
					obj.clearDirtyFlag();
					_historyBar.reload();
					_saveButton.reset();
//					setTimeout(_historyBar.reload, 3000)
				} else {
					StaticUtils.showError(data["reason"]);
				}
				_errorReport.clearError();
				_pdModel.verify();
			} catch (e) {
				console.log(e);
				StaticUtils.showError("Failed to save: " + e);
			}
		}).fail(function(e) {
			StaticUtils.showError("Faied to save: " + e);
		}).always(function() {
			_saveButton.close();
		});
	}
	
	reset() {
		this.clearDirtyFlag();
//		$.extend(true, this.editorModel, this.model);
		this.editorModel = $.extend(true, {}, this.model);
		$.each(Object.values(this.configEditors), function(idx, editor) {
			editor.dispose();
		});
		this.configEditors = {};
		this.reloadMenu();
		this.tabUi.reload();
		if (this.rootEditor) {
			this.rootEditor.reload();
		}
		if (this.curCid && (this.curCid in this.editorModel)) {
			this.loadConfig(this.curCid);
		} else {
			this.load();
		}
	}
		
	remove() {}
	
//	fromHistory() {}
	fromHistory(commitModel) {
		const deviceModel = commitModel[this.did];
//		console.log(deviceModel);
		if (!deviceModel) {
			throw new Error('device not found in history model: ' + this.did);
		}
//		$.extend(true, this.editorModel, deviceModel);
		this.editorModel = deviceModel;
		
		if (this.$menuUl) {
//			this.$menuUl.reload();
			this.reloadMenu();
		}
		
		this.rootEditor.reload();
		if (this.tabUi) {
			this.tabUi.reload();
		}
		
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
	
	deleteConfig(cid) {}
	
	reloadMenu() {}
}

class PhysicalDevice extends AbstractDevice {
	
	dbType = PHYSICAL_DB;
	
	constructor(did, model, $parentUi)
	{
		super(did, model, $parentUi);
//		this.rootEditor = new ImmutableRootUi(this);
		this.rootEditor = new StaticPropertyTable(this);
		this.$addBt = null;
	}
	
//	getType() {
//		return this.model[cid][KEY_DATYPE];
//	}
	
	createMenuUi() {
		const obj = this;
//		var datype = this.model["datype"];
		const datype = this.datype;
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
		html += '</h6></div>';
		
		this.$menuHeader = $(html);
		this.$label = this.$menuHeader.find('span.class_span_mc_name');
		this.$label.attr("draggable", true);
//		this.$label.attr("ondragstart", "drag(event)");
		this.$label.on("dragstart", function(event) {
			obj.drag(event);
		});
		this.$label.click(function() {
			obj.load();
		});
		
		const $menuUl = $('<ul id="ul_mc_'+ this.did + '" class="nav flex-column class_ul_hide class_ul_folder"></ul>');
		$.each(this.editorModel, function(cid, config) {
			if (! (PROPERTY_KEYWORDS.includes(cid))){
				const subItem = new ConfigMenuItem(obj, cid);
				subItem.createUi($menuUl);
				obj.configMenuDict[cid] = subItem;
			}
		});
		const $addBt = $('<li id="li_menu_' + obj.did + '_NEW_DEVICE' + '" class="nav-item class_li_subitem">'
				+ '<a class="nav-link class_a_add_axis" did="' + obj.did + '" href="#"><i class="fas fa-plus"> </a></li>');
		$menuUl.append($addBt);
		this.$addBt = $addBt;
		this.addConfigModal = new NewConfigModal($addBt, obj);
		this.addConfigModal.init();
//		$addBt.click(function() {
//			obj.addNewConfig();
//		});

		this.$menuUl = $menuUl;
		
		this.$parentUi.append(this.$menuHeader);
		this.$parentUi.append(this.$menuUl);
		
	}

	reloadMenu() {
		const obj = this;
		obj.$menuUl.empty();
		$.each(this.editorModel, function(cid, config) {
			if (! (PROPERTY_KEYWORDS.includes(cid))){
				const subItem = new ConfigMenuItem(obj, cid);
				subItem.createUi(obj.$menuUl);
				obj.configMenuDict[cid] = subItem;
			}
		});
		const $addBt = $('<li id="li_menu_' + obj.did + '_NEW_DEVICE' + '" class="nav-item class_li_subitem">'
				+ '<a class="nav-link class_a_add_axis" did="' + obj.did + '" href="#"><i class="fas fa-plus"> </a></li>');
		obj.$menuUl.append($addBt);
		this.$addBt = $addBt;
		this.addConfigModal = new NewConfigModal($addBt, obj);
		this.addConfigModal.init();
	}
	
	
	drag(ev) {
//		var element = $(ev.target.outerHTML);
//		var did = element.attr('did');
		ev.originalEvent.dataTransfer.setData("text", this.did);
	}
	
	load() {
		if (this.checkDirtyFlag()) {
			if (_curDevice != null && _curDevice.id != this.id) {
				_curDevice.hide();
			}
			_curDevice = this;
			this.setMenuActive(true);
			
			const obj = this;
			_title.text(obj.did + " (Physical device)");

			_editorTitle.text('Static properties of the device');

			this.curCid = null;
			$.each(Object.values(this.configEditors), function(idx, propTable) {
				propTable.hide();
			});

			_errorReport.hide();
			this.tabUi.show();
			this.rootEditor.show();
			_historyBar.reload();

		}
	}
	
	loadConfig(cid) {
		if (this.checkDirtyFlag()) {
			
			if (_curDevice != null && _curDevice.id != this.id) {
				_curDevice.hide();
			}
			_curDevice = this;

			this.curCid = cid;
			const obj = this;
			var config = obj.editorModel[cid]; 
//			var datype = obj.editorModel[KEY_DATYPE];
			const datype = config[KEY_DATYPE];
			
			
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
			_errorReport.hide();
			this.setMenuActive(true, cid);
			this.setMenuItemActive(cid);
			
			this.tabUi.show();
			this.tabUi.setActive(cid);
			_historyBar.reload();
		}
	}
	
	addNewConfig(cid) {
		const obj = this;
		const copyDid = this.did;
		var copyCid = null;
		if (_curDevice != null && copyDid == _curDevice.did && obj.curCid != null) {
			copyCid = obj.curCid;
		} else {
			const cidArray = obj.getConfigArray();
			if (cidArray.length > 0) {
				copyCid = cidArray[cidArray.length - 1];
			} else {
				StaticUtils.showError("Failed to create new configuration: no existing one to copy");
				return;
			}
		}
		
		var newName = cid.trim();
		newName = newName.replace(/\W/g, '_');
		if (cid in obj.model) {
			StaticUtils.showError("Failed to create new configuration: id already exists, " + newName);
			return;
		}
		
		var copyModel = obj.editorModel[copyCid];
		if (copyModel === undefined) {
			StaticUtils.showError("Faied to copy: " + obj.did + "/" + copyCid + ", config not found");
			return;
		}
		
//		obj.model[newName] = $.extend(true, {}, copyModel);
		obj.editorModel[newName] = $.extend(true, {}, copyModel);
		
		const subItem = new ConfigMenuItem(obj, newName);
		subItem.createUi(obj.$menuUl);
		obj.configMenuDict[newName] = subItem;
		
		obj.$addBt.detach();
		obj.$menuUl.append(obj.$addBt);
		
		obj.tabUi.reload();

		obj.setDirtyFlag();
		obj.loadConfig(newName);
	}
	
	deleteConfig(cid) {
		const obj = this;
		obj.setDirtyFlag();

		const subDevice = obj.editorModel[cid];
		if (subDevice == null) {
			StaticUtils.showError('sub device not found: ' + cid);
			return;
		}
		const rmType = subDevice[KEY_DATYPE];
		const rmId = subDevice[ID_PROP_ID];
		
//		delete obj.model[cid];
		delete obj.editorModel[cid];
		
		const menuItem = obj.configMenuDict[cid];
		menuItem.dispose();
		
		obj.tabUi.removeConfigTab(cid);
		
		obj.reloadMenu();
		
		obj.configEditors[cid].dispose();
		obj.load();
	}
	
	findIps(cid) {
		const res = [];
		if (ID_PROP_IP in this.STATIC) {
			const ips = this.STATIC[ID_PROP_IP];
			if (typeof ips === "object") {
				$.each(Object.keys(ips), function(idx, key) {
					if (ips[key] == cid) {
						res.push(key);
					}
				});
			}
		}
		return res;
	}

	getAllIps() {
		const res = [];
		if (ID_PROP_IP in this.STATIC) {
			const ips = this.STATIC[ID_PROP_IP];
			if (typeof ips === "object") {
				return Object.keys(ips);
			}
		}
		return [];
	}
//	deleteConfig(cid) {
//		this.setDirtyFlag();
//		
//		var configs = this.getConfigArray();
//		if (configs.length <= 1) {
//			showMsg('Falied to delete configuration: ' + this.did + '/' + cid + ', the device must have more than one configurations before deleting one.', 'danger');
//			return;
//		}
//		
//		var configModel = this.model[cid];
//		if (configModel == null) {
//			StaticUtils.showMsg('Configuration not found: ' + cid, 'danger');
//			return;
//		}
//
//		delete this.model[cid];
//		delete this.editorModel[cid];
//		
//		var liMenuItem = $('#li_menu_' + did + '_' + cid);
//		liMenuItem.remove();
//		
//		var liTabItem = $('#li_tab_' + did + '_'  + cid);
//		liTabItem.remove();
//		
//		var configs = getConfigArray(did);
//		if (configs.length > 0) {
//			loadDeviceConfig(did, configs[0]);
//		}
//		showMsg('NOT saved, please use the save button to commit the change to server', 'warning');
//	}
}

class CompositeDevice extends AbstractDevice {
	
	$label;
	$input;
	$textbox;
	$control;
	init = false;
	dbType = COMPOSITE_DB;
	
	constructor(did, model, $parentUi)
	{
//		this.#did = did;
//		this.#model = model;
//		this.#parentUi = parentUi;
		super(did, model, $parentUi);
		this.rootEditor = new MutableRootUi(this);
		this.newDid = null;
	}

//	get type() {
//		return TYPE_COMPOSITE;
//	}
	get datype() {
		return TYPE_COMPOSITE;
	}
	
	createMenuUi() {
		const obj = this;
//		var datype = this.model["datype"];
		const datype = this.datype;
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
//		if (datype == "C") {
//			html += '<span class="class_span_mc_control"><i class="fas fa-minus"></i> </span>';
//		}
		html += '<span class="class_span_mc_input class_span_hide"><input type="text" class="form-control class_mc_input" value="' + this.did + '"></span>';
		html += '<span class="class_span_mc_control"><i class="fas fa-edit"></i> </span>';

		html += '</h6></div>';
		
		this.$menuHeader = $(html);
		this.$label = this.$menuHeader.find('span.class_span_mc_name');
		this.$label.click(function() {
			obj.load();
		});
		
		this.$control = this.$menuHeader.find('span.class_span_mc_control');
//		var ulList = $div.next('ul');

		this.$input = this.$menuHeader.find('span.class_span_mc_input');
		this.$textbox = this.$input.find('input');
		this.$textbox.keyup(function(event) {
			if ( event.key === "Enter" ) {
				const newDid = obj.$textbox.val();
				obj.newDid = newDid;
//				if (saveDeviceNameChange(did, val)) {
//					saveDeviceNameChange(did, val, deviceItem);
				obj.$label.attr('did', newDid);
				obj.$label.html('<i class="fas fa-caret-down"></i> ' + newDid);
				obj.$input.addClass('class_span_hide');
				obj.$label.removeClass('class_span_hide');
				obj.$control.removeClass('class_span_hide');
				delete _cdModel.model[obj.did];
				_cdModel.model[newDid] = obj.model;
//				var $ul = $("#ul_mc_" + did);
				obj.$menuUl.attr("id", "ul_mc_" + newDid);
				$.each(obj.$menuUl.find("li"), function(idx, val) {
					const $li = $(this);
					const $a = $li.find("a");
					const oCid = $a.attr("cid");
					$li.attr("id", "li_menu_" + newDid + "_" + oCid);
					$a.attr("did", newDid);
				});
//				if (_curDeviceModel != null && _curDeviceModel.did == did) {
//					_curDeviceModel.did = newDid;
//					_curDeviceModel.model = device;
//					_curDid = newDid;
//					_title.text(newDid + " (Composite Device)");
//				}
				if (_curDevice != null && _curDevice.did == obj.did) {
					_title.text(newDid + " (Composite Device)");
				}
				if (!obj.init) {
//					$('#id_modal_changeNameDialog').modal('show');
					_changeDidModal.device = obj;
					_changeDidModal.show();
				} else {
					obj.did = newDid;
					obj.setDirtyFlag();
//					did = newDid;
				}
			} else if ( event.key === "Escape" ) {
				obj.$textbox.val(obj.$label.attr("did"));
				obj.$input.addClass('class_span_hide');
				obj.$label.removeClass('class_span_hide');
				obj.$control.removeClass('class_span_hide');
			}
		}).blur(function() {
			obj.$textbox.val(obj.$label.attr("did"));
			obj.$input.addClass('class_span_hide');
			obj.$label.removeClass('class_span_hide');
			obj.$control.removeClass('class_span_hide');
		});
		
		this.$control = obj.$menuHeader.find('span.class_span_mc_control');
		this.$control.click(function() {
			obj.$control.addClass('class_span_hide');
			obj.$label.addClass('class_span_hide');
			obj.$input.removeClass('class_span_hide');
			const range = obj.$textbox.val().length;
			obj.$textbox.get(0).setSelectionRange(range, range);
			obj.$textbox.focus();
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

	clearDirtyFlag() {
		super.clearDirtyFlag();
		this.$menuHeader.find('i.i_changed').remove();
	}
	
	verify() {
		const obj = this;
		$.each(this.configs, function(idx, cid) {
			const configModel = obj.model[cid];
			const configSelection = configModel[ID_PROP_CONFIGID];
			const driverId = obj.getDriverName(cid);
			if (driverId == null) {
//				StaticUtils.showError('device ID not found in configuration');
				_errorReport.addError(obj.did, cid, "configuration doesn't have a deviceId property");
				return;
			}
			const dModel = _pdModel.getDeviceModel(driverId);
			if (dModel == null) {
				_errorReport.addError(obj.did, cid, 'device ' + driverId + ' not found in the Database');
//				StaticUtils.showError('device not found in the Device Database: ' + driverId);
				return;
			}
			const cModel = dModel[configSelection];
			if (cModel == null) {
//				StaticUtils.showError('configuration not found in database device: ' + newConfigId);
				_errorReport.addError(obj.did, cid, 'configuration ' + configSelection + ' not found in physical device: ' + driverId);
				return;
			}
		});

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

//			_editorTitle.text('Use the tab menu to load configuration of sub-device');
			_editorTitle.text('Use drag & drop action to drop a physical device to the box below.');
			this.curCid = null;
			$.each(Object.values(this.configEditors), function(idx, propTable) {
				propTable.hide();
			});
			_errorReport.hide();
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

			this.curCid = cid;
			const obj = this;
			var config = obj.editorModel[cid]; 
//			var datype = obj.editorModel[KEY_DATYPE];
			const datype = config[KEY_DATYPE];
			
			
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
			_errorReport.hide();
			var table = this.configEditors[cid];
			if (!table) {
				table = new PropertyTable(obj, cid, config);
				this.configEditors[cid] = table;
			}
			table.show();
			this.setMenuActive(true);
			this.setMenuItemActive(cid);
			
			this.tabUi.show();
			this.tabUi.setActive(cid);
			_historyBar.reload();
		}
	}
	
	getDriverName(cid) {
		if (cid in this.model) {
			return this.model[cid][ID_PROP_DRIVER];
		} else if (cid in this.editorModel) {
			return this.editorModel[cid][ID_PROP_DRIVER];
		} else {
			return null;
		}
	}
	
	changeConfig(cid, newConfigId) {
		const obj = this;
		const driverId = this.getDriverName(cid);
		if (driverId == null) {
			StaticUtils.showError('deviceId property not found in this configuration');
			return;
		}
		const dModel = _pdModel.getDeviceModel(driverId);
		if (dModel == null) {
			StaticUtils.showError('device not found in the Device Database: ' + driverId);
			return;
		}
		const cModel = dModel[newConfigId];
		if (cModel == null) {
			StaticUtils.showError('configuration not found in database device: ' + newConfigId);
			return;
		}
		const editorConfig = this.editorModel[cid];
		$.each(cModel, function(key, val) {
			if (!JSON_SKIP_NAME.includes(key) && key in editorConfig) {
				editorConfig[key] = val;
				if (cid in obj.configEditors) {
					const editor = obj.configEditors[cid];
					editor.updateValue(key, val);
				}
			}
		});
		
		const table = this.configEditors[cid];
		if (table) {
			table.updateIpOptions(newConfigId);
		}
	}
	
	makeSubDevice(subDid) {
		const obj = this;
		obj.setDirtyFlag();
		const did = obj.did;
		const deviceModel = _pdModel.getDeviceModel(subDid);
		if (deviceModel == null) {
			StaticUtils.showError('device not found: ' + subDid);
			return;
		}
		const subType = deviceModel[KEY_STATIC][KEY_DATYPE].toUpperCase();
		var idx = 1;
		var newName;
		while(true) {
			newName = subDid + '_' + idx;
			if (!(newName in this.editorModel)) {
				break;
			}
			idx++;
		}
		var newId = 1;
		$.each(this.editorModel, function(key, config) {
			if (config[KEY_DATYPE] == subType && config[ID_PROP_ID] == newId) {
				newId++;
			}
		});
		
		const cid = newName;
//		const menuItem = $('<li id="li_menu_' + did + '_' + cid + '" class="nav-item class_li_subitem"><a class="nav-link class_a_axis" did="' 
//				+ did + '" cid="' + cid + '" href="#">' + cid + '<span class="sr-only">(current)</span></a></li>');
//		menuItem.click(function() {
//			loadDeviceConfig(did, cid);
//		});
//		const ulList = $('#ul_mc_' + did);
//		ulList.append(menuItem);
		const subItem = new ConfigMenuItem(obj, cid);
		subItem.createUi(obj.$menuUl);
		obj.configMenuDict[cid] = subItem;
		
//		var tabItem = $('<li class="nav-item" id="li_tab_' + did + '_' + cid + '"><a class="nav-link" href="#">' + cid + '</a></li>');
//		tabItem.click(function() {
//			loadDeviceConfig(did, cid);
//		});
//		_tabs.append(tabItem);
		obj.tabUi.addConfigTab(cid)
		
		var subDevice = $.extend({}, JSON_TEMP_SUB_DEVICE);
		var configs = _pdModel.getCidsOfDevice(subDid);
		if (configs.length > 0) {
			subDevice["config_id"] = configs[0];
		} else {
			throw new Error("no configuration found for device " + subDid);
		}
		var source = _pdModel.getDeviceModel(subDid)[configs[0]];
		const srcDevice = _pdModel.devices[subDid];
		subDevice["datype"] = subType;
		subDevice["driver"] = subDid;
		subDevice["id"] = newId;
//		var ips = source["ip"];
//		const ips = srcDevice.STATIC[ID_PROP_IP];
//		if (typeof ips === 'object') {
//			subDevice["ip"] = ips[0];
//			console.log("take first IP" + subDevice["ip"]);
//		} else {
//			subDevice["ip"] = ips;
//			console.log("take all IP" + subDevice["ip"]);
//		}
		const ips = srcDevice.findIps(configs[0]);
		if (ips.length > 0) {
			subDevice[ID_PROP_IP] = ips[0];
		} else {
			subDevice[ID_PROP_IP] = "--";
		}
		var desc = source["desc"];
		if (desc) {
			subDevice["desc"] = desc;
		}
		var prefix;
		if (subType in DEFAULT_SUB_DEVICE_NAME_PREFIX) {
			prefix = DEFAULT_SUB_DEVICE_NAME_PREFIX[subType];
		} else {
			prefix = subType;
		}
		subDevice["name"] = prefix + newId;
		subDevice["port"] = source["port"];
//		disable copy all properties to composite subdevice 
//		for (const key of Object.keys(source)) {
//			if (!(key in subDevice) && !(JSON_SKIP_NAME.includes(key))) {
//				subDevice[key] = source[key];
//			}
//		}
//		obj.model[newName] = subDevice;
		obj.editorModel[newName] = $.extend({}, subDevice);
		const newSubDevice = {};
		const subHtml = '<div class="class_div_device_page" id="div_page_' + did + '_' + newName 
			+ '"><a href="#" class="class_a_cid_delete" cid="' + newName 
			+ '"><i class="fas fa-square-minus"></i> </a>'
			+ '<div class="class_div_device_item"><a href="#" class="class_a_cid_label" did="' 
			+ did + '" cid="' + newName + '">' + newName + '</a></div></div>';
//			+ did + '" cid="' + newName + '">' + newName + '<br>(' + subDevice["driver"] + ')' + '</a></div></div>';
		newSubDevice["html"] = subHtml;
//		newSubDevice["html"] = '<div class="class_div_device_item">' + newName + '</div>';
		newSubDevice["cid"] = newName;
		return newSubDevice;
	}
	
	remove(saveMsg) {
		const obj = this;
		var url = URL_PREFIX + 'dbremove?dbtype=CD&did=' + obj.did + '&msg=';
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
					StaticUtils.showMsg("Removed successfully in the server.");
					if (_curDevice != null && _curDevice.id == obj.id) {
						_curDevice = null;
//						_title.text('Please use the side bar to select a device configuration.');
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
					
					_cdModel.removeDevice(obj.did);
//					setTimeout(_historyBar.reload, 3000)
				} else {
					StaticUtils.showError(data["reason"]);
				}
			} catch (e) {
				StaticUtils.showError("Failed to remove: " + e.statusText);
			}
		}).fail(function(e) {
			console.log(e);
			StaticUtils.showError("Faied to remove: " + e.statusText);
		}).always(function() {
			_removeModal.close();
		});
	}
	
	changeName(msg) {
		const obj = this;
		var url = URL_PREFIX + 'changeName?dbtype=' + COMPOSITE_DB + '&msg=';
		const saveMsg = msg.replace(/^\s+|\s+$/gm,'');
		if (saveMsg.length > 0) {
			url += encodeURI(saveMsg);
		}
		const oldDid = obj.did;
		const newDid = obj.newDid;
		obj.did = newDid;
		url += "&" + Date.now();
//		const text = JSON.stringify(_model[newDid]);
		const text = JSON.stringify(obj.model);
		$.post(url,  {oldDid:oldDid, newDid:newDid, model:text}, function(data) {
//			data = $.parseJSON(data);
			try {
				if (data["status"] == "OK") {
					const deviceModel = obj.model;
					delete _cdModel.model[oldDid];
					_cdModel.model[newDid] = deviceModel;
					if (_curDevice !=  null && _curDevice.did == obj.did) {
						_title.text(newDid + " (Composite Device)");
					}
					StaticUtils.showMsg("Device name saved in the server.");
					_changeDidModal.device = null;
					$('td.editable input.changed').removeClass('changed');
					setTimeout(_historyBar.reload, 3000)
				} else {
					StaticUtils.showError("Failed to rename the device: " + data["reason"]);
//					deviceItem.resetChange();
				}
			} catch (e) {
				StaticUtils.showError("Failed to rename the device: " + e);
//				deviceItem.resetChange();
			}
		}).fail(function(e) {
			StaticUtils.showError("Failed to talk to the server: " + e.statusText);
//			deviceItem.resetChange();
		}).always(function() {
			$('#id_modal_saveDialog').modal('hide');
		});
	}
	
	cancelChangeName() {
//		if (saveDeviceNameChange(did, val)) {
//			saveDeviceNameChange(did, val, deviceItem);
		const obj = this;
		const did = this.did;
		var newDid = this.$label.attr('did');
		this.$label.attr('did', did);
		this.$label.html('<i class="fas fa-caret-down"></i> ' + did);
		const device = _cdModel.model[newDid];
		delete _cdModel.model[newDid];
		_cdModel.model[did] = device;
		
		this.$menuUl.attr("id", "ul_mc_" + did);
		$.each(obj.$menuUl.find("li"), function(idx, val) {
			const $li = $(this);
			const $a = $li.find("a");
			const oCid = $a.attr("cid");
			$li.attr("id", "li_menu_" + did + "_" + oCid);
			$a.attr("did", did);
		});
		if (_curDevice != null && _curDevice.did == obj.did) {
			_title.text(did + " (Composite Device)");
		}
		_changeDidModal.device = null;
//		var $ul = $("#ul_mc_" + newDid);
//		$ul.attr("id", "ul_mc_" + did);
//		$.each($ul.find("li"), function(idx, val) {
//			var $li = $(this);
//			var $a = $li.find("a");
//			var oCid = $a.attr("cid");
//			$li.attr("id", "li_menu_" + did + "_" + oCid);
//			$a.attr("did", did);
//			$a.off("click");
//			$a.on("click", function(){
//				loadDeviceConfig(did, oCid);
//			});
//		});
//		if (_curDeviceModel != null && _curDeviceModel.did == newDid) {
//			_curDeviceModel.did = did;
//			_curDeviceModel.model = device;
//			_curDid = did;
//			_title.text(did + " (Composite Device)");
//		}
//		_deviceItem = null;

	}
	
	reloadMenu() {
		const obj = this;
		obj.$menuUl.empty();
		$.each(this.editorModel, function(cid, config) {
			if (! (PROPERTY_KEYWORDS.includes(cid))){
				const subItem = new ConfigMenuItem(obj, cid);
				subItem.createUi(obj.$menuUl);
				obj.configMenuDict[cid] = subItem;
			}
		});
	}

	deleteConfig(cid) {
		const obj = this;
		obj.setDirtyFlag();

		const subDevice = obj.editorModel[cid];
		if (subDevice == null) {
			StaticUtils.showError('sub device not found: ' + cid);
			return;
		}
		const rmType = subDevice[KEY_DATYPE];
		const rmId = subDevice[ID_PROP_ID];
		
//		delete obj.model[cid];
		delete obj.editorModel[cid];
		
		const menuItem = obj.configMenuDict[cid];
		menuItem.dispose();
		
		obj.tabUi.removeConfigTab(cid);
		
		if (obj.rootEditor != null) {
			obj.rootEditor.deleteConfigPage(cid);
		}
		if (cid in obj.configEditors) {
			obj.configEditors[cid].dispose();
		}
		
		$.each(obj.model, function(key, config) {
			if (key != KEY_DATYPE) {
				var iType = config[KEY_DATYPE];
				if (iType == rmType) {
					var iId = config[ID_PROP_ID];
					if (iId > rmId) {
						iId--;
						obj.editorModel[key][ID_PROP_ID] = iId;
						var prefix;
						if (iType in DEFAULT_SUB_DEVICE_NAME_PREFIX) {
							prefix = DEFAULT_SUB_DEVICE_NAME_PREFIX[iType];
						} else {
							prefix = iType;
						}
						obj.editorModel[key][ID_PROP_NAME] = iType + iId;
					}
				}
			}
		});
		obj.load();
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
		$.each(this.device.editorModel, function(cid, config) {
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
	
	reload() {
		this.$tabUi.empty();
		this.createUi();
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
	
	addConfigTab(cid) {
		const obj = this;
		var $li = $('<li class="nav-item" id="li_tab_' + obj.device.did + '_' + cid + '"><a class="nav-link tab_item" href="#">' + cid + '</a></li>');
		$li.click(function() {
			obj.device.loadConfig(cid);
		});					
		obj.$tabUi.append($li);
		
//		$li.insertBefore(obj.$addLi);
		
	}
	
	removeConfigTab(cid) {
		this.$tabUi.find('li#li_tab_' + this.device.did + '_' + cid).remove();
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
	
	reload() {
		this.$editorUi.empty();
		this.createUi();
	}
	
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
		const eid = did + " / " + cid;
		console.log("add error " + eid + ": " + msg);
		const $tr = $('<tr id="' + did + "_" + cid + '"><td><span class="span_error_id" did="' + did + '" cid="' + cid + '">' + eid 
				+ '</span></td><td><span class="span_error_msg">' + msg + '</span></td></tr>');
		$tr.find('span.span_error_id').click(function() {
			console.log("load " + eid);
			const device = _cdModel.getDevice(did);
			device.loadConfig(cid);
		});
		this.$body.append($tr);
	}
	
	removeError(did, cid) {
		this.$body.find('#' + id).remove();
	}
	
	clearError() {
		this.$body.empty();
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

class MutableRootUi extends AbstractMainUi {
	constructor(device) {
		super(device);
		this.$container;
	}
	
	createUi() {
		const obj = this;
		var html = '';
		$.each(obj.device.editorModel, function(subDid, config) {
			if (! (PROPERTY_KEYWORDS.includes(subDid))) {
//				html += '<div class="class_div_device_page" id="div_page_' + obj.device.did + '_' + cid + '">'
//					+ '<div class="class_div_device_item"><a href="#" class="class_a_cid_label" did="' 
//					+ obj.device.did + '" cid="' + cid + '">' + cid + '<br>(' + config[KEY_DEVICE_DRIVER] + ')' + '</a></div></div>';
				html += '<div class="class_div_device_page" id="div_page_' + obj.device.did + '_' + subDid + '"><a href="#" class="class_a_cid_delete" did="' 
					+ obj.device.did + '" cid="' + subDid + '"><i class="fas fa-square-minus"></i> </a>'
					+ '<div class="class_div_device_item"><a href="#" class="class_a_cid_label" did="' 
					+ obj.device.did + '" cid="' + subDid + '">' + subDid + '</a></div></div>';
//					+ obj.device.did + '" cid="' + subDid + '">' + subDid + '<br>(' + config[ID_PROP_DRIVER] + ')' + '</a></div></div>';
			}
		});
		var $div = $('<div class="class_div_device_canvas"/>').append(html);
		$div.find('a.class_a_cid_label').click(function(){
			var acid = $(this).attr('cid');
			obj.device.loadConfig(acid);
		});
		$div.find('a.class_a_cid_delete').click(function(){
			var adid = $(this).attr('did');
			var acid = $(this).attr('cid');
			obj.device.deleteConfig(acid);
		});
		$div.on('dragover', function(event) {
			event.preventDefault();
		});
		$div.on('drop', function(ev) {
			obj.drop(ev);
		});
		this.$container = $div;
		
		const $del = $('<div class="main_footer"><span id="id_button_del" class="btn btn-outline-primary btn-block " href="#"><i class="fas fa-remove"></i> Remove This Device</span></div>');
		$del.find('span').click(function() {
//			$('#id_modal_deleteDialog').modal('show');
			_removeModal.toRemove = obj.device.did;
			_removeModal.open();
		});
//		_property.append($del);

		this.$editorUi.append($div);
		this.$editorUi.append($del);
		
		_editor.append(this.$editorUi);
//		_historyBar.reload();
		this.init = true;
	}
	
	drop(ev) {
		ev.preventDefault();
		const obj = this;
		const subDid = ev.originalEvent.dataTransfer.getData("text");
		const newSubDevice = obj.device.makeSubDevice(subDid);
		const $item = $(newSubDevice["html"]);
		$item.find('a.class_a_cid_label').click(function(){
//			var adid = $(this).attr('did');
			var acid = $(this).attr('cid');
			obj.device.loadConfig(acid);
		});
		$item.find('a.class_a_cid_delete').click(function(){
			var acid = $(this).attr('cid');
			obj.device.deleteConfig(acid);
		});
		this.$container.append($item);
		obj.device.setDirtyFlag();
	}
	
	deleteConfigPage(cid) {
		this.$editorUi.find('#div_page_' + this.device.did + '_' + cid).remove();
	}
}

class PropertyTable extends AbstractMainUi {
	
	constructor(device, cid, config) {
		super(device);
//		this.device = device;
		this.cid = cid;
		this.cModel = config;
		this.driverId = config[KEY_DEVICE_DRIVER];
		if (device.datype == "C") {
			this.dModel = _pdModel.getDeviceModel(this.driverId);
		}
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
		if (typeof obj.dModel === 'undefined') {
//			StaticUtils.showWarning("driver id " + obj.driverId + " not found in the physical device list!");
			$.each(this.cModel, function(key, val){
				if (_option_prop.includes(key)) {
					var pRow = obj.createRow(key, val);
					$tbody.append(pRow.getUI());
				} else {
					var pRow = obj.createRow(key, val, null, false);
					$tbody.append(pRow.getUI());
				}
			});
		} else {
			if (subConfigId in obj.dModel) {
				$.each(this.cModel, function(key, val){
					if (key == ID_PROP_CONFIGID) {
						var options = _pdModel.getConfigNamesOfDevice(obj.driverId);
						obj.configRow = obj.createRow(key, val, options, true);
						$tbody.append(obj.configRow.getUI());
					} else if (key == ID_PROP_IP) {
//						var options = obj.dModel[subConfigId][ID_PROP_IP];
//						var options = obj.dModel[KEY_STATIC][ID_PROP_IP];
						const device = _pdModel.getDevice(obj.driverId);
						const options = device.findIps(subConfigId);
//						console.log(options);
						obj.ipRow = obj.createRow(key, val, options);
						$tbody.append(obj.ipRow.getUI());
					} else if (key == ID_PROP_PORT) {
						var options = obj.dModel[subConfigId][ID_PROP_PORT];
						obj.portRow = obj.createRow(key, val, options);
						$tbody.append(obj.portRow.getUI());
					} else {
//						var pRow = obj.createRow(key, val, null, true);
						var pRow = obj.createRow(key, val);
						$tbody.append(pRow.getUI());
					}
				});
			} else {
				$.each(this.cModel, function(key, val){
					if (key == ID_PROP_CONFIGID) {
//						var options = obj.device.getConfigArray();
						StaticUtils.showWarning("configuration " + subConfigId + " not found in physical device: " + obj.driverId);
						var options = _pdModel.getConfigNamesOfDevice(obj.driverId);
						options.push(val);
						obj.configRow = obj.createRow(key, val, options);
						$tbody.append(obj.configRow.getUI());
					} else if (_option_prop.includes(key)) {
						var pRow = obj.createRow(key, val);
						$tbody.append(pRow.getUI());
					} else {
//						var pRow = obj.createRow(key, val, null, true);
						var pRow = obj.createRow(key, val);
						$tbody.append(pRow.getUI());
					}
				});
			}
		}
//		if (object.configRow != null) {
//			object.configRow.addValueSelectListener(function(value){
//				var newConfig = object.dModel[value];
//				if (newConfig) {
//					var ips = newConfig[ID_PROP_IP];
//					if (object.ipRow) {
//						object.ipRow.updateValueOptions(ips);
//					}
//					var ports = newConfig[ID_PROP_PORT];
//					if (object.portRow) {
//						object.portRow.updateValueOptions(ports);
//					}
//				}
//			});
//		}
		this.$editorUi.append($table);
		
		var $del = $('<div class="main_footer"><span id="id_button_del_config" class="btn btn-outline-primary btn-block " href="#"><i class="fas fa-remove"></i> Remove This Configuration from Device ' + obj.device.did + '</span></div>');
		this.$editorUi.append($del);
		const deleteConfigModal = new DeleteConfigModal($del, obj.device, obj.cid);
		deleteConfigModal.init();
		
		_editor.append(this.$editorUi);
		this.init = true;
	}
	
	updateIpOptions(newConfigId) {
		const device = _pdModel.getDevice(this.driverId);
		const options = device.findIps(newConfigId);
		if (options.length > 0) {
			this.ipRow.setValue(options[0]);
			this.ipRow.updateOptions(options);
		} else {
			this.ipRow.setValue("");
			this.ipRow.updateOptions(device.getAllIps());
		}
	}
	
	updateValue(key, val) {
		const propertyRow = this.rowEditor[key];
		if (propertyRow != null) {
			propertyRow.setValue(val);
		}
	}
	
	createRow(key, val, options, editingDisabled) {
		const cid = this.cid;
//		console.log(cid + "/" + key + ":" + val + ":" + options);
		if (typeof options === 'undefined') {
			options = null;
		} else if (!(typeof options === 'object')) {
			options = [options];
		}
		if (typeof editingDisabled === 'undefined') {
			editingDisabled = false;
		}
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
//					html += '<input name="option_value" class="form-control" value="' + val + '" list="' + key + '"/><datalist id="' + key + '">' + ot + '</datalist>';
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
		const datalistId = obj.device.did + "_" + obj.cid + "_" + obj.key;
		var html = '<tr class="editable_row" key="' + this.key + '"><td class="editable_key">' + this.key + '</td>'
			+ '<td class="editable_value">';
		html += '<input name="option_value" class="form-control" value="' + this.val + '" list="' + datalistId + '"/><datalist id="' + datalistId + '">' + ot + '</datalist>';
		html += '</td></tr>';

		this.$row = $(html);
		this.$colValue = this.$row.find('.editable_value');
		this.$t1Value = this.$colValue.find("> input");
//		this.$t1Select = this.$colValue.find("> select");
		this.$datalist = this.$colValue.find("> datalist");
		this.$t2Body = this.$colValue.find("tbody");
		this.key = this.$row.attr('key');

		this.addEventHandler();
	}
	
	updateOptions(options) {
		const obj = this;
		obj.options = options;
		var ot = "";
		$.each(obj.options, function(idx, op){
			var sl = op == obj.val ? " selected" : "";
			ot += '<option value="' + op + '"' + sl + '>'+ op + '</option>';
		});
		if (this.$datalist) {
			this.$datalist.html(ot);
		}
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
		$t2Key.each(function(){
			const $kInput = $(this);
			const oldKV = $kInput.val();
			$kInput.focus(function() {
				obj.$row.addClass("active");
			}).blur(function() {
				obj.$row.removeClass("active");
				obj.updateT2Pair($kInput, oldKV);
			}).keypress(function( event ) {
				if ( event.which == 13 ) {
					$kInput.blur();
				}
			});
		});

		var $t2Value = obj.$t2Body.find("td.pair_value > input");
		$t2Value.each(function(){
			const $vInput = $(this);
			var oldVV = $vInput.val();
			$vInput.focus(function() {
				obj.$row.addClass("active");
			}).blur(function() {
				obj.$row.removeClass("active");
				obj.updateT2Pair($vInput, oldVV);
			}).keypress(function( event ) {
				if ( event.which == 13 ) {
					$vInput.blur();
				}
			});
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
		if (!kv || !/^[a-z0-9._]+$/i.test(kv)) {
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
		console.log($node.val() + " : " + oldVal + " : " + isValid);
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
				const $trs = obj.$t2Body.find('tr');
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

class StaticPropertyTable extends PropertyTable {
	constructor(device) {
//		super(device);
//		this.cid = cid;
//		this.cModel = config;
//		this.driverId = config[KEY_DEVICE_DRIVER];
//		if (device.datype == "C") {
//			this.dModel = _pdModel.getDeviceModel(this.driverId);
//		}
//		this.configRow = null;
//		this.ipRow = null;
//		this.portRow = null;
		super(device, KEY_STATIC, device.editorModel[KEY_STATIC]);
	}
	
	createUi() {
		super.createUi();

		const del = this.$editorUi.find('.main_footer');
		if (del) {
			del.remove();
		}
	}
	
	reload() {
		this.cModel = this.device.editorModel[KEY_STATIC];
		super.reload();
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
			var url = URL_PREFIX + 'dbhistory?';
			var dbType = COMPOSITE_DB;
			if (_curDevice) {
				dbType = _curDevice.dbType;
				url += 'dbtype=' + dbType + '&did=' + _curDevice.did;
			}
			url += '&' + Date.now();
			$.get(url, function(data) {
				data = $.parseJSON(data);
				$.each(data, function(index, version) {
					var commit = new CommitItem(dbType, version, index);
					commit.init(obj.$side);
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
	
	constructor(dbType, commit, index) {
		this.dbType = dbType;
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
		const obj = this;
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
		var button = $('<span class="class_span_commit_button"><button class="class_button_load_commit btn btn-sm btn-block btn-outline-primary">' + btText + '</button></span>');
		this.$control.append(button);
		button.find('button').click(function() {
			if (_curDevice == null) {
				alert('Please select a device first. Loading history version is only supported on a per-device base.')
				return;
			}
//			var url = URL_PREFIX + 'configload?inst=' + _inst + '&version=' + encodeURI(name) + "&" + Date.now();
			var url = URL_PREFIX + 'dbload?dbtype=' + obj.dbType + '&version=' + encodeURI(name) + "&" + Date.now();
			$.get(url, function(data) {
				try{
					_curDevice.fromHistory(data);
				} catch (e) {
					alert('failed to load model from the commit, ' + e);
					return;
				}
			}).fail(function(e) {
				alert('failed to load the version, ' + e.statusText);
			});
		});
		$parentUi.append(this.$control);
	}
	
};

class SearchWidget {
	constructor($textInput) {
		this.$textInput = $textInput;
		this.$msgPop = $textInput.parent().find('.messagepop');
		this.curSel = -1;
		this.lastText = null;
	}
		
	show($li) {
//		const name = $li.attr('name');
		const path = $li.attr('path');
		const did = $li.attr('did');
//		const parts = path.split('/');
//		const did = parts[1];
		const cid = $li.attr('cid');
		this.$msgPop.hide();
//		const axis = _instModel.getController(mcid).axes[aid];
		const device = _allDevices[did];
//		axis.load(mid);
		device.loadConfig(cid);
		this.$textInput.blur();
	}
	
	doSearch() {
		var target = '';
		this.curSel = -1;

		var html = '';
//		var found = {};
		const obj = this;
		$.each($.extend({}, _pdModel.configsByPath, _cdModel.configsByPath), function(name, pair) {
			var word = obj.$textInput.val().toLowerCase();
			if (name.toLowerCase().indexOf(word) >= 0) {
//				found[name] = path;
				const path = pair[0];
				const did = pair[1];
				const cid = pair[2];
				var desc = pair[3];
				if (desc) {
					desc = '(' + desc + ')';
				}
				html += '<li class="messageitem" href="#" name="' + name + '" path="' + path + '" did="' + did + '" cid="' + cid + '"><span class="widget_text">' + 
						name + ' => ' + path + ' ' + desc + '</span></li>';
			} else {
				var desc = pair[3];
				if (desc && desc.toLowerCase().indexOf(word) >= 0) {
//					found[name] = path;
					const path = pair[0];
					const cid = pair[2];
					const did = pair[1];
					desc = '(' + desc + ')';
					html += '<li class="messageitem" href="#" name="' + name + '" path="' + path + '" did="' + did + '" cid="' + cid + '"><span class="widget_text">' + 
						name + ' => ' + path + ' ' + desc + '</span></li>';
				} 
			}
		});
		
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
				StaticUtils.showWarning('Please select a device for saving. Saving is per-device based.')
				obj.close();
			}
		});
	
		this.$textInput.keypress(function(event) {
			if ( event.key === "Enter" ) {
				if (_curDevice) {
					_curDevice.save();
				} else {
					StaticUtils.showWarning('Please select a device for saving. Saving is per-device based.')
					obj.close();
				}
			}
		});
	}
	
	reset() {
		this.$textInput.val("");
	}
	
	close() {
		this.dialog.modal('hide');
	}
}

class ChangeDidModal {
	constructor()
	{
		this.dialog = $('#id_modal_changeNameDialog');
		this.$OK = $('#id_button_changeNameConfirm');
		this.$cancel = $('#id_button_changeNameDismiss');
		this.$textInput = $('#id_input_changeNameMessage');
		this.device = null;
	}
	
	init() {
		const obj = this;
		this.$OK.click(function() {
			obj.run();
		});
	
		this.$textInput.keypress(function(event) {
			if ( event.key === "Enter" ) {
				obj.run();
			}
		});
		this.$cancel.click(function() {
			obj.cancel();
		});
	}
	
	show() {
		this.dialog.modal('show');
	}
	
	run() {
		this.close();
		if (this.device) {
			this.device.changeName(this.$textInput.val());
		}
	}
	
	cancel() {
		if (this.device) {
			this.device.cancelChangeName();
		}		
	}
	
	close() {
		this.dialog.modal('hide');
	}
}

class AbstractModal {
	$bt;
	
	constructor($bt, modalId) {
		this.$bt = $bt;
		this.dialog = $('#' + modalId).first().clone();
	}
	
	run() {
		this.close();
	}
	
	close() {
		this.dialog.modal('hide');
	}
	
	open() {
		this.dialog.modal('show');
	}

}

class DeleteConfigModal extends AbstractModal {
	
	constructor($bt, device, cid)
	{
		super($bt, 'id_modal_deleteConfigDialog');
		this.device = device;
		this.cid = cid;
		this.$ok = this.dialog.find('#id_button_deleteConfigConfirm');
	}
	
	init() {
		const obj = this;
		obj.$bt.click(function() {
			obj.dialog.find('.modal-title').text("Please confirm deleting the configuration '" + obj.cid + "' from device '" + obj.device.did + "'.");
			obj.open();
		});
		obj.$ok.click(function() {
			obj.run();
		});
	}
	
	run() {
		this.close();
		this.device.deleteConfig(this.cid);
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

		const device = _cdModel.getDevice(did);
		var saveMsg = obj.$textInput.val().replace(/^\s+|\s+$/gm,'');
		
		try {
			device.remove(saveMsg);
		} catch (e) {
			StaticUtils.showError(e);
		} finally {
			obj.close();
		}
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

class NewConfigModal extends AbstractModal {

	constructor($bt, device)
	{
		super($bt, 'id_modal_newConfigDialog');
		this.device = device;
		this.$ok = this.dialog.find('#id_button_newConfig');
		this.$deviceName = this.dialog.find('#id_input_newDeviceName');
		this.$configName = this.dialog.find('#id_input_newConfigName');
	}
	
	init() {
		const obj = this;
		obj.$bt.click(function() {
			obj.$deviceName.val(obj.device.did);
			obj.$configName.val('');
			obj.open();
		});
		obj.$ok.click(function() {
			obj.run();
		});
		this.$configName.keypress(function(event) {
			if ( event.key === "Enter" ) {
				obj.run();
			}
		});
	}
	
	run() {
		this.close();
		const newCid = this.$configName.val();
		this.device.addNewConfig(newCid);
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

class NewDeviceButton {
	constructor($bt)
	{
		this.$bt = $bt;
	}
	
	init() {
		const obj = this;
		obj.$bt.click(function() {
			obj.addNew();
		});
	}
	
	addNew() {
		if (_curDevice == null || _curDevice.checkDirtyFlag()) {
			_editor.empty();
			_tabs.empty();
			_editorTitle.empty();
			_propertyTitle.empty();
			_property.empty();
			$('.class_ul_folder > li').removeClass('active');
//			_curDid = null;
			_historyBar.reload();

			const did = 'New_device';
//			const datype = 'C';
//			var html = '<div class="class_a_mc" href="#"><h6 class="sidebar-subheading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' + 
//				'<span class="class_span_mc_name" did="' + did + '" datype="' + datype + '"><i class="fas fa-caret-down"></i> ' + did + '</span>';
//			html += '<span class="class_span_mc_input class_span_hide"><input type="text" class="form-control class_mc_input" value="' + did + '"></span>';
//			html += '<span class="class_span_mc_control"><i class="fas fa-edit"></i> </span>';
//			html += '</h6></div><ul id="ul_mc_'+ did + '" class="nav flex-column class_ul_folder class_ul_hide ">';
//	
//			html += "</ul>";
//			const $div = $(html);
			const newModel = $.extend({}, JSON_TEMP_COMPOSITE_DEVICE);
			
			_cdModel.addNewCompositeDevice(did, newModel);
			
//			const device = new CompositeDevice(did, newModel, _cdModel.$compositeMenuBar);
//			_model[did] = newDev;
//			_curDeviceModel = new DeviceModel(did, newDev);
//			_curDid = did;
//			_curCid = null;
//			$('#id_div_sidebuttom').append($div);
//			devGroup.load();
//			loadCompositeDevice(did)
//			devGroup.activateEditing();
		}
	}
}

class HomeButton {
	constructor($button) {
		this.$button = $button;
	}
	
	init() {
		const obj = this;
		this.$button.click(function() {
			
			var okToGo = true;
			if (_curDevice != null) {
				if (_curDevice.isDirty()) {
					okToGo = confirm('You have unsaved changes in the current device. If you load another device, your change will be lost. Do you want to continue?');
				}
			}

			if (okToGo) {
				obj.run();
			} else {
				return;
			}
		});
	}
	
	run() {
		if (_curDevice != null) {
			_curDevice.hide();
			_curDevice = null;
		}
		_title.text('Please use the side bar to select a device configuration.');
		_errorReport.show();
		_historyBar.reload();
	}
}


$(document).ready(function() {
	
	if (typeof _DEBUG_ENABLED !== 'undefined') {
		if (_DEBUG_ENABLED) {
			return;
		}
	}
	
	$('#id_a_signout').click(function() {
		signout("sedbConfig.html");
	});
	
	StaticUtils.addPageTitle();

	_errorReport = new ErrorReportUi();
	_errorReport.createUi();
	
	_homeButton = new HomeButton($('#id_span_side_home'));
	_homeButton.init();

	const loadingLabel = new LoadingLabel($('#id_span_waiting'));
	loadingLabel.init();
	_saveButton = new SaveButton($('#id_button_saveConfirm'));
	_saveButton.init();
	
	_resetButton = new ResetButton($('#id_button_reset'));
	_resetButton.init();
	
	_removeModal = new RemoveModal('id_modal_deleteDialog');
	_removeModal.init();

	_changeDidModal = new ChangeDidModal();
	_changeDidModal.init();

	_pdModel.load(function() {
		_cdModel.load(null);
	});
	
	_historyBar = new HistoryBlock($("#id_div_main_area"), $('#id_div_right_bar'));
	$('#id_button_history').click(function(){
		_historyBar.toggle();
	})
	
	$('#id_button_reload_history').click(function() {
		_historyBar.reload();
	});
	_historyBar.toggle();
	
	const newDeviceButton = new NewDeviceButton($('#id_span_add'));
	newDeviceButton.init();
	
	const searchWidget = new SearchWidget($('#id_input_search_text'));
	searchWidget.init();
});
