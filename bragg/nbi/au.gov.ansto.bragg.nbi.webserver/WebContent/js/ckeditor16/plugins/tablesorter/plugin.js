/**
 * Copyright (c) 2017, Max Wilckens. All rights reserved.
 * Licensed under the terms of the MIT License (see LICENSE.md).
 *
 * Plugin to allow table sorting by each column.
 *
 */

 CKEDITOR.plugins.add( 'tablesorter', {
	lang:"de,en",
    init: function( editor ) {

        if ( editor.contextMenu ) {
            editor.addMenuGroup( 'tablesorterGroup' );
            editor.addMenuItem( 'sortasc', {
                label: editor.lang.tablesorter.contextAsc,
                command: 'sortasc',
                group: 'tablesorterGroup'
            });
            editor.addMenuItem( 'sortdesc', {
                label: editor.lang.tablesorter.contextDesc,
                command: 'sortdesc',
                group: 'tablesorterGroup'
            });

            editor.contextMenu.addListener( function( element ) {
                if ( element.getAscendant( 'tr', true ) ) {
                    return { sortasc: CKEDITOR.TRISTATE_OFF};
                }
            });
            editor.contextMenu.addListener( function( element ) {
                if ( element.getAscendant( 'tr', true ) ) {
                    return { sortdesc: CKEDITOR.TRISTATE_OFF};
                }
            });

			editor.addCommand( 'sortasc', {
				exec: function( editor ) {
					tablesort('asc');
				}
			});
			editor.addCommand( 'sortdesc', {
				exec: function( editor ) {
					tablesort('desc');
				}
			});

			var tablesort = function( order ){
				var selection = editor.getSelection();
				var element = selection.getStartElement();
				if ( element ){
					var column_nr = element.getAscendant( { td:1, th:1 }, true ).getIndex();
					var table = element.getAscendant({table:1});
					var tbody = table.getElementsByTag('tbody').getItem(0);
					if(tbody == undefined) tbody = table;
					var items = tbody.$.childNodes;
					var itemsArr = [];
					for (var i in items) {
						if (items[i].nodeType == 1) // get rid of the whitespace text nodes
							itemsArr.push(items[i]);
					}

					itemsArr.sort(function(a, b) {
						var aText = a.childNodes[column_nr].innerText.trim();
						var bText = b.childNodes[column_nr].innerText.trim();
						if(!aText || 0 === aText.length)
							if(!bText || 0 === bText.length) return 0;
							else return 1;
						if(!bText || 0 === bText.length) return -1;
						if(order == 'desc') return bText.localeCompare(aText);
						return aText.localeCompare(bText);
					});

					for (i = 0; i < itemsArr.length; ++i) {
					  tbody.$.appendChild(itemsArr[i]);
					}
				}
			}
		}

    }
});
