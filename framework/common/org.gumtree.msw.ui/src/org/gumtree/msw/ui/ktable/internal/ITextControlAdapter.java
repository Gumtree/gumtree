package org.gumtree.msw.ui.ktable.internal;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

public interface ITextControlAdapter {
	// properties
	public Control getControl();
	
	// methods
	public String getText();
	public void setText(String value);
	public void setBackground(Color color);
	// modify listener
	public void addModifyListener(ModifyListener listener);
	public void removeModifyListener(ModifyListener listener);
}
