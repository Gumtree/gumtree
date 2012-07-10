package org.gumtree.gumnix.sics.control.events;

import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;

public abstract class DynamicControllerListenerAdapter extends ComponentControllerListenerAdapter implements IDynamicControllerListener {

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.control.controller.IDynamicControllerListener#targetChanged(org.gumtree.gumnix.sics.control.controller.IDynamicController, org.gumtree.gumnix.sics.control.controller.IComponentData)
	 */
	public void targetChanged(IDynamicController controller, IComponentData newTarget) {
	}

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.control.controller.IDynamicControllerListener#valueChanged(org.gumtree.gumnix.sics.control.controller.IDynamicController, org.gumtree.gumnix.sics.control.controller.IComponentData)
	 */
	public void valueChanged(IDynamicController controller, IComponentData newValue) {
	}

}
