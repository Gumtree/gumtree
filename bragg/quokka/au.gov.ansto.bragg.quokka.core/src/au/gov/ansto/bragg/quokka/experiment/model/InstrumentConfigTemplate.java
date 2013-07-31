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

package au.gov.ansto.bragg.quokka.experiment.model;

import java.io.File;

public class InstrumentConfigTemplate extends AbstractModelObject {

	private String name;
	
	private String description = "";
	
	private String initScript = "";
	
	private String preTransmissionScript = "";
	
	private String preScatteringScript = "";
	
	private int startingAttenuation = 330;
	
	// This is used to manage user defined template
	private File file;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		firePropertyChange("name", oldValue, name);
	}
	
	public String getDescription() {
		if (description == null) {
			description = EMPTY_STRING;
		}
		return description;
	}

	public void setDescription(String description) {
		String oldValue = this.description;
		this.description = description;
		firePropertyChange("description", oldValue, description);
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		File oldValue = this.file;
		this.file = file;
		firePropertyChange("file", oldValue, file);
	}

	public boolean isStandard() {
		// Standard template has no file associate to it.
		return getFile() == null;
	}
	
	public String getInitScript() {
		return initScript;
	}

	public void setInitScript(String initScript) {
		String oldValue = this.initScript;
		this.initScript = initScript;
		firePropertyChange("initScript", oldValue, initScript);
	}

	public String getPreTransmissionScript() {
		return preTransmissionScript;
	}

	public void setPreTransmissionScript(String preTransmissionScript) {
		String oldValue = this.preTransmissionScript;
		this.preTransmissionScript = preTransmissionScript;
		firePropertyChange("preTransmissionScript", oldValue, preTransmissionScript);
	}

	public String getPreScatteringScript() {
		return preScatteringScript;
	}

	public void setPreScatteringScript(String preScatteringScript) {
		String oldValue = this.preScatteringScript;
		this.preScatteringScript = preScatteringScript;
		firePropertyChange("preScatteringScript", oldValue, preScatteringScript);
	}

	public int getStartingAttenuation() {
		return startingAttenuation;
	}

	public void setStartingAttenuation(int startingAttenuation) {
		int oldValue = this.startingAttenuation;
		this.startingAttenuation = startingAttenuation;
		firePropertyChange("startingAttenuation", oldValue, startingAttenuation);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InstrumentConfigTemplate [");
		builder.append("description=").append(description);
		builder.append(", file=").append(file);
		builder.append(", initScript=").append(initScript);
		builder.append(", name=").append(name);
		builder.append(", preScatteringScript=").append(preScatteringScript);
		builder.append(", preTransmissionScript=").append(preTransmissionScript);
		builder.append(", startingAttenuation=").append(startingAttenuation);
		builder.append("]");
		return builder.toString();
	}
	
}
