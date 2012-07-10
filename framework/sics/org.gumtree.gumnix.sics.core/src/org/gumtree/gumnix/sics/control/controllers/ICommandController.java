package org.gumtree.gumnix.sics.control.controllers;

import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;

public interface ICommandController extends IComponentController {
	
	public void asyncExecute() throws SicsIOException;

	// If this controller does not have child controller
	// /feedback/status, it will work as asyncExecute()
	public void syncExecute() throws SicsIOException, SicsExecutionException;

	// Can be null if child node /feedback/status does not exist 
	public IDynamicController getStatusController();

	// Can be UNKNONW if child node /feedback/status does not exist
	public CommandStatus getCommandStatus() throws SicsIOException;
	
	// Indicate whether the status has been changed since execution
	// This dirty flag is reset everytime asyncExecute() or syncExecute() is called
	public boolean getStatusDirtyFlag();
}
