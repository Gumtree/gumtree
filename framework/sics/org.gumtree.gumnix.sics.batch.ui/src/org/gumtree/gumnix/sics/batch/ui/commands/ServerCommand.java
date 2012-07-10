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

package org.gumtree.gumnix.sics.batch.ui.commands;

import org.gumtree.gumnix.sics.batch.ui.definition.ISicsCommand;
import org.gumtree.gumnix.sics.batch.ui.util.SicsCommandFactory;

public class ServerCommand extends DynamicCommand {

	public ISicsCommand[] generateAvailableCommands() {
		return SicsCommandFactory.createServerCommands();
	}

}
