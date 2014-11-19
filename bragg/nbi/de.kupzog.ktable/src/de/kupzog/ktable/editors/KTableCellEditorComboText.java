
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


package de.kupzog.ktable.editors;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellEditor;

/**
 * A cell editor with a combo (not read only)
 * 
 * @author Friederich Kupzog
 */
public class KTableCellEditorComboText 
extends KTableCellEditor 
{
	private CCombo m_Combo;
	private String m_Items[];
	private Cursor m_ArrowCursor;

	private KeyAdapter keyListener = new KeyAdapter() {
	        public void keyPressed(KeyEvent e) {
	            try {
	                onKeyPressed(e);
	            } catch (Exception ex) {
	                // Do nothing
	            }
	        }
	    };
	    
    private TraverseListener travListener = new TraverseListener() {
        public void keyTraversed(TraverseEvent e) {
            onTraverse(e);
        }
    };

    // Attention: Before the col/row parameters were mislabeled.
	public void open(KTable table, int col, int row, Rectangle rect) {
		super.open(table, col, row, rect);
		m_Combo.setFocus();
		m_Combo.setText((String)m_Model.getContentAt(m_Col, m_Row));
		m_Combo.setSelection(new Point(0, m_Combo.getText().length()));
		m_Combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				close(true);
			}
		});
	}

	public void close(boolean save) {
		if (save)
			m_Model.setContentAt(m_Col, m_Row, m_Combo.getText());
        m_Combo.removeKeyListener(keyListener);
        m_Combo.removeTraverseListener(travListener);
		super.close(save);
        m_Combo = null;
        m_ArrowCursor.dispose();
	}

	protected Control createControl() {
		m_Combo = new CCombo(m_Table, SWT.NONE);
		m_Combo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		if (m_Items != null)
			m_Combo.setItems(m_Items);
        m_Combo.addKeyListener(keyListener);        
        m_Combo.addTraverseListener(travListener);
        m_ArrowCursor = new Cursor(Display.getDefault(), SWT.CURSOR_ARROW);
		m_Combo.setCursor(m_ArrowCursor);
		return m_Combo;
	}
	
	public void setBounds(Rectangle rect) 
	{
		super.setBounds(new Rectangle(rect.x, rect.y+1,
									  rect.width, rect.height-2));
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
	

	public void setItems(String items[]) {
		m_Items = items;
	}
	
    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableCellEditor#setContent(java.lang.String)
     */
    public void setContent(Object content) {
    	m_Combo.setText(content != null ? content.toString() : "");
    }
}
