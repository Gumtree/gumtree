
de.kupzog.ktable.*  Version 2.1.3

URL: http://ktable.sourceforge.net/
_______________________________________________________________________

This is the source of a custom table class for Java SWT applications.

The original version of this table was written by Konstantin Scheglov. 
KTable is a complete new implementation but uses the structure and
some code fragments from the old version.

The features of this table implementation in short terms:

- a table model provides the data for the table (comparable
  to the Swing table model)
  
- cell rendering is done by extern classes and thus can easily
  be changed to any rendering one could imagine...
  
- Columns and rows can be resized by mouse.
  
- There can be horizontal and vertical headers (fixed cells)
  as many as needed.
  
- Different selection modes are available

- In place editing is possibel by using editor classes that
  can easily be adjusted to special needs.
  
- Cells may span over several columns and/or rows.
  
  
For a detailed function description refer to the api documentation that 
is included in the sourcefiles.

For examples how to use KTable, see the "ExampleGUI" class. You can 
run this class and see different examples of KTables.

Text Table		Shows a Table with 100 rows and 100 columns.
				Row selection mode is demonstrated - with properly configured cellrenderers.
				The use of cell editors is shown (textcelleditor and combocelleditor).
				You can resize rows and columns.
				Selection Listeners are used (see the console output).
				
Boolean Table   Demonstrates a huge table (1000000x1000000)
                Operates with boolean values and shows them using
                CheckableCellRenderer
                Demonstrates the usage of KTableCellEditorCheckbox2 which
                is only sensitive for activation at a specific region of the cell.
                Cells that have focus are also shown in the fixed cells (like in Excel)
                
Span Table		Demonstrates the cell spanning feature. Cells in the content area 
				always span over 2 rows and 2 columns.
				
Sortable Table	Demonstrates the usage of a KTableSortedModel and the appropriate usage
				of KTableSortComparator.
				Shows how sort indicators are painted to column headers.
				Shows how cells that span react when the table is sorted.
				Multi-Selection-Mode is on.
				
Non-Scrolling Table Demonstrates a tablemodel that splits the available horizontal space 
				between the columns. When resizing, the other columns get larger or smaller,
				keeping the total size of the table constant.
				
Color Palette	Here you can see that a table does not have to
				look like a table...
				See how the cell renderer is implemented and 
				what the table model does.
				
Towns			This example shows how images can be included in
				table cells together with text. It also shows the use
				of the multi line cell editor.
				


The authors welcomes any feedback:  fkmk@kupzog.de
                                    lorenz.maierhofer@logicmindguide.com













Changes in Version 1.1 compared to previous version:

- empty table without header looks better now
- right-click on cell also changes the selection

Changes in Version 1.2
- License changed from GPL to LGPL
- Table does no longer throw NullPointerException if no model is set.
- minor other bug fixes
