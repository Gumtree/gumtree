package org.gumtree.control.core;

import java.util.Map;

import org.gumtree.control.exception.SicsException;

public interface ICommandController extends IGroupController {
	
	boolean run(ISicsCallback callback) throws SicsException;
	
	boolean isBusy();
	
	boolean run(Map<String, Object> parameters, ISicsCallback callback) throws SicsException;
}
