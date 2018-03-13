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

package org.gumtree.control.ui.batch.command;

import org.gumtree.control.batch.tasks.ISicsCommand;
import org.gumtree.workflow.ui.models.AbstractModelObject;

public abstract class SicsCommand extends AbstractModelObject implements ISicsCommand {

	private String name;
	
	private String description = "";
	
	public SicsCommand(String name) {
		super();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof SicsCommand) {
			return getName().equals(((SicsCommand) obj).getName());
		}
		return false;
	}
	
	public int hashCode() {
		return getName().hashCode();
	}
	 
}
