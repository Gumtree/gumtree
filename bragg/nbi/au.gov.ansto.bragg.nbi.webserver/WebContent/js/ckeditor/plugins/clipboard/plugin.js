/**
 * @license Copyright (c) 2003-2015, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

/**
 * @ignore
 * File overview: Clipboard support.
 */

//
// COPY & PASTE EXECUTION FLOWS:
// -- CTRL+C
// 		* if ( isCustomCopyCutSupported )
// 			* dataTransfer.setData( 'text/html', getSelectedHtml )
//		* else
//			* browser's default behavior
// -- CTRL+X
//		* listen onKey (onkeydown)
//		* fire 'saveSnapshot' on editor
// 		* if ( isCustomCopyCutSupported )
// 			* dataTransfer.setData( 'text/html', getSelectedHtml )
// 			* extractSelectedHtml // remove selected contents
//		* else
//			* browser's default behavior
//		* deferred second 'saveSnapshot' event
// -- CTRL+V
//		* listen onKey (onkeydown)
//		* simulate 'beforepaste' for non-IEs on editable
//		* listen 'onpaste' on editable ('onbeforepaste' for IE)
//		* fire 'beforePaste' on editor
//		* if ( !canceled && ( htmlInDataTransfer || !external paste) && dataTransfer is not empty ) getClipboardDataByPastebin
//		* fire 'paste' on editor
//		* !canceled && fire 'afterPaste' on editor
// -- Copy command
//		* tryToCutCopy
//			* execCommand
//		* !success && notification
// -- Cut command
//		* fixCut
//		* tryToCutCopy
//			* execCommand
//		* !success && notification
// -- Paste command
//		* fire 'paste' on editable ('beforepaste' for IE)
//		* !canceled && execCommand 'paste'
//		* !success && fire 'pasteDialog' on editor
// -- Paste from native context menu & menubar
//		(Fx & Webkits are handled in 'paste' default listner.
//		Opera cannot be handled at all because it doesn't fire any events
//		Special treatment is needed for IE, for which is this part of doc)
//		* listen 'onpaste'
//		* cancel native event
//		* fire 'beforePaste' on editor
//		* if ( !canceled && ( htmlInDataTransfer || !external paste) && dataTransfer is not empty ) getClipboardDataByPastebin
//		* execIECommand( 'paste' ) -> this fires another 'paste' event, so cancel it
//		* fire 'paste' on editor
//		* !canceled && fire 'afterPaste' on editor
//
//
// PASTE EVENT - PREPROCESSING:
// -- Possible dataValue types: auto, text, html.
// -- Possible dataValue contents:
//		* text (possible \n\r)
//		* htmlified text (text + br,div,p - no presentional markup & attrs - depends on browser)
//		* html
// -- Possible flags:
//		* htmlified - if true then content is a HTML even if no markup inside. This flag is set
//			for content from editable pastebins, because they 'htmlify' pasted content.
//
// -- Type: auto:
//		* content: htmlified text ->	filter, unify text markup (brs, ps, divs), set type: text
//		* content: html ->				filter, set type: html
// -- Type: text:
//		* content: htmlified text ->	filter, unify text markup
//		* content: html ->				filter, strip presentional markup, unify text markup
// -- Type: html:
//		* content: htmlified text ->	filter, unify text markup
//		* content: html ->				filter
//
// -- Phases:
// 		* if dataValue is empty copy data from dataTransfer to dataValue (priority 1)
//		* filtering (priorities 3-5) - e.g. pastefromword filters
//		* content type sniffing (priority 6)
//		* markup transformations for text (priority 6)
//
// DRAG & DROP EXECUTION FLOWS:
// -- Drag
//		* save to the global object:
//			* drag timestamp (with 'cke-' prefix),
//			* selected html,
//			* drag range,
//			* editor instance.
//		* put drag timestamp into event.dataTransfer.text
// -- Drop
//		* if events text == saved timestamp && editor == saved editor
//			internal drag & drop occurred
//			* getRangeAtDropPosition
//			* create bookmarks for drag and drop ranges starting from the end of the document
//			* dragRange.deleteContents()
//			* fire 'paste' with saved html and drop range
//		* if events text == saved timestamp && editor != saved editor
//			cross editor drag & drop occurred
//			* getRangeAtDropPosition
//			* fire 'paste' with saved html
//			* dragRange.deleteContents()
//			* FF: refreshCursor on afterPaste
//		* if events text != saved timestamp
//			drop form external source occurred
//			* getRangeAtDropPosition
//			* if event contains html data then fire 'paste' with html
//			* else if event contains text data then fire 'paste' with encoded text
//			* FF: refreshCursor on afterPaste

'use strict';

