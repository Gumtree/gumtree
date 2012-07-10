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
package org.gumtree.data.nexus;

import org.gumtree.data.interfaces.IDataset;

/**
 * This class represents files in NeXus format.
 * 
 * @author nxi
 * 
 */
public interface INXDataset extends IDataset {

	/**
	 * Return the NXroot object from the NeXus dataset.
	 * 
	 * @return NXroot
	 */
	public abstract INXroot getNXroot();

	public abstract Object clone() throws CloneNotSupportedException;
}
