CKEDITOR.dialog.add("simpleimageDialog", function(editor) {
	var proportionalScale, isSizePreload, isBase64Allowed, validationMessage;
	isSizePreload = false;
	isBase64Allowed = CKEDITOR.config.simpleImageBase64allowed || false;
	validationMessage = isBase64Allowed ? "Source cannot be empty." : "Source can neither be empty nor have Base64 format. Type an external url."


	function isBase64URL(url) {
		return /data\:image/.test(url);
	}


	return {
		allowedContent: "img[src,alt,width,height]",
		title: "Insert Image",
		minWidth: 550,
		minHeight: 100,
		resizable: CKEDITOR.DIALOG_RESIZE_NONE,
		contents:[{
			id: "SimpleImage",
			label: "Details",
			elements:[{
				type: "text",
				label: "Source",
				id: "edp-src",
				validate: CKEDITOR.dialog.validate.notEmpty( validationMessage ),
				setup: function (element) {
					if(element.getAttribute("src")) {
						if(element.getAttribute("width") && element.getAttribute("height")) {
							isSizePreload = true;
						}
						this.setValue( element.getAttribute("src") );
					}
				},
				commit: function (element) {
					element.setAttribute("src", this.getValue());
				},
				onChange: function () {
					if(isBase64URL(this.getValue())) {
						if(!isBase64Allowed) {
							this.setValue("");
							return;
						}
						if(!isSizePreload) {
							var img = new Image();
							var dialog = this.getDialog();
							img.onload = function(f) {
								if(f) {
									proportionalScale = this.width/this.height;
									dialog.setValueOf("Dimensions","edp-width", this.width);
									dialog.setValueOf("Dimensions","edp-height", this.height);
								}
							};
							img.src = this.getValue();
						} else {
							isSizePreload = false;
						}
					}
				}
			}, {
				type: "text",
				label: "Image Description",
				id: "edp-text-description",
				setup: function (element) {
					if(element.getAttribute("alt")) {
						this.setValue( element.getAttribute("alt") );
					}
				},
				commit: function (element) {
					if(this.getValue()) {
						element.setAttribute("alt", this.getValue());
					}
				},
			}]
		},
 			{
 				id:"Dimensions",
 				label: "Dimensions (in pixels)",
				elements:[{
						type: "text",
						label: "Width",
						id: "edp-width",
						setup: function (element) {
							if(element.getAttribute("width")) {
								this.setValue( element.getAttribute("width") );
							}
						},
						commit: function (element) {
							if(this.getValue()) {
								element.setAttribute("width", this.getValue());
							}
						},
						onKeyUp: function() {
							var dialog = this.getDialog();
							var width  = dialog.getValueOf("Dimensions","edp-width");
							var height = dialog.getValueOf("Dimensions","edp-height");
							var newHeight = 1/proportionalScale * this.getValue();
							if (!isNaN(newHeight)) {
								newHeight = newHeight.toFixed(1)
								if(width && height && (newHeight != height)) {
									dialog.setValueOf("Dimensions","edp-height",newHeight);
								}
							}
						}
				}, {
						type:"text",
						label: "Height",
						id: "edp-height",
						setup: function (element) {
							if(element.getAttribute("height")) {
								this.setValue( element.getAttribute("height") );
							}
						},
						commit: function (element) {
							if(this.getValue()) {
								element.setAttribute("height", this.getValue());
							}
						},
						onKeyUp: function() {
							var dialog = this.getDialog();
							var width  = dialog.getValueOf("Dimensions","edp-width");
							var height = dialog.getValueOf("Dimensions","edp-height");
							var newWidth = proportionalScale * this.getValue();
							if (!isNaN(newWidth)) {
								newWidth = newWidth.toFixed(1);
								if(width && height && (newWidth != width)) {
									dialog.setValueOf("Dimensions","edp-width",newWidth);
								}
							}
						}
				}]
			}
		],
		onShow: function () {
			var selection = editor.getSelection();
			var selector = selection.getStartElement()
			var element;

			if(selector) {
				 element = selector.getAscendant( 'img', true );
			}

			if ( !element || element.getName() != 'img' ) {
				element = editor.document.createElement( 'img' );
        this.insertMode = true;
			}
			else {
				this.insertMode = false;
			}

			this.element = element;

			this.setupContent(this.element);
		},
		onOk: function() {
			var dialog = this;
			var anchorElement = this.element;

			this.commitContent(this.element);

			if(this.insertMode) {
				editor.insertElement(this.element);
			}
		}
	};
});
