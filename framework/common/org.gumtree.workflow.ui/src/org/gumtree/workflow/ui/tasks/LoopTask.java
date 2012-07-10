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

package org.gumtree.workflow.ui.tasks;

import org.gumtree.util.collection.IParameters;
import org.gumtree.util.collection.Parameters;
import org.gumtree.workflow.ui.AbstractMarkerTask;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.MarkerType;
import org.gumtree.workflow.ui.WorkflowException;
import org.gumtree.workflow.ui.views.ParametersBasedTaskView;

public class LoopTask extends AbstractMarkerTask {

	private static final String PROP_LOOP = "loop";
	
	private int counter;
	
	public MarkerType getMarkerType() {
		return MarkerType.END;
	}
	
	public void setMarkerType(MarkerType type) {
	}
	
	@Override
	protected Object createModelInstance() {
		IParameters paramters = new Parameters();
		paramters.put(PROP_LOOP, 0);
		return paramters;
	}

	@Override
	protected ITaskView createViewInstance() {
		ParametersBasedTaskView view = new ParametersBasedTaskView(getDataModel());
		view.setLabel(PROP_LOOP, "Loop for");
		view.setUnit(PROP_LOOP, "times");
		return view;
	}

	public IParameters getDataModel() {
		return (IParameters) super.getDataModel();
	}
	
	@Override
	protected Object run(Object input) throws WorkflowException {
		counter++;
		if (counter > getDataModel().get(PROP_LOOP, Integer.class, 0)) {
			setRepeatLevel(false);
			counter = 0;
		} else {
			setRepeatLevel(true);
		}
		return input;
	}
	
}
