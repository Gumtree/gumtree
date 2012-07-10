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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class DrivableCommand extends AbstractSicsCommand {

	private static final String SICS_DRIVE_MULTIPLE_ALLOWED = "sics.drive.multiple.allowed";
	
	private static final String[] methods = {"drive", "run" };
	
	private String method = methods[0];
	
	private List<DrivableParameter> parameters;
	
	public DrivableCommand() {
		parameters = new ArrayList<DrivableParameter>(2);
	}
	
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		String oldValue = this.method;
		this.method = method;
		firePropertyChange("method", oldValue, method);
	}

	public String[] getAvailableMethods() {
		return methods;
	}
	
	public void addDrivableParameter(DrivableParameter parameter) {
		parameters.add(parameter);
	}
	
	public void removeDrivableParameter(DrivableParameter parameter) {
		parameters.remove(parameter);
	}
	
	public DrivableParameter[] getParameters() {
		return parameters.toArray(new DrivableParameter[parameters.size()]);
	}
	
	public String toScript() {
		StringBuilder builder = new StringBuilder();
		builder.append(getMethod());
		builder.append(" ");
		for (DrivableParameter parameter : parameters) {
			builder.append(parameter.getDeviceId());
			builder.append(" ");
			builder.append(parameter.getTarget());
			if (parameters.indexOf(parameter) != parameters.size() - 1) {
				// Append space except for the last item
				builder.append(" ");
			}
		}
		return builder.toString();
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		super.addPropertyChangeListener(listener);
		for (DrivableParameter parameter : parameters)
			parameter.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		super.removePropertyChangeListener(listener);
		for (DrivableParameter parameter : parameters)
			parameter.removePropertyChangeListener(listener);
	}

	public static boolean isDrivingMultipleAllowed() {
		String isAllowed = System.getProperty(SICS_DRIVE_MULTIPLE_ALLOWED);
		if (isAllowed != null && !isAllowed.isEmpty()) {
			try {
				return Boolean.valueOf(isAllowed);
			} catch (Exception e) {
			}
		}
		return true;
	}
}
