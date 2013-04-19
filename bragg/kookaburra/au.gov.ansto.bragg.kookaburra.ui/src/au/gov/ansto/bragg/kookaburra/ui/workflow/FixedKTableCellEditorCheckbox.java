package au.gov.ansto.bragg.kookaburra.ui.workflow;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellEditor;

public class FixedKTableCellEditorCheckbox extends KTableCellEditor {

    /**
	 * Activates the editor at the given position.
	 * Instantly closes the editor and switch the boolean content value.
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
		
		close(true);
		
		GC gc = new GC(m_Table);
		m_Table.updateCell(m_Col, m_Row);
		gc.dispose();
	}
	
	
	/**
	 * Simply switches the boolean value in the model!
	 */
	public void close(boolean save) {
		// [TONY] 2010-02-05 avoid runtime error as m_Row can be -1 for some reason
		// See GUMTREE-413
		if (m_Row < 0 || m_Col < 0) {
			System.out.println("Bad..." + " m_Row=" + m_Row + ", m_Col=" + m_Col);
		}
//	    if (save) {
		if (save && m_Row >= 0 && m_Col >= 0) {
	        Object o = m_Model.getContentAt(m_Col, m_Row);
	        if (!(o instanceof Boolean))
	            throw new ClassCastException("CheckboxCellEditor needs a Boolean content!");
	        
	        boolean newVal = !((Boolean)o).booleanValue();
	        
	        m_Model.setContentAt(m_Col, m_Row, new Boolean(newVal));
	    }
	    super.close(save);
	}
    
    /**
     * This editor does not have a control, it only switches 
     * the boolean value on activation!
     * @see de.kupzog.ktable.KTableCellEditor#createControl()
     */
    protected Control createControl() {
		return null;
	}

    /**
     * This implementation does nothing!
     * @see de.kupzog.ktable.KTableCellEditor#setContent(java.lang.Object)
     */
    public void setContent(Object content) {
    }

    /**
	 * @return Returns a value indicating on which actions 
	 * this editor should be activated.
	 */
	public int getActivationSignals() {
	    return SINGLECLICK | KEY_RETURN_AND_SPACE;
	}
	
}

