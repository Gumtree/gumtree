/**
 * @license Copyright (c) 2003-2021, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see https://ckeditor.com/legal/ckeditor-oss-license
 */

CKEDITOR.editorConfig = function( config ) {
	// %REMOVE_START%
	// The configuration options below are needed when running CKEditor from source files.
	config.plugins = 'dialogui,dialog,about,basicstyles,blockquote,notification,button,toolbar,'
				   + 'clipboard,elementspath,enterkey,entities,floatingspace,wysiwygarea,indent,'
				   + 'indentlist,fakeobjects,link,list,undo,allowsave,autosave,'
				   + 'lineutils,widgetselection,widget,codesnippet,'
				   + 'panelbutton,panel,floatpanel,colorbutton,menu,contextmenu,'
				   + 'colordialog,docprops,docfont,editorplaceholder,find,'
				   + 'listblock,richcombo,font,imageresize,imagepaste,'
				   + 'imageresizerowandcolumn,indentblock,textselection,'
				   + 'basewidget,lineheight,liststyle,'
				   + 'magicline,newpage,pagebreak,pastetext,pastefromexcel,xml,ajax,'
				   + 'pastefromgdocs,pastefromword,preview,print,'
				   + 'removeformat,selectall,stylescombo,tab,table,'
				   + 'toc,tabletools,tableresize,'
				   + 'tableselection,tabletoolstoolbar,texttransform,'
				   + 'htmlwriter,wordcount,sourcearea,'
				   + 'menubutton,scayt,format,specialchar,smiley,symbol';
	config.skin = 'moono-lisa';
	// %REMOVE_END%

	// ******** removed plugins = 'tablesorter,ckeditortablecellsselection,save,ckeditor_fa,
	// pastetools,simage,save-to-pdf,layoutmanager,letterspacing,pastecode,insertpre,previewinserver,'
	
	// Define changes to default configuration here.
	// For complete reference see:
	// https://ckeditor.com/docs/ckeditor4/latest/api/CKEDITOR_config.html

	// The toolbar groups arrangement, optimized for a single toolbar row.
	config.toolbarGroups = [
		{ name: 'document',	   groups: [ 'mode', 'document', 'doctools' ] },
		{ name: 'tools' },
		{ name: 'clipboard',   groups: [ 'clipboard', 'undo' ] },
		{ name: 'editing',     groups: [ 'find', 'selection', 'spellchecker' ] },
		{ name: 'colors' },
		{ name: 'styles' },
		{ name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
		{ name: 'paragraph',   groups: [ 'list', 'indent', 'blocks', 'align', 'bidi' ] },
		{ name: 'links' },
		{ name: 'insert' },
		{ name: 'others' }
//		{ name: 'forms' },
	];

	config.keystrokes = [
	    [ CKEDITOR.CTRL + 83, 'save' ],    // Ctrl+S
	];
	
	config.extraPlugins = 'pdf,word,lock';

	// The default plugins included in the basic setup define some buttons that
	// are not needed in a basic editor. They are removed here.
//	config.removeButtons = 'Anchor,Underline,Strike,Subscript,Superscript';
	
	config.filebrowserImageUploadUrl = 'notebook/imageupload';

//	config.extraPlugins = 'pasteUploadImage';
	config.pasteUploadFileApi = 'notebook/imageupload';
	config.pasteUploadImageUrlApi = 'notebook/imageurl';
//	config.filebrowserImageUploadUrl = 'notebook/imageurl?';
	
	// Dialog windows are also simplified.
	config.removeDialogTabs = 'link:advanced';

	config.autosave_delay = 300;

	CKEDITOR.config.imageResize = { maxWidth : 660, maxHeight : 880 };

	config.undoStackSize = 50;
};
