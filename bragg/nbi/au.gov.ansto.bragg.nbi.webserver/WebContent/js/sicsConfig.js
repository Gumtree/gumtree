const HTML_ID_SIDEBAR = '#id_div_sidebar';

const URL_PREFIX = 'yaml/';
const PAR_INSTRUMENT_ID = "inst";
const _inst = getParam(PAR_INSTRUMENT_ID);
const BRANCH_GALIL = 'GALIL_CONTROLLERS';

const KEY_DEVICE_DESC = "description";

const HTML_NAV_DIV = '<div class="nav"/>';
const HTML_HIDDEN_FILL_DIV = '<div class="div_fill div_hidden"/>';
//const TABLE_TIER1_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="34%">Key</th><th width="66%">Value</th></tr></thead><tbody>';
//const TABLE_TIER2_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="40%">Key</th><th width="40%">Value</th><th width="20%"></th></tr></thead><tbody>';
const TABLE_TIER1_HEADER = '<table class="table table-striped table-sm"><thead><tr><th class="editable_control div_hidden" width="5%"></th><th width="25%">Key</th><th width="20%">Type</th><th width="50%">Value</th></tr></thead><tbody>';
const TABLE_TIER2_HEADER = '<table class="table table-striped table-sm"><thead><tr><th width="40%">Key</th><th width="40%">Value</th><th width="20%"></th></tr></thead><tbody>';
const EMPTY_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" value="';
const EMPTY_ROW_PART2 = '"></td><td class="pair_value"><input type="text" class="form-control" value="';
const EMPTY_ROW_PART3 = '"></td><td class="pair_control input-group-btn"><button type="button" class="btn btn-outline-primary button_plus">+</button><button type="button" class="btn btn-outline-primary button_minus">-</button></td></tr>';

const NEW_ROW_TIER1 = '<tr class="editable_row">'
		+ '<td class="editable_control"><span class="property_control"><i class="fas fa-minus"> </span></td>'
		+ '<td class="editable_key"><input type="text" class="form-control" value=""></td>'
		+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair">name-value pair</option></select></td>'
		+ '<td class="editable_value"><input type="text" class="form-control" value=""></td></tr>';

const PLUS_ROW_TIER1 = '<tr class="editable_row"><td class="editable_control div_hidden" style="line-height: 2"><span class="property_control"><i class="fas fa-plus"> </span></td><td/><td/><td/></tr>';

const DISABLED_ROW_PART1 = '<tr class="tr_entry"><td class="pair_key"><input type="text" class="form-control" disabled value="';

const _message = $('#id_div_info');
const _title = $('#id_device_title');
const _editorTitle = $('#id_editor_subtitle');
const _tabs = $('#id_ul_tabs');
const _editor = $('#id_div_editor_table');
const _propertyTitle = $('#id_property_subtitle');
const _property = $('#id_div_property_table');

var _homeButton;
var _historyBar;
var _curAxis;
var _manageButton;

var showMsg = function(msg, type, timeLast) {
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
};

var showError = function(errorMsg, timeLast) {
	showMsg(errorMsg, 'danger', timeLast);
}

var showWarning = function(warnMsg, timeLast) {
	showMsg(warnMsg, 'warning', timeLast);
}

class InstrumentModel {
	
	
	#model;
	#didOfAxis;
	#controllers = {};

	constructor() {
		this.$menuBar = $(HTML_ID_SIDEBAR);
		this.axes = {};
		this.motors = {};
		this.firstMotor = {};
		this.motorIdsOfAxis = {};
		this.url = URL_PREFIX + 'model?inst=' + _inst + '&' + Date.now();
	}
	
	get model() {
		return this.#model;
	}
	
	set model(model) {
		this.#model = model;
	}
	
	get controllers() {
		return this.#controllers;
	}
	
	get controllerNames() {
		return this.#controllers.keys();
	}
	
	getAidsOfController(mcid) {
		return this.controllers[mcid];
	}
	
	getControllerModel(mcid) {
		return this.model[mcid];
	}
	
	getController(mcid) {
		return this.controllers[mcid];
	}
	
	setController(mcid, controller) {
		this.controllers[mcid] = controller;
	}
	
	load() {
		var obj = this;
		$.get(this.url, function(data) {
			const galil = data[BRANCH_GALIL];
			obj.model = galil;
			$.each(galil, function(mcid, controller) {
				$.each(controller, function(aid, axis) {
					const path = "/" + mcid + "/" + aid;
					const names = [];
					var isFirst = true;
					$.each(axis, function(mid, motorModel) {
						var desc = "";
						if (motorModel.hasOwnProperty(KEY_DEVICE_DESC)) {
							desc = motorModel[KEY_DEVICE_DESC];
						}
						var name = mcid + ":" + aid + ":" + mid
						obj.motors[name] = [path, mid, desc];
						if (isFirst) {
							obj.firstMotor[path] = mid;
							isFirst = false;
						}
						names.push(mid);
					});
					obj.motorIdsOfAxis[path] = names;
					obj.axes[path] = axis;
				});
			});
			obj.createUi();
//			$("#id_div_sidebar").empty();
//			showModelInSidebar();
		}).fail(function(e) {
			if (e.status == 401) {
				alert("sign in required");
				window.location = 'signin.html?redirect=sicsConfig.html?inst=' + _inst;
			} else {
				alert(e.statusText);
			}
		});
	}
		
	createUi() {
		const obj = this;
		$.each(Object.keys(obj.model).sort(), (idx, mcid) => {
			const controller = new Controller(mcid, obj.model[mcid], obj.$menuBar);
			controller.createMenuUi();
			obj.setController(mcid, controller);
		});
	}

	getNextControllerId() {
		var mcIdx = 1;
		var mcName = "mc" + mcIdx;
		while(mcName in this.model) {
			mcIdx++;
			mcName = "mc" + mcIdx;
		}
		return mcName;
	}
	
