package org.gumtree.control.core;

import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsModelException;

public interface IDynamicController extends ISicsController {

	Object getValue() throws SicsModelException;
	
	IControllerData getControllerDataValue() throws SicsModelException;

	void updateModelValue(String value) throws SicsModelException;
	
	void setTargetValue(Object value);

	IControllerData getTargetValue();
	
	boolean commitTargetValue() throws SicsException;
	
	void setValue(Object value) throws SicsException;
	
	boolean isBusy();
	
	String getUnits();
	
	void refreshValue() throws SicsException;
	
	void asyncCommitTarget() throws SicsException;
}
