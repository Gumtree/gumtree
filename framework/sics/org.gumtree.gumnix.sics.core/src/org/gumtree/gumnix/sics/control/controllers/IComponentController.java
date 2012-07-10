/*******************************************************************************
 * Copyright (c) 2006 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.gumnix.sics.control.controllers;


import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.events.IComponentControllerListener;

import ch.psi.sics.hipadaba.Component;

/**
 * Component controller is a wrapper to the instrument component from SICS.
 * Controller allows client side to store status and extra logic to interpret
 * and control the corresponding SICS objects.
 *
 * @since 1.0
 */
public interface IComponentController {

	/**
	 * Returns the current status of this component.
	 *
	 * @return component status
	 */
	public ControllerStatus getStatus();

	/**
	 * Returns the original component which is wrapped by this controller.
	 *
	 * @return the component associated with this controller
	 */
	public Component getComponent();

	/**
	 * Returns the parent controller of this controller.
	 *
	 * @return the parent controller
	 */
//	public IComponentController getParentController();

	/**
	 * Returns the child controllers of this controller.
	 *
	 * @return a set of child controllers
	 */
	public IComponentController[] getChildControllers();

	/**
	 * Returns an offspring controller under this node, based on the relative path.
	 * 
	 * @param relativePath
	 * @return
	 */
	public IComponentController getChildController(String relativePath);

	/**
	 * Manually force to update status. Children of this node may call this to
	 * update the status.
	 * 
	 */
	public void refreshStatus();
	
	/**
	 * Returns the id of this component.
	 * 
	 * @return component id
	 */
	public String getId();
	
	/**
	 * Returns the path of the wrapped component.
	 *
	 * @return the path string for the component
	 */
	public String getPath();
	
	/**
	 * Returns the device id, if available, of this controller.
	 * 
	 * @return the device id; or null if it does not exist
	 */
	public String getDeviceId();
	
	/**
	 * @param listener
	 */
	public void addComponentListener(IComponentControllerListener listener);

	/**
	 * @param listener
	 */
	public void removeComponentListener(IComponentControllerListener listener);

}
