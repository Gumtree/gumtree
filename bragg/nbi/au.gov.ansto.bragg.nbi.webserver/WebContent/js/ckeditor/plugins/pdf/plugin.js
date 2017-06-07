/**
 * @license Copyright (c) 2003-2015, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

/**
 * @fileOverview PDF Plugin
 */

CKEDITOR.plugins.add( 'pdf', {
	// jscs:enable maximumLineLength
	icons: 'pdf,', // %REMOVE_LINE_CORE%
	hidpi: true, // %REMOVE_LINE_CORE%
	toolbar: 'PDF',
	init: function( editor ) {
		// PDF plugin isn't available in inline mode yet.
		if ( editor.elementMode == CKEDITOR.ELEMENT_MODE_INLINE )
			return;

		var pluginName = 'pdf';

		// Register the command.
		editor.addCommand( pluginName, CKEDITOR.plugins.pdf );

		// Register the toolbar button.
		editor.ui.addButton && editor.ui.addButton( 'PDF', {
			label: 'PDF',
			command: pluginName,
			toolbar: 'document,50'
		} );
	}
} );

CKEDITOR.plugins.pdf = {
	exec: function( editor ) {
		if (typeof(getPdf) !== 'undefined') {
			getPdf();
		}else {
			alert("PDF service is not available.");
		}
	},
	canUndo: false,
	readOnly: 1,
};

