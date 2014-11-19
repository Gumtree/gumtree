package de.kupzog.ktable;

import org.eclipse.swt.graphics.Cursor;

/**
 * This interface defines a cursor provider for KTable. Depending on the cell and position that the mouse currently 
 * hovers over, the cursor provider may choose different cursors. The cursor provider cannot override the cursor
 * adaption that KTable itself employs (for example the resize cursor); it can only replace the default cursor. The
 * cursor provider is called on all mouse movements within KTable.
 *
 * @author Magnus von Koeller (magnus@vonkoeller.de)
 */
public interface KTableCursorProvider {
	
	/**
	 * Determine the cursor to display for a given mouse hover position.
	 * 
	 * @param col The column of the cell that the mouse currently hovers over.
	 * @param row The row of the cell that the mouse currently hovers over.
	 * @param x The horizontal position of the mouse.
	 * @param y The vertical position of the mouse.
	 * @return The cursor to display.
	 */
	public Cursor getCursor(int col, int row, int x, int y);

}
