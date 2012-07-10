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

package org.gumtree.workflow.ui;

public abstract class AbstractMarkerTask extends AbstractTask {
	
	private static final String PROP_MARKER_TYPE = "markerType";
	
	// Assign by workflow at runtime
	public int level;
	
	// Assign by subclass at runtime
	public boolean repeatLevel;

	public MarkerType getMarkerType() {
		return getParameters().get(PROP_MARKER_TYPE, MarkerType.class, MarkerType.START);
	}
	
	public void setMarkerType(MarkerType type) {
		getParameters().put(PROP_MARKER_TYPE, type);
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public boolean isRepeatLevel() {
		return repeatLevel;
	}

	public void setRepeatLevel(boolean repeatLevel) {
		this.repeatLevel = repeatLevel;
	}
	
	public Class<?>[] getInputTypes() {
		return new Class[] { Object.class };
	}

	public Class<?>[] getOutputTypes() {
		return new Class[] { Object.class };
	}
	
	public void clearTask() {
		repeatLevel = false;
		super.clearTask();
	}

}
