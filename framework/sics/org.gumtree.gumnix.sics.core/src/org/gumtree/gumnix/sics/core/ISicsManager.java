/*******************************************************************************
 * Copyright (c) 2006 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.gumnix.sics.core;

import org.gumtree.core.service.IService;
import org.gumtree.gumnix.sics.control.ISicsControl;
import org.gumtree.gumnix.sics.control.ISicsMonitor;
import org.gumtree.gumnix.sics.io.ISicsProxy;

public interface ISicsManager extends IService {

//	public static ISicsManager INSTANCE = SicsManager.getDefault();

	public ISicsControl control();

	public ISicsProxy proxy();

	public ISicsCoreService service();
	
	public ISicsMonitor monitor();

}
