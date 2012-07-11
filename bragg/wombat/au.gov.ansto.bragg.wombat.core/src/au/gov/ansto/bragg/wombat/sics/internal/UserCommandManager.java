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

import java.util.ArrayList;
import java.util.List;

import org.gumtree.gumnix.sics.control.controllers.ICommandController;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.core.SicsCore;

import au.gov.ansto.bragg.wombat.sics.IUserCommand;
import au.gov.ansto.bragg.wombat.sics.IUserCommandManager;


public class UserCommandManager implements IUserCommandManager {

	private static final String PROP_USER_COMMANDS_PATH = "sics.userCommands.path";
	
	private List<IUserCommand> userCommands;

	public UserCommandManager() {
		super();
	}

	public IUserCommand[] getUserCommands() {
		if (userCommands == null) {
			userCommands = new ArrayList<IUserCommand>();
			String path = System.getProperty(PROP_USER_COMMANDS_PATH);
			IComponentController userCommandNode = SicsCore.getSicsController()
					.findComponentController(path);
			if (userCommandNode != null) {
				for (IComponentController child : userCommandNode
						.getChildControllers()) {
					if (child instanceof ICommandController) {
						userCommands.add(new UserCommand(child.getPath()));
					}
				}
			}
		}
		return userCommands.toArray(new IUserCommand[userCommands.size()]);
	}
	
}
