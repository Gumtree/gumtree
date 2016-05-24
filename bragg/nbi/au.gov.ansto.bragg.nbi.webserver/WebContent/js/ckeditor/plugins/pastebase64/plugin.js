(function () {
	'use strict';

	CKEDITOR.plugins.add('pastebase64', {
		init: init
	});

	function init(editor) {
		if (editor.addFeature) {
			editor.addFeature({
				allowedContent: 'img[alt,id,!src]{width,height};'
			});
		}

		editor.on("contentDom", function () {
			var editableElement = editor.editable ? editor.editable() : editor.document;
			editableElement.on("paste", onPaste, null, {editor: editor});
			editableElement.on('drop', onDrop, null, {editor: editor});
		});
	}

	function listProp(obj){
		var text = "";
		$.each( obj, function(i, n){
			text += i + ":" + n + "\n";
		});
		return text;
	}

	function onDrop(event) {
		var editor = event.listenerData && event.listenerData.editor;
		var $event = event.data.$;
//		var clipboardData = $event.clipboardData;

		// Let user modify drag and drop range.
		var dropRange = $event.dropRange,
		dragRange = $event.dragRange,
		dataTransfer = $event.dataTransfer;

		if (!dataTransfer) {
			return;
		}

		var found = false;
		var imageType = /^image/;

		if (dataTransfer.files && dataTransfer.files.length > 0) {
			for (var i = 0; i < dataTransfer.files.length; i++){
				var file = dataTransfer.files[i];
				if (file.type.match(imageType)) {
					readImageAsBase64(file, editor);
					found = true;
				}
			}
		}
		if (found) {
			return found;
		}

		if (dataTransfer.items && dataTransfer.items.length > 0) {
			for (var i = 0; i < dataTransfer.items.length; i++){
				var file = dataTransfer.items[i];
				if (file.type.match(imageType)) {
					readImageAsBase64(file, editor);
					found = true;
				}
			}
		}

		return found;

	}

	function onPaste(event) {
		var editor = event.listenerData && event.listenerData.editor;
		var $event = event.data.$;
		var clipboardData = $event.clipboardData;
		var found = false;
		var imageType = /^image/;

		if (!clipboardData) {
			return;
		}
//		var eventData = CKEDITOR.plugins.clipboard.initPasteDataTransfer(event);
//		eventData.cacheData();
//		console.log(CKEDITOR.plugins.clipboard.initPasteDataTransfer(event)._.data);
//		var text = eventData.getData('text/html');
		
//		var text = clipboardData.getData('text/html');
//		clipboardData.setData('text/html', '<p>haha</p>');
//		console.log(text);
		
		if (clipboardData.files && clipboardData.files.length > 0) {
			for (var i = 0; i < clipboardData.files.length; i++){
				var file = clipboardData.files[i];
				if (file.type.match(imageType)) {
					readImageAsBase64(file, editor);
					found = true;
				}
			}
		}
		if (found) {
			return found;
		}

		if (clipboardData.items && clipboardData.items.length > 0) {
			for (var i = 0; i < clipboardData.items.length; i++){
				var file = clipboardData.items[i];
				if (file.type.match(imageType)) {
					readImageAsBase64(file, editor);
					found = true;
				}
			}
		}
		return found;
//		return Array.prototype.forEach.call(clipboardData.types, function (type, i) {
//		if (found) {
//		return;
//		}
//		if (clipboardData.items) {
//		if (type.match(imageType) || clipboardData.items[i].type.match(imageType)) {
//		readImageAsBase64(clipboardData.items[i], editor);
//		return found = true;
//		}            	
//		} else if (clipboardData.files && clipboardData.files.length > 0) {
//		if (type.match(imageType) || clipboardData.files[i].type.match(imageType)) {
//		readImageAsBase64(clipboardData.files[i], editor);
//		return found = true;
//		}
//		}
//		});
	}

	function readImageAsBase64(item, editor) {
		if (!item) {
			return;
		}

		var file;
		if ( typeof item.getAsFile === 'function') {
			file = item.getAsFile();
		} else {
			file = item;
		}

		var reader = new FileReader();

		reader.onload = function (evt) {
			var element = editor.document.createElement('img', {
				attributes: {
					src: evt.target.result
				}
			});

			// We use a timeout callback to prevent a bug where insertElement inserts at first caret
			// position
			setTimeout(function () {
				editor.insertElement(element);
			}, 10);
		};

		reader.readAsDataURL(file);
	}
})();
