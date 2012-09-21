package org.gumtree.widgets.swt;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IControlFactory {

	public Control createControl(Composite parent);
	
}
