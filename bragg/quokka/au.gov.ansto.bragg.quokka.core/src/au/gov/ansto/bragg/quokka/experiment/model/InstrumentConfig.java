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

public class InstrumentConfig extends AbstractModelObject {
		
	private String name;
	private String group = "";
	private String description = "";
	
	private ScanMode mode = ScanMode.TIME;

	private long defaultSetting = 240;
	
	private String initScript = "";
	
	private String preTransmissionScript = "";
	
	private String preScatteringScript = "";
	
	// TODO: set to false after the new algorithm is tested
	private boolean useManualAttenuationAlgorithm = true;
	
	private int startingAttenuation = 330;
	
	private ScanMode transmissionMode = ScanMode.TIME;
	
	private long transmissionPreset = 60;
		
	private InstrumentConfigTemplate template;
	
	// [[GT-207] The following 3 attributes relate to the file association propagation
	private String emptyCellTransmissionDataFile;
	
	private String emptyCellScatteringDataFile;
	
	private String emptyBeamTransmissionDataFile;
	
	public InstrumentConfig() {
		super();
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		firePropertyChange("name", oldValue, name);
	}

	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		String oldValue = this.group;
		this.group = group;
		firePropertyChange("group", oldValue, group);
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		String oldValue = this.description;
		this.description = description;
		firePropertyChange("description", oldValue, description);
	}
    
	public ScanMode getMode() {
		return mode;
	}

	public void setMode(ScanMode mode) {
		ScanMode oldValue = this.mode;
		this.mode = mode;
		firePropertyChange("mode", oldValue, mode);
	}

	public long getDefaultSetting() {
		return defaultSetting;
	}

	public void setDefaultSetting(long defaultSetting) {
		long oldValue = this.defaultSetting;
		this.defaultSetting = defaultSetting;
		firePropertyChange("defaultSetting", oldValue, defaultSetting);
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

	public boolean isUseManualAttenuationAlgorithm() {
		return useManualAttenuationAlgorithm;
	}

	public void setUseManualAttenuationAlgorithm(
			boolean useManualAttenuationAlgorithm) {
		boolean oldValue = this.useManualAttenuationAlgorithm;
		this.useManualAttenuationAlgorithm = useManualAttenuationAlgorithm;
		firePropertyChange("useManualAttenuationAlgorithm", oldValue, useManualAttenuationAlgorithm);
	}

	public int getStartingAttenuation() {
		return startingAttenuation;
	}

	public void setStartingAttenuation(int startingAttenuation) {
		int oldValue = this.startingAttenuation;
		this.startingAttenuation = startingAttenuation;
		firePropertyChange("startingAttenuation", oldValue, startingAttenuation);
	}

	public InstrumentConfigTemplate getTemplate() {
		return template;
	}

	public void setTemplate(InstrumentConfigTemplate template) {
		InstrumentConfigTemplate oldValue = this.template;
		this.template = template;
		firePropertyChange("template", oldValue, template);
		setName(template.getName());
		
		restoreScripts();
	}
	
	public ScanMode getTransmissionMode() {
		return transmissionMode;
	}

	public void setTransmissionMode(ScanMode transmissionMode) {
		ScanMode oldValue = this.transmissionMode;
		this.transmissionMode = transmissionMode;
		firePropertyChange("transmissionMode", oldValue, transmissionMode);
	}

	public long getTransmissionPreset() {
		return transmissionPreset;
	}

	public void setTransmissionPreset(long transmissionPreset) {
		long oldValue = this.transmissionPreset;
		this.transmissionPreset = transmissionPreset;
		firePropertyChange("transmissionPreset", oldValue, transmissionPreset);
	}

	public void restoreScripts() {
		setDescription(getTemplate().getDescription());
		setInitScript(getTemplate().getInitScript());
		setPreTransmissionScript(getTemplate().getPreTransmissionScript());
		setPreScatteringScript(getTemplate().getPreScatteringScript());
		setStartingAttenuation(getTemplate().getStartingAttenuation());
	}
	
	public String getEmptyCellTransmissionDataFile() {
		return emptyCellTransmissionDataFile;
	}
	

	public void setEmptyCellTransmissionDataFile(
			String emptyCellTransmissionDataFile) {
		String oldValue = this.emptyCellTransmissionDataFile;
		this.emptyCellTransmissionDataFile = emptyCellTransmissionDataFile;
		firePropertyChange("emptyCellTransmissionDataFile", oldValue, emptyCellTransmissionDataFile);
	}
	
	

	public String getEmptyCellScatteringDataFile() {
		return emptyCellScatteringDataFile;
	}
	

	public void setEmptyCellScatteringDataFile(String emptyCellScatteringDataFile) {
		String oldValue = this.emptyCellScatteringDataFile;
		this.emptyCellScatteringDataFile = emptyCellScatteringDataFile;
		firePropertyChange("emptyCellScatteringDataFile", oldValue, emptyCellScatteringDataFile);
	}
	

	public String getEmptyBeamTransmissionDataFile() {
		return emptyBeamTransmissionDataFile;
	}
	

	public void setEmptyBeamTransmissionDataFile(
			String emptyBeamTransmissionDataFile) {
		String oldValue = this.emptyBeamTransmissionDataFile;
		this.emptyBeamTransmissionDataFile = emptyBeamTransmissionDataFile;
		firePropertyChange("emptyBeamTransmissionDataFile", oldValue, emptyBeamTransmissionDataFile);
	}

	@Override
	public String toString() {
		return "InstrumentConfig [defaultSetting=" + defaultSetting
				+ ", emptyBeamTransmissionDataFile="
				+ emptyBeamTransmissionDataFile
				+ ", emptyCellScatteringDataFile="
				+ emptyCellScatteringDataFile
				+ ", emptyCellTransmissionDataFile="
				+ emptyCellTransmissionDataFile + ", initScript=" + initScript
				+ ", mode=" + mode + ", name=" + name + ", group=" + group
				+ ", preScatteringScript=" + preScatteringScript
				+ ", preTransmissionScript=" + preTransmissionScript
				+ ", startingAttenuation=" + startingAttenuation
				+ ", template=" + template + ", transmissionMode="
				+ transmissionMode + ", transmissionPreset="
				+ transmissionPreset + ", useManualAttenuationAlgorithm="
				+ useManualAttenuationAlgorithm + "]";
	}

	public InstrumentConfigTemplate generateTemplate() {
		InstrumentConfigTemplate result = new InstrumentConfigTemplate();

		result.setName(name);
		result.setDescription(description);
		result.setInitScript(initScript);
		result.setPreTransmissionScript(preTransmissionScript);
		result.setPreScatteringScript(preScatteringScript);
		result.setStartingAttenuation(startingAttenuation);
		
		return result;
	}
}
