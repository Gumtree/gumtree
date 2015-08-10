/**
 * @license Copyright (c) 2003-2015, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function( config ) {
	
	// %REMOVE_START%
	// The configuration options below are needed when running CKEditor from source files.
	config.plugins = 'dialogui,dialog,a11yhelp,dialogadvtab,basicstyles,bidi,blockquote,clipboard,button,panelbutton,panel,floatpanel,colorbutton,colordialog,templates,menu,contextmenu,div,resize,toolbar,elementspath,enterkey,entities,popup,filebrowser,find,fakeobjects,flash,floatingspace,listblock,richcombo,font,forms,format,horizontalrule,htmlwriter,iframe,wysiwygarea,image,indent,indentblock,indentlist,smiley,justify,menubutton,language,link,list,liststyle,magicline,maximize,newpage,pagebreak,pastetext,pastefromword,preview,print,removeformat,save,selectall,showblocks,showborders,sourcearea,specialchar,scayt,stylescombo,tab,table,tabletools,undo,wsc,texzilla,xml,ajax,allowsave,autocorrect,notification,autosave,backgrounds,base64image,lineutils,widget,chart,widgettemplatemenu,devtools,dropdownmenumanager,floating-tools,insertpre,lineheight,mathedit,onchange,pastebase64,removespan,resizewithwindow,tableresize,toolbarswitch';
	config.skin = 'moonocolor';
	// %REMOVE_END%

	// Define changes to default configuration here. For example:
	// config.language = 'fr';
	config.uiColor = '#909090';
//	config.toolbarGroups = [
//	                        { name: 'clipboard',   groups: [ 'clipboard', 'undo' ] },
//	                        { name: 'editing',     groups: [ 'find', 'selection', 'spellchecker' ] },
//	                        { name: 'links' },
//	                        { name: 'insert' },
//	                        { name: 'forms' },
//	                        { name: 'tools' },
//	                        { name: 'document',    groups: [ 'mode', 'document', 'doctools' ] },
//	                        { name: 'others' },
//	                        '/',
//	                        { name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
//	                        { name: 'paragraph',   groups: [ 'list', 'indent', 'blocks', 'align' ] },
//	                        { name: 'styles' },
//	                        { name: 'colors' },
//	                        { name: 'about' }
//	                    ];
//	
	config.toolbar = [
	                  { name: 'document', items: [ 'Source', '-', 'Save', 'Templates', '-', 'Preview', 'Print'] },
	                  { name: 'clipboard', items: [ 'Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord', '-', 'Undo', 'Redo' ] },
	                  { name: 'find', items: ["Find", "SelectAll", "Scayt"]},
	                  { name: 'basicstyles', items: [ "Bold", "Italic", "Underline", "Strike", "Subscript", "Superscript", "RemoveFormat" ] },
	                  '/',
	                  { name: 'paragraph', items: [ "NumberedList", "BulletedList", "Outdent", "Indent", "Blockquote", 
	                                                  "JustifyLeft", "JustifyCenter", "JustifyRight", "JustifyBlock" ] },
	                  { name: 'styles', items: [ "Styles", "Format", "Font", "FontSize"] },
	                  { name: 'links', items: [ "Link", "Unlink", "Anchor"] },
	                  '/',
	                  { name: 'basicstyles', items: [ "TextColor", "BGColor", "UIColor", "ShowBlocks" ] },
	                  { name: 'media', items: [ "CreatePlaceholder", "Image", "Flash", "Table", "HorizontalRule", "Smiley", "SpecialChar",
	                                            "PageBreak", "InsertPre"] },
//	                  { name: 'buttons', items: [ "Form", "Checkbox", "Radio", "TextField", "Textarea", "Select",
//	                                              "Button", "ImageButton", "HiddenField"] },
	              ];
	config.extraPlugins = 'image2';
	
	config.blockedKeystrokes =
		[
			 CKEDITOR.CTRL + 83 /*S*/,
			 CKEDITOR.CTRL + 73 /*I*/,
			 CKEDITOR.CTRL + 85 /*U*/
		];
	
	config.keystrokes = [
	                 		[ CKEDITOR.CTRL + 83 /*S*/, 'save' ],
	                 	];
	
};
