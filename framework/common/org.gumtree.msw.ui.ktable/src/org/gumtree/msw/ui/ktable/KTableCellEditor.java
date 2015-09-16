/*
 * Copyright (C) 2004 by Friederich Kupzog Elektronik & Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

Authors: 
Friederich Kupzog,  fkmk@kupzog.de, www.kupzog.de/fkmk
Lorenz Maierhofer, lorenz.maierhofer@logicmindguide.com

*/
package org.gumtree.msw.ui.ktable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;


public abstract class KTableCellEditor {
	
	protected KTableModel m_Model;
	protected KTable m_Table;
	protected Rectangle m_Rect;
	protected int m_Row;
	protected int m_Col;
	private Control m_Control;
	private List listeners;
	
	protected String m_toolTip;
	
	private Rectangle lastProvidedCellRect;

	// Action constants for activation:
    public static final int DOUBLECLICK = 1<<1;
    public static final int SINGLECLICK = 1<<2;
    public static final int KEY_ANY = 1<<3;
    public static final int KEY_RETURN_AND_SPACE = 1<<4;
	
    protected KTableCellEditor() {
    	listeners = new ArrayList();
    }
	
	/**
	 * disposes the editor and its components
	 */
	public void dispose() {
		if (m_Control != null) {
			Control contr = m_Control;
			m_Control = null;
			contr.dispose();
		}
	}

	public void addListener(ITableCellEditorListener listener) {
		if (listener != null)
			listeners.add(listener);
	}
	public boolean removeListener(ITableCellEditorListener listener) {
		return listeners.remove(listener);
	}
	
