const TITLE_TEXT = "Sample Environment Device Configuration Database"
const URL_PREFIX = "seyaml/"
const KEY_DEVICE_DRIVER = "driver";
const KEY_DEVICE_DESC = "desc";
const HTML_ID_PHYSICALBAR = '#id_div_sidebar';
const HTML_ID_COMPOSITEBAR = '#id_div_sidebuttom';
const HTML_HIDDEN_FILL_DIV = '<div class="div_fill div_hidden"/>';
const HTML_NAV_DIV = '<div class="nav"/>';

const TABLE_TIER1_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="34%">Key</th><th width="66%">Value</th></tr></thead><tbody>';
const TABLE_TIER2_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="40%">Key</th><th width="40%">Value</th><th width="20%"></th></tr></thead><tbody>';
const EMPTY_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" value="';
const EMPTY_ROW_PART2 = '"></td><td class="pair_value"><input type="text" class="form-control" value="';
const EMPTY_ROW_PART3 = '"></td><td class="pair_control input-group-btn"><button type="button" class="btn btn-outline-primary button_plus">+</button><button type="button" class="btn btn-outline-primary button_minus">-</button></td></tr>';
const DISABLED_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" disabled value="';

const KEY_DATYPE = "datype";
const TYPE_COMPOSITE = "C";
const PROPERTY_KEYWORDS = [
	KEY_DATYPE
];
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
const _option_prop = ["config_id", "ip", "port"];
const JSON_TEMP_COMPOSITE_DEVICE = {
		"datype" : "C"
}
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

const _message = $('#id_div_info');
const _title = $('#id_device_title');
const _editorTitle = $('#id_editor_subtitle');
const _tabs = $('#id_ul_tabs');
const _editor = $('#id_div_editor_table');
const _propertyTitle = $('#id_property_subtitle');
const _property = $('#id_div_property_table');

