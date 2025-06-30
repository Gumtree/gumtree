/*
 * @file image paste plugin for CKEditor
	Feature introduced in: https://bugzilla.mozilla.org/show_bug.cgi?id=490879
	doesn't include images inside HTML (paste from word): https://bugzilla.mozilla.org/show_bug.cgi?id=665341
 * Copyright (C) 2011-13 Alfonso Martínez de Lizarrondo
 *
 * == BEGIN LICENSE ==
 *
 * Licensed under the terms of any of the following licenses at your
 * choice:
 *
 *  - GNU General Public License Version 2 or later (the "GPL")
 *    http://www.gnu.org/licenses/gpl.html
 *
 *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
 *    http://www.gnu.org/licenses/lgpl.html
 *
 *  - Mozilla Public License Version 1.1 or later (the "MPL")
 *    http://www.mozilla.org/MPL/MPL-1.1.html
 *
 * == END LICENSE ==
 *
 * version 1.1.1: Added allowedContent settings in case the Advanced tab has been removed from the image dialog
 */

 // Handles image pasting in Firefox
CKEDITOR.plugins.add( 'imagepaste',
{
	init : function( editor )
	{

		// v 4.1 filters
		if (editor.addFeature)
		{
			editor.addFeature( {
				allowedContent: 'img[!src,id];'
			} );
		}

		// Paste from clipboard:
		editor.on( 'paste', function(e) {
			var data = e.data;
			var html = (data.html || ( data.type && data.type=='html' && data.dataValue));
			if (e.data.html) {
				
				// strip out webkit-fake-url as they are useless:
				if (CKEDITOR.env.webkit && (html.indexOf("webkit-fake-url")>0) )
				{
					alert("Sorry, the images pasted with Safari aren't usable");
//					window.open("https://bugs.webkit.org/show_bug.cgi?id=49141");
//					html = html.replace( /<img src="webkit-fake-url:.*?">/g, "");
				}

				// Replace data: images in Firefox and upload them
				console.log("using imagepaste 16");
				html = html.replace( /<img src="data:image\/png;base64,.*?>/g, function( img )
				{
					var data = img.match(/"data:image\/png;base64,(.*?)"/)[1];
					var id = CKEDITOR.tools.getNextId();
					var src;

//						var url= editor.config.filebrowserImageUploadUrl;
					var url= editor.config.imagePasteBase64Api;
					if (url.indexOf("?") == -1)
						url += "?";
					else
						url += "&";
					url += 'CKEditor=' + editor.name + '&CKEditorFuncNum=2&langCode=' + editor.langCode;
			        
//						var postUrl = 'notebook/save' + (session != null ? '?session=' + session : '?pageid=' + pageId);
			        $.post( url, { upload: data }, function(ret, status) {
			            if (status == "success") {
			            	console.log("ret = " + ret);
				        	src = ret;
				        	var theImage = editor.document.getById( id );
							theImage.data( 'cke-saved-src', ret);
							theImage.setAttribute( 'src', ret);
							theImage.removeAttribute( 'id' );
							img = new Image();
							img.onload = function() {
							  console.log(this.width + 'x' + this.height);
							  try {
								  if (this.width / this.height > 660 / 880) {
									  if (this.width > 660) {
										  theImage.setAttribute( 'width', 660);										  
									  }
								  } else {
									  if (this.height > 880) {
										  theImage.setAttribute( 'height', 880);
									  }
								  }
								} catch (e) {
									console.log("failed to resize");
								}
							}
							img.src = src;
				        }
			        })
			        .fail(function(e) {
			        	var notification = new CKEDITOR.plugins.notification( 
			        			CKEDITOR.instances.id_editable_inner, { 
			        				message: 'Failed to upload image.', 
			        				type: 'warning' } );
			            notification.show();
			        });
			        
					return img.replace(/>/, ' id="' + id + '">');
			       
				});
				e.data.html = html;
			} else {
		        var dataTransfer = e.data.dataTransfer;
		        var filesCount = dataTransfer.getFilesCount();
		        console.log("file counts = " + filesCount);
		        var id = CKEDITOR.tools.getNextId();
		        var file = dataTransfer.getFile(0);
		        var formData = new FormData();
		        formData.append('upload', file);

				var url= editor.config.filebrowserImageUploadUrl;
				const ts = Date.now();
				var src = 'notebook/images?id=' + ts + ".png";
				if (url.indexOf("?") == -1)
					url += "?ts=" + ts + "&";
				else
					url += "&ts=" + ts + "&";
				url += 'CKEditor=' + editor.name + '&CKEditorFuncNum=2&langCode=' + editor.langCode;
		        
//		        $.post( url, formData, function(ret, status) {
//		            if (status == "success") {
//		            	console.log("ret = " + ret);
//			        	src = ret;
//			        }
//		        })
//		        .fail(function(e) {
//		        	var notification = new CKEDITOR.plugins.notification( 
//		        			CKEDITOR.instances.id_editable_inner, { 
//		        				message: 'Failed to upload image.', 
//		        				type: 'warning' } );
//		            notification.show();
//		        });
		        
			    var timeout = 10000;
			    var xhr = new XMLHttpRequest();
			    var p = new Promise(function (resolve, reject) {
			      xhr.open('post', url);
			      xhr.send(formData);
			      xhr.onreadystatechange = function() {
			    	  console.log(xhr.readyState);
			    	  console.log(xhr.status);
			        if(xhr.readyState === 4 && xhr.status == 200) {
			          var text =  xhr.responseText || '{}';
			          var data = JSON.parse(text);
			          if (data.url) {
			        	console.log(data.url);
			            src = data.url;
				          resolve(src);
			          } else {
			            src = "error";
			            reject();
			          }
			          xhr = null;
			        } 
			        else if (xhr.readyState === 4 && xhr.status !== 200) {
			        	src = "error";
			          xhr = null;
			          reject();
			        }
			      }
			    });
			    var t = new Promise(function(resolve) {
			      var t = setTimeout(function () {
			        if (xhr) {
			          xhr && xhr.abort();
			          resolve('request time out');
			          clearTimeout(t);
			        }
			      }, timeout);
			    });
			    const r = Promise.race([p, t]);
			    
			    r.then(function(ret) {
			    	var theImage = editor.document.getById( id );
					theImage.data( 'cke-saved-src', ret);
					theImage.setAttribute( 'src', ret);
					theImage.removeAttribute( 'id' );
					img = new Image();
					img.onload = function() {
					  console.log(this.width + 'x' + this.height);
					  try {
						  if (this.width / this.height > 660 / 880) {
							  if (this.width > 660) {
								  theImage.setAttribute( 'width', 660);										  
							  }
						  } else {
							  if (this.height > 880) {
								  theImage.setAttribute( 'height', 880);
							  }
						  }
						} catch (e) {
							console.log("failed to resize");
						}
					}
					img.src = ret;
			    });
			    html = '<img alt="" src="' + src + '" id="' + id + '"/>';
			    console.log("html = " + html);
		        e.data.dataValue = html;

//				return;
			}

//			if (e.data.html)
//				e.data.html = html;
//			else
//				e.data.dataValue = html;
//	        e.preventDefault();
		});

	} //Init
} );

function ajaxPost (option) {
    var timeout = 10000;
    var xhr = new XMLHttpRequest();
    var p = new Promise(function (resolve, reject) {
      option = option || {};
      xhr.open('post', option.url);
      xhr.send(option.data);
      console.log(option.url);
      xhr.onreadystatechange = function() {
    	  console.log(xhr.readyState);
    	  console.log(xhr.status);
        if(xhr.readyState === 4 && xhr.status == 200) {
          var text =  xhr.responseText || '{}';
          var data = JSON.parse(text);
          if (data.url) {
        	console.log(data.url);
            resolve(data.url);
          } else {
            // 没有返回图片链接则reject
            reject();
          }
          xhr = null;
        } 
        else if (xhr.readyState === 4 && xhr.status !== 200) {
          reject();
          xhr = null;
        }
      }
    });
    var t = new Promise(function(resolve) {
      var t = setTimeout(function () {
        if (xhr) {
          xhr && xhr.abort();
          resolve('request time out');
          clearTimeout(t);
        }
      }, timeout);
    });
    return Promise.race([p, t]);
  }