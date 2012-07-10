package org.gumtree.sics.control;

import org.gumtree.sics.io.ISicsData;

public interface IControllerCallback {

	public void getCurrentValue(ISicsData data);
	
	public void getTargetValue(ISicsData data);
	
}
