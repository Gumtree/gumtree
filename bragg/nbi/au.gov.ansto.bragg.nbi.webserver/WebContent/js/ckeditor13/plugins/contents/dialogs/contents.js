/**
 * Copyright (c) 2014-2016, CKSource - Frederico Knabben. All rights reserved.
 * Licensed under the terms of the MIT License (see LICENSE.md).
 */

// Note: This automatic widget to dialog window binding (the fact that every field is set up from the widget
// and is committed to the widget) is only possible when the dialog is opened by the Widgets System
// (i.e. the widgetDef.dialog property is set).
// When you are opening the dialog window by yourself, you need to take care of this by yourself too.

CKEDITOR.dialog.add( 'contents', function( editor ) {
	return {
		title: 'Edit',
		minWidth: 200,
		minHeight: 100,
		contents: [
			{
				id: 'info',
				elements: [
					{
						id: 'align',
						type: 'select',
						label: 'Align',
						items: [
							[ editor.lang.common.notSet, '' ],
							[ editor.lang.common.alignLeft, 'float-left' ],
							[ editor.lang.common.alignRight, 'float-right' ],
						],
                        'default': editor.lang.common.notSet,
						// When setting up this field, set its value to the "align" value from widget data.
						// Note: Align values used in the widget need to be the same as those defined in the "items" array above.
						setup: function( widget ) {
							widget.data.align === undefined ? this.setValue( '' ) : this.setValue( widget.data.align );
						},
						// When committing (saving) this field, set its value to the widget data.
						commit: function( widget ) {
							widget.setData( 'align', this.getValue() );
						}
					},

                    {
                        id: 'chkInsertOpt',
                        type: 'checkbox',
                        label: 'Ignore nested headers',
                        'default': true,
                        setup: function( widget ) {
                            widget.data.chkInsertOpt === undefined ? this.setValue( false ) : this.setValue( widget.data.chkInsertOpt );
                        },


                        commit: function( widget ) {
                            widget.setData( 'chkInsertOpt', this.getValue());
                        }
                    }

				]
			},

		]
	};
} );