	addController(newMcid, newAid, newMid) {
		newMcid = newMcid.trim();
		newMcid = newMcid.replace(/\W/g, '_');
		if (newMcid.length == 0) {
			throw new Error('Controller name cannot be empty.');
		}
		if (newMcid in this.model) {
			throw new Error('motion controller already exists: ' + newMcid);
		}
		
		newAid = newAid.trim();
		newAid = newAid.replace(/\W/g, '_');
		if (newAid.length == 0) {
			throw new Error('Axis name cannot be empty.');
		}
		
		newMid = newMid.trim();
		newMid = newMid.replace(/\W/g, '_');
		if (newMid.length == 0) {
			throw new Error('Motor name cannot be empty.');
		}
		
		const obj = this;
		var toCopy;
		if (_curAxis != null) {
			if (_curAxis.curMid != null) {
				toCopy = _curAxis.model[_curAxis.curMid];
			} else {
				toCopy = Utils.getFirstObject(_curAxis.model);
			}
		} else {
			toCopy = Utils.getFirstObject(Utils.getFirstObject(Utils.getFirstObject(obj.model)));
		}
		const newMotorModel = $.extend(true, {}, toCopy);
		const newAxisModel = {};
		const newControllerModel = {};
		newAxisModel[newMid] = newMotorModel;
		newControllerModel[newAid] = newAxisModel;
		obj.model[newMcid] = newControllerModel;
		const controller = new Controller(newMcid, newControllerModel, obj.$menuBar);
		controller.createMenuUi();
		obj.setController(newMcid, controller);
		
		var desc = "";
		if (newMotorModel.hasOwnProperty(KEY_DEVICE_DESC)) {
			desc = newMotorModel[KEY_DEVICE_DESC];
		}
		obj.updatePathTable(newMcid, newAid, newMid, desc)
		
		controller.axes[newAid].setDirtyFlag();
		controller.axes[newAid].load(newMid);
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
}
const _instModel = new InstrumentModel();

class Controller {
	mcid;
	model;
	motors;
	$addAxis;
	$parentUi;
	$menuHeader;
	$menuUl;
	
	constructor(mcid, model, $parentUi)
	{
		this.mcid = mcid;
		this.model = model;
		this.$parentUi = $parentUi;
		this.axes = {};
		this.motors = {};
		this.motorMenuDict = {};
	}
	
	getAxis(aid) {
		return this.axes[aid];
	}
	
	createMenuUi() {
		const obj = this;
		var html = '<div class="class_a_mc" href="#"><h6 class="sidebar-subheading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 ">' 
				+ '<span class="class_span_mc_name" mcid="' + this.mcid + '"><i class="fas fa-caret-down"></i> ' + this.mcid + '</span>';
		html += '</h6></div>';
		
		this.$menuHeader = $(html);
		this.$menuHeader.find('span.class_span_mc_name').click(function() {
			obj.$menuUl.toggleClass('class_ul_hide');
		});
		
//		this.$menuHeader.find('span.class_span_mc_control').click(function() {
//			_removeModal.toRemove = obj.aid;
//			_removeModal.open();
//		});
		
		const $menuUl = $('<ul id="ul_mc_'+ this.aid + '" class="nav flex-column class_ul_hide class_ul_folder"></ul>');
		$.each(Object.keys(obj.model).sort(), function(idx, aid) {
			const axis = new Axis(obj, aid, obj.model[aid], $menuUl);
			axis.createMenuUi();
			obj.axes[aid] = axis;
//			obj.motors[aid] = axis;
		});
		this.$addAxis = $('<li id="li_menu_' + obj.mcid + '_NEW_DEVICE' + '" class="nav-item class_li_subitem">'
				+ '<a class="nav-link class_a_add_axis" mcid="' + obj.mcid 
				+ '" href="#" data-toggle="tooltip" title="Duplicate the selected axis in the MC. If none selected, duplicate the first axis."><i class="fas fa-plus"> </a></li>');
		$menuUl.append(this.$addAxis);
		this.$menuUl = $menuUl;
		
		this.$parentUi.append(this.$menuHeader);
		this.$parentUi.append(this.$menuUl);
		
		const addAxisDialog = new NewAxisModel(this.$addAxis, obj);
		addAxisDialog.init();
	}

	_setMenuItemActive(aid, mid) {
		this.$menuUl.find('li.class_li_subitem').removeClass('active');
		if (aid) {
			this.axes[aid].setMenuActive(true);
			if (mid) {
				this.axes[aid].tabUi.setActive(mid);
			}
		}
	}
	
	setMenuActive(isActive, aid, mid) {
		if (isActive) {
			this.$menuHeader.find(".class_span_mc_name").addClass("span_highlight");
			if (this.$menuUl) {
				this.$menuUl.removeClass("class_ul_hide");
			}
		} else {
			this.$menuHeader.find(".class_span_mc_name").removeClass("span_highlight");
//			if (this.$menuUl) {
//				this.$menuUl.addClass("class_ul_hide");
//			}
		}
		this._setMenuItemActive(aid, mid);
	}
	
	getNextAxisId() {
		var newAid = "A";
		while(newAid in this.model) {
			const cc = newAid.charCodeAt(0) + 1;
			newAid = String.fromCharCode(cc);
		}
		return newAid;
	}
	
	addAxis(newAid, newMid) {
		
		newAid = newAid.trim();
		newAid = newAid.replace(/\W/g, '_');
		if (newAid.length == 0) {
			throw new Error('Axis name cannot be empty.');
		}
		
		newMid = newMid.trim();
		newMid = newMid.replace(/\W/g, '_');
		if (newMid.length == 0) {
			throw new Error('Motor name cannot be empty.');
		}
		
		const obj = this;
//		const toCopy = Utils.getFirstObject(Utils.getFirstObject(Utils.getFirstObject(obj.model)));
		var toCopy;
//		if (_curAxis != null && _curAxis.controller.mcid == obj.mcid) {
//		if (_curAxis != null) {
//			if (_curAxis.curMid != null) {
//				toCopy = _curAxis.model;
//			}
//		} else {
//			toCopy = Utils.getFirstObject(obj.model);
//		}
//		if (toCopy == null) {
//			toCopy = Utils.getFirstObject(Utils.getFirstObject(_instModel.model));
//		}
//		toCopy = Utils.getFirstObject(toCopy);
		
		var toCopy;
		if (_curAxis != null) {
			if (_curAxis.curMid != null) {
				toCopy = _curAxis.model[_curAxis.curMid];
			} else {
				toCopy = Utils.getFirstObject(_curAxis.model);
			}
		} else {
			const axisModel = Utils.getFirstObject(obj.model);
			if (axisModel != null) {
				toCopy = Utils.getFirstObject(axisModel);
			}
		}
		if (toCopy == null) {
			toCopy = Utils.getFirstObject(Utils.getFirstObject(Utils.getFirstObject(_instModel.model)));
		}

		const newMotorModel = $.extend(true, {}, toCopy);
		const newAxisModel = {};
		newAxisModel[newMid] = newMotorModel;
		obj.model[newAid] = newAxisModel;
		
		const axis = new Axis(obj, newAid, newAxisModel, obj.$menuUl);
		axis.createMenuUi();
		obj.axes[newAid] = axis;
		
		obj.$addAxis.detach();
		obj.$menuUl.append(obj.$addAxis);
		
		var desc = "";
		if (newMotorModel.hasOwnProperty(KEY_DEVICE_DESC)) {
			desc = newMotorModel[KEY_DEVICE_DESC];
		}
		_instModel.updatePathTable(obj.mcid, newAid, newMid, desc);
		
		axis.setDirtyFlag();
		axis.load(newMid);
	}
	