	/**
	 * Activates the editor at the given position.
	 * @param row
	 * @param col
	 * @param rect
	 */
	public void open(KTable table, int col, int row, Rectangle rect) {
		m_Table = table;
		m_Model = table.getModel();
		m_Rect = rect;
		m_Row = row;
		m_Col = col;
		if (m_Control == null) {
			m_Control = createControl();
			m_Control.setToolTipText(m_toolTip);
			m_Control.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent arg0) {
					if (m_Control!=null && !m_Control.isDisposed())
						close(true);
				}
			});
			m_Control.addMouseMoveListener(new MouseMoveListener() {
	            public void mouseMove(MouseEvent e) {
	               m_Table.setCursor(null);
	            }
	        });
		}
		setBounds(m_Rect);
		GC gc = new GC(m_Table);
		m_Table.drawCell(gc, m_Col, m_Row);
		gc.dispose();
		
		for (int i = 0, n = listeners.size(); i != n; i++)
			((ITableCellEditorListener)listeners.get(i)).onOpen(m_Control);
		
		m_Control.setFocus();
	}
	
	/**
	 * Deactivates the editor.
	 * @param save
	 * If true, the content is saved to the underlying table.
	 */
	public void close(boolean save) {
		for (int i = 0, n = listeners.size(); i != n; i++)
			((ITableCellEditorListener)listeners.get(i)).onClose(m_Control);
		
		m_Table.m_CellEditor = null;
		//m_Control.setVisible(false);
		GC gc = new GC(m_Table);
		m_Table.drawCell(gc, m_Col, m_Row);
		gc.dispose();
		this.dispose();
		m_Table.setFocus();
	}
	
	/**
	 * Returns true if the editor has the focus.
	 * @return boolean
	 */
	public boolean isFocused() {
		if (m_Control == null)
			return false;
		return m_Control.isFocusControl();
	}

	/**
	 * Sets the editor's position and size
	 * @param rect
	 */
	public void setBounds(Rectangle rect) {
		if (m_Control != null)
			m_Control.setBounds(rect);
	}

	/**
	 * @return Returns the current bounds of the celleditor.
	 */
	public Rectangle getBounds() {
	    if (m_Control!=null) {
	       Rectangle b =   m_Control.getBounds();
	       return b;
	    }
	    return new Rectangle(0,0,0,0);
	}

	/**
	 * Called only by KTable to inform the editor of changes to the displayed cell's rectangle.
	 * KTable must not call setBounds() as this will lead to an infinite redraw loop between
	 * the KTable and the open editor.
	 * 
	 * @param rect The Rectangle of the cell the editor is currently displaying.
	 */
	final void setCellRect(Rectangle rect) {
		// check: did the cell rectangle change? only then do we change the control's bounds
		// otherwise, we will get an infinite redraw loop between the KTable and the open editor
		if (lastProvidedCellRect == null || !lastProvidedCellRect.equals(rect)) {
			setBounds(rect);
			lastProvidedCellRect = rect;
		}
	}

	/*
	 * Creates the editor's control. Has to be overwritten 
	 * by useful editor implementations. 
	 */
	protected abstract Control createControl();
	

	protected void onKeyPressed(KeyEvent e) {
		if ((e.character == '\r')  && ((e.stateMask & SWT.SHIFT) == 0)) 
		{
			close(true);
		}
		else if (e.character == SWT.ESC) {
			close(false);
		}
		else
		{
			m_Table.scrollToFocus();
		}
	}
	
	protected void onTraverse(TraverseEvent e)
	{		
		// set selection to the appropriate next element:
		switch (e.keyCode) {
			case SWT.ARROW_LEFT: 
			{
			    close(true);
			    // select cell to the left; no cell merging can be in the way here, so no special handling is necessary
			    m_Table.setSelection(m_Col-1, m_Row, true);
			    break;
			}
			case SWT.ARROW_RIGHT: {
			    close(true);

			    /*
			     * Select cell to the right; however, the immediately adjacent cell might actually still be a part of
			     * this cell through cell merging. In that case, we will have to keep on couting up the row number until
			     * we find a cell that is not merged.
			     */
			    Point currentCell = new Point(m_Col, m_Row);
			    int newCol = m_Col+1;
			    while(currentCell.equals(m_Table.getValidCell(newCol, m_Row)))
			    	newCol++;

			    m_Table.setSelection(newCol, m_Row, true);
			    break;
			}
			case SWT.ARROW_UP: {
			    close(true);
			    // select cell to the top; no cell merging can be in the way here, so no special handling is necessary
			    m_Table.setSelection(m_Col, m_Row-1, true);
			    break;
			}
			case SWT.ARROW_DOWN: {
			    close(true);

			    /*
			     * Select cell to the right; however, the immediately adjacent cell might actually still be a part of
			     * this cell through cell merging. In that case, we will have to keep on couting up the row number until
			     * we find a cell that is not merged.
			     */
			    Point currentCell = new Point(m_Col, m_Row);
			    int newRow = m_Row+1;
			    while(currentCell.equals(m_Table.getValidCell(m_Col, newRow)))
			    	newRow++;

			    m_Table.setSelection(m_Col, newRow, true);
			    break;
			}
			case SWT.TAB: {
			    close(true);
			    if ((e.stateMask & SWT.SHIFT)!=0)
			        m_Table.setSelection(m_Col-1, m_Row, true);
			    else
			        m_Table.setSelection(m_Col+1, m_Row, true);
			    break;
			}
		}
		m_Table.setFocus();
	}
	/**
	 * @param toolTip
	 */
	public void setToolTipText(String toolTip) {
		this.m_toolTip = toolTip;
	}
	
	/**
	 * Allows that external classes can set the content of 
	 * the underlying 
	 * @param content The new content to set.
	 */
	public abstract void setContent(Object content);
	
	/**
	 * @return Returns a value indicating on which actions 
	 * this editor should be activated.
	 */
	public int getActivationSignals() {
	    return DOUBLECLICK | KEY_ANY;
	}
	
	/**
	 * Is called when an activation is triggered via a mouse click.<p> 
	 * If false is returned, the editor does not get activated.
	 * @param eventType The reason why the <code>KTable</code>  
	 * wants to open the editor. Either: <br>
	 * <code>KTableCellEditor.DOUBLECLICK</code><br>
	 * <code>KTableCellEditor.SINGLECLICK</code><br>
	 * <code>KTableCellEditor.KEY_ANY</code><br>
	 * <code>KTableCellEditor.KEY_RETURN_AND_SPACE</code>
	 * @param table The KTable instance this editor operates on. 
	 * @param col The column index to operate on.
	 * @param row The row index to operate on.
	 * @param clickLocation The point where the mouseclick occured. 
	 * <code>null</code> if not activated when called for a non-mouse event. (keyboard input). 
	 * @param keyInput The character input in case of keyboard event. 
	 * <code>null</code> if activated because of a mouse event. 
	 * @param stateMask The statemask from the causing event.
	 * @return Returns true if the editor activation should happen.
	 */
	public boolean isApplicable(int eventType, KTable table, int col, int row, 
			Point clickLocation, String keyInput, int stateMask) {
	    return true;
	}
	
	public Control getControl() {
	    return m_Control;
	}
}
