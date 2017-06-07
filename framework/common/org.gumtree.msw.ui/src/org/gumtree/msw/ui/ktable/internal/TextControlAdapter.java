package org.gumtree.msw.ui.ktable.internal;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Text;

public class TextControlAdapter implements ITextControlAdapter {
	// fields
	private Text control;
	
	// construction
	public TextControlAdapter() {
		control = null;
	}
	
	// properties
	@Override
	public Text getControl() {
		return control;
	}
	public void setControl(Text control) {
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