	deleteAxis(aid, commitMsg) {
		const obj = this;
		const axis = obj.axes[aid];
		var url = URL_PREFIX + 'delete?inst=' + _inst + '&path=' + axis.path + '&msg=';
		if (commitMsg.length > 0) {
			url += encodeURI(commitMsg);
		}
		url += "&" + Date.now();
		$.get(url, function(data) {
			try {
				if (data["status"] == "OK") {
					showMsg("Deleted successfully in the server.");
					if (_curAxis != null && _curAxis.aid == aid) {
						_curAxis = null;
					}
					axis.dispose();
					delete obj.axes[aid];
					obj.motors = {};
					obj.motorMenuDict = {};
					_homeButton.run();
					if (Object.keys(obj.axes).length == 0) {
						obj.dispose();
					}
				} else {
					showMsg(data["reason"], 'danger');
				}
			} catch (e) {
				showMsg("Failed to delete: " + e.statusText, 'danger');
			}
		}).fail(function(e) {
			console.log(e);
			showMsg("Faied to delete: " + e.statusText, "danger");
		});
	}
	
	dispose() {
		this.$addAxis.remove();
		this.$menuUl.remove();
		this.$menuHeader.remove();
		this.axes = {};
		this.motors = {};
		this.motorMenuDict = {};
	}
}

class Axis {
	
	controller;
	aid;
	model;
	mids;
//	motors;
	firstMid;
	#dirtyFlag = false;
	$parentUi;
	$ui;
	tabUi;
//	rootEditor;
	motorEditors;
	
	constructor(controller, aid, model, $parentUi)
	{
		this.controller = controller;
		this.aid = aid;
		const mids = [];
		this.mids = mids;
//		this.devices = {};
		this.model = model;
		this.editorModel = $.extend(true, {}, model);
		this.$parentUi = $parentUi;
		this.motorEditors = {};
		this.tabUi = new TabUi(this);
//		this.rootEditor = new MutableRootUi(this);
//		this.motorMenuDict = {};
		this.curMid = null;
		$.each(this.model, function(mid, motorModel) {
			mids.push(mid);
		});
		this.firstMid = mids[0];
	}

	dispose() {
		this.$ui.remove();
		this.tabUi.dispose();
		$.each(Object.values(this.motorEditors), function(idx, propTable) {
			propTable.dispose();
		});
		this.editorModel = {};
		this.mids = [];
	}
	
	get path() {
		return "/" + this.controller.mcid + "/" + this.aid;
	}
	
	setDirtyFlag() {
		if (!this.isDirty()) {
			this.#dirtyFlag = true;
			this.$ui.find('a.class_a_axis').append('<i class="fas fa-asterisk i_changed"> </i>');
		}
//		super.setDirtyFlag();
	}
	
	clearDirtyFlag() {
//		super.clearDirtyFlag();
		this.#dirtyFlag = false;
		this.$ui.find('i.i_changed').remove();
	}
	
	isDirty() {
		return this.#dirtyFlag;
	}
	
	createMenuUi() {
		const obj = this;
		var html = '<li id="li_menu_' + obj.controller.mcid + '_' + obj.aid + '" class="nav-item class_li_subitem"><a class="nav-link class_a_axis" mcid="' 
			+ this.controller.mcid + '" aid="' + this.aid + '" href="#">' 
			+ this.aid + '</a></li>';
		this.$ui = $(html);
		this.$ui.click(function(){
			obj.load();
		});
		this.$parentUi.append(this.$ui);
	}
	
	setMenuActive(flag) {
		if (flag) {
			if (!(this.$ui.hasClass("active"))) {
				this.$ui.addClass("active");
			}
		} else {
			this.$ui.removeClass("active");
		}
	}
	
	checkDirtyFlag() {
		var okToGo = true;
		if (_curAxis != null) {
			if (_curAxis.path != this.path && _curAxis.isDirty()) {
				okToGo = confirm('You have unsaved changes in the current device. If you load another device, your change will be lost. Do you want to continue?');
			}
		}
		return okToGo;
	}
	
//	load(mid) {
//		if (this.checkDirtyFlag()) {
//			const obj = this;
//			if (_curAxis != null && _curAxis.path != this.path) {
//				_curAxis.hide();
//			}
//			_curAxis = this;
//			this.controller.setMenuActive(true);
//			
//			if (typeof mid === 'undefined') {
//				mid = obj.firstMid;
//			}
//			_title.text(mid);
//
//			_editorTitle.text('Use the tab menu to load configuration of motor devices');
//			$.each(Object.values(this.motorEditors), function(idx, propTable) {
//				propTable.hide();
//			});
//			this.tabUi.show();
//			this.rootEditor.show();
//			_historyBar.reload();
//		} else {
//			return;
//		}
//	}

	hide() {
//		this.dirtyFlag = false;
		this.setMenuActive(false);
		if (this.tabUi) {
			this.tabUi.hide();
		}
//		if (this.rootEditor) {
//			this.rootEditor.hide();
//		}
		$.each(Object.values(this.motorEditors), function(idx, editor) {
			editor.hide();
		});
		_title.text('');
		_editorTitle.text('');
	}
	
	load(mid) {
		if (this.checkDirtyFlag()) {
			
			if (_curAxis != null && _curAxis.path != this.path) {
				_curAxis.hide();
				if (_curAxis.controller.mcid != this.controller.mcid) {
					_curAxis.controller.setMenuActive(false);
				}
			}
			const obj = this;
			_curAxis = this;
			if (typeof mid === 'undefined') {
				mid = obj.firstMid;
			}
			this.curMid = mid;
			var config = obj.editorModel[mid]; 
//			_tabs.empty();
			
			var desc = config[KEY_DEVICE_DESC];
			_title.text(obj.aid + ":" + mid + " (" + desc + ")");

//			_editor.empty();
			_editorTitle.text('Device properties');
			
			$.each(Object.values(this.motorEditors), function(idx, propTable) {
				if (propTable.mid != mid) {
					propTable.hide();
				}
			});
//			this.rootEditor.hide();
			this.tabUi.show();
			var table = this.motorEditors[mid];
			if (!table) {
				table = new PropertyTable(obj, mid, config);
				this.motorEditors[mid] = table;
			}
			table.show();
			this.controller.setMenuActive(true, this.aid, mid);
//			this.controller.setMenuItemActive(this.aid);
			
			_historyBar.reload();
		}
	}
	
	setManagingEnabled(isEnabled) {
		if (this.curMid != null) {
			const table = this.motorEditors[this.curMid];
			if (table != null) {
				table.setManagingEnabled(isEnabled);
			}
			this.tabUi.setManageEnabled(this.curMid, isEnabled);
		}
	}
	
