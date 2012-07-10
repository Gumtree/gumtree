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


import java.util.List;

import org.gumtree.data.interfaces.IGroup;

/**
 * This class represent the Group objects in the NeXus format.
 * 
 * @author nxi
 * 
 */
public interface INXGroup extends IGroup {

	public List<INXDataItem> getNXDataItemList();
	
	public List<INXGroup> getNXGroupList();
	
	/**
	 * Get the title as a string. Usually it is an attribute of the group, with the name as 'title'
	 * @return String object
	 */
	public String getTitle();
	
	/**
	 * Set the title as an attribute of the group, with the name as 'title'
	 * @param title: String object
	 */
	public void setTitle(String title);

}
