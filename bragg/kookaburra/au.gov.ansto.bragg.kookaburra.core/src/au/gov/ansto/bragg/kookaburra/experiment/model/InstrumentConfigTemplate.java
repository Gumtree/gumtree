package au.gov.ansto.bragg.kookaburra.experiment.model;

import java.io.File;

public class InstrumentConfigTemplate extends AbstractModelObject {

	private String name;
	
	private String description = "";
	
	private String initScript = "";
	
	private String preTransmissionScript = "";
	
	private String preScatteringScript = "";
	
	private int startingAtteunation = 330;
	
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

	public int getStartingAtteunation() {
		return startingAtteunation;
	}

	public void setStartingAtteunation(int startingAtteunation) {
		int oldValue = this.startingAtteunation;
		this.startingAtteunation = startingAtteunation;
		firePropertyChange("startingAtteunation", oldValue, startingAtteunation);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InstrumentConfigTemplate [description=");
		builder.append(description);
		builder.append(", file=");
		builder.append(file);
		builder.append(", initScript=");
		builder.append(initScript);
		builder.append(", name=");
		builder.append(name);
		builder.append(", preScatteringScript=");
		builder.append(preScatteringScript);
		builder.append(", preTransmissionScript=");
		builder.append(preTransmissionScript);
		builder.append(", startingAtteunation=");
		builder.append(startingAtteunation);
		builder.append("]");
		return builder.toString();
	}
	
}
