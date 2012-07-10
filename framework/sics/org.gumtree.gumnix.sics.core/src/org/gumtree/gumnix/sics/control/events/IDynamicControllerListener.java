package org.gumtree.gumnix.sics.control.events;

import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;

public interface IDynamicControllerListener extends IComponentControllerListener {

	public void targetChanged(IDynamicController controller, IComponentData newTarget);

	public void valueChanged(IDynamicController controller, IComponentData newValue);

}
