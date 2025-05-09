//const LLM_URL = "http://localhost:8800/ollama/stream_log";
const LLM_IP = "137.157.204.74:8000";
const URL_PREFIX = "http://" + LLM_IP + "/"
const LLM_URL = "ws://" + LLM_IP + "/ws"
const $_mainArea = $('#id_div_property_table');
const THREAD_LIST_PAGE_SIZE = 50;
const THREAD_PAGE_SIZE = 30;

const $_sidebar = $('#id_div_sidebar');
const _message = $('#id_div_info');

var _scrollBlocker = false;
var _currentThread = null;
var _threadList = null;
var _removeModal = null;

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

class ChatInput {
	constructor() {
		this.$input = $('#id_input_chatbox');
		this.$think = $('#id_button_think');
		this.$concise = $('#id_button_concise');
	}
	
	init() {
		const obj = this;
//		this.$input.keypress(function(event) {
//			if ( event.which == 13 ) {
//				const text = obj.$input.val().trim();
//				if (text.length > 0) {}
//				obj.send(text);
//				return;
//			}
//		});
		this.$input.keyup(function(event) {
			if ( event.which == 13 ) {
				if (!event.shiftKey) {
					const text = obj.$input.val().trim();
					if (text.length > 0) {}
					obj.send(text);
				}
			}
			if ($(this).val().length > 0) {
				this.style.height = 'auto';
				var newHeight = "2.4em";
				var newMargin = "64px";
				const sHeight = this.scrollHeight;
				if (sHeight > 40) {
					newHeight = sHeight + 'px';
					newMargin = (sHeight + 24) + 'px';
				}
				this.style.height = newHeight;
				$_mainArea.css("margin-bottom", newMargin);
			} else {
				this.style.height = '2.4em';
				$_mainArea.css("margin-bottom", "64px");
			}
		});
	}
	
//	resize() {
//		this.style.overflow = 'hidden';
//		this.style.height = 0;
//		this.style.height = this.scrollHeight + 'px';
//	}
	
	renderDiv($div) {
		var html = $div.html();
		console.log(html);
//		html = html.replace(/\```([^|]*)\```/g, '<div class="code-block">$1</div>');
		
//		html = html.replace(/```(.+?)(?=<p>)/g, '```<span class="code-block-title">$1</span><p>');
//		html = html.replace(/```(.+?)(?=<p>)/g, '<div class="code-block"><span class="code-block-title">$1</span><p>');
		
//		console.log(html);
		
		let count = 0;
		html = html.replace(/```/g, () => {
		  count++;
		  return count % 2 === 1 ? '<div class="code-block">' : '</div>';
		});
//		html = html.replace(/```/g, '</div>');
		const idx = html.indexOf("{THINK_END}");
		if (idx > 0 && idx < 30) {
			html = html.substring(idx + 11);
		}
		html = html.replace("{THINK_START}", '<div class="div_chat_think"><span class="span_chat_think">think ...</span>');
		html = html.replace("{THINK_END}", '</div>');
		console.log(html);
		$div.html(html);
	}
	
	send(query) {
		const obj = this;
//		const query = this.$input.val();
		const doThink = obj.$think.hasClass("active");
		const doConcise = obj.$concise.hasClass("active");
		this.$input.val("");
		$('#id_input_chatbox').html('');
		var isThinking = true;
		const $inputDiv = $('<div class="class_div_input"></div>');
		$inputDiv.text(query);
		var $divWrap = $('<div class="class_div_wrap"></div>');
		$divWrap.append($inputDiv);
		$_mainArea.append($divWrap);
		
		const $outputDiv = $('<div class="class_div_output">thinking <i class="fas fa-spinner fa-spin"></i> </div>');
		$divWrap = $('<div class="class_div_wrap"></div>');
		$divWrap.append($outputDiv);
		$_mainArea.append($divWrap);
		window.scrollTo(0, document.body.scrollHeight);
		
//		const inputs = JSON.stringify({"input": query});
		const inputObj = {"input":{"content":query,"type":"human","role":"user"},
				"thread_id": _currentThread.tid,
				"config":{"think" : doThink, "concise" : doConcise}}
		const inputs = JSON.stringify(inputObj);
		
		const socket = new WebSocket(LLM_URL);

		// Connection opened
		socket.addEventListener("open", (event) => {
		  socket.send(inputs);
		});

		// Listen for messages
		socket.addEventListener("message", (event) => {
			if (isThinking) {
				$outputDiv.empty();
				isThinking = false;
			}
			const data = JSON.parse(event.data);
//		  console.log("Message from server ", event.data);
			console.log(event.data);
//		  console.log(data['more_body']);
			var msg = data["body"];
			if (msg.startsWith("<think>")) {
				msg = msg.replace("<think>", "{THINK_START}");
			} else if (msg.startsWith("</think>")) {
				msg = msg.replace("</think>", "{THINK_END}");
			} else {
				msg = msg.replaceAll("\n", "<p/>");
			}
		  $outputDiv.append(msg);
		  if (!_scrollBlocker) {
			  window.scrollTo(0, document.body.scrollHeight);
		  }
		  
		  if (!data['more_body']) {
			  console.log("close socket");
			  socket.close(1000);
			  obj.renderDiv($outputDiv);
			  if (!_currentThread.title) {
//				  obj.setTitle('Give a short title for the following text, '
//						  + 'do not wrap the answer with quotation marks : ' + query, 
//						  function(res) {
//					  $('#id_editor_subtitle').html(res);
//				  });
				  obj.setTitle('Give a short title for the following text, '
						  + 'do not wrap the answer with quotation marks : ' + query)
			  }
		  }
		});

	}
	
