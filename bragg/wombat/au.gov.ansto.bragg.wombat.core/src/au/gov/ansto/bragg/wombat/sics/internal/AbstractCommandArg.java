/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package au.gov.ansto.bragg.wombat.sics.internal;

import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.SicsCore;

import au.gov.ansto.bragg.wombat.sics.ICommandArg;

public abstract class AbstractCommandArg implements ICommandArg {

	private String id;
	
	private String commandArgPath;
	
	private int order;
	
	public AbstractCommandArg(String commandArgPath, int order) {
		this.commandArgPath = commandArgPath;
		this.order = order;
	}
	
	public String getId() {
		if (id == null) {
			id = getCommandArgController().getId();
		}
		return id;
	}
	public String getCommandArgPath() {
		return commandArgPath;
	}
	
	public int getOrder() {
		return order;
	}
	
	public IDynamicController getCommandArgController() {
		return (IDynamicController) SicsCore.getSicsController().findComponentController(getCommandArgPath());
	}
	
	
}
