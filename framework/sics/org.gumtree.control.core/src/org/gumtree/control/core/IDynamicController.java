package org.gumtree.control.core;

import org.gumtree.control.exception.SicsModelException;

public interface IDynamicController extends ISicsController {

	public IControllerData getValue() throws SicsModelException;

	public void updateValue(String value);
	
	public void setTargetValue(IControllerData value);
	
	public IControllerData getTargetValue();
	
}
