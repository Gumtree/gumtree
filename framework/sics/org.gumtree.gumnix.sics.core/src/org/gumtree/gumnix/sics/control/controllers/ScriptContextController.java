package org.gumtree.gumnix.sics.control.controllers;

import ch.psi.sics.hipadaba.Component;

public class ScriptContextController extends ComponentController {

	public ScriptContextController(Component component) {
		super(component);
	}

	public void preInitialise() {
	}

	public void postInitialise() {
	}
	
	public void activate() {
	}

	public String toString() {
		return "[ScriptContextController] : " + getPath();
	}
	
}