( function() {
	// Register the plugin.
	CKEDITOR.plugins.add( 'clipboard', {
		requires: 'dialog',
		// jscs:disable maximumLineLength
		lang: 'af,ar,bg,bn,bs,ca,cs,cy,da,de,el,en,en-au,en-ca,en-gb,eo,es,et,eu,fa,fi,fo,fr,fr-ca,gl,gu,he,hi,hr,hu,id,is,it,ja,ka,km,ko,ku,lt,lv,mk,mn,ms,nb,nl,no,pl,pt,pt-br,ro,ru,si,sk,sl,sq,sr,sr-latn,sv,th,tr,tt,ug,uk,vi,zh,zh-cn', // %REMOVE_LINE_CORE%
		// jscs:enable maximumLineLength
		icons: 'copy,copy-rtl,cut,cut-rtl,paste,paste-rtl', // %REMOVE_LINE_CORE%
		hidpi: true, // %REMOVE_LINE_CORE%
		init: function( editor ) {
			var filterType,
				filtersFactory = filtersFactoryFactory();

			if ( editor.config.forcePasteAsPlainText ) {
				filterType = 'plain-text';
			} else if ( editor.config.pasteFilter ) {
				filterType = editor.config.pasteFilter;
			}
			// On Webkit the pasteFilter defaults 'semantic-content' because pasted data is so terrible
			// that it must be always filtered.
			else if ( CKEDITOR.env.webkit && !( 'pasteFilter' in editor.config ) ) {
				filterType = 'semantic-content';
			}

			editor.pasteFilter = filtersFactory.get( filterType );

			initPasteClipboard( editor );
			initDragDrop( editor );

			CKEDITOR.dialog.add( 'paste', CKEDITOR.getUrl( this.path + 'dialogs/paste.js' ) );

			editor.on( 'paste', function( evt ) {
				// Init `dataTransfer` if `paste` event was fired without it, so it will be always available.
				if ( !evt.data.dataTransfer ) {
					evt.data.dataTransfer = new CKEDITOR.plugins.clipboard.dataTransfer();
				}

				// If dataValue is already set (manually or by paste bin), so do not override it.
				if ( evt.data.dataValue ) {
					return;
				}

				var dataTransfer = evt.data.dataTransfer,
					// IE support only text data and throws exception if we try to get html data.
					// This html data object may also be empty if we drag content of the textarea.
					value = dataTransfer.getData( 'text/html' );

				if ( value ) {
					evt.data.dataValue = value;
					evt.data.type = 'html';
				} else {
					// Try to get text data otherwise.
					value = dataTransfer.getData( 'text/plain' );

					if ( value ) {
						evt.data.dataValue = editor.editable().transformPlainTextToHtml( value );
						evt.data.type = 'text';
					}
				}
			}, null, null, 1 );

			editor.on( 'paste', function( evt ) {
				var data = evt.data.dataValue,
					blockElements = CKEDITOR.dtd.$block;

				// Filter webkit garbage.
				if ( data.indexOf( 'Apple-' ) > -1 ) {
					// Replace special webkit's &nbsp; with simple space, because webkit
					// produces them even for normal spaces.
					data = data.replace( /<span class="Apple-converted-space">&nbsp;<\/span>/gi, ' ' );

					// Strip <span> around white-spaces when not in forced 'html' content type.
					// This spans are created only when pasting plain text into Webkit,
					// but for safety reasons remove them always.
					if ( evt.data.type != 'html' ) {
						data = data.replace( /<span class="Apple-tab-span"[^>]*>([^<]*)<\/span>/gi, function( all, spaces ) {
							// Replace tabs with 4 spaces like Fx does.
							return spaces.replace( /\t/g, '&nbsp;&nbsp; &nbsp;' );
						} );
					}

					// This br is produced only when copying & pasting HTML content.
					if ( data.indexOf( '<br class="Apple-interchange-newline">' ) > -1 ) {
						evt.data.startsWithEOL = 1;
						evt.data.preSniffing = 'html'; // Mark as not text.
						data = data.replace( /<br class="Apple-interchange-newline">/, '' );
					}

					// Remove all other classes.
					data = data.replace( /(<[^>]+) class="Apple-[^"]*"/gi, '$1' );
				}

				// Strip editable that was copied from inside. (#9534)
				if ( data.match( /^<[^<]+cke_(editable|contents)/i ) ) {
					var tmp,
						editable_wrapper,
						wrapper = new CKEDITOR.dom.element( 'div' );

					wrapper.setHtml( data );
					// Verify for sure and check for nested editor UI parts. (#9675)
					while ( wrapper.getChildCount() == 1 &&
							( tmp = wrapper.getFirst() ) &&
							tmp.type == CKEDITOR.NODE_ELEMENT &&	// Make sure first-child is element.
							( tmp.hasClass( 'cke_editable' ) || tmp.hasClass( 'cke_contents' ) ) ) {
						wrapper = editable_wrapper = tmp;
					}

					// If editable wrapper was found strip it and bogus <br> (added on FF).
					if ( editable_wrapper )
						data = editable_wrapper.getHtml().replace( /<br>$/i, '' );
				}

				if ( CKEDITOR.env.ie ) {
					// &nbsp; <p> -> <p> (br.cke-pasted-remove will be removed later)
					data = data.replace( /^&nbsp;(?: |\r\n)?<(\w+)/g, function( match, elementName ) {
						if ( elementName.toLowerCase() in blockElements ) {
							evt.data.preSniffing = 'html'; // Mark as not a text.
							return '<' + elementName;
						}
						return match;
					} );
				} else if ( CKEDITOR.env.webkit ) {
					// </p><div><br></div> -> </p><br>
					// We don't mark br, because this situation can happen for htmlified text too.
					data = data.replace( /<\/(\w+)><div><br><\/div>$/, function( match, elementName ) {
						if ( elementName in blockElements ) {
							evt.data.endsWithEOL = 1;
							return '</' + elementName + '>';
						}
						return match;
					} );
				} else if ( CKEDITOR.env.gecko ) {
					// Firefox adds bogus <br> when user pasted text followed by space(s).
					data = data.replace( /(\s)<br>$/, '$1' );
				}

				evt.data.dataValue = data;
			}, null, null, 3 );

			editor.on( 'paste', function( evt ) {
				var dataObj = evt.data,
					type = dataObj.type,
					data = dataObj.dataValue,
					trueType,
					// Default is 'html'.
					defaultType = editor.config.clipboard_defaultContentType || 'html',
					transferType = dataObj.dataTransfer.getTransferType( editor );

				// If forced type is 'html' we don't need to know true data type.
				if ( type == 'html' || dataObj.preSniffing == 'html' ) {
					trueType = 'html';
				} else {
					trueType = recogniseContentType( data );
				}

				// Unify text markup.
				if ( trueType == 'htmlifiedtext' ) {
					data = htmlifiedTextHtmlification( editor.config, data );
				}

				// Strip presentional markup & unify text markup.
				// Forced plain text (dialog or forcePAPT).
				// Note: we do not check dontFilter option in this case, because forcePAPT was implemented
				// before pasteFilter and pasteFilter is automatically used on Webkit&Blink since 4.5, so
				// forcePAPT should have priority as it had before 4.5.
				if ( type == 'text' && trueType == 'html' ) {
					data = filterContent( editor, data, filtersFactory.get( 'plain-text' ) );
				}
				// External paste and pasteFilter exists and filtering isn't disabled.
				else if ( transferType == CKEDITOR.DATA_TRANSFER_EXTERNAL && editor.pasteFilter && !dataObj.dontFilter ) {
					data = filterContent( editor, data, editor.pasteFilter );
				}

				if ( dataObj.startsWithEOL ) {
					data = '<br data-cke-eol="1">' + data;
				}
				if ( dataObj.endsWithEOL ) {
					data += '<br data-cke-eol="1">';
				}

				if ( type == 'auto' ) {
					type = ( trueType == 'html' || defaultType == 'html' ) ? 'html' : 'text';
				}

				dataObj.type = type;
				dataObj.dataValue = data;
				delete dataObj.preSniffing;
				delete dataObj.startsWithEOL;
				delete dataObj.endsWithEOL;
			}, null, null, 6 );

			// Inserts processed data into the editor at the end of the
			// events chain.
			editor.on( 'paste', function( evt ) {
				var data = evt.data;

				if ( data.dataValue ) {
					var html = data.dataValue
					var div = $('<div id="insert_temp_holder"/>').html(html);
					var getarray = [], i, len;
					div.find('img').each(function() {
						if ($(this).attr('src').startsWith('http')){
							var dfd = $.Deferred();
							convertImgToBase64($(this), $(this).attr('src'), function(image, base64Img){
						        image.attr('src', 'data:image/png;base64,' + base64Img);
						        dfd.resolve();
						    });
							getarray.push(dfd.promise());
						}
					});
					
					$.when.apply($, getarray).done(function () {
						editor.insertHtml( div.html(), data.type, data.range );
					});
					
//					var images = div.find('img');
//					console.log('found image');
//					
//					var getarray = [], i, len;
//					images.each(function(idx, value) {
//						var dfd = $.Deferred();
//						convertImgToBase64($(this).attr('src'), function(base64Img){
//					        console.log(base64Img);
//					        $(this).attr('src', base64Img);
//					        console.log("converted");
//					        console.log($(this).parent().html());
//					        dfd.resolve();
//					    });
//						getarray.push(dfd.promise());
//					});
//					$.when.apply($, getarray).done(function () {
//						// do things that need to wait until ALL gets are done
//						console.log("insert html");
//						console.log(div.html());
//						editor.insertHtml( div.html(), data.type, data.range );
//					});

//					images.each(function(idx, value) {
//						convertImgToBase64($(this).attr('src'), function(base64Img){
//					        $(this).attr('src', base64Img);
//					        console.log(base64Img);
//					        var img = $('<img />', { 
//					        	  id: 'Myid',
//					        	  src: base64Img,
//					        	  alt: 'MyAlt'
//					        	});
//					        var html =  $('<div/>').append(img).html();
//					        console.log(html);
//					        editor.insertHtml( html, data.type, data.range );
//					    });
////						$(this).attr('src', "data:image/png;base64,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HYOI8iAWLhERkQxYuERERDJg4RIREcmAhUtERCQDFi4REZEMWLhEREQyYOESERHJgIVLREQkAxYuERGRDFi4REREMmDhEhERyYCFS0REJAMWLhERkQxYuERERDJg4RIREcmAhUtERCQDFi4REZEMWLhEREQyYOESERHJgIVLREQkAxYuERGRDFi4REREMmDhEhERyYCFS0REJAMWLhERkQxYuERERDJg4RIREcmAhUtERCQDFi4REZEMWLhEREQyYOESERHJgIVLREQkAxYuERGRDFi4REREMmDhEhERyYCFS0REJAMWLhERkQxYuERERDJg4RIREcmAhUtERCQDFi4REZEMWLhEREQyYOESERHJgIVLREQkAxYuERGRDFi4REREMmDhEhERyYCFS0REJAMWLhERkQxYuERERDJg4RIREcmAhUtERCQDFi4REZEMWLhEREQyYOESERHJgIVLREQkAxYuERGRDFi4REREMmDhEhERyYCFS0REJAMWLhERkQxYuERERDJg4RIREcmAhUtERCQDFi4REZEMWLhEREQyYOESERHJgIVLREQkAxYuERGRDFi4REREMmDhEhERyYCFS0REJAMWLhERkQxYuERERDJg4RIREcmAhUtERCQDFi4REZEMWLhEREQyYOESERHJgIVLREQkAxYuERGRDFi4REREMmDhEhERyYCFS0REJAMWLhERkQxYuERERDJg4RIREcmAhUtERCQDFi4REZEMWLhEREQyYOESERHJgIVLREQkAxYuERGRDFi4REREMmDhEhERyYCFS0REJAMWLhERkQxYuERERDJg4RIREcmAhUtERCQDFi4REZEMWLhEREQyYOESERHJgIVLREQkAxYuERGRDFi4REREMmDhEhERyYCFS0REJAMWLhERkQxYuERERDJg4RIREcmAhUtERCQDFi4REZEMWLhEREQyYOESERHJgIVLREQkAxYuERGRDFi4REREMmDhEhERyYCFS0REJAMWLhERkQxYuERERDJg4RIREcmAhUtERCQDFi4REZEMWLhEREQyYOESERHJgIVLREQkAxYuERGRDFi4REREMmDhEhH9f+3VsQAAAADAIH/rWewqiWAgXAAYCBcABsIFgIFwAWAQ0mRuUSPJFnkAAAAASUVORK5CYII=");
////						console.log($(this).attr('src'));
//					});
//					console.log(div.html());
//					editor.insertHtml( div.html() + '<p>haha</p>', data.type, data.range );
//					editor.insertHtml( data.dataValue, data.type, data.range );

					// Defer 'afterPaste' so all other listeners for 'paste' will be fired first.
					// Fire afterPaste only if paste inserted some HTML.
					setTimeout( function() {
						editor.fire( 'afterPaste' );
					}, 0 );
				}
			}, null, null, 1000 );

			editor.on( 'pasteDialog', function( evt ) {
				// TODO it's possible that this setTimeout is not needed any more,
				// because of changes introduced in the same commit as this comment.
				// Editor.getClipboardData adds listener to the dialog's events which are
				// fired after a while (not like 'showDialog').
				setTimeout( function() {
					// Open default paste dialog.
					editor.openDialog( 'paste', evt.data );
				}, 0 );
			} );
		}
	} );
	
//	function convertImgToBase64(image, url, callback, outputFormat){
//		var img = new Image();
//		img.crossOrigin = 'Anonymous';
//		console.log("converting");
//		img.onload = function(){
//		    var canvas = document.createElement('CANVAS');
//		    var ctx = canvas.getContext('2d');
//			canvas.height = this.height;
//			canvas.width = this.width;
//		  	ctx.drawImage(this,0,0);
//		  	var ret = url;
//		  	try {
//			  	ret = canvas.toDataURL(outputFormat || 'image/png');
//			} catch (e) {
//			  	console.log(e);
//			} finally {
//				callback(image, ret);
//			}
//		  	canvas = null; 
//		};
//		try {
//			img.src = url;			
//		} catch (e) {
//			callback(image, url);
//		}
//	}

//	function encode64(inputStr){
//		var b64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
//		var outputStr = "";
//		var i = 0;
//
//		while (i<inputStr.length){
//			var byte1 = inputStr.charCodeAt(i++) & 0xff;
//			var byte2 = inputStr.charCodeAt(i++) & 0xff;
//			var byte3 = inputStr.charCodeAt(i++) & 0xff;
//
//			var enc1 = byte1 >> 2;
//			var enc2 = ((byte1 & 3) << 4) | (byte2 >> 4);
//
//			var enc3, enc4;
//			if (isNaN(byte2)){
//				enc3 = enc4 = 64;
//			} else{
//				enc3 = ((byte2 & 15) << 2) | (byte3 >> 6);
//				if (isNaN(byte3)){
//					enc4 = 64;
//				} else {
//					enc4 = byte3 & 63;
//				}
//			}
//			outputStr +=  b64.charAt(enc1) + b64.charAt(enc2) + b64.charAt(enc3) + b64.charAt(enc4);
//		} 
//		return outputStr;
//	}

	function convertImgToBase64(image, url, callback, outputFormat){
		console.log('using ajax')
		var localUrl = "notebook/imageService?url=" + url;
		$.ajax({
			cache: false,
			type: "GET",
			url: localUrl,
			dataType: "text",
			success: function (data) {
				console.log("ajax done");
				callback(image, data);
			},
			error: function (xhr) {
				alert("Error occurred while loading the image. ");
			},
		});
	}

	function getBase64Image(img) {
		var canvas = document.createElement("canvas");
		canvas.width = img.width;
		canvas.height = img.height;
		var ctx = canvas.getContext("2d");
		ctx.drawImage(img, 0, 0);
		var dataURL = canvas.toDataURL("image/png");
//		return dataURL.replace(/^data:image\/(png|jpg);base64,/, "");
		return dataURL;
	}
	
	function firePasteEvents( editor, data, withBeforePaste ) {
		if ( !data.type ) {
			data.type = 'auto';
		}

		if ( withBeforePaste ) {
			// Fire 'beforePaste' event so clipboard flavor get customized
			// by other plugins.
			if ( editor.fire( 'beforePaste', data ) === false )
				return false; // Event canceled
		}

		// Do not fire paste if there is no data (dataValue and dataTranfser are empty).
		// This check should be done after firing 'beforePaste' because for native paste
		// 'beforePaste' is by default fired even for empty clipboard.
		if ( !data.dataValue && data.dataTransfer.isEmpty() ) {
			return false;
		}

		if ( !data.dataValue ) {
			data.dataValue = '';
		}

		// Because of FF bug we need to use this hack, otherwise cursor is hidden
		// or it is not possible to move it (#12420).
		// Also, check that editor.toolbox exists, because the toolbar plugin might not be loaded (#13305).
		if ( CKEDITOR.env.gecko && data.method == 'drop' && editor.toolbox ) {
			editor.once( 'afterPaste', function() {
				editor.toolbox.focus();
			} );
		}

		return editor.fire( 'paste', data );
	}

	function initPasteClipboard( editor ) {
		var clipboard = CKEDITOR.plugins.clipboard,
			preventBeforePasteEvent = 0,
			preventPasteEvent = 0,
			inReadOnly = 0,
			// Safari doesn't like 'beforepaste' event - it sometimes doesn't
			// properly handles ctrl+c. Probably some race-condition between events.
			// Chrome and Firefox works well with both events, so better to use 'paste'
			// which will handle pasting from e.g. browsers' menu bars.
			// IE7/8 doesn't like 'paste' event for which it's throwing random errors.
			mainPasteEvent = CKEDITOR.env.ie ? 'beforepaste' : 'paste';

		addListeners();
		addButtonsCommands();

		/**
		 * Gets clipboard data by directly accessing the clipboard (IE only) or opening the paste dialog window.
		 *
		 *		editor.getClipboardData( { title: 'Get my data' }, function( data ) {
		 *			if ( data )
		 *				alert( data.type + ' ' + data.dataValue );
		 *		} );
		 *
		 * @member CKEDITOR.editor
		 * @param {Object} options
		 * @param {String} [options.title] The title of the paste dialog window.
		 * @param {Function} callback A function that will be executed with `data.type` and `data.dataValue`
		 * or `null` if none of the capturing methods succeeded.
		 */
		editor.getClipboardData = function( options, callback ) {
			var beforePasteNotCanceled = false,
				dataType = 'auto',
				dialogCommited = false;

			// Options are optional - args shift.
			if ( !callback ) {
				callback = options;
				options = null;
			}

			// Listen with maximum priority to handle content before everyone else.
			// This callback will handle paste event that will be fired if direct
			// access to the clipboard succeed in IE.
			editor.on( 'paste', onPaste, null, null, 0 );

			// Listen at the end of listeners chain to see if event wasn't canceled
			// and to retrieve modified data.type.
			editor.on( 'beforePaste', onBeforePaste, null, null, 1000 );

			// getClipboardDataDirectly() will fire 'beforePaste' synchronously, so we can
			// check if it was canceled and if any listener modified data.type.

			// If command didn't succeed (only IE allows to access clipboard and only if
			// user agrees) open and handle paste dialog.
			if ( getClipboardDataDirectly() === false ) {
				// Direct access to the clipboard wasn't successful so remove listener.
				editor.removeListener( 'paste', onPaste );

				// If beforePaste was canceled do not open dialog.
				// Add listeners only if dialog really opened. 'pasteDialog' can be canceled.
				if ( beforePasteNotCanceled && editor.fire( 'pasteDialog', onDialogOpen ) ) {
					editor.on( 'pasteDialogCommit', onDialogCommit );

					// 'dialogHide' will be fired after 'pasteDialogCommit'.
					editor.on( 'dialogHide', function( evt ) {
						evt.removeListener();
						evt.data.removeListener( 'pasteDialogCommit', onDialogCommit );

						// Because Opera has to wait a while in pasteDialog we have to wait here.
						setTimeout( function() {
							// Notify even if user canceled dialog (clicked 'cancel', ESC, etc).
							if ( !dialogCommited )
								callback( null );
						}, 10 );
					} );
				} else {
					callback( null );
				}
			}

			function onPaste( evt ) {
				evt.removeListener();
				evt.cancel();
				callback( evt.data );
			}

			function onBeforePaste( evt ) {
				evt.removeListener();
				beforePasteNotCanceled = true;
				dataType = evt.data.type;
			}

			function onDialogCommit( evt ) {
				evt.removeListener();
				// Cancel pasteDialogCommit so paste dialog won't automatically fire
				// 'paste' evt by itself.
				evt.cancel();
				dialogCommited = true;
				callback( { type: dataType, dataValue: evt.data, method: 'paste' } );
			}

			function onDialogOpen() {
				this.customTitle = ( options && options.title );
			}
		};

		function addButtonsCommands() {
			addButtonCommand( 'Cut', 'cut', createCutCopyCmd( 'cut' ), 10, 1 );
			addButtonCommand( 'Copy', 'copy', createCutCopyCmd( 'copy' ), 20, 4 );
			addButtonCommand( 'Paste', 'paste', createPasteCmd(), 30, 8 );

			function addButtonCommand( buttonName, commandName, command, toolbarOrder, ctxMenuOrder ) {
				var lang = editor.lang.clipboard[ commandName ];

				editor.addCommand( commandName, command );
				editor.ui.addButton && editor.ui.addButton( buttonName, {
					label: lang,
					command: commandName,
					toolbar: 'clipboard,' + toolbarOrder
				} );

				// If the "menu" plugin is loaded, register the menu item.
				if ( editor.addMenuItems ) {
					editor.addMenuItem( commandName, {
						label: lang,
						command: commandName,
						group: 'clipboard',
						order: ctxMenuOrder
					} );
				}
			}
		}

		function addListeners() {
			editor.on( 'key', onKey );
			editor.on( 'contentDom', addPasteListenersToEditable );

			// For improved performance, we're checking the readOnly state on selectionChange instead of hooking a key event for that.
			editor.on( 'selectionChange', function( evt ) {
				inReadOnly = evt.data.selection.getRanges()[ 0 ].checkReadOnly();
				setToolbarStates();
			} );

			// If the "contextmenu" plugin is loaded, register the listeners.
			if ( editor.contextMenu ) {
				editor.contextMenu.addListener( function( element, selection ) {
					inReadOnly = selection.getRanges()[ 0 ].checkReadOnly();
					return {
						cut: stateFromNamedCommand( 'cut' ),
						copy: stateFromNamedCommand( 'copy' ),
						paste: stateFromNamedCommand( 'paste' )
					};
				} );
			}
		}

		// Add events listeners to editable.
		function addPasteListenersToEditable() {
			var editable = editor.editable();

			if ( CKEDITOR.plugins.clipboard.isCustomCopyCutSupported ) {
				var initOnCopyCut = function( evt ) {
					clipboard.initPasteDataTransfer( evt, editor );
					evt.data.preventDefault();
				};

				editable.on( 'copy', initOnCopyCut );
				editable.on( 'cut', initOnCopyCut );

				// Delete content with the low priority so one can overwrite cut data.
				editable.on( 'cut', function() {
					editor.extractSelectedHtml();
				}, null, null, 999 );
			}

			// We'll be catching all pasted content in one line, regardless of whether
			// it's introduced by a document command execution (e.g. toolbar buttons) or
			// user paste behaviors (e.g. CTRL+V).
			editable.on( mainPasteEvent, function( evt ) {
				if ( CKEDITOR.env.ie && preventBeforePasteEvent )
					return;

				// If you've just asked yourself why preventPasteEventNow() is not here, but
				// in listener for CTRL+V and exec method of 'paste' command
				// you've asked the same question we did.
				//
				// THE ANSWER:
				//
				// First thing to notice - this answer makes sense only for IE,
				// because other browsers don't listen for 'paste' event.
				//
				// What would happen if we move preventPasteEventNow() here?
				// For:
				// * CTRL+V - IE fires 'beforepaste', so we prevent 'paste' and pasteDataFromClipboard(). OK.
				// * editor.execCommand( 'paste' ) - we fire 'beforepaste', so we prevent
				//		'paste' and pasteDataFromClipboard() and doc.execCommand( 'Paste' ). OK.
				// * native context menu - IE fires 'beforepaste', so we prevent 'paste', but unfortunately
				//		on IE we fail with pasteDataFromClipboard() here, because of... we don't know why, but
				//		we just fail, so... we paste nothing. FAIL.
				// * native menu bar - the same as for native context menu.
				//
				// But don't you know any way to distinguish first two cases from last two?
				// Only one - special flag set in CTRL+V handler and exec method of 'paste'
				// command. And that's what we did using preventPasteEventNow().

				pasteDataFromClipboard( evt );
			} );

			// It's not possible to clearly handle all four paste methods (ctrl+v, native menu bar
			// native context menu, editor's command) in one 'paste/beforepaste' event in IE.
			//
			// For ctrl+v & editor's command it's easy to handle pasting in 'beforepaste' listener,
			// so we do this. For another two methods it's better to use 'paste' event.
			//
			// 'paste' is always being fired after 'beforepaste' (except of weird one on opening native
			// context menu), so for two methods handled in 'beforepaste' we're canceling 'paste'
			// using preventPasteEvent state.
			//
			// 'paste' event in IE is being fired before getClipboardDataByPastebin executes its callback.
			//
			// QUESTION: Why didn't you handle all 4 paste methods in handler for 'paste'?
			//		Wouldn't this just be simpler?
			// ANSWER: Then we would have to evt.data.preventDefault() only for native
			//		context menu and menu bar pastes. The same with execIECommand().
			//		That would force us to mark CTRL+V and editor's paste command with
			//		special flag, other than preventPasteEvent. But we still would have to
			//		have preventPasteEvent for the second event fired by execIECommand.
			//		Code would be longer and not cleaner.
			CKEDITOR.env.ie && editable.on( 'paste', function( evt ) {
				if ( preventPasteEvent )
					return;
				// Cancel next 'paste' event fired by execIECommand( 'paste' )
				// at the end of this callback.
				preventPasteEventNow();

				// Prevent native paste.
				evt.data.preventDefault();

				pasteDataFromClipboard( evt );

				// Force IE to paste content into pastebin so pasteDataFromClipboard will work.
				if ( !execIECommand( 'paste' ) )
					editor.openDialog( 'paste' );
			} );

			// [IE] Dismiss the (wrong) 'beforepaste' event fired on context/toolbar menu open. (#7953)
			if ( CKEDITOR.env.ie ) {
				editable.on( 'contextmenu', preventBeforePasteEventNow, null, null, 0 );

				editable.on( 'beforepaste', function( evt ) {
					// Do not prevent event on CTRL+V and SHIFT+INS because it blocks paste (#11970).
					if ( evt.data && !evt.data.$.ctrlKey && !evt.data.$.shiftKey )
						preventBeforePasteEventNow();
				}, null, null, 0 );

			}

			editable.on( 'beforecut', function() {
				!preventBeforePasteEvent && fixCut( editor );
			} );

			var mouseupTimeout;

			// Use editor.document instead of editable in non-IEs for observing mouseup
			// since editable won't fire the event if selection process started within
			// iframe and ended out of the editor (#9851).
			editable.attachListener( CKEDITOR.env.ie ? editable : editor.document.getDocumentElement(), 'mouseup', function() {
				mouseupTimeout = setTimeout( function() {
					setToolbarStates();
				}, 0 );
			} );

			// Make sure that deferred mouseup callback isn't executed after editor instance
			// had been destroyed. This may happen when editor.destroy() is called in parallel
			// with mouseup event (i.e. a button with onclick callback) (#10219).
			editor.on( 'destroy', function() {
				clearTimeout( mouseupTimeout );
			} );

			editable.on( 'keyup', setToolbarStates );
		}

		// Create object representing Cut or Copy commands.
		function createCutCopyCmd( type ) {
			return {
				type: type,
				canUndo: type == 'cut', // We can't undo copy to clipboard.
				startDisabled: true,
				exec: function() {
					// Attempts to execute the Cut and Copy operations.
					function tryToCutCopy( type ) {
						if ( CKEDITOR.env.ie )
							return execIECommand( type );

						// non-IEs part
						try {
							// Other browsers throw an error if the command is disabled.
							return editor.document.$.execCommand( type, false, null );
						} catch ( e ) {
							return false;
						}
					}

					this.type == 'cut' && fixCut();

					var success = tryToCutCopy( this.type );

					if ( !success ) {
						// Show cutError or copyError.
						editor.showNotification( editor.lang.clipboard[ this.type + 'Error' ] ); // jshint ignore:line
					}

					return success;
				}
			};
		}

		function createPasteCmd() {
			return {
				// Snapshots are done manually by editable.insertXXX methods.
				canUndo: false,
				async: true,

				exec: function( editor, data ) {
					var fire = function( data, withBeforePaste ) {
							data &&	firePasteEvents( editor, data, !!withBeforePaste );

							editor.fire( 'afterCommandExec', {
								name: 'paste',
								command: cmd,
								returnValue: !!data
							} );
						},
						cmd = this;

					// Check data precisely - don't open dialog on empty string.
					if ( typeof data == 'string' )
						fire( {
								dataValue: data,
								method: 'paste',
								dataTransfer: clipboard.initPasteDataTransfer()
							}, 1 );
					else
						editor.getClipboardData( fire );
				}
			};
		}

		function preventPasteEventNow() {
			preventPasteEvent = 1;
			// For safety reason we should wait longer than 0/1ms.
			// We don't know how long execution of quite complex getClipboardData will take
			// and in for example 'paste' listner execCommand() (which fires 'paste') is called
			// after getClipboardData finishes.
			// Luckily, it's impossible to immediately fire another 'paste' event we want to handle,
			// because we only handle there native context menu and menu bar.
			setTimeout( function() {
				preventPasteEvent = 0;
			}, 100 );
		}

		function preventBeforePasteEventNow() {
			preventBeforePasteEvent = 1;
			setTimeout( function() {
				preventBeforePasteEvent = 0;
			}, 10 );
		}

		// Tries to execute any of the paste, cut or copy commands in IE. Returns a
		// boolean indicating that the operation succeeded.
		// @param {String} command *LOWER CASED* name of command ('paste', 'cut', 'copy').
		function execIECommand( command ) {
			var doc = editor.document,
				body = doc.getBody(),
				enabled = false,
				onExec = function() {
					enabled = true;
				};

			// The following seems to be the only reliable way to detect that
			// clipboard commands are enabled in IE. It will fire the
			// onpaste/oncut/oncopy events only if the security settings allowed
			// the command to execute.
			body.on( command, onExec );

			// IE7: document.execCommand has problem to paste into positioned element.
			if ( CKEDITOR.env.version > 7 ) {
				doc.$.execCommand( command );
			} else {
				doc.$.selection.createRange().execCommand( command );
			}

			body.removeListener( command, onExec );

			return enabled;
		}

		// Cutting off control type element in IE standards breaks the selection entirely. (#4881)
		function fixCut() {
			if ( !CKEDITOR.env.ie || CKEDITOR.env.quirks )
				return;

			var sel = editor.getSelection(),
				control, range, dummy;

			if ( ( sel.getType() == CKEDITOR.SELECTION_ELEMENT ) && ( control = sel.getSelectedElement() ) ) {
				range = sel.getRanges()[ 0 ];
				dummy = editor.document.createText( '' );
				dummy.insertBefore( control );
				range.setStartBefore( dummy );
				range.setEndAfter( control );
				sel.selectRanges( [ range ] );

				// Clear up the fix if the paste wasn't succeeded.
				setTimeout( function() {
					// Element still online?
					if ( control.getParent() ) {
						dummy.remove();
						sel.selectElement( control );
					}
				}, 0 );
			}
		}

		// Allow to peek clipboard content by redirecting the
		// pasting content into a temporary bin and grab the content of it.
		function getClipboardDataByPastebin( evt, callback ) {
			var doc = editor.document,
				editable = editor.editable(),
				cancel = function( evt ) {
					evt.cancel();
				},
				blurListener;

			// Avoid recursions on 'paste' event or consequent paste too fast. (#5730)
			if ( doc.getById( 'cke_pastebin' ) )
				return;

			var sel = editor.getSelection();
			var bms = sel.createBookmarks();

			// #11384. On IE9+ we use native selectionchange (i.e. editor#selectionCheck) to cache the most
			// recent selection which we then lock on editable blur. See selection.js for more info.
			// selectionchange fired before getClipboardDataByPastebin() cached selection
			// before creating bookmark (cached selection will be invalid, because bookmarks modified the DOM),
			// so we need to fire selectionchange one more time, to store current seleciton.
			// Selection will be locked when we focus pastebin.
			if ( CKEDITOR.env.ie )
				sel.root.fire( 'selectionchange' );

			// Create container to paste into.
			// For rich content we prefer to use "body" since it holds
			// the least possibility to be splitted by pasted content, while this may
			// breaks the text selection on a frame-less editable, "div" would be
			// the best one in that case.
			// In another case on old IEs moving the selection into a "body" paste bin causes error panic.
			// Body can't be also used for Opera which fills it with <br>
			// what is indistinguishable from pasted <br> (copying <br> in Opera isn't possible,
			// but it can be copied from other browser).
			var pastebin = new CKEDITOR.dom.element(
				( CKEDITOR.env.webkit || editable.is( 'body' ) ) && !CKEDITOR.env.ie ? 'body' : 'div', doc );

			pastebin.setAttributes( {
				id: 'cke_pastebin',
				'data-cke-temp': '1'
			} );

			var containerOffset = 0,
				offsetParent,
				win = doc.getWindow();

			if ( CKEDITOR.env.webkit ) {
				// It's better to paste close to the real paste destination, so inherited styles
				// (which Webkits will try to compensate by styling span) differs less from the destination's one.
				editable.append( pastebin );
				// Style pastebin like .cke_editable, to minimize differences between origin and destination. (#9754)
				pastebin.addClass( 'cke_editable' );

				// Compensate position of offsetParent.
				if ( !editable.is( 'body' ) ) {
					// We're not able to get offsetParent from pastebin (body element), so check whether
					// its parent (editable) is positioned.
					if ( editable.getComputedStyle( 'position' ) != 'static' )
						offsetParent = editable;
					// And if not - safely get offsetParent from editable.
					else
						offsetParent = CKEDITOR.dom.element.get( editable.$.offsetParent );

					containerOffset = offsetParent.getDocumentPosition().y;
				}
			} else {
				// Opera and IE doesn't allow to append to html element.
				editable.getAscendant( CKEDITOR.env.ie ? 'body' : 'html', 1 ).append( pastebin );
			}

			pastebin.setStyles( {
				position: 'absolute',
				// Position the bin at the top (+10 for safety) of viewport to avoid any subsequent document scroll.
				top: ( win.getScrollPosition().y - containerOffset + 10 ) + 'px',
				width: '1px',
				// Caret has to fit in that height, otherwise browsers like Chrome & Opera will scroll window to show it.
				// Set height equal to viewport's height - 20px (safety gaps), minimum 1px.
				height: Math.max( 1, win.getViewPaneSize().height - 20 ) + 'px',
				overflow: 'hidden',
				// Reset styles that can mess up pastebin position.
				margin: 0,
				padding: 0
			} );

			// Paste fails in Safari when the body tag has 'user-select: none'. (#12506)
			if ( CKEDITOR.env.safari )
				pastebin.setStyles( CKEDITOR.tools.cssVendorPrefix( 'user-select', 'text' ) );

			// Check if the paste bin now establishes new editing host.
			var isEditingHost = pastebin.getParent().isReadOnly();

			if ( isEditingHost ) {
				// Hide the paste bin.
				pastebin.setOpacity( 0 );
				// And make it editable.
				pastebin.setAttribute( 'contenteditable', true );
			}
			// Transparency is not enough since positioned non-editing host always shows
			// resize handler, pull it off the screen instead.
			else {
				pastebin.setStyle( editor.config.contentsLangDirection == 'ltr' ? 'left' : 'right', '-1000px' );
			}

			editor.on( 'selectionChange', cancel, null, null, 0 );

			// Webkit fill fire blur on editable when moving selection to
			// pastebin (if body is used). Cancel it because it causes incorrect
			// selection lock in case of inline editor (#10644).
			// The same seems to apply to Firefox (#10787).
			if ( CKEDITOR.env.webkit || CKEDITOR.env.gecko )
				blurListener = editable.once( 'blur', cancel, null, null, -100 );

			// Temporarily move selection to the pastebin.
			isEditingHost && pastebin.focus();
			var range = new CKEDITOR.dom.range( pastebin );
			range.selectNodeContents( pastebin );
			var selPastebin = range.select();

			// If non-native paste is executed, IE will open security alert and blur editable.
			// Editable will then lock selection inside itself and after accepting security alert
			// this selection will be restored. We overwrite stored selection, so it's restored
			// in pastebin. (#9552)
			if ( CKEDITOR.env.ie ) {
				blurListener = editable.once( 'blur', function() {
					editor.lockSelection( selPastebin );
				} );
			}

			var scrollTop = CKEDITOR.document.getWindow().getScrollPosition().y;

			// Wait a while and grab the pasted contents.
			setTimeout( function() {
				// Restore main window's scroll position which could have been changed
				// by browser in cases described in #9771.
				if ( CKEDITOR.env.webkit )
					CKEDITOR.document.getBody().$.scrollTop = scrollTop;

				// Blur will be fired only on non-native paste. In other case manually remove listener.
				blurListener && blurListener.removeListener();

				// Restore properly the document focus. (#8849)
				if ( CKEDITOR.env.ie )
					editable.focus();

				// IE7: selection must go before removing pastebin. (#8691)
				sel.selectBookmarks( bms );
				pastebin.remove();

				// Grab the HTML contents.
				// We need to look for a apple style wrapper on webkit it also adds
				// a div wrapper if you copy/paste the body of the editor.
				// Remove hidden div and restore selection.
				var bogusSpan;
				if ( CKEDITOR.env.webkit && ( bogusSpan = pastebin.getFirst() ) && ( bogusSpan.is && bogusSpan.hasClass( 'Apple-style-span' ) ) )
					pastebin = bogusSpan;

				editor.removeListener( 'selectionChange', cancel );
				callback( pastebin.getHtml() );
			}, 0 );
		}

		// Try to get content directly on IE from clipboard, without native event
		// being fired before. In other words - synthetically get clipboard data
		// if it's possible.
		// mainPasteEvent will be fired, so if forced native paste:
		// * worked, getClipboardDataByPastebin will grab it,
		// * didn't work, dataValue and dataTransfer will be empty and editor#paste won't be fired.
		// On browsers other then IE it is not possible to get data directly so function will
		// return false.
		function getClipboardDataDirectly() {
			// On non-IE it is not possible to get data directly.
			if ( !CKEDITOR.env.ie ) {
				// beforePaste should be fired when dialog open so it can be canceled.
				editor.fire( 'beforePaste', { type: 'auto', method: 'paste' } );
				return false;
			}

			// Prevent IE from pasting at the begining of the document.
			editor.focus();

			// Command will be handled by 'beforepaste', but as
			// execIECommand( 'paste' ) will fire also 'paste' event
			// we're canceling it.
			preventPasteEventNow();

			// #9247: Lock focus to prevent IE from hiding toolbar for inline editor.
			var focusManager = editor.focusManager;
			focusManager.lock();

			if ( editor.editable().fire( mainPasteEvent ) && !execIECommand( 'paste' ) ) {
				focusManager.unlock();
				return false;
			}
			focusManager.unlock();

			return true;
		}

		// Listens for some clipboard related keystrokes, so they get customized.
		// Needs to be bind to keydown event.
		function onKey( event ) {
			if ( editor.mode != 'wysiwyg' )
				return;

			switch ( event.data.keyCode ) {
				// Paste
				case CKEDITOR.CTRL + 86: // CTRL+V
				case CKEDITOR.SHIFT + 45: // SHIFT+INS
					var editable = editor.editable();

					// Cancel 'paste' event because ctrl+v is for IE handled
					// by 'beforepaste'.
					preventPasteEventNow();

					// Simulate 'beforepaste' event for all none-IEs.
					!CKEDITOR.env.ie && editable.fire( 'beforepaste' );

					return;

					// Cut
				case CKEDITOR.CTRL + 88: // CTRL+X
				case CKEDITOR.SHIFT + 46: // SHIFT+DEL
					// Save Undo snapshot.
					editor.fire( 'saveSnapshot' ); // Save before cut
					setTimeout( function() {
						editor.fire( 'saveSnapshot' ); // Save after cut
					}, 50 ); // OSX is slow (#11416).
			}
		}

		function pasteDataFromClipboard( evt ) {
			// Default type is 'auto', but can be changed by beforePaste listeners.
			var eventData = {
					type: 'auto',
					method: 'paste',
					dataTransfer: clipboard.initPasteDataTransfer( evt )
				},
				// True if data transfer contains HTML data.
				htmlInExternalDataTransfer = !CKEDITOR.env.ie && !CKEDITOR.env.safari,
				external = eventData.dataTransfer.getTransferType( editor ) === CKEDITOR.DATA_TRANSFER_EXTERNAL;

			eventData.dataTransfer.cacheData();

			// Fire 'beforePaste' event so clipboard flavor get customized by other plugins.
			// If 'beforePaste' is canceled continue executing getClipboardDataByPastebin and then do nothing
			// (do not fire 'paste', 'afterPaste' events). This way we can grab all - synthetically
			// and natively pasted content and prevent its insertion into editor
			// after canceling 'beforePaste' event.
			var beforePasteNotCanceled = editor.fire( 'beforePaste', eventData ) !== false;

			// Do not use paste bin if the browser let us get HTML or files from dataTranfer.
			if ( beforePasteNotCanceled && ( htmlInExternalDataTransfer || !external ) && !eventData.dataTransfer.isEmpty() ) {
				evt.data.preventDefault();
				setTimeout( function() {
					firePasteEvents( editor, eventData );
				}, 0 );
			} else {
				getClipboardDataByPastebin( evt, function( data ) {
					// Clean up.
					eventData.dataValue = data.replace( /<span[^>]+data-cke-bookmark[^<]*?<\/span>/ig, '' );

					// Fire remaining events (without beforePaste)
					beforePasteNotCanceled && firePasteEvents( editor, eventData );
				} );
			}
		}

		function setToolbarStates() {
			if ( editor.mode != 'wysiwyg' )
				return;

			var pasteState = stateFromNamedCommand( 'paste' );

			editor.getCommand( 'cut' ).setState( stateFromNamedCommand( 'cut' ) );
			editor.getCommand( 'copy' ).setState( stateFromNamedCommand( 'copy' ) );
			editor.getCommand( 'paste' ).setState( pasteState );
			editor.fire( 'pasteState', pasteState );
		}

		function stateFromNamedCommand( command ) {
			if ( inReadOnly && command in { paste: 1, cut: 1 } )
				return CKEDITOR.TRISTATE_DISABLED;

			if ( command == 'paste' )
				return CKEDITOR.TRISTATE_OFF;

			// Cut, copy - check if the selection is not empty.
			var sel = editor.getSelection(),
				ranges = sel.getRanges(),
				selectionIsEmpty = sel.getType() == CKEDITOR.SELECTION_NONE || ( ranges.length == 1 && ranges[ 0 ].collapsed );

			return selectionIsEmpty ? CKEDITOR.TRISTATE_DISABLED : CKEDITOR.TRISTATE_OFF;
		}
	}

	// Returns:
	// * 'htmlifiedtext' if content looks like transformed by browser from plain text.
	//		See clipboard/paste.html TCs for more info.
	// * 'html' if it is not 'htmlifiedtext'.
	function recogniseContentType( data ) {
		if ( CKEDITOR.env.webkit ) {
			// Plain text or ( <div><br></div> and text inside <div> ).
			if ( !data.match( /^[^<]*$/g ) && !data.match( /^(<div><br( ?\/)?><\/div>|<div>[^<]*<\/div>)*$/gi ) )
				return 'html';
		} else if ( CKEDITOR.env.ie ) {
			// Text and <br> or ( text and <br> in <p> - paragraphs can be separated by new \r\n ).
			if ( !data.match( /^([^<]|<br( ?\/)?>)*$/gi ) && !data.match( /^(<p>([^<]|<br( ?\/)?>)*<\/p>|(\r\n))*$/gi ) )
				return 'html';
		} else if ( CKEDITOR.env.gecko ) {
			// Text or <br>.
			if ( !data.match( /^([^<]|<br( ?\/)?>)*$/gi ) )
				return 'html';
		} else {
			return 'html';
		}

		return 'htmlifiedtext';
	}

	// This function transforms what browsers produce when
	// pasting plain text into editable element (see clipboard/paste.html TCs
	// for more info) into correct HTML (similar to that produced by text2Html).
	function htmlifiedTextHtmlification( config, data ) {
		function repeatParagraphs( repeats ) {
			// Repeat blocks floor((n+1)/2) times.
			// Even number of repeats - add <br> at the beginning of last <p>.
			return CKEDITOR.tools.repeat( '</p><p>', ~~( repeats / 2 ) ) + ( repeats % 2 == 1 ? '<br>' : '' );
		}

			// Replace adjacent white-spaces (EOLs too - Fx sometimes keeps them) with one space.
		data = data.replace( /\s+/g, ' ' )
			// Remove spaces from between tags.
			.replace( /> +</g, '><' )
			// Normalize XHTML syntax and upper cased <br> tags.
			.replace( /<br ?\/>/gi, '<br>' );

		// IE - lower cased tags.
		data = data.replace( /<\/?[A-Z]+>/g, function( match ) {
			return match.toLowerCase();
		} );

		// Don't touch single lines (no <br|p|div>) - nothing to do here.
		if ( data.match( /^[^<]$/ ) )
			return data;

		// Webkit.
		if ( CKEDITOR.env.webkit && data.indexOf( '<div>' ) > -1 ) {
				// One line break at the beginning - insert <br>
			data = data.replace( /^(<div>(<br>|)<\/div>)(?!$|(<div>(<br>|)<\/div>))/g, '<br>' )
				// Two or more - reduce number of new lines by one.
				.replace( /^(<div>(<br>|)<\/div>){2}(?!$)/g, '<div></div>' );

			// Two line breaks create one paragraph in Webkit.
			if ( data.match( /<div>(<br>|)<\/div>/ ) ) {
				data = '<p>' + data.replace( /(<div>(<br>|)<\/div>)+/g, function( match ) {
					return repeatParagraphs( match.split( '</div><div>' ).length + 1 );
				} ) + '</p>';
			}

			// One line break create br.
			data = data.replace( /<\/div><div>/g, '<br>' );

			// Remove remaining divs.
			data = data.replace( /<\/?div>/g, '' );
		}

		// Opera and Firefox and enterMode != BR.
		if ( CKEDITOR.env.gecko && config.enterMode != CKEDITOR.ENTER_BR ) {
			// Remove bogus <br> - Fx generates two <brs> for one line break.
			// For two line breaks it still produces two <brs>, but it's better to ignore this case than the first one.
			if ( CKEDITOR.env.gecko )
				data = data.replace( /^<br><br>$/, '<br>' );

			// This line satisfy edge case when for Opera we have two line breaks
			//data = data.replace( /)

			if ( data.indexOf( '<br><br>' ) > -1 ) {
				// Two line breaks create one paragraph, three - 2, four - 3, etc.
				data = '<p>' + data.replace( /(<br>){2,}/g, function( match ) {
					return repeatParagraphs( match.length / 4 );
				} ) + '</p>';
			}
		}

		return switchEnterMode( config, data );
	}

	function filtersFactoryFactory() {
		var filters = {};

		function setUpTags() {
			var tags = {};

			for ( var tag in CKEDITOR.dtd ) {
				if ( tag.charAt( 0 ) != '$' && tag != 'div' && tag != 'span' ) {
					tags[ tag ] = 1;
				}
			}

			return tags;
		}

		function createSemanticContentFilter() {
			var filter = new CKEDITOR.filter();

			filter.allow( {
				$1: {
					elements: setUpTags(),
					attributes: true,
					styles: false,
					classes: false
				}
			} );

			return filter;
		}

		return {
			get: function( type ) {
				if ( type == 'plain-text' ) {
					// Does this look confusing to you? Did we forget about enter mode?
					// It is a trick that let's us creating one filter for edidtor, regardless of its
					// activeEnterMode (which as the name indicates can change during runtime).
					//
					// How does it work?
					// The active enter mode is passed to the filter.applyTo method.
					// The filter first marks all elements except <br> as disallowed and then tries to remove
					// them. However, it cannot remove e.g. a <p> element completely, because it's a basic structural element,
					// so it tries to replace it with an element created based on the active enter mode, eventually doing nothing.
					//
					// Now you can sleep well.
					return filters.plainText || ( filters.plainText = new CKEDITOR.filter( 'br' ) );
				} else if ( type == 'semantic-content' ) {
					return filters.semanticContent || ( filters.semanticContent = createSemanticContentFilter() );
				} else if ( type ) {
					// Create filter based on rules (string or object).
					return new CKEDITOR.filter( type );
				}

				return null;
			}
		};
	}

	function filterContent( editor, data, filter ) {
		var fragment = CKEDITOR.htmlParser.fragment.fromHtml( data ),
			writer = new CKEDITOR.htmlParser.basicWriter();

		filter.applyTo( fragment, true, false, editor.activeEnterMode );
		fragment.writeHtml( writer );

		return writer.getHtml();
	}

	function switchEnterMode( config, data ) {
		if ( config.enterMode == CKEDITOR.ENTER_BR ) {
			data = data.replace( /(<\/p><p>)+/g, function( match ) {
				return CKEDITOR.tools.repeat( '<br>', match.length / 7 * 2 );
			} ).replace( /<\/?p>/g, '' );
		} else if ( config.enterMode == CKEDITOR.ENTER_DIV ) {
			data = data.replace( /<(\/)?p>/g, '<$1div>' );
		}

		return data;
	}

	function initDragDrop( editor ) {
		var clipboard = CKEDITOR.plugins.clipboard;

		editor.on( 'contentDom', function() {
			var editable = editor.editable(),
				dropTarget = CKEDITOR.plugins.clipboard.getDropTarget( editor ),
				top = editor.ui.space( 'top' ),
				bottom = editor.ui.space( 'bottom' );

			// -------------- DRAGOVER TOP & BOTTOM --------------

			function preventDefaultSetDropEffectToNone( evt ) {
				evt.data.preventDefault();
				evt.data.$.dataTransfer.dropEffect = 'none';
			}

			// Not allowing dragging on toolbar and bottom (#12613).
			top && top.on( 'dragover', preventDefaultSetDropEffectToNone );
			bottom && bottom.on( 'dragover', preventDefaultSetDropEffectToNone );

			// -------------- DRAGSTART --------------
			// Listed on dragstart to mark internal and cross-editor drag & drop
			// and save range and selected HTML.

			editable.attachListener( dropTarget, 'dragstart', fireDragEvent );

			// Make sure to reset data transfer (in case dragend was not called or was canceled).
			editable.attachListener( editor, 'dragstart', clipboard.resetDragDataTransfer, clipboard, null, 1 );

			// Create a dataTransfer object and save it globally.
			editable.attachListener( editor, 'dragstart', function( evt ) {
				clipboard.initDragDataTransfer( evt, editor );

				// Save drag range globally for cross editor D&D.
				if ( clipboard.dragRange = editor.getSelection().getRanges()[ 0 ] ) {
					// Store number of children, so we can later tell if any text node was split on drop. (#13011, #13447)
					if ( CKEDITOR.env.ie && CKEDITOR.env.version < 10 ) {
						clipboard.dragStartContainerChildCount = getContainerChildCount( clipboard.dragRange.startContainer );
						clipboard.dragEndContainerChildCount = getContainerChildCount( clipboard.dragRange.endContainer );
					}
				}
			}, null, null, 2 );

			// -------------- DRAGEND --------------
			// Clean up on dragend.

			editable.attachListener( dropTarget, 'dragend', fireDragEvent );

			// Init data transfer if someone wants to use it in dragend.
			editable.attachListener( editor, 'dragend', clipboard.initDragDataTransfer, clipboard, null, 1 );

			// When drag & drop is done we need to reset dataTransfer so the future
			// external drop will be not recognize as internal.
			editable.attachListener( editor, 'dragend', clipboard.resetDragDataTransfer, clipboard, null, 100 );

			// -------------- DRAGOVER --------------
			// We need to call preventDefault on dragover because otherwise if
			// we drop image it will overwrite document.

			editable.attachListener( dropTarget, 'dragover', function( evt ) {
				var target = evt.data.getTarget();

				// Prevent reloading page when dragging image on empty document (#12619).
				if ( target && target.is && target.is( 'html' ) ) {
					evt.data.preventDefault();
					return;
				}

				// If we do not prevent default dragover on IE the file path
				// will be loaded and we will lose content. On the other hand
				// if we prevent it the cursor will not we shown, so we prevent
				// dragover only on IE, on versions which support file API and only
				// if the event contains files.
				if ( CKEDITOR.env.ie &&
					CKEDITOR.plugins.clipboard.isFileApiSupported &&
					evt.data.$.dataTransfer.types.contains( 'Files' ) ) {
					evt.data.preventDefault();
				}
			} );

			// -------------- DROP --------------

			editable.attachListener( dropTarget, 'drop', function( evt ) {
				// Cancel native drop.
				evt.data.preventDefault();

				var target = evt.data.getTarget(),
					readOnly = target.isReadOnly();

				// Do nothing if drop on non editable element (#13015).
				// The <html> tag isn't editable (body is), but we want to allow drop on it
				// (so it is possible to drop below editor contents).
				if ( readOnly && !( target.type == CKEDITOR.NODE_ELEMENT && target.is( 'html' ) ) ) {
					return;
				}

				// Getting drop position is one of the most complex parts.
				var dropRange = clipboard.getRangeAtDropPosition( evt, editor ),
					dragRange = clipboard.dragRange;

				// Do nothing if it was not possible to get drop range.
				if ( !dropRange ) {
					return;
				}

				// Fire drop.
				fireDragEvent( evt, dragRange, dropRange  );
			} );

			// Create dataTransfer or get it, if it was created before.
			editable.attachListener( editor, 'drop', clipboard.initDragDataTransfer, clipboard, null, 1 );

			// Execute drop action, fire paste.
			editable.attachListener( editor, 'drop', function( evt ) {
				var data = evt.data;

				if ( !data ) {
					return;
				}

				// Let user modify drag and drop range.
				var dropRange = data.dropRange,
					dragRange = data.dragRange,
					dataTransfer = data.dataTransfer;

				if ( dataTransfer.getTransferType( editor ) == CKEDITOR.DATA_TRANSFER_INTERNAL ) {
					// Execute drop with a timeout because otherwise selection, after drop,
					// on IE is in the drag position, instead of drop position.
					setTimeout( function() {
						clipboard.internalDrop( dragRange, dropRange, dataTransfer, editor );
					}, 0 );
				} else if ( dataTransfer.getTransferType( editor ) == CKEDITOR.DATA_TRANSFER_CROSS_EDITORS ) {
					crossEditorDrop( dragRange, dropRange, dataTransfer );
				} else {
					externalDrop( dropRange, dataTransfer );
				}
			}, null, null, 9999 );

			// Cross editor drag and drop (drag in one Editor and drop in the other).
			function crossEditorDrop( dragRange, dropRange, dataTransfer ) {
				// Paste event should be fired before delete contents because otherwise
				// Chrome have a problem with drop range (Chrome split the drop
				// range container so the offset is bigger then container length).
				dropRange.select();
				firePasteEvents( editor, { dataTransfer: dataTransfer, method: 'drop' }, 1 );

				// Remove dragged content and make a snapshot.
				dataTransfer.sourceEditor.fire( 'saveSnapshot' );

				dataTransfer.sourceEditor.editable().extractHtmlFromRange( dragRange );

				// Make some selection before saving snapshot, otherwise error will be thrown, because
				// there will be no valid selection after content is removed.
				dataTransfer.sourceEditor.getSelection().selectRanges( [ dragRange ] );
				dataTransfer.sourceEditor.fire( 'saveSnapshot' );
			}

			// Drop from external source.
			function externalDrop( dropRange, dataTransfer ) {
				// Paste content into the drop position.
				dropRange.select();

				firePasteEvents( editor, { dataTransfer: dataTransfer, method: 'drop' }, 1 );

				// Usually we reset DataTranfer on dragend,
				// but dragend is called on the same element as dragstart
				// so it will not be called on on external drop.
				clipboard.resetDragDataTransfer();
			}

			// Fire drag/drop events (dragstart, dragend, drop).
			function fireDragEvent( evt, dragRange, dropRange ) {
				var eventData = {
						$: evt.data.$,
						target: evt.data.getTarget()
					};

				if ( dragRange ) {
					eventData.dragRange = dragRange;
				}
				if ( dropRange ) {
					eventData.dropRange = dropRange;
				}

				if ( editor.fire( evt.name, eventData ) === false ) {
					evt.data.preventDefault();
				}
			}

			function getContainerChildCount( container ) {
				if ( container.type != CKEDITOR.NODE_ELEMENT ) {
					container = container.getParent();
				}

				return container.getChildCount();
			}
		} );
	}

	/**
	 * @singleton
	 * @class CKEDITOR.plugins.clipboard
	 */
	CKEDITOR.plugins.clipboard = {
		/**
		 * True if the environment allows to set data on copy or cut manually. This value is false in IE, because this browser
		 * shows the security dialog window when the script tries to set clipboard data and on iOS, because custom data is
		 * not saved to clipboard there.
		 *
		 * @since 4.5
		 * @readonly
		 * @property {Boolean}
		 */
		isCustomCopyCutSupported: !CKEDITOR.env.ie && !CKEDITOR.env.iOS,

		/**
		 * True if the environment supports MIME types and custom data types in dataTransfer/cliboardData getData/setData methods.
		 *
		 * @since 4.5
		 * @readonly
		 * @property {Boolean}
		 */
		isCustomDataTypesSupported: !CKEDITOR.env.ie,

		/**
		 * True if the environment supports File API.
		 *
		 * @since 4.5
		 * @readonly
		 * @property {Boolean}
		 */
		isFileApiSupported: !CKEDITOR.env.ie || CKEDITOR.env.version > 9,

		/**
		 * Returns the element that should be used as the target for the drop event.
		 *
		 * @since 4.5
		 * @param {CKEDITOR.editor} editor The editor instance.
		 * @returns {CKEDITOR.dom.domObject} the element that should be used as the target for the drop event.
		 */
		getDropTarget: function( editor ) {
			var editable = editor.editable();

			// #11123 Firefox needs to listen on document, because otherwise event won't be fired.
			// #11086 IE8 cannot listen on document.
			if ( ( CKEDITOR.env.ie && CKEDITOR.env.version < 9 ) || editable.isInline() ) {
				return editable;
			} else {
				return editor.document;
			}
		},

		/**
		 * IE 8 & 9 split text node on drop so the first node contains the
		 * text before the drop position and the second contains the rest. If you
		 * drag the content from the same node you will be not be able to get
		 * it (the range becomes invalid), so you need to join them back.
		 *
		 * Note that the first node in IE 8 & 9 is the original node object
		 * but with shortened content.
		 *
		 *		Before:
		 *		  --- Text Node A ----------------------------------
		 *		                                             /\
		 *		                                        Drag position
		 *
		 *		After (IE 8 & 9):
		 *		  --- Text Node A -----  --- Text Node B -----------
		 *		                       /\                    /\
		 *		                  Drop position        Drag position
		 *		                                         (invalid)
		 *
		 *		After (other browsers):
		 *		  --- Text Node A ----------------------------------
		 *		                       /\                    /\
		 *		                  Drop position        Drag position
		 *
		 * **Note:** This function is in the public scope for tests usage only.
		 *
		 * @since 4.5
		 * @private
		 * @param {CKEDITOR.dom.range} dragRange The drag range.
		 * @param {CKEDITOR.dom.range} dropRange The drop range.
		 * @param {Number} preDragStartContainerChildCount The number of children of the drag range start container before the drop.
		 * @param {Number} preDragEndContainerChildCount The number of children of the drag range end container before the drop.
		 */
		fixSplitNodesAfterDrop: function( dragRange, dropRange, preDragStartContainerChildCount, preDragEndContainerChildCount ) {
			var dropContainer = dropRange.startContainer;

			if (
				typeof preDragEndContainerChildCount != 'number' ||
				typeof preDragStartContainerChildCount != 'number'
			) {
				return;
			}

			// We are only concerned about ranges anchored in elements.
			if ( dropContainer.type != CKEDITOR.NODE_ELEMENT ) {
				return;
			}

			if ( handleContainer( dragRange.startContainer, dropContainer, preDragStartContainerChildCount ) ) {
				return;
			}

			if ( handleContainer( dragRange.endContainer, dropContainer, preDragEndContainerChildCount ) ) {
				return;
			}

			function handleContainer( dragContainer, dropContainer, preChildCount ) {
				var dragElement = dragContainer;
				if ( dragElement.type == CKEDITOR.NODE_TEXT ) {
					dragElement = dragContainer.getParent();
				}

				if ( dragElement.equals( dropContainer ) && preChildCount != dropContainer.getChildCount() ) {
					applyFix( dropRange );
					return true;
				}
			}

			function applyFix( dropRange ) {
				var nodeBefore = dropRange.startContainer.getChild( dropRange.startOffset - 1 ),
					nodeAfter = dropRange.startContainer.getChild( dropRange.startOffset );

				if (
					nodeBefore && nodeBefore.type == CKEDITOR.NODE_TEXT &&
					nodeAfter && nodeAfter.type == CKEDITOR.NODE_TEXT
				) {
					var offset = nodeBefore.getLength();

					nodeBefore.setText( nodeBefore.getText() + nodeAfter.getText() );
					nodeAfter.remove();

					dropRange.setStart( nodeBefore, offset );
					dropRange.collapse( true );
				}
			}
		},

		/**
		 * Checks whether turning the drag range into bookmarks will invalidate the drop range.
		 * This usually happens when the drop range shares the container with the drag range and is
		 * located after the drag range, but there are countless edge cases.
		 *
		 * This function is stricly related to {@link #internalDrop} which toggles
		 * order in which it creates bookmarks for both ranges based on a value returned
		 * by this method. In some cases this method returns a value which is not necessarily
		 * true in terms of what it was meant to check, but it is convenient, because
		 * we know how it is interpreted in {@link #internalDrop}, so the correct
		 * behavior of the entire algorithm is assured.
		 *
		 * **Note:** This function is in the public scope for tests usage only.
		 *
		 * @since 4.5
		 * @private
		 * @param {CKEDITOR.dom.range} dragRange The first range to compare.
		 * @param {CKEDITOR.dom.range} dropRange The second range to compare.
		 * @returns {Boolean} `true` if the first range is before the second range.
		 */
		isDropRangeAffectedByDragRange: function( dragRange, dropRange ) {
			var dropContainer = dropRange.startContainer,
				dropOffset = dropRange.endOffset;

			// Both containers are the same and drop offset is at the same position or later.
			// " A L] A " " M A "
			//       ^ ^
			if ( dragRange.endContainer.equals( dropContainer ) && dragRange.endOffset <= dropOffset ) {
				return true;
			}

			// Bookmark for drag start container will mess up with offsets.
			// " O [L A " " M A "
			//           ^       ^
			if (
				dragRange.startContainer.getParent().equals( dropContainer ) &&
				dragRange.startContainer.getIndex() < dropOffset
			) {
				return true;
			}

			// Bookmark for drag end container will mess up with offsets.
			// " O] L A " " M A "
			//           ^       ^
			if (
				dragRange.endContainer.getParent().equals( dropContainer ) &&
				dragRange.endContainer.getIndex() < dropOffset
			) {
				return true;
			}

			return false;
		},

		/**
		 * Internal drag and drop (drag and drop in the same editor instance).
		 *
		 * **Note:** This function is in the public scope for tests usage only.
		 *
		 * @since 4.5
		 * @private
		 * @param {CKEDITOR.dom.range} dragRange The first range to compare.
		 * @param {CKEDITOR.dom.range} dropRange The second range to compare.
		 * @param {CKEDITOR.plugins.clipboard.dataTransfer} dataTransfer
		 * @param {CKEDITOR.editor} editor
		 */
		internalDrop: function( dragRange, dropRange, dataTransfer, editor ) {
			var clipboard = CKEDITOR.plugins.clipboard,
				editable = editor.editable(),
				dragBookmark, dropBookmark, isDropRangeAffected;

			// Save and lock snapshot so there will be only
			// one snapshot for both remove and insert content.
			editor.fire( 'saveSnapshot' );
			editor.fire( 'lockSnapshot', { dontUpdate: 1 } );

			if ( CKEDITOR.env.ie && CKEDITOR.env.version < 10 ) {
				this.fixSplitNodesAfterDrop(
					dragRange,
					dropRange,
					clipboard.dragStartContainerChildCount,
					clipboard.dragEndContainerChildCount
				);
			}

			// Because we manipulate multiple ranges we need to do it carefully,
			// changing one range (event creating a bookmark) may make other invalid.
			// We need to change ranges into bookmarks so we can manipulate them easily in the future.
			// We can change the range which is later in the text before we change the preceding range.
			// We call isDropRangeAffectedByDragRange to test the order of ranges.
			isDropRangeAffected = this.isDropRangeAffectedByDragRange( dragRange, dropRange );
			if ( !isDropRangeAffected ) {
				dragBookmark = dragRange.createBookmark( 1 );
			}
			dropBookmark = dropRange.clone().createBookmark( 1 );
			if ( isDropRangeAffected ) {
				dragBookmark = dragRange.createBookmark( 1 );
			}

			// No we can safely delete content for the drag range...
			dragRange = editor.createRange();
			dragRange.moveToBookmark( dragBookmark );
			editable.extractHtmlFromRange( dragRange, 1 );

			// ...and paste content into the drop position.
			dropRange = editor.createRange();
			dropRange.moveToBookmark( dropBookmark );

			// We do not select drop range, because of may be in the place we can not set the selection
			// (e.g. between blocks, in case of block widget D&D). We put range to the paste event instead.
			firePasteEvents( editor, { dataTransfer: dataTransfer, method: 'drop', range: dropRange }, 1 );

			editor.fire( 'unlockSnapshot' );
		},

		/**
		 * Gets the range from the `drop` event.
		 *
		 * @since 4.5
		 * @param {Object} domEvent A native DOM drop event object.
		 * @param {CKEDITOR.editor} editor The source editor instance.
		 * @returns {CKEDITOR.dom.range} range at drop position.
		 */
		getRangeAtDropPosition: function( dropEvt, editor ) {
			var $evt = dropEvt.data.$,
				x = $evt.clientX,
				y = $evt.clientY,
				$range,
				defaultRange = editor.getSelection( true ).getRanges()[ 0 ],
				range = editor.createRange();

			// Make testing possible.
			if ( dropEvt.data.testRange )
				return dropEvt.data.testRange;

			// Webkits.
			if ( document.caretRangeFromPoint ) {
				$range = editor.document.$.caretRangeFromPoint( x, y );
				range.setStart( CKEDITOR.dom.node( $range.startContainer ), $range.startOffset );
				range.collapse( true );
			}
			// FF.
			else if ( $evt.rangeParent ) {
				range.setStart( CKEDITOR.dom.node( $evt.rangeParent ), $evt.rangeOffset );
				range.collapse( true );
			}
			// IEs 9+.
			// We check if editable is focused to make sure that it's an internal DnD. External DnD must use the second
			// mechanism because of http://dev.ckeditor.com/ticket/13472#comment:6.
			else if ( CKEDITOR.env.ie && CKEDITOR.env.version > 8 && defaultRange && editor.editable().hasFocus ) {
				// On IE 9+ range by default is where we expected it.
				// defaultRange may be undefined if dragover was canceled (file drop).
				return defaultRange;
			}
			// IE 8 and all IEs if !defaultRange or external DnD.
			else if ( document.body.createTextRange ) {
				// To use this method we need a focus (which may be somewhere else in case of external drop).
				editor.focus();

				$range = editor.document.getBody().$.createTextRange();
				try {
					var sucess = false;

					// If user drop between text line IEs moveToPoint throws exception:
					//
					//		Lorem ipsum pulvinar purus et euismod
					//
					//		dolor sit amet,| consectetur adipiscing
					//		               *
					//		vestibulum tincidunt augue eget tempus.
					//
					// * - drop position
					// | - expected cursor position
					//
					// So we try to call moveToPoint with +-1px up to +-20px above or
					// below original drop position to find nearest good drop position.
					for ( var i = 0; i < 20 && !sucess; i++ ) {
						if ( !sucess ) {
							try {
								$range.moveToPoint( x, y - i );
								sucess = true;
							} catch ( err ) {
							}
						}
						if ( !sucess ) {
							try {
								$range.moveToPoint( x, y + i );
								sucess = true;
							} catch ( err ) {
							}
						}
					}

					if ( sucess ) {
						var id = 'cke-temp-' + ( new Date() ).getTime();
						$range.pasteHTML( '<span id="' + id + '">\u200b</span>' );

						var span = editor.document.getById( id );
						range.moveToPosition( span, CKEDITOR.POSITION_BEFORE_START );
						span.remove();
					} else {
						// If the fist method does not succeed we might be next to
						// the short element (like header):
						//
						//		Lorem ipsum pulvinar purus et euismod.
						//
						//
						//		SOME HEADER|        *
						//
						//
						//		vestibulum tincidunt augue eget tempus.
						//
						// * - drop position
						// | - expected cursor position
						//
						// In such situation elementFromPoint returns proper element. Using getClientRect
						// it is possible to check if the cursor should be at the beginning or at the end
						// of paragraph.
						var $element = editor.document.$.elementFromPoint( x, y ),
							element = new CKEDITOR.dom.element( $element ),
							rect;

						if ( !element.equals( editor.editable() ) && element.getName() != 'html' ) {
							rect = element.getClientRect();

							if ( x < rect.left ) {
								range.setStartAt( element, CKEDITOR.POSITION_AFTER_START );
								range.collapse( true );
							} else {
								range.setStartAt( element, CKEDITOR.POSITION_BEFORE_END );
								range.collapse( true );
							}
						}
						// If drop happens on no element elementFromPoint returns html or body.
						//
						//		*      |Lorem ipsum pulvinar purus et euismod.
						//
						//		       vestibulum tincidunt augue eget tempus.
						//
						// * - drop position
						// | - expected cursor position
						//
						// In such case we can try to use default selection. If startContainer is not
						// 'editable' element it is probably proper selection.
						else if ( defaultRange && defaultRange.startContainer &&
							!defaultRange.startContainer.equals( editor.editable() ) ) {
							return defaultRange;

						// Otherwise we can not find any drop position and we have to return null
						// and cancel drop event.
						} else {
							return null;
						}

					}
				} catch ( err ) {
					return null;
				}
			} else {
				return null;
			}

			return range;
		},

		/**
		 * This function tries to link the `evt.data.dataTransfer` property of the {@link CKEDITOR.editor#dragstart},
		 * {@link CKEDITOR.editor#dragend} and {@link CKEDITOR.editor#drop} events to a single
		 * {@link CKEDITOR.plugins.clipboard.dataTransfer} object.
		 *
		 * This method is automatically used by the core of the drag and drop functionality and
		 * usually does not have to be called manually when using the drag and drop events.
		 *
		 * This method behaves differently depending on whether the drag and drop events were fired
		 * artificially (to represent a non-native drag and drop) or whether they were caused by the native drag and drop.
		 *
		 * If the native event is not available, then it will create a new {@link CKEDITOR.plugins.clipboard.dataTransfer}
		 * instance (if it does not exist already) and will link it to this and all following event objects until
		 * the {@link #resetDragDataTransfer} method is called. It means that all three drag and drop events must be fired
		 * in order to ensure that the data transfer is bound correctly.
		 *
		 * If the native event is available, then the {@link CKEDITOR.plugins.clipboard.dataTransfer} is identified
		 * by its ID and a new instance is assigned to the `evt.data.dataTransfer` only if the ID changed or
		 * the {@link #resetDragDataTransfer} method was called.
		 *
		 * @since 4.5
		 * @param {CKEDITOR.dom.event} [evt] A drop event object.
		 * @param {CKEDITOR.editor} [sourceEditor] The source editor instance.
		 */
		initDragDataTransfer: function( evt, sourceEditor ) {
			// Create a new dataTransfer object based on the drop event.
			// If this event was used on dragstart to create dataTransfer
			// both dataTransfer objects will have the same id.
			var nativeDataTransfer = evt.data.$ ? evt.data.$.dataTransfer : null,
				dataTransfer = new this.dataTransfer( nativeDataTransfer, sourceEditor );

			if ( !nativeDataTransfer ) {
				// No native event.
				if ( this.dragData ) {
					dataTransfer = this.dragData;
				} else {
					this.dragData = dataTransfer;
				}
			} else {
				// Native event. If there is the same id we will replace dataTransfer with the one
				// created on drag, because it contains drag editor, drag content and so on.
				// Otherwise (in case of drag from external source) we save new object to
				// the global clipboard.dragData.
				if ( this.dragData && dataTransfer.id == this.dragData.id ) {
					dataTransfer = this.dragData;
				} else {
					this.dragData = dataTransfer;
				}
			}

			evt.data.dataTransfer = dataTransfer;
		},

		/**
		 * Removes the global {@link #dragData} so the next call to {@link #initDragDataTransfer}
		 * always creates a new instance of {@link CKEDITOR.plugins.clipboard.dataTransfer}.
		 *
		 * @since 4.5
		 */
		resetDragDataTransfer: function() {
			this.dragData = null;
		},

		/**
		 * Global object storing the data transfer of the current drag and drop operation.
		 * Do not use it directly, use {@link #initDragDataTransfer} and {@link #resetDragDataTransfer}.
		 *
		 * Note: This object is global (meaning that it is not related to a single editor instance)
		 * in order to handle drag and drop from one editor into another.
		 *
		 * @since 4.5
		 * @private
		 * @property {CKEDITOR.plugins.clipboard.dataTransfer} dragData
		 */

		/**
		 * Range object to save the drag range and remove its content after the drop.
		 *
		 * @since 4.5
		 * @private
		 * @property {CKEDITOR.dom.range} dragRange
		 */

		/**
		 * Initializes and links data transfer objects based on the paste event. If the data
		 * transfer object was already initialized on this event, the function will
		 * return that object. In IE it is not possible to link copy/cut and paste events
		 * so the method always returns a new object. The same happens if there is no paste event
		 * passed to the method.
		 *
		 * @since 4.5
		 * @param {CKEDITOR.dom.event} [evt] A paste event object.
		 * @param {CKEDITOR.editor} [sourceEditor] The source editor instance.
		 * @returns {CKEDITOR.plugins.clipboard.dataTransfer} The data transfer object.
		 */
		initPasteDataTransfer: function( evt, sourceEditor ) {
			if ( !this.isCustomCopyCutSupported ) {
				return new this.dataTransfer( null, sourceEditor );
			} else if ( evt && evt.data && evt.data.$ ) {
				var dataTransfer = new this.dataTransfer( evt.data.$.clipboardData, sourceEditor );

				if ( this.copyCutData && dataTransfer.id == this.copyCutData.id ) {
					dataTransfer = this.copyCutData;
					dataTransfer.$ = evt.data.$.clipboardData;
				} else {
					this.copyCutData = dataTransfer;
				}

				return dataTransfer;
			} else {
				return new this.dataTransfer( null, sourceEditor );
			}
		}
	};

	// Data type used to link drag and drop events.
	//
	// In IE URL data type is buggie and there is no way to mark drag & drop  without
	// modifying text data (which would be displayed if user drop content to the textarea)
	// so we just read dragged text.
	//
	// In Chrome and Firefox we can use custom data types.
	var clipboardIdDataType = CKEDITOR.plugins.clipboard.isCustomDataTypesSupported ? 'cke/id' : 'Text';
	/**
	 * Facade for the native `dataTransfer`/`clipboadData` object to hide all differences
	 * between browsers.
	 *
	 * @since 4.5
	 * @class CKEDITOR.plugins.clipboard.dataTransfer
	 * @constructor Creates a class instance.
	 * @param {Object} [nativeDataTransfer] A native data transfer object.
	 * @param {CKEDITOR.editor} [editor] The source editor instance. If the editor is defined, dataValue will
	 * be created based on the editor content and the type will be 'html'.
	 */
	CKEDITOR.plugins.clipboard.dataTransfer = function( nativeDataTransfer, editor ) {
		if ( nativeDataTransfer ) {
			this.$ = nativeDataTransfer;
		}

		this._ = {
			chromeLinuxRegExp: /^<meta.*?>/,
			chromeWindowsRegExp: /<!--StartFragment-->([\s\S]*)<!--EndFragment-->/,

			data: {},
			files: [],

			normalizeType: function( type ) {
				type = type.toLowerCase();

				if ( type == 'text' || type == 'text/plain' ) {
					return 'Text'; // IE support only Text and URL;
				} else if ( type == 'url' ) {
					return 'URL'; // IE support only Text and URL;
				} else {
					return type;
				}
			}
		};

		// Check if ID is already created.
		this.id = this.getData( clipboardIdDataType );

		// If there is no ID we need to create it. Different browsers needs different ID.
		if ( !this.id ) {
			if ( clipboardIdDataType == 'Text' ) {
				// For IE10+ only Text data type is supported and we have to compare dragged
				// and dropped text. If the ID is not set it means that empty string was dragged
				// (ex. image with no alt). We change null to empty string.
				this.id = '';
			} else {
				// String for custom data type.
				this.id = 'cke-' + CKEDITOR.tools.getUniqueId();
			}
		}

		// In IE10+ we can not use any data type besides text, so we do not call setData.
		if ( clipboardIdDataType != 'Text' ) {
			// Try to set ID so it will be passed from the drag to the drop event.
			// On some browsers with some event it is not possible to setData so we
			// need to catch exceptions.
			try {
				this.$.setData( clipboardIdDataType, this.id );
			} catch ( err ) {}
		}

		if ( editor ) {
			this.sourceEditor = editor;

			this.setData( 'text/html', editor.getSelectedHtml( 1 ) );

			// Without setData( 'text', ... ) on dragstart there is no drop event in Safari.
			// Also 'text' data is empty as drop to the textarea does not work if we do not put there text.
			if ( clipboardIdDataType != 'Text' && !this.getData( 'text/plain' ) ) {
				this.setData( 'text/plain', editor.getSelection().getSelectedText() );
			}
		}

		/**
		 * Data transfer ID used to bind all dataTransfer
		 * objects based on the same event (e.g. in drag and drop events).
		 *
		 * @readonly
		 * @property {String} id
		 */

		/**
		 * A native DOM event object.
		 *
		 * @readonly
		 * @property {Object} $
		 */

		/**
		 * Source editor &mdash; the editor where the drag starts.
		 * Might be undefined if the drag starts outside the editor (e.g. when dropping files to the editor).
		 *
		 * @readonly
		 * @property {CKEDITOR.editor} sourceEditor
		 */

		/**
		 * Private properties and methods.
		 *
		 * @private
		 * @property {Object} _
		 */
	};

	/**
	 * Data transfer operation (drag and drop or copy and paste) started and ended in the same
	 * editor instance.
	 *
	 * @since 4.5
	 * @readonly
	 * @property {Number} [=1]
	 * @member CKEDITOR
	 */
	CKEDITOR.DATA_TRANSFER_INTERNAL = 1;

	/**
	 * Data transfer operation (drag and drop or copy and paste) started in one editor
	 * instance and ended in another.
	 *
	 * @since 4.5
	 * @readonly
	 * @property {Number} [=2]
	 * @member CKEDITOR
	 */
	CKEDITOR.DATA_TRANSFER_CROSS_EDITORS = 2;

	/**
	 * Data transfer operation (drag and drop or copy and paste) started outside of the editor.
	 * The source of the data may be a textarea, HTML, another application, etc.
	 *
	 * @since 4.5
	 * @readonly
	 * @property {Number} [=3]
	 * @member CKEDITOR
	 */
	CKEDITOR.DATA_TRANSFER_EXTERNAL = 3;

	CKEDITOR.plugins.clipboard.dataTransfer.prototype = {
		/**
		 * Facade for the native `getData` method.
		 *
		 * @param {String} type The type of data to retrieve.
		 * @returns {String} type Stored data for the given type or an empty string if the data for that type does not exist.
		 */
		getData: function( type ) {
			function isEmpty( data ) {
				return data === undefined || data === null || data === '';
			}

			type = this._.normalizeType( type );

			var data = this._.data[ type ],
				result;

			if ( isEmpty( data ) ) {
				try {
					data = this.$.getData( type );
				} catch ( e ) {}
			}

			if ( isEmpty( data ) ) {
				data = '';
			}

//			if ( type == 'text/html' || type == 'Text' ) {
//				data = data.replace('abc', 'def');
//				console.log(data);				
//			}
			
			// Chrome add <meta http-equiv="content-type" content="text/html; charset=utf-8">
			// at the begging of the HTML data on Linux and surround by <html><body><!--StartFragment-->
			// and <!--EndFragment--></body></html> on Windows. This code remove these tags.
			if ( type == 'text/html' && CKEDITOR.env.chrome ) {
				data = data.replace( this._.chromeLinuxRegExp, '' );

				result = this._.chromeWindowsRegExp.exec( data );
				if ( result && result.length > 1 ) {
					data = result[ 1 ];
				}
			}
			// Firefox on Linux put files paths as a text/plain data if there are files
			// in the dataTransfer object. We need to hide it, because files should be
			// handled on paste only if dataValue is empty.
			else if ( type == 'Text' && CKEDITOR.env.gecko && this.getFilesCount() &&
				data.substring( 0, 7 ) == 'file://' ) {
				data = '';
			}

			return data;
		},

		/**
		 * Facade for the native `setData` method.
		 *
		 * @param {String} type The type of data to retrieve.
		 * @param {String} value The data to add.
		 */
		setData: function( type, value ) {
			type = this._.normalizeType( type );

			this._.data[ type ] = value;

			// There is "Unexpected call to method or property access." error if you try
			// to set data of unsupported type on IE.
			if ( !CKEDITOR.plugins.clipboard.isCustomDataTypesSupported && type != 'URL' && type != 'Text' ) {
				return;
			}

			try {
				this.$.setData( type, value );
			} catch ( e ) {}
		},

		/**
		 * Gets the data transfer type.
		 *
		 * @param {CKEDITOR.editor} targetEditor The drop/paste target editor instance.
		 * @returns {Number} Possible values: {@link CKEDITOR#DATA_TRANSFER_INTERNAL},
		 * {@link CKEDITOR#DATA_TRANSFER_CROSS_EDITORS}, {@link CKEDITOR#DATA_TRANSFER_EXTERNAL}.
		 */
		getTransferType: function( targetEditor ) {
			if ( !this.sourceEditor ) {
				return CKEDITOR.DATA_TRANSFER_EXTERNAL;
			} else if ( this.sourceEditor == targetEditor ) {
				return CKEDITOR.DATA_TRANSFER_INTERNAL;
			} else {
				return CKEDITOR.DATA_TRANSFER_CROSS_EDITORS;
			}
		},

		/**
		 * Copies the data from the native data transfer to a private cache.
		 * This function is needed because the data from the native data transfer
		 * is available only synchronously to the event listener. It is not possible
		 * to get the data asynchronously, after a timeout, and the {@link CKEDITOR.editor#paste}
		 * event is fired asynchronously &mdash; hence the need for caching the data.
		 */
		cacheData: function() {
			if ( !this.$ ) {
				return;
			}

			var that = this,
				i, file;

			function getAndSetData( type ) {
				type = that._.normalizeType( type );

				var data = that.getData( type );
				if ( data ) {
					that._.data[ type ] = data;
				}
			}

			// Copy data.
			if ( CKEDITOR.plugins.clipboard.isCustomDataTypesSupported ) {
				if ( this.$.types ) {
					for ( i = 0; i < this.$.types.length; i++ ) {
						getAndSetData( this.$.types[ i ] );
					}
				}
			} else {
				getAndSetData( 'Text' );
				getAndSetData( 'URL' );
			}

			// Copy files references.
			file = this._getImageFromClipboard();
			if ( ( this.$ && this.$.files ) || file ) {
				this._.files = [];

				for ( i = 0; i < this.$.files.length; i++ ) {
					this._.files.push( this.$.files[ i ] );
				}

				// Don't include $.items if both $.files and $.items contains files, because,
				// according to spec and browsers behavior, they contain the same files.
				if ( this._.files.length === 0 && file ) {
					this._.files.push( file );
				}
			}
		},

		/**
		 * Gets the number of files in the dataTransfer object.
		 *
		 * @returns {Number} The number of files.
		 */
		getFilesCount: function() {
			if ( this._.files.length ) {
				return this._.files.length;
			}

			if ( this.$ && this.$.files && this.$.files.length ) {
				return this.$.files.length;
			}

			return this._getImageFromClipboard() ? 1 : 0;
		},

		/**
		 * Gets the file at the index given.
		 *
		 * @param {Number} i Index.
		 * @returns {File} File instance.
		 */
		getFile: function( i ) {
			if ( this._.files.length ) {
				return this._.files[ i ];
			}

			if ( this.$ && this.$.files && this.$.files.length ) {
				return this.$.files[ i ];
			}

			// File or null if the file was not found.
			return i === 0 ? this._getImageFromClipboard() : undefined;
		},

		/**
		 * Checks if the data transfer contains any data.
		 *
		 * @returns {Boolean} `true` if the object contains no data.
		 */
		isEmpty: function() {
			var typesToCheck = {},
				type;

			// If dataTransfer contains files it is not empty.
			if ( this.getFilesCount() ) {
				return false;
			}

			// Add custom types.
			for ( type in this._.data ) {
				typesToCheck[ type ] = 1;
			}

			// Add native types.
			if ( this.$ ) {
				if ( CKEDITOR.plugins.clipboard.isCustomDataTypesSupported ) {
					if ( this.$.types ) {
						for ( var i = 0; i < this.$.types.length; i++ ) {
							typesToCheck[ this.$.types[ i ] ] = 1;
						}
					}
				} else {
					typesToCheck.Text = 1;
					typesToCheck.URL = 1;
				}
			}

			// Remove ID.
			if ( clipboardIdDataType != 'Text' ) {
				typesToCheck[ clipboardIdDataType ] = 0;
			}

			for ( type in typesToCheck ) {
				if ( typesToCheck[ type ] && this.getData( type ) !== '' ) {
					return false;
				}
			}

			return true;
		},

		/**
		 * When the content of the clipboard is pasted in Chrome, the clipboard data object has an empty `files` property,
		 * but it is possible to get the file as `items[0].getAsFile();` (#12961).
		 *
		 * @private
		 * @returns {File} File instance or `null` if not found.
		 */
		_getImageFromClipboard: function() {
			var file;

			if ( this.$ && this.$.items && this.$.items[ 0 ] ) {
				try {
					file = this.$.items[ 0 ].getAsFile();
					// Duck typing
					if ( file && file.type ) {
						return file;
					}
				} catch ( err ) {
					// noop
				}
			}

			return undefined;
		}
	};
} )();

