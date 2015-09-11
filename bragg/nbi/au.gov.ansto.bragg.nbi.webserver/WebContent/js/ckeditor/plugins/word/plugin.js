/**
 * @license Copyright (c) 2003-2015, CKSource - nxi. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

/**
 * @fileOverview WORD Plugin
 */

CKEDITOR.plugins.add( 'word', {
	// jscs:enable maximumLineLength
	icons: 'word', // %REMOVE_LINE_CORE%
	hidpi: true, // %REMOVE_LINE_CORE%
	toolbar: 'WORD',
	init: function( editor ) {
		CKEDITOR.scriptLoader.load(CKEDITOR.getUrl(CKEDITOR.plugins.getPath('word') + 'js/FileSaver.js'), function() {
			CKEDITOR.scriptLoader.load(CKEDITOR.getUrl(CKEDITOR.plugins.getPath('word') + 'js/jquery.wordexport.js'));			
		});

		var pluginName = 'word';

		// Register the command.
		editor.addCommand( pluginName, CKEDITOR.plugins.word);

		// Register the toolbar button.
		editor.ui.addButton && editor.ui.addButton( 'WORD', {
			label: 'WORD',
			command: pluginName,
			toolbar: 'document,60'
		} );
	}
} );

CKEDITOR.plugins.word = {
	exec: function( editor ) {
		if (typeof(getWord) !== 'undefined') {
			getWord();
		}else {
			alert("WORD exporter is not available.");
		}
	},
	canUndo: false,
	readOnly: 1,
};

