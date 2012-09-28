package org.gumtree.ui.missioncontrol;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IApp {

	public String getLabel();

	public void setLabel(String label);
	
	public Control creatWidget(Composite parent);

}
