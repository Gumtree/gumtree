package org.gumtree.control.core;

import org.gumtree.control.exception.SicsException;

public interface ICommandController extends IGroupController {
	
	boolean run(ISicsCallback callback) throws SicsException;
	
	boolean isBusy();
}
