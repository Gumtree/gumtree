/**
 * @license Copyright (c) 2003-2019, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see https://ckeditor.com/legal/ckeditor-oss-license
 */

CKEDITOR.editorConfig = function( config ) {
	
	// %REMOVE_START%
	// The configuration options below are needed when running CKEditor from source files.
	config.plugins = 'dialogui,dialog,about,a11yhelp,basicstyles,blockquote,notification,button,'
					+ 'toolbar,clipboard,panel,floatpanel,menu,contextmenu,elementspath,enterkey,'
					+ 'entities,popup,filetools,filebrowser,floatingspace,listblock,richcombo,format,'
					+ 'horizontalrule,htmlwriter,wysiwygarea,indent,indentlist,fakeobjects,link,'
					+ 'list,magicline,pastetext,pastetools,pastefromgdocs,pastefromword,removeformat,'
					+ 'showborders,sourcearea,specialchar,menubutton,scayt,stylescombo,tab,table,tabletools,'
					+ 'tableselection,undo,lineutils,widgetselection,widget,notificationaggregator,'
					+ 'uploadwidget,wsc,allowsave,autosave,balloonpanel,btbutton,'
					+ 'bt_table,zoom,tabletoolstoolbar,tableresizerowandcolumn,tableresize,contents,'
					+ 'panelbutton,spacingsliders,simplebutton,selectall,removespan,print,preview,'
//					+ 'pastebase64,pasteFromGoogleDoc,pastefromexcel,textindent,pagebreak,newpage,'
					+ 'pasteFromGoogleDoc,pastefromexcel,textindent,pagebreak,newpage,'
					+ 'lineheight,letterspacing,ckeditor_wiris,symbol,smiley,imagerotate,' 
					+ 'imageresizerowandcolumn,imageresize,zsuploader,font,find,colordialog,'
					+ 'docprops,docfont,colorbutton,pbckcode,mathjax';
	config.skin = 'moono-lisa';
	
	config.height = '880';
	
	// autogrow config
//	config.autoGrow_minHeight = 600;
//	config.autoGrow_maxHeight = 800;
	config.resize_minHeight = 600;
	config.resize_maxHeight = 800;
	
	// %REMOVE_END%

	// Define changes to default configuration here.
	// For complete reference see:
	// https://ckeditor.com/docs/ckeditor4/latest/api/CKEDITOR_config.html

	// The toolbar groups arrangement, optimized for two toolbar rows.
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
		
	];

	config.keystrokes = [
	    [ CKEDITOR.CTRL + 83, 'save' ],    // Ctrl+S
	];
	
	// Remove some buttons provided by the standard plugins, which are
	// not needed in the Standard(s) toolbar.
	config.removeButtons = 'Underline,Subscript,Superscript,resize';

	config.extraPlugins = 'pdf,word,lock';

	config.autosave_delay = 300;

	// Set the most common block elements.
	config.format_tags = 'p;h1;h2;h3;pre';

	// Simplify the dialog windows.
	config.removeDialogTabs = 'image:advanced;link:advanced';
	
	config.mathJaxLib = '//cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.4/MathJax.js?config=TeX-AMS_HTML';
	
	CKEDITOR.config.imageResize = { maxWidth : 660, maxHeight : 880 };
	

};
