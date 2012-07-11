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
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsUtils;

import au.gov.ansto.bragg.wombat.sics.ICommandArg;
import au.gov.ansto.bragg.wombat.sics.IUserCommand;

public class UserCommand implements IUserCommand {

	private static final String HDB_PROP_ARGTYPE = "argtype";
	
	private String id;
	
	private String commandPath;
	
	private List<ICommandArg> args;
	
	public UserCommand(String commandPath) {
		this.commandPath = commandPath;
	}

	public String getId() {
		if (id == null) {
			id = getCommandController().getId();
		}
		return id;
	}
	public String getCommandPath() {
		return commandPath;
	}
	
	public ICommandController getCommandController() {
		return (ICommandController) SicsCore.getSicsController().findComponentController(commandPath);
	}
	
	public ICommandArg[] getCommandArgs() {
		if (args == null) {
			args = new ArrayList<ICommandArg>();
			ICommandController controller = getCommandController();
			int order = 0;
			for (IComponentController child : controller.getChildControllers()) {
				if (child instanceof IDynamicController) {
					args.add(createCommandArg((IDynamicController) child, order));
				}
				order++;
			}
		}
		return args.toArray(new ICommandArg[args.size()]);
	}

	private static ICommandArg createCommandArg(IDynamicController controller, int order) {
		String argType = SicsUtils.getPropertyFirstValue(controller.getComponent(), HDB_PROP_ARGTYPE);
		if (argType.equals("int")) {
			return new IntCommandArg(controller.getPath(), order);
		} else if (argType.equals("float")) {
			return new FloatCommandArg(controller.getPath(), order);
		} else if (argType.equals("drivable")) {
			return new DrivableCommandArg(controller.getPath(), order);
		} else {
			// Default: string type arg
			return new StringCommandArg(controller.getPath(), order);
		}
	}
	
}
