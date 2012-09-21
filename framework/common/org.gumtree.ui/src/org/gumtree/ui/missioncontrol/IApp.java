package org.gumtree.ui.missioncontrol;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IApp {

	public String getName();

	public Control creatWidget(Composite parent);

}
