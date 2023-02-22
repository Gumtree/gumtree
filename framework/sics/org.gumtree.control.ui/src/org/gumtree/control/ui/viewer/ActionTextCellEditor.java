/**
 * 
 */
package org.gumtree.control.ui.viewer;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * @author nxi
 *
 */
public class ActionTextCellEditor extends TextCellEditor {

	/**
	 * 
	 */
	public ActionTextCellEditor() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param parent
	 */
	public ActionTextCellEditor(Composite parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param parent
	 * @param style
	 */
	public ActionTextCellEditor(Composite parent, int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void keyReleaseOccured(KeyEvent keyEvent) {
		if (keyEvent.character == '\u001b') { // Escape character
			fireCancelEditor();
		} else if (keyEvent.character == '\r') { // Return key
			markDirty();
			fireApplyEditorValue();
			deactivate();
		}
	}
}
