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

package org.gumtree.gumnix.sics.batch.ui.commands;

import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.workflow.ui.models.AbstractModelObject;

public abstract class AbstractSicsCommand extends AbstractModelObject implements ISicsCommandElement {
	
	public AbstractSicsCommand() {
		super();
	}
	
	public float getEstimatedTime(){
		return 0;
	}
	
	public String getEstimationUnits(){
		return "";
	}
	
	public String getPrintable() {
		return toScript();
	}
	
}
