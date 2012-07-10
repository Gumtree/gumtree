package org.gumtree.gumnix.sics.control;

import ch.psi.sics.hipadaba.Instrument;

public interface IInstrumentController extends IComponentController {

	public Instrument getInstrument();

	public IPartController[] getChildPartControllers();

	public IDeviceController[] getChildDeviceControllers();

}