/**
 * The default content type that is used when pasted data cannot be clearly recognized as HTML or text.
 *
 * For example: `'foo'` may come from a plain text editor or a website. It is not possible to recognize the content
 * type in this case, so the default type will be used. At the same time it is clear that `'<b>example</b> text'` is
 * HTML and its origin is a web page, email or another rich text editor.
 *
 * **Note:** If content type is text, then styles of the paste context are preserved.
 *
 *		CKEDITOR.config.clipboard_defaultContentType = 'text';
 *
 * See also the {@link CKEDITOR.editor#paste} event and read more about the integration with clipboard
 * in the [Clipboard Deep Dive guide](#!/guide/dev_clipboard).
 *
 * @since 4.0
 * @cfg {'html'/'text'} [clipboard_defaultContentType='html']
 * @member CKEDITOR.config
 */

/**
 * Fired after the user initiated a paste action, but before the data is inserted into the editor.
 * The listeners to this event are able to process the content before its insertion into the document.
 *
 * Read more about the integration with clipboard in the [Clipboard Deep Dive guide](#!/guide/dev_clipboard).
 *
 * See also:
 *
 * * the {@link CKEDITOR.config#pasteFilter} option,
 * * the {@link CKEDITOR.editor#drop} event,
 * * the {@link CKEDITOR.plugins.clipboard.dataTransfer} class.
 *
 * @since 3.1
 * @event paste
 * @member CKEDITOR.editor
 * @param {CKEDITOR.editor} editor This editor instance.
 * @param data
 * @param {String} data.type The type of data in `data.dataValue`. Usually `'html'` or `'text'`, but for listeners
 * with a priority smaller than `6` it may also be `'auto'` which means that the content type has not been recognised yet
 * (this will be done by the content type sniffer that listens with priority `6`).
 * @param {String} data.dataValue HTML to be pasted.
 * @param {String} data.method Indicates the data transfer method. It could be drag and drop or copy and paste.
 * Possible values: `'drop'`, `'paste'`. Introduced in CKEditor 4.5.
 * @param {CKEDITOR.plugins.clipboard.dataTransfer} data.dataTransfer Facade for the native dataTransfer object
 * which provides access to various data types and files, and passes some data between linked events
 * (like drag and drop). Introduced in CKEditor 4.5.
 * @param {Boolean} [data.dontFilter=false] Whether the {@link CKEDITOR.editor#pasteFilter paste filter} should not
 * be applied to data. This option has no effect when `data.type` equals `'text'` which means that for instance
 * {@link CKEDITOR.config#forcePasteAsPlainText} has a higher priority. Introduced in CKEditor 4.5.
 */