	getConfigArray() {
		const arr = [];
		$.each(this.model, function(mid, config){
			arr.push(mid);
		});
		return arr;
	}
	
	getValue(mid, key) {
		return this.editorModel[mid][key];
	}
	
	setValue(mid, key, val) {
		this.editorModel[mid][key] = val;
	}
	
	save() {
		const obj = this;
		var url = URL_PREFIX + 'save?inst=' + _inst + '&msg=';
		var saveMsg = $('#id_input_saveMessage').val().replace(/^\s+|\s+$/gm,'');
		if (saveMsg.length > 0) {
			url += encodeURI(saveMsg);
		}
		url += "&" + Date.now();
		_saveButton.close();
		var text = JSON.stringify(obj.editorModel);
		$.post(url,  {path:obj.path, model:text}, function(data) {
			try {
				if (data["status"] == "OK") {
					showMsg("Saved in the server.");
					$('td.editable_value input.changed').removeClass('changed');
					$.extend(true, obj.model, obj.editorModel);
					obj.clearDirtyFlag();
					_historyBar.reload();
//					setTimeout(_historyBar.reload, 3000)
				} else {
					showError(data["reason"]);
				}
			} catch (e) {
				console.log(e);
				showError("Failed to save: " + e);
			}
		}).fail(function(e) {
			console.log(e);
			showError("Faied to save: " + e.statusText);
		}).always(function() {
			_saveButton.close();
		});
	}
	
	reset() {
		this.clearDirtyFlag();
		$.extend(true, this.editorModel, this.model);
		$.each(Object.values(this.motorEditors), function(idx, editor) {
			editor.dispose();
		});
		this.motorEditors = {};
		if (this.curMid) {
			this.loadConfig(this.curMid);
		} else {
			this.load();
		}
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
					if (_curAxis != null && _curAxis.path == obj.path) {
						_curAxis.hide();
						_title.text('Please use the side bar to select a device configuration.');
					}
					
					obj.$menuHeader.remove();
					$.each(obj.motorMenuDict, function(cid, configMenu) {
						configMenu.dispose();
						delete obj.motorMenuDict[cid];
					});
					if (obj.$menuUl) {
						obj.$menuUl.remove();
					}
					if (obj.tabUi) {
						obj.tabUi.dispose();
					}
//					if (obj.rootEditor) {
//						obj.rootEditor.dispose();						
//					}
					$.each(obj.motorEditors, function(cid, editor) {
						editor.dispose();
						delete obj.motorEditors[cid];
					});
					
					_instModel.removeDevice(obj.did);
//					setTimeout(_historyBar.reload, 3000)
				} else {
					showError(data["reason"]);
				}
			} catch (e) {
				showError("Failed to remove: " + e.statusText);
			}
		}).fail(function(e) {
			console.log(e);
			showError("Faied to remove: " + e.statusText);
		}).always(function() {
			$('#id_modal_deleteDialog').modal('hide');
		});
	}
	
	fromHistory(commitModel) {
		const galil = commitModel[BRANCH_GALIL];
		const controllerModel = galil[this.controller.mcid];
		const deviceModel = controllerModel[this.aid];
		if (!deviceModel) {
			throw new Error('device not found in history model: ' + this.aid);
		}
		$.extend(true, this.editorModel, deviceModel);
		$.each(Object.values(this.motorEditors), function(idx, editor) {
			editor.dispose();
		});
		this.motorEditors = {};
		if (this.curMid) {
			this.load(this.curMid);
		} else {
			this.load();
		}
		this.setDirtyFlag();
	}

	addMotor(newMid) {
		newMid = newMid.trim();
		newMid = newMid.replace(/\W/g, '_');
		if (newMid.length == 0) {
			throw new Error('Motor name cannot be empty.');
		}
		if (newMid in this.model) {
			throw new Error('motor name already exists: ' + newMid);
		}
		
		const obj = this;

//		const toCopy = Utils.getFirstObject(Utils.getFirstObject(Utils.getFirstObject(obj.model)));
		var toCopy;
		if (_curAxis != null) {
			if (_curAxis.curMid != null) {
				toCopy = _curAxis.model[_curAxis.curMid];
			} else {
				toCopy = Utils.getFirstObject(_curAxis.model);
			}
		} else {
			if (obj.curMid != null) {
				toCopy = obj.model[obj.curMid];
			} else {
				toCopy = Utils.getFirstObject(obj.model);
			}
		}
		const newMotorModel = $.extend(true, {}, toCopy);
		obj.model[newMid] = newMotorModel;
		obj.editorModel[newMid] = $.extend(true, {}, toCopy);
		
//		const axis = new Axis(obj, newAid, newAxisModel, obj.$menuUl);
//		axis.createMenuUi();
//		obj.axes[newAid] = axis;
		obj.mids.push(newMid);
		
		obj.tabUi.addMotorTab(newMid);
		
		var desc = "";
		if (newMotorModel.hasOwnProperty(KEY_DEVICE_DESC)) {
			desc = newMotorModel[KEY_DEVICE_DESC];
		}
		_instModel.updatePathTable(obj.controller.mcid, obj.aid, newMid, desc);

		obj.setDirtyFlag();
		obj.load(newMid);
	}
	
	removeMotor(mid) {
		const obj = this;
		
		delete obj.editorModel[mid];
		
		const idx = obj.mids.indexOf(mid);
		if (idx > -1) {
			obj.mids.splice(idx, 1);
		}
		
		obj.tabUi.removeMotorTab(mid);
		
		_instModel.removePathTableItem(obj.controller.mcid, obj.aid, mid);

		const editorUi = obj.motorEditors[mid];
		if (editorUi) {
			editorUi.remove();
		}
		delete obj.motorEditors[mid];
		
		const mids = [];
		$.each(obj.editorModel, function(motorId, motorModel) {
			mids.push(motorId);
		});
		obj.firstMid = mids[0];
		
		obj.setDirtyFlag();
	}
}


//class AxisMenuItem {
//	
//	$ui;
//	$parentUi;
//
//	constructor(controller, aid)
//	{
//		this.controller = controller;
//		this.aid = aid;
//	}
//	
//	createUi($parentUi) {
//		var obj = this;
//		this.$parentUi = $parentUi;
//		var html = '<li id="li_menu_' + this.controller.mcid + '_' + this.aid + '" class="nav-item class_li_subitem"><a class="nav-link class_a_axis" mcid="' 
//			+ this.controller.mcid + '" aid="' + this.aid + '" href="#">' 
//			+ this.aid + '<span class="sr-only">(current)</span></a></li>';
//		this.$ui = $(html);
//		this.$ui.click(function(){
//			obj.load(obj.aid);
//		});
//		this.$parentUi.append(this.$ui);
//	}
//	
//	load() {
//		this.controller.loadConfig(this.aid);
//	}
//	
//	setActive() {
//		this.$ui.addClass("active");
//	}
//	
//	dispose() {
//		if (this.$ui) {
//			this.$ui.remove();
//		}
//	}
//}

