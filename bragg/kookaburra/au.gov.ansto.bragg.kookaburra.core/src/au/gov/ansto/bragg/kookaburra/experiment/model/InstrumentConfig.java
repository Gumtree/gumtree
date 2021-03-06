package au.gov.ansto.bragg.kookaburra.experiment.model;

import java.util.ArrayList;
import java.util.List;

public class InstrumentConfig extends AbstractModelObject {

	public static List<String> modeList;
	
	static {
		modeList = new ArrayList<String>();
		modeList.add("Counter");
		modeList.add("Timer");
	}
	
	private String name;
	
	private String mode = "Timer";

	private long defaultSetting = 240;
	
	private String initScript = "";
	
	private String preTransmissionScript = "";
	
	private String preScatteringScript = "";
	
	// TODO: set to false after the new algorithm is tested
	private boolean useManualAttenuationAlgorithm = true;
	
	private int startingAttenuation = 330;
	
	private String transmissionMode = "Timer";
	
	private long transmissionPreset = 60;
	
	private List<String> availableModes;
	
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

	public List<String> getAvailableModes() {
		if (availableModes == null) {
			availableModes = modeList;
		}
        return availableModes;
    }

    public void setAvailableModes(List<String> availableModes) {
    	List<String> oldValue = this.availableModes;
        this.availableModes = availableModes;
        firePropertyChange("availableModes", oldValue, availableModes);
    }
    
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		String oldValue = this.mode;
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
	
	public String getTransmissionMode() {
		return transmissionMode;
	}

	public void setTransmissionMode(String transmissionMode) {
		String oldValue = this.transmissionMode;
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
		setInitScript(getTemplate().getInitScript());
		setPreTransmissionScript(getTemplate().getPreTransmissionScript());
		setPreScatteringScript(getTemplate().getPreScatteringScript());
		setStartingAttenuation(getTemplate().getStartingAtteunation());
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
		return "InstrumentConfig [availableModes=" + availableModes
				+ ", defaultSetting=" + defaultSetting
				+ ", emptyBeamTransmissionDataFile="
				+ emptyBeamTransmissionDataFile
				+ ", emptyCellScatteringDataFile="
				+ emptyCellScatteringDataFile
				+ ", emptyCellTransmissionDataFile="
				+ emptyCellTransmissionDataFile + ", initScript=" + initScript
				+ ", mode=" + mode + ", name=" + name
				+ ", preScatteringScript=" + preScatteringScript
				+ ", preTransmissionScript=" + preTransmissionScript
				+ ", startingAttenuation=" + startingAttenuation
				+ ", template=" + template + ", transmissionMode="
				+ transmissionMode + ", transmissionPreset="
				+ transmissionPreset + ", useManualAttenuationAlgorithm="
				+ useManualAttenuationAlgorithm + "]";
	}
	
}