/**
 * Fired before the {@link #paste} event. Allows to preset data type.
 *
 * **Note:** This event is deprecated. Add a `0` priority listener for the
 * {@link #paste} event instead.
 *
 * @deprecated
 * @event beforePaste
 * @member CKEDITOR.editor
 */

 /**
 * Fired after the {@link #paste} event if content was modified. Note that if the paste
 * event does not insert any data, the `afterPaste` event will not be fired.
 *
 * @event afterPaste
 * @member CKEDITOR.editor
 */

/**
 * Internal event to open the Paste dialog window.
 *
 * @private
 * @event pasteDialog
 * @member CKEDITOR.editor
 * @param {CKEDITOR.editor} editor This editor instance.
 * @param {Function} [data] Callback that will be passed to {@link CKEDITOR.editor#openDialog}.
 */

/**
 * Facade for the native `drop` event. Fired when the native `drop` event occurs.
 *
 * **Note:** To manipulate dropped data, use the {@link CKEDITOR.editor#paste} event.
 * Use the `drop` event only to control drag and drop operations (e.g. to prevent the ability to drop some content).
 *
 * Read more about integration with drag and drop in the [Clipboard Deep Dive guide](#!/guide/dev_clipboard).
 *
 * See also:
 *
 * * The {@link CKEDITOR.editor#paste} event,
 * * The {@link CKEDITOR.editor#dragstart} and {@link CKEDITOR.editor#dragend} events,
 * * The {@link CKEDITOR.plugins.clipboard.dataTransfer} class.
 *
 * @since 4.5
 * @event drop
 * @member CKEDITOR.editor
 * @param {CKEDITOR.editor} editor This editor instance.
 * @param data
 * @param {Object} data.$ Native drop event.
 * @param {CKEDITOR.dom.node} data.target Drop target.
 * @param {CKEDITOR.plugins.clipboard.dataTransfer} data.dataTransfer DataTransfer facade.
 * @param {CKEDITOR.dom.range} data.dragRange Drag range, lets you manipulate the drag range.
 * Note that dragged HTML is saved as `text/html` data on `dragstart` so if you change the drag range
 * on drop, dropped HTML will not change. You need to change it manually using
 * {@link CKEDITOR.plugins.clipboard.dataTransfer#setData dataTransfer.setData}.
 * @param {CKEDITOR.dom.range} data.dropRange Drop range, lets you manipulate the drop range.
 */

