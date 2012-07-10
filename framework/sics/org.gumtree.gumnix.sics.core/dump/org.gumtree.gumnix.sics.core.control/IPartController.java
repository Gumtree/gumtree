package org.gumtree.gumnix.sics.control;

import ch.psi.sics.hipadaba.Part;

public interface IPartController extends IComponentController {

	public Part getPart();

	public IPartController[] getChildPartControllers();

	public IDeviceController[] getChildDeviceControllers();

}
