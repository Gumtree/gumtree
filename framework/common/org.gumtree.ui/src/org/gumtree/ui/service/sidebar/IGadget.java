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

import org.eclipse.swt.widgets.Composite;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.util.collection.IParameters;

public interface IGadget {
	
	public String getName();
	
	public void setName(String name);
	
	public String getPerspectives();
	
	public void setPerspectives(String perspectives);

	public int getLevel();
	
	public void setLevel(int level);
	
	public IParameters getParameters();
	
	public void setParameters(IParameters parameters);
	
	/**
	 * @param parent
	 * @throws ObjectCreateException
	 */
	public Composite createGadget(Composite parent);

}
