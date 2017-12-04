package org.gumtree.control.core;

import org.gumtree.control.exception.SicsException;

public interface ICommandController extends IGroupController {
	
	void run(ISicsCallback callback) throws SicsException;
	
}
