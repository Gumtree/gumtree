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

package org.gumtree.ui.service.sidebar;

import org.gumtree.core.service.IService;

public interface IGadgetRegistry extends IService {
	
	public void addGadget(IGadget gadget);
	
	public void removeGadget(IGadget gadget);
	
	public IGadget[] getAllGadgets();
	
	public IGadget[] getGadgetsByPerspective(String perspectiveId);
	
}
