package org.gumtree.gumnix.sics.control.events;

import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;

public interface IDynamicControllerCallback {

	public void handleGetValueCallback(IDynamicController controller, IComponentData value);

	public void handleOperationCompleted(IDynamicController controller);
	
	public void handleOperationError(IDynamicController controller, String errorMessage);

}
