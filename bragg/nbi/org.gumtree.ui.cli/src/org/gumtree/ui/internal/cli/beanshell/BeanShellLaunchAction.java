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

package org.gumtree.ui.internal.cli.beanshell;

import org.gumtree.ui.cli.CliUIConstants;
import org.gumtree.ui.util.workbench.ViewLaunchAction;

public class BeanShellLaunchAction extends ViewLaunchAction {

	@Override
	public String getViewId() {
		return CliUIConstants.ID_VIEW_BEANSHELL_CLI;
	}

}