var _curDevice;
var _curDid;

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
		showMsg(errorMsg, 'danger', timeLast);
	}

	static showWarning(warnMsg, timeLast) {
		showMsg(warnMsg, 'warning', timeLast);
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

	constructor() {
		this.$physicalMenuBar = $(HTML_ID_PHYSICALBAR);
		this.$compositeMenuBar = $(HTML_ID_COMPOSITEBAR);
		this.url = URL_PREFIX + 'sedb';
		this.devices = {};
		this.physicalDevices = {};
		this.compositeDevices = {};
		this.configsByPath = {};
		this.firstConfigs = {};
		this.configNamesOfDevice = [];
	}
	
	get model() {
		return this.#model;
	}
	
	set model(model) {
		this.#model = model;
	}
	
	get physicalDids() {
		return this.physicalDevices.keys();
	}
	
	get compositeDids() {
		return this.compositeDevices.keys();
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
	
	load() {
		var obj = this;
		$.get(this.url, function(data) {
			obj.model = data;
			obj.configs = {};
			obj.firstConfigs = {};
			obj.configNamesOfDevice = {};
			$.each(data, function(did, deviceModel) {
				const names = [];
				$.each(deviceModel, function(cid, cfg) {
					if (!(PROPERTY_KEYWORDS.includes(cid))) {
						var path = obj.type + "/" + did + "/" + cid;
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
		$.each(this.model, function(did, deviceModel){
			const datype = deviceModel["datype"];
			if (datype == "C") {
				const device = new CompositeDevice(did, deviceModel, obj.$compositeMenuBar);
				device.createMenuUi();
				obj.setDevice(did, device);
				obj.compositeDevices[did] = device;
			} else {
				const device = new PhysicalDevice(did, deviceModel, obj.$physicalMenuBar);
				device.createMenuUi();
				obj.setDevice(did, device);
				obj.compositeDevices[did] = device;
			}
		});
	}

	addNewCompositeDevice(did, deviceModel) {
		const device = new CompositeDevice(did, deviceModel, this.$compositeMenuBar);
		device.createMenuUi();
		this.setDevice(did, device);
		this.compositeDevices[did] = device;
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
	
	removeController(mcid) {
		const obj = this;
		if (mcid in this.model) {
			const controllerModel = this.model[mcid];
			$.each(controllerModel, function(aid, cfg) {
				var name = mcid + ":" + aid;
				delete obj.motors[name];
			});
			delete this.model[did];
			delete this.firstMotor[did];
			delete this.configNamesOfDevice[did];
			delete this.devices[did];
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
const _dbModel = new DBModel();

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
		this.editorModel = $.extend(true, {}, model);
		this.$parentUi = $parentUi;
		this.tabUi = new TabUi(this);
		this.configEditors = {};
		this.configMenuDict = {};
		this.curCid = null;
	}

	get datype() {
		return this.model["datype"];
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
		return this.type + ":" + this.did;
	}
	
	setDirtyFlag() {
		if (!this.isDirty()) {
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
		$.each(Object.values(this.configEditors), function(idx, editor) {
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
		var url = URL_PREFIX + 'dbsave?msg=';
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
					StaticUtils.showMsg("Saved in the server.");
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
					StaticUtils.showMsg(data["reason"], 'danger');
				}
			} catch (e) {
				StaticUtils.showMsg("Failed to save: " + e.statusText, 'danger');
			}
		}).fail(function(e) {
			console.log(e);
			StaticUtils.showMsg("Faied to save: " + e.statusText, "danger");
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
		
	remove() {}
	
	fromHistory() {}
	
	deleteConfig(cid) {}
}

class PhysicalDevice extends AbstractDevice {
	
	constructor(did, model, $parentUi)
	{
		super(did, model, $parentUi);
		this.rootEditor = new ImmutableRootUi(this);
		this.$addBt = null;
	}
	
	get type() {
		return this.model[KEY_DATYPE];
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
		$.each(this.model, function(cid, config) {
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

			_editorTitle.text('Use the menu bar in the left to load a configuration of this device.');

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
				StaticUtils.showMsg("Failed to create new configuration: no existing one to copy", "danger");
				return;
			}
		}
		
		var newName = cid.trim();
		newName = newName.replace(/\W/g, '_');
		if (cid in obj.model) {
			StaticUtils.showMsg("Failed to create new configuration: id already exists, " + newName, "danger");
			return;
		}
		
		var copyModel = obj.model[copyCid];
		if (copyModel === undefined) {
			StaticUtils.showMsg("Faied to copy: " + obj.did + "/" + copyCid + ", config not found", "danger");
			return;
		}
		
		obj.model[newName] = $.extend(true, {}, copyModel);
		obj.editorModel[newName] = $.extend(true, {}, copyModel);
		
		const subItem = new ConfigMenuItem(obj, newName);
		subItem.createUi(obj.$menuUl);
		obj.configMenuDict[newName] = subItem;
		
		obj.$addBt.detach();
		obj.$menuUl.append(obj.$addBt);

		obj.loadConfig(newName);
	}
	
	deleteConfig(cid) {
		const obj = this;
		obj.setDirtyFlag();

		const subDevice = obj.model[cid];
		if (subDevice == null) {
			StaticUtils.showMsg('sub device not found: ' + cid, 'danger');
			return;
		}
		const rmType = subDevice[KEY_DATYPE];
		const rmId = subDevice[ID_PROP_ID];
		
		delete obj.model[cid];
		delete obj.editorModel[cid];
		
		const menuItem = obj.configMenuDict[cid];
		menuItem.dispose();
		
		obj.tabUi.removeConfigTab(cid);
		
		obj.configEditors[cid].dispose();
		obj.load();
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
	
	constructor(did, model, $parentUi)
	{
//		this.#did = did;
//		this.#model = model;
//		this.#parentUi = parentUi;
		super(did, model, $parentUi);
		this.rootEditor = new MutableRootUi(this);
		this.newDid = null;
	}

	get type() {
		return TYPE_COMPOSITE;
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
				delete _dbModel.model[obj.did];
				_dbModel.model[newDid] = obj.model;
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
			console.log(obj.$textbox.val());
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

			this.curCid = cid;
			const obj = this;
			var config = obj.editorModel[cid]; 
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
	
	makeSubDevice(subDid) {
		const obj = this;
		const did = obj.did;
		const deviceModel = _dbModel.getDeviceModel(subDid);
		if (deviceModel == null) {
			StaticUtils.showMsg('device not found: ' + subDid, 'danger');
			return;
		}
		const subType = deviceModel['datype'].toUpperCase();
		var idx = 1;
		var newName;
		while(true) {
			newName = subDid + '_' + idx;
			if (!(newName in this.model)) {
				break;
			}
			idx++;
		}
		var newId = 1;
		$.each(this.model, function(key, config) {
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
		var configs = _dbModel.getCidsOfDevice(subDid);
		if (configs.length > 0) {
			subDevice["config_id"] = configs[0];
		}
		var source = _dbModel.getDeviceModel(subDid)[configs[0]];
		subDevice["datype"] = subType;
		subDevice["driver"] = subDid;
		subDevice["id"] = newId;
		var ips = source["ip"];
		if (typeof ips === 'object') {
			subDevice["ip"] = ips[0];
		} else {
			subDevice["ip"] = ips;
		}
		var desc = source["desc"];
		if (desc) {
			subDevice["desc"] = desc;
		}
		subDevice["name"] = DEFAULT_SUB_DEVICE_NAME_PREFIX[subType] + newId;
		subDevice["port"] = source["port"];
		obj.model[newName] = subDevice;
		obj.editorModel[newName] = $.extend({}, subDevice);
		const newSubDevice = {};
		const subHtml = '<div class="class_div_device_page" id="div_page_' + did + '_' + newName + '"><a href="#" class="class_a_cid_delete"><i class="fas fa-square-minus"></i> </a>'
			+ '<div class="class_div_device_item"><a href="#" class="class_a_cid_label" did="' 
			+ did + '" cid="' + newName + '">' + newName + '</a></div></div>';
		newSubDevice["html"] = subHtml;
//		newSubDevice["html"] = '<div class="class_div_device_item">' + newName + '</div>';
		newSubDevice["cid"] = newName;
		return newSubDevice;
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
					StaticUtils.showMsg("Removed successfully in the server.");
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
					StaticUtils.showMsg(data["reason"], 'danger');
				}
			} catch (e) {
				StaticUtils.showMsg("Failed to remove: " + e.statusText, 'danger');
			}
		}).fail(function(e) {
			console.log(e);
			StaticUtils.showMsg("Faied to remove: " + e.statusText, "danger");
		}).always(function() {
			$('#id_modal_deleteDialog').modal('hide');
		});
	}
	
	changeName(msg) {
		const obj = this;
		var url = URL_PREFIX + 'changeName?msg=';
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
					delete _dbModel.model[oldDid];
					_dbModel.model[newDid] = deviceModel;
					if (_curDevice !=  null && _curDevice.did == obj.did) {
						_title.text(newDid + " (Composite Device)");
					}
					StaticUtils.showMsg("Device name saved in the server.");
					_changeDidModal.device = null;
					$('td.editable input.changed').removeClass('changed');
					setTimeout(_historyBar.reload, 3000)
				} else {
					StaticUtils.showMsg("Failed to rename the device: " + data["reason"], 'danger');
//					deviceItem.resetChange();
				}
			} catch (e) {
				StaticUtils.showMsg("Failed to rename the device: " + e, 'danger');
//				deviceItem.resetChange();
			}
		}).fail(function(e) {
			StaticUtils.showMsg("Failed to talk to the server: " + e.statusText, "danger");
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
		const device = _dbModel.model[newDid];
		delete _dbModel.model[newDid];
		_dbModel.model[did] = device;
		
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
	
	deleteConfig(cid) {
		const obj = this;
		obj.setDirtyFlag();

		const subDevice = obj.model[cid];
		if (subDevice == null) {
			StaticUtils.showMsg('sub device not found: ' + cid, 'danger');
			return;
		}
		const rmType = subDevice[KEY_DATYPE];
		const rmId = subDevice[ID_PROP_ID];
		
		delete obj.model[cid];
		delete obj.editorModel[cid];
		
		const menuItem = obj.configMenuDict[cid];
		menuItem.dispose();
		
		obj.tabUi.removeConfigTab(cid);
		
		if (obj.rootEditor != null) {
			obj.rootEditor.deleteConfigPage(cid);
		}
		obj.configEditors[cid].dispose();
		
		$.each(obj.model, function(key, config) {
			if (key != KEY_DATYPE) {
				var iType = config[KEY_DATYPE];
				if (iType == rmType) {
					var iId = config[ID_PROP_ID];
					if (iId > rmId) {
						iId--;
						obj.editorModel[key][ID_PROP_ID] = iId;
						obj.editorModel[key][ID_PROP_NAME] = DEFAULT_SUB_DEVICE_NAME_PREFIX[iType] + iId;
					}
				}
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
		console.log('dispose ' + this.cid);
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
		$.each(obj.device.model, function(subDid, config) {
			if (! (PROPERTY_KEYWORDS.includes(subDid))) {
//				html += '<div class="class_div_device_page" id="div_page_' + obj.device.did + '_' + cid + '">'
//					+ '<div class="class_div_device_item"><a href="#" class="class_a_cid_label" did="' 
//					+ obj.device.did + '" cid="' + cid + '">' + cid + '<br>(' + config[KEY_DEVICE_DRIVER] + ')' + '</a></div></div>';
				html += '<div class="class_div_device_page" id="div_page_' + obj.device.did + '_' + subDid + '"><a href="#" class="class_a_cid_delete" did="' 
					+ obj.device.did + '" cid="' + subDid + '"><i class="fas fa-square-minus"></i> </a>'
					+ '<div class="class_div_device_item"><a href="#" class="class_a_cid_label" did="' 
					+ obj.device.did + '" cid="' + subDid + '">' + subDid + '<br>(' + config[ID_PROP_DRIVER] + ')' + '</a></div></div>';
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
		this.$editorUi.append($div);
		
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
		console.log("remove page " + cid);
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
			this.dModel = _dbModel.getDeviceModel(this.driverId);
		}
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
					var pRow = object.createRow(key, val, null, false);
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
		
		var $del = $('<div class="main_footer"><span id="id_button_del_config" class="btn btn-outline-primary btn-block " href="#"><i class="fas fa-remove"></i> Remove This Configuration from Device ' + object.device.did + '</span></div>');
		this.$editorUi.append($del);
		const deleteConfigModal = new DeleteConfigModal($del, object.device, object.cid);
		deleteConfigModal.init();
		
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
	constructor(device, cid, $tr) {
		this.device = device;
		this.$row = $tr;
		this.cid = cid;
		this.configModel = device.model[cid];
//		var colKey = tr.find('.editable_key');
		this.$colValue = $tr.find('.editable_value');
		this.$t1Value = this.$colValue.find("> input");
		this.key = $tr.attr('key');
		this.addEventHandler();
	}
	
	addEventHandler() {
		const obj = this;
		const colType = this.$row.find('.editable_type');
		const sel = colType.find('select');
		const oldVal = obj.configModel[this.key];
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
		t1Value.focus(function() {
			obj.$row.addClass("active");
		}).blur(function() {
			obj.$row.removeClass("active");
			obj.updateNode(t1Value, t1Value.val());
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
					obj.updateT2Pair(t2Key, obj.key, t2Body, oldKV, isPair);
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
				obj.updateT2Pair(t2Value, obj.key, t2Body, oldVV, isPair);
			}).keypress(function( event ) {
				if ( event.which == 13 ) {
					t2Value.blur();
				}
			});
			
			var t2Add = t2Body.find("td.pair_control > button.button_plus");
			t2Add.click(function() {
				obj.addRow(t2Add, obj.key, t2Body, isPair);
			});
			var t2Remove = t2Body.find("td.pair_control > button.button_minus");
			t2Remove.click(function() {
				obj.removeRow(t2Remove, obj.key, t2Body, isPair);
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
	
	updateT2Pair($node, key, tbody, oldVal, isPair) {
		const obj = this;
		var tr = $node.parent().parent();
		var $key = tr.find("td.pair_key > input");
		var $value = tr.find("td.pair_value > input");
//		var isPair = !$key.prop('disabled');
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
			
			var curVal = obj.device.editorModel[obj.cid][obj.key];
			var isChanged = true;
			console.log(typeof curVal);
			if (typeof curVal === 'object') {
				if (curVal.hasOwnProperty(kv)) {
					var cv = curVal[kv];
					if (cv == vv) {
						isChanged = false;
					}
				}
			}
			if (isChanged) {
				obj.device.setDirtyFlag();
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
					obj.device.editorModel[obj.cid][obj.key] = newVal;
				} else {
					newVal = [];
					var trs = tbody.find('tr');
					trs.each(function() {
						var rv = $(this).find('td.pair_value > input').val();
						newVal.push(rv);
					});
					obj.device.editorModel[obj.cid][obj.key]  = newVal;
				}
			} 
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
	
	addRow(bt, key, t2Body, isPair) {
		const obj = this;
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
				obj.updateT2Pair($(this), key, t2Body, oldKV, isPair);
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
			obj.updateT2Pair($(this), key, t2Body, oldVV, isPair);
		}).keypress(function( event ) {
			if ( event.which == 13 ) {
				$(this).blur();
			}
		});
		
		var t2Add = nRow.find("td.pair_control > button.button_plus");
		t2Add.click(function() {
			obj.addRow($(this), key, t2Body, isPair);
		});
		var t2Remove = nRow.find("td.pair_control > button.button_minus");
		t2Remove.click(function() {
			obj.removeRow($(this), key, t2Body, isPair);
		});

		if (isPair) {
			t2Key.focus();
		} else {
			t2Value.focus();
		}
	}

	removeRow(bt, key, tbody, isPair) {
		const obj = this;
		
		var cRow = bt.parent().parent();
		cRow.remove();
		
		
		if (isPair) {
			var newVal = {};
			var trs = tbody.find('tr');
			trs.each(function() {
				var rk = $(this).find('td.pair_key > input').val();
				var rv = $(this).find('td.pair_value > input').val();
				newVal[rk] = rv;
			});
		} else {
			newVal = [];
			var trs = tbody.find('tr');
			trs.each(function() {
				var rv = $(this).find('td.pair_value > input').val();
				newVal.push(rv);
			});
		}
		obj.device.editorModel[obj.cid][obj.key] = newVal;
	};

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
			if (_curDid) {
				url += 'did=' + _curDid;
			}
			url += '&' + Date.now();
			$.get(url, function(data) {
				data = $.parseJSON(data);
				$.each(data, function(index, version) {
					var commit = new CommitItem(version, index);
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
		this.$control.append('<span class="badge badge-secondary class_span_commit_timestamp">' + star + StaticUtils.getTimeString(timestamp) + '</span>');
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
		const mid = $li.attr('mid');
		const parts = path.split('/');
		const mcid = parts[1];
		const aid = parts[2];
		this.$msgPop.hide();
		const axis = _instModel.getController(mcid).axes[aid];
		axis.load(mid);
		this.$textInput.blur();
	}
	
	doSearch() {
		var target = '';
		this.curSel = -1;

		var html = '';
//		var found = {};
		const obj = this;
		$.each(_dbModel.motors, function(name, pair) {
			var word = obj.$textInput.val().toLowerCase();
			if (name.toLowerCase().indexOf(word) >= 0) {
//				found[name] = path;
				var path = pair[0];
				var mid = pair[1];
				var desc = pair[2];
				if (desc) {
					desc = '(' + desc + ')';
				}
				html += '<li class="messageitem" href="#" name="' + name + '" path="' + path + '" mid="' + mid + '"><span class="widget_text">' + 
						name + ' => ' + path + ' ' + desc + '</span></li>';
			} else {
				var desc = pair[2];
				if (desc && desc.toLowerCase().indexOf(word) >= 0) {
//					found[name] = path;
					var path = pair[0];
					var mid = pair[1];
					desc = '(' + desc + ')';
					html += '<li class="messageitem" href="#" name="' + name + '" path="' + path + '" mid="' + mid + '"><span class="widget_text">' + 
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
				StaticUtils.showMsg('Please select a device for saving. Saving is per-device based.', 'warning')
				obj.close();
			}
		});
	
		this.$textInput.keypress(function(event) {
			if ( event.key === "Enter" ) {
				if (_curDevice) {
					_curDevice.save();
				} else {
					StaticUtils.showMsg('Please select a device for saving. Saving is per-device based.', 'warning')
					obj.close();
				}
			}
		});
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
			_curDid = null;
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
			
			_dbModel.addNewCompositeDevice(did, newModel);
			
//			const device = new CompositeDevice(did, newModel, _dbModel.$compositeMenuBar);
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
		_historyBar.reload();
	}
}


$(document).ready(function() {
	
	$('#id_a_signout').click(function() {
		signout("sedbConfig.html");
	});
	
	StaticUtils.addPageTitle();
	
	const homeButton = new HomeButton($('#id_span_side_home'));
	homeButton.init();

	const loadingLabel = new LoadingLabel($('#id_span_waiting'));
	loadingLabel.init();
	_saveButton = new SaveButton($('#id_button_saveConfirm'));
	_saveButton.init();
	
	_resetButton = new ResetButton($('#id_button_reset'));
	_resetButton.init();
	
//	_removeModal = new RemoveModal('id_modal_deleteDialog');
//	_removeModal.init();

	_changeDidModal = new ChangeDidModal();
	_changeDidModal.init();

	_dbModel.load();
	
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
