/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package au.gov.ansto.bragg.nbi.ui.core.commands;


/**
 * @author nxi
 *
 */
public class DndTransferData {

	private Object parent;
	private Object child;
	/**
	 * @return the parent
	 */
	public Object getParent() {
		return parent;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParent(Object parent) {
		this.parent = parent;
	}
	/**
	 * @return the child
	 */
	public Object getChild() {
		return child;
	}
	/**
	 * @param child the child to set
	 */
	public void setChild(Object child) {
		this.child = child;
	}

	
}
