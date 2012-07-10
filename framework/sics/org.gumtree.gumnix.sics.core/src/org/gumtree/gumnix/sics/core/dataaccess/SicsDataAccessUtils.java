/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.gumnix.sics.core.dataaccess;

import java.net.URI;

import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;

public final class SicsDataAccessUtils {

	public static URI createControllerURI(IComponentController controller) {
		return URI.create("sics://hdb" + controller.getPath());
	}

	/*************************************************************************
	 * Special URIs
	 *************************************************************************/
	public static URI createControllerStatusURI(IComponentController controller) {
		return URI.create("sics://hdb" + controller.getPath() + "?status");
	}
	
	public static URI createControllerStatusURI(URI controllerURI) {
		return URI.create(controllerURI.toString() + "?status");
	}
	
	public static URI createControllerTargetURI(IDynamicController controller) {
		return URI.create("sics://hdb" + controller.getPath() + "?target");
	}
	
	public static URI createControllerTargetURI(URI controllerURI) {
		return URI.create(controllerURI.toString() + "?target");
	}
	
	public static URI createControllerIdURI(IComponentController controller) {
		return URI.create("sics://hdb" + controller.getPath() + "?ID");
	}
	
	public static URI createControllerIdURI(URI controllerURI) {
		return URI.create(controllerURI.toString() + "?ID");
	}
	
	public static URI createControllerLabelURI(IComponentController controller) {
		return URI.create("sics://hdb" + controller.getPath() + "?label");
	}
	
	public static URI createControllerLabelURI(URI controllerURI) {
		return URI.create(controllerURI.toString() + "?label");
	}
	
	/*************************************************************************
	 * Model attribute related URIs
	 *************************************************************************/
	public static URI createControllerAttributeURI(IComponentController controller, String attribute) {
		return URI.create("sics://hdb" + controller.getPath() + "?" + attribute);
	}
	
	public static URI createControllerAttributeURI(URI controllerURI, String attribute) {
		return URI.create(controllerURI.toString() + "?" + attribute);
	}
	
	public static URI createControllerDeviceIdURI(IComponentController controller) {
		return createControllerAttributeURI(controller, "sicsdev");
	}
	
	public static URI createControllerDeviceIdURI(URI controllerURI, String attribute) {
		return URI.create(controllerURI.toString() + "?" + "sicsdev");
	}
	
	public static URI createControllerValuesURI(IComponentController controller) {
		return createControllerAttributeURI(controller, "values");
	}
	
	public static URI createControllerValuesURI(URI controllerURI, String attribute) {
		return URI.create(controllerURI.toString() + "?" + "values");
	}
	
	private SicsDataAccessUtils() {
		super();
	}
	
}