/**
 * Facade for the native `dragstart` event. Fired when the native `dragstart` event occurs.
 *
 * This event can be canceled in order to block the drag start operation. It can also be fired to mimic the start of the drag and drop
 * operation. For instance, the `widget` plugin uses this option to integrate its custom block widget drag and drop with
 * the entire system.
 *
 * Read more about integration with drag and drop in the [Clipboard Deep Dive guide](#!/guide/dev_clipboard).
 *
 * See also:
 *
 * * The {@link CKEDITOR.editor#paste} event,
 * * The {@link CKEDITOR.editor#drop} and {@link CKEDITOR.editor#dragend} events,
 * * The {@link CKEDITOR.plugins.clipboard.dataTransfer} class.
 *
 * @since 4.5
 * @event dragstart
 * @member CKEDITOR.editor
 * @param {CKEDITOR.editor} editor This editor instance.
 * @param data
 * @param {Object} data.$ Native dragstart event.
 * @param {CKEDITOR.dom.node} data.target Drag target.
 * @param {CKEDITOR.plugins.clipboard.dataTransfer} data.dataTransfer DataTransfer facade.
 */

/**
 * Facade for the native `dragend` event. Fired when the native `dragend` event occurs.
 *
 * Read more about integration with drag and drop in the [Clipboard Deep Dive guide](#!/guide/dev_clipboard).
 *
 * See also:
 *
 * * The {@link CKEDITOR.editor#paste} event,
 * * The {@link CKEDITOR.editor#drop} and {@link CKEDITOR.editor#dragend} events,
 * * The {@link CKEDITOR.plugins.clipboard.dataTransfer} class.
 *
 * @since 4.5
 * @event dragend
 * @member CKEDITOR.editor
 * @param {CKEDITOR.editor} editor This editor instance.
 * @param data
 * @param {Object} data.$ Native dragend event.
 * @param {CKEDITOR.dom.node} data.target Drag target.
 * @param {CKEDITOR.plugins.clipboard.dataTransfer} data.dataTransfer DataTransfer facade.
 */

