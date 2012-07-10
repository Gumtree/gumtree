package org.gumtree.gumnix.sics.control.controllers;

import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;

public interface IDrivableController extends IDynamicController {

	public void drive(float value) throws SicsIOException, SicsExecutionException;
	
}
