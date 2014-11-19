package de.kupzog.ktable;

import org.eclipse.swt.graphics.Rectangle;
import de.kupzog.ktable.models.KTableModel;

public abstract class KTableCellAction {
	protected KTableModel m_Model;
	protected KTable m_Table;
	protected Rectangle m_Rect;
	protected int m_Row;
	protected int m_Col;

	/**
	 * Запускает action, вызывается kTable-ом
	 * @param table
	 * @param col
	 * @param row
	 * @param rect
	 */
	public void run(KTable table, int col, int row, Rectangle rect) {
		m_Table = table;
		m_Model = table.getModel();
		m_Rect = rect;
		m_Row = row;
		m_Col = col;
	}
}