	setTitle(query) {
		const socket = new WebSocket(LLM_URL);
		const inputObj = {"input":{"content":query,"type":"human","role":"user","get_title":true},
				"thread_id": _currentThread.tid,
				"config":{}}
		const inputs = JSON.stringify(inputObj);

		// Connection opened
		socket.addEventListener("open", (event) => {
		  socket.send(inputs);
		});

		var res = "";
		// Listen for messages
		socket.addEventListener("message", (event) => {
			const data = JSON.parse(event.data);
			const msg = data["body"];
			res += msg;
			if (!data['more_body']) {
			    console.log("close socket");
//			    callback(res);
			    res = res.trim();
			    if (res.length > 2 && res.startsWith('"') && res.endsWith('"')) {
			    	res = res.substring(1, res.length - 1);
			    }
			    res = res.replace("<think>", "").replace("</think>", "").trim();
			    $('#id_editor_subtitle').html(res);
			    _currentThread.title = res;
			    const $tag = _threadList.tags[_currentThread.tid];
			    $tag.find('span').html(res);
				socket.close(1000);
			}
		});
	}
	
	quietSend(query, callback) {
		const socket = new WebSocket(LLM_URL);
		const inputObj = {"input":{"content":query,"type":"human","role":"user","no_search":true},
				"thread_id": _currentThread.tid,
				"config":{}}
		const inputs = JSON.stringify(inputObj);

		// Connection opened
		socket.addEventListener("open", (event) => {
		  socket.send(inputs);
		});

		var res = "";
		// Listen for messages
		socket.addEventListener("message", (event) => {
			const data = JSON.parse(event.data);
			const msg = data["body"];
			res += msg;
			if (!data['more_body']) {
			    console.log("close socket");
			    callback(res);
				socket.close(1000);
			}
		});

	}
}

class ChatThread {
	
	title;
	
	constructor(tid)
	{
		this.chat = [];
		this.tid = tid;
		this.title = "";
		this.start = -1;
		this.end = -1;
	}
	
	load() {
		const obj = this;
		const url = URL_PREFIX + "thread/" + this.tid + "?start=-1&length=" + THREAD_PAGE_SIZE;
		$.get(url, function(data) {
			console.log(data);
			obj.tid = data["thread_id"];
			obj.title = data["thread_title"];
			obj.start = data["start"];
			obj.end = data["end"];
//			console.log("start=" + obj.start + ", end=" + obj.end);
			$('#id_editor_subtitle').html(obj.title);
			obj.chat = data["batch"];
			$_mainArea.empty();
			obj.createUi(obj.chat.reverse());
//			window.scrollTo(0, document.body.scrollHeight);
		});
	}
	
	loadMore() {
		const obj = this;
		if (obj.start <= 0) {
			return;
		}
		var newStart = obj.start - THREAD_PAGE_SIZE;
		if (newStart < 0) {
			newStart = 0;
		}
		const url = URL_PREFIX + "thread/" + this.tid + "?start=" 
			+ newStart + "&length=" + THREAD_PAGE_SIZE;
		$.get(url, function(data) {
			console.log(data);
			obj.start = data["start"];
			obj.chat = data["batch"].concat(obj.chat);
			obj.createUi(data["batch"].reverse());
			document.getElementById("id_editor_subtitle").scrollIntoView();
		});
	}
	
	initNew() {
		$('#id_editor_subtitle').html(this.title);
		$_mainArea.empty();
		$('#id_input_chatbox').focus();
	}
	
