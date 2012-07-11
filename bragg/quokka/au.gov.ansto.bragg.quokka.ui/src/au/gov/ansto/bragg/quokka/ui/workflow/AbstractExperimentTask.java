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

package au.gov.ansto.bragg.quokka.ui.workflow;

import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.WorkflowException;

import au.gov.ansto.bragg.quokka.experiment.model.Experiment;

/**
 * Abstract experiment task provides empty implemention to some workflow task
 * methods. Quokka experiment workflow tests should extend this class.
 * 
 */
public abstract class AbstractExperimentTask extends AbstractTask {

	@Override
	protected Object createModelInstance() {
		// Quokka workflow tasks do not use individual model instance.
		// It uses the global experiment model instead.
		return null;
	}

	@Override
	public Object run(Object object) throws WorkflowException {
		// Do nothing. Subclass should override this to add run action.
		return null;
	}

	protected Experiment getExperiment() {
		return getContext().getSingleValue(Experiment.class);
	}
	
	public Class<?>[] getInputTypes() {
		return null;
	}
	
	public Class<?>[] getOutputTypes() {
		return null;
	}

}