/**
 * Defines a filter which is applied to external data pasted or dropped into the editor. Possible values are:
 *
 * * `'plain-text'` &ndash; Content will be pasted as a plain text.
 * * `'semantic-content'` &ndash; Known tags (except `div`, `span`) with all attributes (except
 * `style` and `class`) will be kept.
 * * `'h1 h2 p div'` &ndash; Custom rules compatible with {@link CKEDITOR.filter}.
 * * `null` &ndash; Content will not be filtered by the paste filter (but it still may be filtered
 * by [Advanvced Content Filter](#!/guide/dev_advanced_content_filter)). This value can be used to
 * disable the paste filter in Chrome and Safari, where this option defaults to `'semantic-content'`.
 *
 * Example:
 *
 *		config.pasteFilter = 'plain-text';
 *
 * Custom setting:
 *
 *		config.pasteFilter = 'h1 h2 p ul ol li; img[!src, alt]; a[!href]';
 *
 * Based on this configuration option, a proper {@link CKEDITOR.filter} instance will be defined and assigned to the editor
 * as a {@link CKEDITOR.editor#pasteFilter}. You can tweak the paste filter settings on the fly on this object
 * as well as delete or replace it.
 *
 *		var editor = CKEDITOR.replace( 'editor', {
 *			pasteFilter: 'semantic-content'
 *		} );
 *
 *		editor.on( 'instanceReady', function() {
 *			// The result of this will be that all semantic content will be preserved
 *			// except tables.
 *			editor.pasteFilter.disallow( 'table' );
 *		} );
 *
 * Note that the paste filter is applied only to **external** data. There are three data sources:
 *
 * * copied and pasted in the same editor (internal),
 * * copied from one editor and pasted into another (cross-editor),
 * * coming from all other sources like websites, MS Word, etc. (external).
 *
 * If {@link CKEDITOR.config#allowedContent Advanced Content Filter} is not disabled, then
 * it will also be applied to pasted and dropped data. The paste filter job is to "normalize"
 * external data which often needs to be handled differently than content produced by the editor.
 *
 * This setting defaults to `'semantic-content'` in Chrome, Opera and Safari (all Blink and Webkit based browsers)
 * due to messy HTML which these browsers keep in the clipboard. In other browsers it defaults to `null`.
 *
 * @since 4.5
 * @cfg {String} [pasteFilter='semantic-content' in Chrome and Safari and `null` in other browsers]
 * @member CKEDITOR.config
 */

