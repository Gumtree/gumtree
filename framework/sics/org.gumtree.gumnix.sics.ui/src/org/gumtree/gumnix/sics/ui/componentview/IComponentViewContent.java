package org.gumtree.gumnix.sics.ui.componentview;

import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;

public interface IComponentViewContent {

	public void createPartControl(Composite parent, IComponentController controller);

	public void dispose();

	public IComponentController getController();

}