	render(text) {
		var html = "";
		let count = 0;
		html = text.replace(/```/g, () => {
		  count++;
		  return count % 2 === 1 ? '<div class="code-block">' : '</div>';
		});
		html = html.replace("<think>", '<div class="div_chat_think"><span class="span_chat_think">think ...</span>');
		html = html.replace("</think>", '</div>');

		return html.replaceAll("\n", "<p/>");
	}
	
	createUi(chatList) {
		const obj = this;
		chatList.forEach((item, i) => {
			if (item["role"] == "user") {
				const $inputDiv = $('<div class="class_div_input"></div>');
				$inputDiv.text(item["content"]);
				var $divWrap = $('<div class="class_div_wrap" tid="' + item["id"] + '"></div>');
				$divWrap.append($inputDiv);
				$_mainArea.prepend($divWrap);
			} else if (item["role"] == "assistant") {
				const $outputDiv = $('<div class="class_div_output"></div>');
				$outputDiv.append(obj.render(item["content"]));
				$divWrap = $('<div class="class_div_wrap" tid="' + item["id"] + '"></div>');
				$divWrap.append($outputDiv);
				$_mainArea.prepend($divWrap);
			}
		});
	}
	
	remove() {
		const obj = this;
		const tid = this.tid;
		const url = URL_PREFIX + "remove_thread/" + tid;
		$.get(url, function(data) {
			console.log(data);
			if (data["status"] == "OK") {
				_threadList.remove_thread(tid);
			} else {
				StaticUtils.showError(data["reason"]);
			}
		}).fail(function(e) {
			StaticUtils.showError(e.statusText);
		});
	}
}

class ThreadList {
	
	constructor() {
		this.model = null;
		this.start = -1;
		this.end = -1;
		this.tags = {};
	}
	
	load() {
		const obj = this;
		const url = URL_PREFIX + "list_thread?start=-1&length=" + THREAD_LIST_PAGE_SIZE;
		$.get(url, function(data) {
			console.log(data);
			obj.model = data.reverse();
			obj.createUi(obj.model);
			const ml = obj.model.length;
			if (ml > 0) {
				const cid = obj.model[0]["id"];
				_currentThread = new ChatThread(cid);
				obj.start = obj.model[ml - 1]["index"];
				obj.end = obj.model[0]["index"];
			} else {
				_currentThread = new ChatThread("last");
				obj.start = 0;
				obj.end = 0;
			}
			_currentThread.load();
			window.scrollTo(0, document.body.scrollHeight);
		});
	}
	
	loadMore() {
		const obj = this;
		if (obj.start <= 0) {
			return;
		}
		var start;
		var length;
		if (obj.start >= THREAD_LIST_PAGE_SIZE) {
			start = obj.start - THREAD_LIST_PAGE_SIZE;
			length = THREAD_LIST_PAGE_SIZE;
		} else {
			start = 0;
			length = obj.start;
		}
		const url = URL_PREFIX + "list_thread?start=" + start + "&length=" + length;
		$.get(url, function(data) {
			console.log(data);
			data = data.reverse();
			if (data.length > 0) {
				obj.model = obj.model.concat(data);
				obj.createUi(data);
				obj.start = data[data.length - 1]["index"];
			}
		});
	}
	
	createUi(model) {
		const obj = this;
		$.each(model, function(idx, thread) {
			const $tag = $('<div class="class_div_thread_tag"><span class="class_span_thread_tag" tid="' 
					+ thread["id"] + '">' + thread["title"] + '</span></div>');
			obj.tags[thread["id"]] = $tag;
			$tag.find('span').click(function() {
				_currentThread = new ChatThread(thread["id"]);
				_currentThread.load();
			});
			$_sidebar.append($tag);
		});
	}
	
	addNew() {
		const obj = this;
		const url = URL_PREFIX + "new_thread";
		$.get(url, function(data) {
			console.log(data);
			const thread = data;
			obj.model.unshift(data);
			obj.end++;
			const $tag = $('<div class="class_div_thread_tag"><span class="class_span_thread_tag" tid="' 
					+ thread["id"] + '">New thread</span></div>');
			$tag.find('span').click(function() {
				_currentThread = new ChatThread(thread["id"]);
				_currentThread.load();
			});
			_threadList.tags[thread["id"]] = $tag;
			$_sidebar.prepend($tag);
			_currentThread = new ChatThread(thread["id"]);
			_currentThread.initNew(); 
		});
	}
	
	remove_thread(tid) {
		const obj = this;
		const to_remove = obj._findThread(tid);
		if (to_remove >= 0) {
			obj.model.splice(to_remove, 1);
		}
		const $tag = obj.tags[tid];
		if ($tag) {
			$tag.find('span').off("click").removeClass("class_span_thread_tag").addClass("class_span_thread_removed");
			delete obj.tags[tid];
		}
		if (obj.model.length > 0) {
			const tid = obj.model[0]["id"];
			_currentThread = new ChatThread(tid);
			_currentThread.load();
		} else {
			obj.addNew();
		}
	}
	
	_findThread(tid) {
		for (var i = 0; i < this.model.length; i++) {
			if (this.model[i]["id"] == tid) {
				return i;
			}
		}
		return -1;
	}
}

class RemoveModal {

	constructor(id)
	{
		this.id = id;
		this.$modelUi = $('#' + id);
		this.$confirm = this.$modelUi.find('#' + this.id + '_confirm');
	}
	
	init() {
		const obj = this;
		this.$confirm.click(function() {
			obj.remove();
		});
	
	}
	
	remove() {
		const obj = this;
		if (!_currentThread) {
			StaticUtils.showError("failed to remove: no chat thread selected");
			return;
		}

		
		try {
			_currentThread.remove();
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
		const tid = $li.attr('tid');
		_currentThread = new ChatThread(tid);
		_currentThread.load();
		this.$textInput.blur();
	}
	
	doSearch() {
		var target = '';
		this.curSel = -1;

		var html = '';
//		var found = {};
		const obj = this;
		const fl = [];
		$.each(_threadList.model, function(idx, thread) {
			const title = thread["title"];
			const tid = thread["id"];
			var word = obj.$textInput.val().toLowerCase();
			const tf = obj._getTf(title, word);
			if (tf["tf"] >= 1) {
				fl.push(Object.assign({"tid": tid, "title": title}, tf));
			}
//			if (title.toLowerCase().indexOf(word) >= 0) {
//				html += '<li class="messageitem" href="#" tid="' + tid + '"><span class="widget_text">' + 
//						title + '</span></li>';
//			} 
//			console.log(obj._getTf(title, word), title);
		});
		fl.sort(function(a, b) {
			if (a["present"] == b["present"]) {
				return b["tf"] - a["tf"];
			} else {
				return b["present"] - a["present"];
			}
		});
		
		fl.forEach(function(item) {
			html += '<li class="messageitem" href="#" tid="' + item["tid"] + '"><span class="widget_text">' + 
			item["title"] + '</span></li>';
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
	
	_getObject (rawData) {
		var index = {};
		const words = rawData.replace(/[.,?!:;()"'-]/g, " ").replace(/\s+/g, " ").toLowerCase().split(" ");

		words.forEach(function (word) {

			if (!(index.hasOwnProperty(word))) {
				index[word] = 0;
			}

			index[word]++;
		});

		return index;
	}

	_getTf (text, searchTerm) {

		var success;
		var termPresent = 0;
		var frequency = 0;
		var maxVal = 0;
		var nPresent = 0;

		const doc = this._getObject(text);
		searchTerm = searchTerm.replace(/[.,?!;()"'-]/g, " ").replace(/\s+/g, " ").toLowerCase().split(" ");

		for (var word in doc) {
			if (maxVal < doc[word]) {
				maxVal = doc[word];
			}
		}

		for (var j=0 ; j<searchTerm.length ; ++j) {
			success = false;
			for (var word in doc) {
//				if (word === searchTerm[j]) {
				if (word.indexOf(searchTerm[j]) >= 0) {
					success = true;
					frequency += doc[word];
				}
			}

			if (success === true) {
				termPresent++;
			}
		}

		const tf = 0.5 + (0.5 * frequency)/maxVal;

		if (termPresent === searchTerm.length) {
			nPresent++;
		}

		return {"tf": tf, "present": nPresent};
	}

};

$(document).ready(function() {
	const loadingLabel = new LoadingLabel($('#id_span_waiting'));
	loadingLabel.init();

	const chatInput = new ChatInput();
	chatInput.init();
	
	_threadList = new ThreadList();
	_threadList.load();
	
//	var jsonStream = new EventSource('http://localhost:5000')
//	jsonStream.onmessage = function (e) {
//	   var message = JSON.parse(e.data);
//	   console.log(message);
//	};
	
	$("#id_span_addThread").click(function() {
		_threadList.addNew();
	});
	
	$("#id_button_new").click(function() {
		_threadList.addNew();
	});
	
	$(window).scroll(function () {
		const $obj = $(this);
	    if ($obj.scrollTop()  <= 0 ){
	    	if (_currentThread) {
	    		_currentThread.loadMore();
	    	}
	    }
	    _scrollBlocker = $(document).height() > 200 && ($obj.scrollTop() + $obj.height() < $(document).height() - 200);
	});
	
	$_sidebar.scroll(function() {
		const $obj = $(this);
        if ($obj.scrollTop() + $obj.innerHeight() >= $obj[0].scrollHeight) {
            _threadList.loadMore();
        }
	});
	
	_removeModal = new RemoveModal('id_modal_deleteChat');
	_removeModal.init();
	$('#id_button_delete').click(function() {
		_removeModal.open();
	});
	
	$('#id_button_save').click(function() {
		window.print();
	});
	
	const searchWidget = new SearchWidget($('#id_input_search_text'));
	searchWidget.init();

	console.log('finished init');
});