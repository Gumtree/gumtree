package org.gumtree.control.ui.viewer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.gumtree.control.core.ICommandController;
import org.gumtree.control.exception.SicsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunDialogCellEditor extends DialogCellEditor {
	
	private static Logger logger = LoggerFactory.getLogger(RunDialogCellEditor.class);
	
	private ICommandController controller;
	
	public RunDialogCellEditor(Composite parent, String text) {
		super(parent);
	}

	public void setCommandController(ICommandController controller) {
		this.controller = controller;
	}
	
	protected Object openDialogBox(Control cellEditorWindow) {
		if (controller != null) {
			try {
				controller.run(null);
			} catch (SicsException e) {
				MessageDialog.openError(getControl().getShell(), "Error", "An error has occurred on starting a SICS command");
				logger.error("An error has occuried on starting a SICS command", e);
			}
		}
		deactivate();
		return null;
	}
	
	protected Control createContents(Composite cell) {
		Label label = new Label(cell, SWT.RIGHT);
		label.setFont(cell.getFont());
		label.setBackground(cell.getBackground());
		label.setText("Run --> ");
		return label;
	}
	 
}