/**
 * {@link CKEDITOR.filter Content filter} which is used when external data is pasted or dropped into the editor
 * or a forced paste as plain text occurs.
 *
 * This object might be used on the fly to define rules for pasted external content.
 * This object is available and used if the {@link CKEDITOR.plugins.clipboard clipboard} plugin is enabled and
 * {@link CKEDITOR.config#pasteFilter} or {@link CKEDITOR.config#forcePasteAsPlainText} was defined.
 *
 * To enable the filter:
 *
 *		var editor = CKEDITOR.replace( 'editor', {
 *			pasteFilter: 'plain-text'
 *		} );
 *
 * You can also modify the filter on the fly later on:
 *
 *		editor.pasteFilter = new CKEDITOR.filter( 'p h1 h2; a[!href]' );
 *
 * Note that the paste filter is only applied to **external** data. There are three data sources:
 *
 * * copied and pasted in the same editor (internal),
 * * copied from one editor and pasted into another (cross-editor),
 * * coming from all other sources like websites, MS Word, etc. (external).
 *
 * If {@link CKEDITOR.config#allowedContent Advanced Content Filter} is not disabled, then
 * it will also be applied to pasted and dropped data. The paste filter job is to "normalize"
 * external data which often needs to be handled differently than content produced by the editor.
 *
 * @since 4.5
 * @readonly
 * @property {CKEDITOR.filter} [pasteFilter]
 * @member CKEDITOR.editor
 */
