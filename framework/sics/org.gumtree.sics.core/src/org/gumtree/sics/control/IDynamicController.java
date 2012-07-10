package org.gumtree.sics.control;

import org.gumtree.sics.io.ISicsData;

public interface IDynamicController extends ISicsController {

	public void getCurrentValue(IControllerCallback callback);
	
	public void getTargetValue(IControllerCallback callback);
	
	public void setTargetValue(ISicsData data);
	
	public void commitTargetValue();
	
}
