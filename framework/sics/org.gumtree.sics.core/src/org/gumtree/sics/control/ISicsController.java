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

package org.gumtree.sics.control;

import org.gumtree.core.object.IDisposable;
import org.gumtree.sics.io.ISicsProxy;

import ch.psi.sics.hipadaba.Component;

public interface ISicsController extends IDisposable {

	public static final String EVENT_TOPIC_STATUS_CHANGE = "org/gumtree/cs/sics/controller/status";
	
	public static final String EVENT_PROP_TIMESTAMP = "timestamp";
	
	public static final String EVENT_PROP_STATUS = "status";
	
	public static final String EVENT_PROP_CONTROLLER = "controller";
	
	/*************************************************************************
	 * Attributes
	 *************************************************************************/
	
	public String getId();
	
	public void setId(String id);
	
	public String getDeviceId();
	
	public void setDeviceId(String deviceId);
	
	public String getPath();
	
	public void setPath(String path);
		
	public Component getComponentModel();	

	public void setComponentModel(Component componentModel);
	
	public ControllerStatus getStatus();
	
	/*************************************************************************
	 * Components
	 *************************************************************************/
	
	public ISicsProxy getProxy();
	
	public void setProxy(ISicsProxy proxy);
	
	/*************************************************************************
	 * Structures
	 *************************************************************************/
	
	public ISicsController getParent();
	
	public void setParent(ISicsController parent);
	
	public ISicsController getChild(String childControllerId);
	
	public ISicsController[] getChildren();

	public void addChild(ISicsController child);
	
	public void removeChild(ISicsController child);
	
	public ISicsController findChild(String relativePath);
	
	public ISicsController findChildByDeviceId(String deviceId);
	
}
