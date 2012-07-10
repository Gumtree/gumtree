package org.gumtree.gumnix.sics.control.controllers;

public interface IScanController extends ICommandController {

	public IScanConfig config();

	public IScanStatus status();

}
