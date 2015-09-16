package org.gumtree.msw.ui.ktable.internal;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;

public class CComboControlAdapter implements ITextControlAdapter {
	// fields
	private CCombo control;
	
	// construction
	public CComboControlAdapter() {
		control = null;
	}
	
	// properties
	@Override
	public CCombo getControl() {
		return control;
	}
	public void setControl(CCombo control) {
		this.control = control;
	}
	
	// methods
	@Override
	public String getText() {
		return control.getText();
	}
	@Override
	public void setText(String value) {
		control.setText(value);
	}
	@Override
	public void setBackground(Color color) {
		control.setBackground(color);
	}
	// modify listener
	@Override
	public void addModifyListener(ModifyListener listener) {
		control.addModifyListener(listener);
	}
	@Override
	public void removeModifyListener(ModifyListener listener) {
		control.removeModifyListener(listener);
	}
}