class TabUi {
	axis;
	init = false;
	$addLi;
	$tabUi = $(HTML_NAV_DIV).addClass("div_hidden");
	
	constructor(axis)
	{
		this.axis = axis;
		this.manageEnabled = {};
	}
	
	createUi() {
		var obj = this;
//		var $li = $('<li class="nav-item active"><a class="nav-link tab_root active" href="#">Root view</a></li>');
//		$li.click(function() {
//			obj.axis.load();
//		});
//
//		obj.$tabUi.append($li);
		$.each(this.axis.model, function(mid, motorModel) {
			var $li = $('<li class="nav-item" id="li_tab_' + obj.axis.aid + '_' + mid + '"><a class="nav-link tab_item" href="#">' + mid + ' <span class="tab_remove div_hidden"><i class="fas fa-minus-circle"> </span></a></li>');
			$li.click(function() {
				obj.axis.load(mid);
			});					
			obj.$tabUi.append($li);
			$li.find('span.tab_remove').click(function(e) {
				e.stopPropagation();
				if (obj.axis.mids.length > 1) {
					obj.axis.removeMotor(mid);
					obj.axis.load(obj.axis.mids[obj.axis.mids.length - 1]);
				} else {
					if (Object.keys(obj.axis.controller.axes).length == 1) {
						const deleteMCModel = new DeleteMCModel($(this), obj.axis);
						deleteMCModel.open();
					} else {
						const deleteAxisModel = new DeleteAxisModel($(this), obj.axis);
						deleteAxisModel.open();
					}
				}
			});
		});
		obj.$addLi = $('<li class="nav-item"><a class="nav-link tab_item" href="#" data-toggle="tooltip" title="Add a new motor to the current axis."><i class="fas fa-plus"> </a></li>');
		obj.$tabUi.append(obj.$addLi);
		_tabs.append(obj.$tabUi);
		
		const newMotorModel = new NewMotorModel(obj.$addLi, obj.axis);
		newMotorModel.init();
		
		this.init = true;
	}
	
	addMotorTab(mid) {
		const obj = this;
		var $li = $('<li class="nav-item" id="li_tab_' + obj.axis.aid + '_' + mid + '"><a class="nav-link tab_item" href="#">' + mid + ' <span class="tab_remove div_hidden"><i class="fas fa-minus"> </span></a></li>');
		$li.click(function() {
			obj.axis.load(mid);
		});					
//		obj.$tabUi.append($li);
//		obj.$addLi.detach();
//		obj.$tabUi.append(obj.$addLi);
		
		$li.insertBefore(obj.$addLi);
		
//		obj.$addAxis.detach();
//		obj.$menuUl.append(obj.$addAxis);
		
	}
	
	setManageEnabled(mid, isEnabled) {
		this.manageEnabled[mid] = isEnabled;
		const $bt = this.$tabUi.find('li#li_tab_' + this.axis.aid + '_' + mid + " span.tab_remove");
		if ($bt) {
			if (isEnabled) {
				$bt.removeClass("div_hidden");
			} else {
				if (!$bt.hasClass("div_hidden")) {
					$bt.addClass("div_hidden");
				}
			}
		}
	}
	
