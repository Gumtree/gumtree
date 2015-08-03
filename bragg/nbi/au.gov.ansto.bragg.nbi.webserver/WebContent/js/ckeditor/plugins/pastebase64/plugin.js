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
        });


    }

    function listProp(obj){
        var text = "";
        $.each( obj, function(i, n){
            text += i + ":" + n + "\n";
        });
        return text;
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

        return Array.prototype.forEach.call(clipboardData.types, function (type, i) {
            if (found) {
                return;
            }
            console.log("try");
            console.log(listProp(clipboardData));
            if (clipboardData.items) {
                if (type.match(imageType) || clipboardData.items[i].type.match(imageType)) {
                    readImageAsBase64(clipboardData.items[i], editor);
                    return found = true;
                }            	
            } else if (clipboardData.files && clipboardData.files.length > 0) {
            	if (type.match(imageType) || clipboardData.files[i].type.match(imageType)) {
                    readImageAsBase64(clipboardData.files[i], editor);
                    return found = true;
                }
            }
        });
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
