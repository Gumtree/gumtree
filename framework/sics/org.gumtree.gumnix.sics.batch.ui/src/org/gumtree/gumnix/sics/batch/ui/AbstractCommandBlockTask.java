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

package org.gumtree.gumnix.sics.batch.ui;

import org.gumtree.gumnix.sics.batch.ui.model.ISicsBatchScript;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandBlock;
import org.gumtree.gumnix.sics.batch.ui.model.SicsBatchScript;
import org.gumtree.gumnix.sics.batch.ui.model.SicsCommandBlock;
import org.gumtree.workflow.ui.AbstractTask;

public abstract class AbstractCommandBlockTask extends AbstractTask {

	protected Object createModelInstance() {
		return new SicsCommandBlock();
	}
	
	public void initialise() {
		// Find shared batch script object from context
		ISicsBatchScript batchScript = sicsBatchScript();
		if (batchScript == null) {
			batchScript = new SicsBatchScript();
			// Inject one into the context if it is missing globally
			getContext().put(batchScript, true);
		}
		batchScript.addCommandBlock(getDataModel());
	}
	
	public ISicsCommandBlock getDataModel() {
		return (ISicsCommandBlock) super.getDataModel();
	}
	
	public ISicsBatchScript sicsBatchScript() {
		return getContext().getSingleValue(ISicsBatchScript.class);
	}
	
	protected Object run(Object object) {
		// Optional behaviour: when it run, it passes the script object to the next task
		return sicsBatchScript();
	}
	
	public Class<?>[] getOutputTypes() {
		return new Class[] { ISicsBatchScript.class };
	}
	
}
