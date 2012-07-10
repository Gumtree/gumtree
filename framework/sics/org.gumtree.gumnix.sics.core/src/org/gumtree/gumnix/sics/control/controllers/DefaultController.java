package org.gumtree.gumnix.sics.control.controllers;

import ch.psi.sics.hipadaba.Component;

public class DefaultController extends ComponentController {

	public DefaultController(Component component) {
		super(component);
	}

	public void preInitialise() {
	}

	public void postInitialise() {
	}
	
	public void activate() {
	}
	
	public String toString() {
		return "[DefaultController] : " + getPath();
	}
}
