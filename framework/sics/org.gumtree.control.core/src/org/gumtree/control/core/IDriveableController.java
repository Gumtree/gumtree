package org.gumtree.control.core;

import org.gumtree.control.exception.SicsException;

public interface IDriveableController extends IDynamicController {

	boolean drive() throws SicsException;
	void run() throws SicsException;
	void setTarget(double target);
	double getSoftzero();
	double getUpperlim();
	double getLowerlim();
	boolean isFixed();
	double getPrecision();
	
}
