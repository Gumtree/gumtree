package org.gumtree.control.core;

import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.exception.SicsException;

public interface IDriveableController extends IDynamicController {

	boolean drive() throws SicsException;
	boolean drive(double target) throws SicsException;
	boolean drive(double target, ISicsCallback callback) throws SicsException;
	boolean commitTargetWithDrive(ISicsCallback callback) throws SicsException;
	void run() throws SicsException;
	void run(double target) throws SicsException;
	void setTarget(double target);
	double getSoftzero();
	double getUpperlim();
	double getLowerlim();
	boolean isFixed();
	double getPrecision();
	
}