	removeMotorTab(mid) {
		this.$tabUi.find('li#li_tab_' + this.axis.aid + '_' + mid).remove();
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
	
	setActive(mid) {
		const obj = this;
		obj.$tabUi.find('a').removeClass("active");
		const $spans = obj.$tabUi.find('span.tab_remove');
		$spans.each(function() {
			const span = $(this);
			if (!span.hasClass("div_hidden")) {
				span.addClass("div_hidden");
			}
		});
		const enabled = obj.manageEnabled[mid];
		if (enabled) {
			obj.$tabUi.find('li#li_tab_' + this.axis.aid + '_' + mid + " span.tab_remove").removeClass("div_hidden");
		}
		if (mid) {
			obj.$tabUi.find('#li_tab_' + this.axis.aid + '_' + mid + '>a').addClass("active");
		} else {
			obj.$tabUi.find('a.tab_root').addClass("active");
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

class PropertyTable extends AbstractMainUi {
	
	constructor(device, mid, config) {
		super(device);
//		this.device = device;
		this.mid = mid;
		this.cModel = config;
//		this.driverId = config[KEY_DEVICE_DRIVER];
//		this.dModel = _instModel.getDeviceModel(this.driverId);
		this.$plusRow = null;
		this.newRows = [];
		this.isManagingEnabled = false;
	}
	
	createUi() {
		var $table = $(TABLE_TIER1_HEADER + '</tbody></table>');
		var $tbody = $table.find('tbody');
		var object = this;
		$.each(this.cModel, function(key, val){
			var pRow = object.createRow(key, val, null);
			$tbody.append(pRow.getUI());
		});
		this.$plusRow = $(PLUS_ROW_TIER1);
		$tbody.append(this.$plusRow);
		const plusButton = this.$plusRow.find('span.property_control');
		plusButton.click(function() {
			object.addEmptyRow();
		});
		this.$editorUi.append($table);
		_editor.append(this.$editorUi);
		this.init = true;
	}
	
	addEmptyRow() {
		const $newRow = $(NEW_ROW_TIER1);
		$newRow.insertBefore(this.$plusRow);
		const row = new PropertyRow(this.device, this.mid, $newRow);
		this.newRows.push(row);
	}
	
	createRow(key, val, options, editingDisabled) {
		const mid = this.mid;
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
			html = '<tr class="editable_row" key="' + key + '"><td class="editable_control div_hidden"><span class="property_control"><i class="fas fa-minus"> </span></td><td class="editable_key">' + key + '</td>' 
			+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair" selected>name-value pair</option></select></td>'
			+ '<td class="editable_value">' + TABLE_TIER2_HEADER;
			$.each(val, function(subKey, subVal){
				if (Array.isArray(val)) {
					html += DISABLED_ROW_PART1 + '-' + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
				} else {
					html += EMPTY_ROW_PART1 + subKey + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
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
				html = '<tr class="editable_row" key="' + key + '">'
				+ '<td class="editable_control div_hidden"><span class="property_control"><i class="fas fa-minus"> </span></td>'
				+ '<td class="editable_key">' + key + '</td>'
				+ '<td class="editable_type"></td>' 
				+ '<td class="editable_value">';
				if (editingDisabled) {
					html += '<select name="option_value" class="form-control">' + ot + '</select>';
				} else {
					html += '<input name="option_value" class="form-control" value="' + val + '" list="' + key + '"/><datalist id="' + key + '">' + ot + '</datalist>';
				}
				html += '</td></tr>';
			} else {
				html = '<tr class="editable_row" key="' + key + '">'
				+ '<td class="editable_control div_hidden"><span class="property_control"><i class="fas fa-minus"> </span></td>'
				+ '<td class="editable_key">' + key + '</td>'
				+ '<td class="editable_type"><select name="value_type" class="form-control"><option value="text">plain text</option><option value="pair">name-value pair</option></select></td>'
				+ '<td class="editable_value"><input type="text" class="form-control" value="' + val + '"' + (editingDisabled ? ' disabled' : '') + '></td></tr>';
			}
		}
		var $row = $(html);
		return new PropertyRow(this.device, mid, $row);
	};

	show() {
		super.show();
		_manageButton.setActive(this.isManagingEnabled);
	}
	
	setManagingEnabled(isEnabled) {
		this.isManagingEnabled = isEnabled;
		const tds = this.$editorUi.find('.editable_control');
		if (isEnabled) {
			tds.removeClass('div_hidden');
		} else {
			tds.addClass('div_hidden');
		}
	}
	
	remove() {
		this.$editorUi.remove();
	}
}

class PropertyRow {
	constructor(device, mid, tr) {
		this.device = device;
		this.editorModel = device.editorModel;
		this.row = tr;
		this.mid = mid;
		this.colKey = tr.find('.editable_key');
		this.colValue = tr.find('.editable_value');
		this.key = tr.attr('key');
		this.minus = tr.find('span.property_control');
		this.isNewRow = false;
		this.init();
	}
	
	init() {
		const obj = this;
		const colType = this.row.find('.editable_type');
		const sel = colType.find('select');
		const oldVal = this.device.model[this.mid][this.key];
		var newTextHtml;
		var newPairHtml;
		if (typeof oldVal === 'object') {
			newPairHtml = TABLE_TIER2_HEADER;
			$.each(oldVal, function(subKey, subVal){
				newPairHtml += EMPTY_ROW_PART1 + subKey + EMPTY_ROW_PART2 + subVal + EMPTY_ROW_PART3;
			});
			newPairHtml += '</tbody></table>';
			newTextHtml = '<input type="text" key="' + obj.key + '" class="form-control" value="">';
		} else {
			newPairHtml = TABLE_TIER2_HEADER + EMPTY_ROW_PART1 + EMPTY_ROW_PART2 + EMPTY_ROW_PART3 + '</tbody></table>';
			newTextHtml = '<input type="text" key="' + obj.key + '" class="form-control" value="' + oldVal + '">';
		}
		sel.change(function() {
			var selVal = sel.val();
			if (selVal === 'text') {
				obj.colValue.html(newTextHtml);
			} else {
				obj.colValue.html(newPairHtml);
			}
			obj.addEventHandler();
		});

		obj.addEventHandler();
	}
	
	addEventHandler() {
		const obj = this;
		
		const t1Key = obj.colKey.find("> input");
		if (t1Key.length > 0) {
			obj.isNewRow = true;
			t1Key.focus(function() {
				obj.row.addClass("active");
			}).blur(function() {
				obj.row.removeClass("active");
				const val = t1Key.val().trim();
				if (val.length > 0) {
					obj.key = val;
					obj.updateNode(t1Value, val, t1Value.val());
				}
			}).keypress(function( event ) {
				if ( event.which == 13 ) {
					t1Key.blur();
				}
			});
		}
		
		obj.minus.click(function() {
			const config = obj.editorModel[obj.mid];
			delete config[obj.key];
			obj.row.remove();
			obj.device.setDirtyFlag();
			showWarning("Property row removed: " + obj.key);
		});
		
		const t1Value = obj.colValue.find("> input");
		t1Value.focus(function() {
			obj.row.addClass("active");
		}).blur(function() {
			obj.row.removeClass("active");
			console.log("t1Value set");
			obj.updateNode(t1Value, obj.key, t1Value.val());
		}).keypress(function( event ) {
			if ( event.which == 13 ) {
				t1Value.blur();
			}
		});
		
		const t1Select = obj.colValue.find("> select");
		if (t1Select) {
			t1Select.change(function() {
				obj.updateNode(t1Select, obj.key, t1Select.val());
			});
		}
		
		const t2Body = obj.colValue.find("tbody");
		if (t2Body) {
			var t2Key = t2Body.find("td.pair_key > input");
			var isPair = !t2Key.prop('disabled');
			if (isPair) {
				t2Key.each(function(){
					const $kInput = $(this);
					const oldKV = $kInput.val();
					$kInput.focus(function() {
						obj.row.addClass("active");
					}).blur(function() {
						obj.row.removeClass("active");
						obj.updateT2Pair($kInput, obj.key, t2Body, oldKV, isPair);
					}).keypress(function( event ) {
						if ( event.which == 13 ) {
							$kInput.blur();
						}
					});
				});
			}

			const t2Value = t2Body.find("td.pair_value > input");
			t2Value.each(function(){
				const $vInput = $(this);
				const oldVV = $vInput.val();
				$vInput.focus(function() {
					obj.row.addClass("active");
				}).blur(function() {
					obj.row.removeClass("active");
					obj.updateT2Pair($vInput, obj.key, t2Body, oldVV, isPair);
				}).keypress(function( event ) {
					if ( event.which == 13 ) {
						$vInput.blur();
					}
				});
			});
			
			const t2Add = t2Body.find("td.pair_control > button.button_plus");
			t2Add.click(function() {
				obj.addRow($(this), obj.key, t2Body, isPair);
			});
			
			const t2Remove = t2Body.find("td.pair_control > button.button_minus");
			t2Remove.click(function() {
				obj.removeRow($(this), obj.key, t2Body, isPair);
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
		const obj = this;
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
			obj.setValue(options[0]);			
		} else {
			obj.setValue("");
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
			
//			var curVal = _editorModel[_curmid][key];
			var curVal = obj.editorModel[obj.mid][key];
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
					obj.editorModel[obj.mid][key] = newVal;
				} else {
					newVal = [];
					var trs = tbody.find('tr');
					trs.each(function() {
						var rv = $(this).find('td.pair_value > input').val();
						newVal.push(rv);
					});
					obj.editorModel[obj.mid][key] = newVal;
				}
			} 
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
		console.log("update node " + key);
		const curVal = this.device.getValue(this.mid, key);
		if (curVal == null && val.length == 0) {
			$node.removeClass('changed');
		} else if (curVal == null && val.length > 0) {
			$node.addClass('changed');
			this.device.setDirtyFlag();
		} else if (curVal.toString() != val.toString()) {
			$node.addClass('changed');
			this.device.setDirtyFlag();
		} else {
			$node.removeClass('changed');
		}
//		_editorModel[_curmid][key] = val;
		this.device.setValue(this.mid, key, val);
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
		console.log(nRow);
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
	};

	removeRow(bt, key, tbody, isPair) {
		var cRow = bt.parent().parent();
		cRow.remove();
		
		_dirtyFlag = true;
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
		obj.editorModel[obj.mid][key] = newVal;
	};

}

//class MutableRootUi extends AbstractMainUi {
//	constructor(device) {
//		super(device);
//	}
//	
//	createUi() {
//		const obj = this;
//		var html = '';
//		$.each(obj.device.model, function(cid, config) {
//			html += '<div class="class_div_device_page" id="div_page_' + obj.device.did + '_' + cid + '">'
//				+ '<div class="class_div_device_item"><a href="#" class="class_a_cid_label" did="' 
//				+ obj.device.did + '" cid="' + cid + '">' + cid + '<br>' + '</a></div></div>';
//		});
//		var $div = $('<div class="class_div_device_canvas"/>').append(html);
//		$div.find('a.class_a_cid_label').click(function(){
//			var acid = $(this).attr('cid');
//			obj.device.loadConfig(acid);
//		});
//		
//		this.$editorUi.append($div);
//		
//		_editor.append(this.$editorUi);
////		_historyBar.reload();
//		this.init = true;
//	}
//	
//}

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
			if (_curAxis) {
				_curAxis.save();
			} else {
				showWarning('Please select a device for saving. Saving is per-device based.');
				obj.close();
			}
		});
	
		this.$textInput.keypress(function(event) {
			if ( event.which == 13 ) {
				if (_curAxis) {
					_curAxis.save();
				} else {
					showWarning('Please select a device for saving. Saving is per-device based.');
					obj.close();
				}
			}
		});
	}
	
	close() {
		this.dialog.modal('hide');
	}
}

class ResetButton {
	constructor($button)
	{
		this.$button = $button;
	}
	
	init() {
		this.$button.click(function(){
			if (_curAxis) {
				_curAxis.reset();
			}
		});
	}
}

var addPageTitle = function(){
	var titleString = "Motor Configuration - " + _inst.charAt(0).toUpperCase() + _inst.slice(1);
	$(document).attr("title", titleString);
	var subTitle = _inst.toUpperCase() + " Setup";
	$('#id_span_side_title').html('<h5>' + subTitle + '</h5>');
};

class HomeButton {
	constructor($button) {
		this.$button = $button;
	}
	
	init() {
		const obj = this;
		this.$button.click(function() {
			
			var okToGo = true;
			if (_curAxis != null) {
				if (_curAxis.isDirty()) {
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
		if (_curAxis != null) {
			_curAxis.hide();
			_curAxis = null;
		}
		_title.text('Please use the side bar to select a device configuration.');
		_historyBar.reload();
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
		$.each(_instModel.motors, function(name, pair) {
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

class HistoryBlock {
	constructor($main, $side) {
		this.$main = $main;
		this.$side = $side;
//		this.$holder = $('.class_div_commit_item');
		this.enabled = false;
		const obj = this;
		$( window ).on( "resize", function() {
			obj.resize();
		});
	}
	
	empty() {
		this.$side.find('.class_div_commit_item').remove();
	}
	
	reload() {
		if (this.enabled) {
			this.empty();
			const obj = this;
			var url = URL_PREFIX + 'history?inst=' + _inst;
			if (_curAxis) {
				url += '&path=' + _curAxis.path;
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
	}
	
	toggle() {
		this.enabled = !this.enabled;
		if (this.enabled) {
			this.$main.css('width', '70%');
			this.$side.show();
//			var bodyHeight = $(window).height() - 160;
//			var mainHeight = $("#id_div_main_area").height() - 32;
//			var newHeight = bodyHeight > mainHeight ? bodyHeight : mainHeight;
//			console.log("body=" + bodyHeight + ", main=" + mainHeight);
//			const pos = this.$side.position();
//			const bodyHeight = $(window).height();
//			const newHeight = $(window).height() - pos.top - 20;
//			console.log("window=" + bodyHeight + ", yTop=" + pos.top + ", newHeight=" + newHeight);
//			console.log("body=" + $('body').height() + ", main=" + $('main').height());
//			this.$side.height(newHeight);
			this.resize();
			this.reload();
		} else {
			this.$side.hide();
			this.$main.css('width', '100%');
		}
	};
	
	resize() {
		const pos = this.$side.position();
		const bodyHeight = $(window).height();
		const newHeight = $(window).height() - pos.top - 20;
		this.$side.height(newHeight);
	}
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
			if (_curAxis == null) {
				alert('Please select a device first. Loading history version is only supported on a per-device base.')
				return;
			}
			var url = URL_PREFIX + 'load?inst=' + _inst + '&version=' + encodeURI(name) + "&" + Date.now();
			$.get(url, function(data) {
				try{
					_curAxis.fromHistory(data);
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
	
	static getFirstObject(obj) {
		const key = Object.keys(obj).sort()[0];
		return obj[key];
	}
	
}

class SignOutButton {
	$bt;
	
	constructor($bt)
	{
		this.$bt = $bt;
	}
	
	init() {
		this.$bt.click(function() {
			signout("sicsConfigMenu.html");
		});
	}
}

class NewControllerModel {
	$bt;
	
	constructor($bt)
	{
		this.$bt = $bt;
		this.dialog = $('#id_modal_newControllerDialog');
		this.$ok = $('#id_button_newController');
		this.$mcName = $('#id_input_newControllerName');
		this.$axisName = $('#id_input_newAxisName');
		this.$motorName = $('#id_input_newMotorName');
	}
	
	init() {
		const obj = this;
		obj.$bt.click(function() {
			obj.$mcName.val(_instModel.getNextControllerId());
			obj.$axisName.val('A');
			obj.$motorName.val("");
			obj.open();
		});
		obj.$ok.click(function() {
			obj.run();
		});
	}
	
	run() {
		this.close();
		const newMcid = this.$mcName.val();
		const newAid = this.$axisName.val();
		const newMid = this.$motorName.val();
		_instModel.addController(newMcid, newAid, newMid);
	}
	
	close() {
		this.dialog.modal('hide');
	}
	
	open() {
		this.dialog.modal('show');
	}

}

class NewAxisModel {
	$bt;
	
	constructor($bt, controller)
	{
		this.$bt = $bt;
		this.controller = controller;
		this.dialog = $('#id_modal_newAxisDialog').first().clone();
		this.dialog.attr('id', '');
		this.$ok = this.dialog.find('#id_button_newAxis');
		this.$mcName = this.dialog.find('#id_span_newControllerName2');
		this.$axisName = this.dialog.find('#id_input_newAxisName2');
		this.$motorName = this.dialog.find('#id_input_newMotorName2');
	}
	
	init() {
		const obj = this;
		obj.$bt.click(function() {
			obj.$mcName.text(obj.controller.mcid);
			obj.$axisName.val(obj.controller.getNextAxisId());
			obj.$motorName.val("");
			obj.open();
		});
		obj.$ok.click(function() {
			obj.run();
		});
	}
	
	run() {
		this.close();
		const newAid = this.$axisName.val();
		const newMid = this.$motorName.val();
		this.controller.addAxis(newAid, newMid);
	}
	
	close() {
		this.dialog.modal('hide');
	}
	
	open() {
		this.dialog.modal('show');
	}

}

class NewMotorModel {
	$bt;
	
	constructor($bt, axis)
	{
		this.$bt = $bt;
		this.axis = axis;
		this.dialog = $('#id_modal_newMotorDialog').first().clone();
		this.dialog.attr('id', '');
		this.$ok = this.dialog.find('#id_button_newMotor');
		this.$mcName = this.dialog.find('#id_span_newControllerName3');
		this.$axisName = this.dialog.find('#id_span_newAxisName3');
		this.$motorName = this.dialog.find('#id_input_newMotorName3');
	}
	
	init() {
		const obj = this;
		obj.$bt.click(function() {
			obj.$mcName.text(obj.axis.controller.mcid);
			obj.$axisName.text(obj.axis.aid);
			obj.$motorName.val("");
			obj.open();
		});
		obj.$ok.click(function() {
			obj.run();
		});
		obj.$motorName.keypress(function(event) {
			if ( event.which == 13 ) {
				obj.run();
			}
		});
	}
	
	run() {
		this.close();
		const newMid = this.$motorName.val();
		this.axis.addMotor(newMid);
	}
	
	close() {
		this.dialog.modal('hide');
	}
	
	open() {
		this.dialog.modal('show');
	}

}

class DeleteAxisModel {
	$bt;
	
	constructor($bt, axis)
	{
		this.$bt = $bt;
		this.axis = axis;
		this.dialog = $('#id_modal_deleteAxis').first().clone();
		this.$input = this.dialog.find('#id_input_deleteAxisMessage4');
		this.$ok = this.dialog.find('#id_button_deleteAxis4');
		this.$axisName = this.dialog.find('#id_span_deleteAxisName4');
		this.init();
	}
	
	init() {
		const obj = this;
		obj.$axisName.text('/' + obj.axis.controller.mcid + '/' + obj.axis.aid);
		obj.$ok.click(function() {
			obj.run();
		});
		obj.$input.keypress(function(event) {
			if ( event.which == 13 ) {
				obj.run();
			}
		});
	}
	
	run() {
		this.close();
		this.axis.controller.deleteAxis(this.axis.aid, this.$input.val());
	}
	
	close() {
		this.dialog.modal('hide');
	}
	
	open() {
		this.dialog.modal('show');
	}

}

class DeleteMCModel {
	$bt;
	
	constructor($bt, axis)
	{
		this.$bt = $bt;
		this.axis = axis;
		this.dialog = $('#id_modal_deleteMC').first().clone();
		this.$input = this.dialog.find('#id_input_deleteMCMessage5');
		this.$ok = this.dialog.find('#id_button_deleteMC5');
		this.$axisName = this.dialog.find('#id_span_deleteAxisName5');
		this.$mcName = this.dialog.find('#id_span_deleteMCid5');
		this.init();
	}
	
	init() {
		const obj = this;
		obj.$axisName.text('/' + obj.axis.controller.mcid + '/' + obj.axis.aid);
		obj.$mcName.text('/' + obj.axis.controller.mcid);
		obj.$ok.click(function() {
			obj.run();
		});
		obj.$input.keypress(function(event) {
			if ( event.which == 13 ) {
				obj.run();
			}
		});
	}
	
	run() {
		this.close();
		this.axis.controller.deleteAxis(this.axis.aid, this.$input.val());
	}
	
	close() {
		this.dialog.modal('hide');
	}
	
	open() {
		this.dialog.modal('show');
	}

}

class ManageButton {

	constructor($bt)
	{
		this.$bt = $bt;
		this.isActive = false;
	}
	
	init() {
		const obj = this;
		this.$bt.click(function() {
			obj.isActive = !obj.isActive;
//			console.log(obj.enabled);
			obj.toggle();
		});
	}
	
	toggle() {
//		if (obj.isActive) {
//			$('.editable_control').removeClass('div_hidden');
//		} else {
//			$('.editable_control').addClass('div_hidden');
//		}
		if (_curAxis != null) {
			_curAxis.setManagingEnabled(this.isActive);
		}
	}
	
	setActive(isActive) {
		this.isActive = isActive;
		if (isActive) {
			if (!this.$bt.hasClass('active')) {
				this.$bt.addClass('active');
			} 
		} else {
			this.$bt.removeClass('active');
		}
	}
}

class MainAreaUI {
	constructor($ui)
	{
		this.$ui = $ui;
		this.resize();
	}
	
	init() {
		const obj = this;
		$( window ).on( "resize", function() {
			obj.resize();
		});
	}
	
	resize() {
		const pos = this.$ui.position();
		const bodyHeight = $(window).height();
		const newHeight = $(window).height() - pos.top;
		this.$ui.height(newHeight);
	}
}

$(document).ready(function() {

	const signOut = new SignOutButton($('#id_a_signout'));
	
	addPageTitle();

	_instModel.load();

	$('#id_button_help').click(function() {
		var win = window.open('js/mcConfigHelp.pdf', '_blank');
		if (win) {
		    win.focus();
		} else {
			showWarning('Please allow popups for this website');
		}
	});
	
	_historyBar = new HistoryBlock($("#id_div_main_area"), $('#id_div_right_bar'));
	$('#id_button_history').click(function(){
		_historyBar.toggle();
	})
	
	$('#id_button_reload_history').click(function() {
		_historyBar.reload();
	});
	_historyBar.toggle();
	
	_homeButton = new HomeButton($('#id_span_side_home>a'));
	_homeButton.init();

	const loadingLabel = new LoadingLabel($('#id_span_waiting'));
	loadingLabel.init();
	_saveButton = new SaveButton($('#id_button_saveConfirm'));
	_saveButton.init();
	
	_resetButton = new ResetButton($('#id_button_reset'));
	_resetButton.init();
	
	const searchWidget = new SearchWidget($('#id_input_search_text'));
	searchWidget.init();

	const newControllerModel = new NewControllerModel($("#id_span_addController"));
	newControllerModel.init();
	
	_manageButton = new ManageButton($('#id_button_manage'));
	_manageButton.init();
	
	const mainAreaUi = new MainAreaUI($('#id_div_main_area'));
	mainAreaUi.init();
});