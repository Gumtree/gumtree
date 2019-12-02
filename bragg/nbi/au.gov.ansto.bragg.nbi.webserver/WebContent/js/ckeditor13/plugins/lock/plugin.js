/**
 * @license Copyright (c) 2003-2015, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

/**
 * @fileOverview PDF Plugin
 */

CKEDITOR.plugins.add( 'lock', {
	// jscs:enable maximumLineLength
	icons: 'lock,', // %REMOVE_LINE_CORE%
	hidpi: true, // %REMOVE_LINE_CORE%
	toolbar: 'LOCK',
	init: function( editor ) {
		// PDF plugin isn't available in inline mode yet.
		if ( editor.elementMode == CKEDITOR.ELEMENT_MODE_INLINE )
			return;

		var pluginName = 'lock';

		// Register the command.
		editor.addCommand( pluginName, CKEDITOR.plugins.lock );

		// Register the toolbar button.
		editor.ui.addButton && editor.ui.addButton( 'LOCK', {
			label: 'Lock this page',
			command: pluginName,
			toolbar: 'document,70'
		} );
	}
} );

CKEDITOR.plugins.lock = {
	exec: function( editor ) {
		if (typeof(lockPage) !== 'undefined') {
			lockPage();
		}else {
			alert("Page lock service is not available.");
		}
	},
	canUndo: false,
	readOnly: 1,
};

