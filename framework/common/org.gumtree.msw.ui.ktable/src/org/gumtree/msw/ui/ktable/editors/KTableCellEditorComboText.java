
/*******************************************************************************
 * Copyright (C) 2004 by Friederich Kupzog Elektronik & Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Friederich Kupzog - initial API and implementation
 *    	fkmk@kupzog.de
 *		www.kupzog.de/fkmk
 *******************************************************************************/ 


package org.gumtree.msw.ui.ktable.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellEditor;


/**
 * A cell editor with a combo (not read only)
 * 
 * @author Friederich Kupzog
 */
public class KTableCellEditorComboText extends KTableCellEditor {
	private CCombo m_Combo;
	private String m_Items[];
	private Font font;
	private int visibleItemCount = -1;
	
	// construction
	public KTableCellEditorComboText() {
	}
	public KTableCellEditorComboText(String items[]) {
		m_Items = items;
	}

	private KeyAdapter keyListener = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			try {
				onKeyPressed(e);
			} catch (Exception ex) {
                ex.printStackTrace();
				// Do nothing
			}
		}
	};

	private TraverseListener travListener = new TraverseListener() {
		public void keyTraversed(TraverseEvent e) {
			onTraverse(e);
		}
	};


	public int getVisibleItemCount() {
		if (m_Combo != null)
			return m_Combo.getVisibleItemCount();
		else
			return visibleItemCount;
	}
	public void setVisibleItemCount (int count) {
		visibleItemCount = count;
		if (m_Combo != null)
			m_Combo.setVisibleItemCount(count);
	}

	public void setItems(String items[]) {
		m_Items = items;
	}
	public void setContent(Object content) {
		if (content instanceof Integer) 
			m_Combo.select(((Integer)content).intValue());
		else {
			String str = content.toString();
			m_Combo.setText(str);
			m_Combo.setSelection(new Point(str.length(), str.length()));
		}
	}
	public void setBounds(Rectangle rect) {
		super.setBounds(new Rectangle(
				rect.x, rect.y+1,
				rect.width, rect.height-2));
	}
	public void open(KTable table, int col, int row, Rectangle rect) {
		super.open(table, col, row, rect);
		
		String content = m_Model.getContentAt(m_Col, m_Row).toString();
		m_Combo.setText(content);
		m_Combo.setSelection(new Point(0, content.length()));
		m_Combo.setFocus();
	}
	public void close(boolean save) {
		if (save)
			m_Model.setContentAt(m_Col, m_Row, m_Combo.getText());
		
		m_Combo.removeKeyListener(keyListener);
		m_Combo.removeTraverseListener(travListener);
		m_Combo = null;
		
		super.close(save);
	}

	public void setFont(Font font) {
		this.font = font;
	}
	
	protected Control createControl() {
		m_Combo = new CCombo(m_Table, SWT.NONE);
		m_Combo.setFont(font);
		m_Combo.addKeyListener(keyListener);        
		m_Combo.addTraverseListener(travListener);
		
		if (visibleItemCount != -1)
			m_Combo.setVisibleItemCount(visibleItemCount);
		
		if (m_Items != null) {
			m_Combo.setItems(m_Items);
			if (visibleItemCount == -1)
				m_Combo.setVisibleItemCount(m_Items.length - 1);
		}
		
		return m_Combo;
	}

	/**
	 * Overwrite the onTraverse method to ignore arrowup and arrowdown
	 * events so that they get interpreted by the editor control.<p>
	 * Comment that out if you want the up and down keys move the editor.<br>
	 * Hint by David Sciamma.
	 */
	protected void onTraverse(TraverseEvent e)
	{
		// set selection to the appropriate next element:
		switch (e.keyCode)
		{
		case SWT.ARROW_UP: // Go to previous item
		case SWT.ARROW_DOWN: // Go to next item
		{
			// Just don't treat the event
			break;
		}
		default: {
			super.onTraverse(e);
			break;
		}
		}
	}
}
