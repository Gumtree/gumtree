package au.gov.ansto.bragg.kookaburra.experiment.model;


public class AcquisitionSetting extends AbstractModelObject {

	private boolean runTransmission = true;
	
	private boolean runScattering = true;
	
	private Long preset;

	private InstrumentConfig config;
	
	private boolean isRunningTransmission;
	
	private boolean isRunningScattering;
	
	private String transmissionDataFile;
	
	private String scatteringDataFile;
	
	private float transmissionWavelength;
	
	private float scatteringWavelength;
	
	private float transmissionAttenuation;
	
	private float scatteringAttenuation;
	
	private float transmissionL1;
	
	private float scatteringL1;
	
	private float transmissionL2;
	
	private float scatteringL2;
	
	public AcquisitionSetting(InstrumentConfig config) {
		super();
		this.config = config;
	}

	public InstrumentConfig getConfig() {
		return config;
	}

	public void setConfig(InstrumentConfig config) {
		this.config = config;
	}

	public boolean isRunTransmission() {
		return runTransmission;
	}

	public void setRunTransmission(boolean runTransmission) {
		this.runTransmission = runTransmission;
	}

	public boolean isRunScattering() {
		return runScattering;
	}

	public void setRunScattering(boolean runScattering) {
		this.runScattering = runScattering;
	}

	public Long getPreset() {
		if (preset == null) {
			return config.getDefaultSetting(); 
		} else {
			return preset;
		}
	}

	public void setPreset(Long preset) {
		if (preset == config.getDefaultSetting()) {
			this.preset = null;
		} else {
			this.preset = preset;
		}
	}

	public boolean isRunningTransmission() {
		return isRunningTransmission;
	}

	public void setRunningTransmission(boolean isRunning) {
		this.isRunningTransmission = isRunning;
	}

	public boolean isRunningScattering() {
		return isRunningScattering;
	}

	public void setRunningScattering(boolean isRunning) {
		this.isRunningScattering = isRunning;
	}
	
	public String getTransmissionDataFile() {
		return transmissionDataFile;
	}

	public void setTransmissionDataFile(String transmissionDataFile) {
		this.transmissionDataFile = transmissionDataFile;
	}

	public String getScatteringDataFile() {
		return scatteringDataFile;
	}

	public void setScatteringDataFile(String scatteringDataFile) {
		this.scatteringDataFile = scatteringDataFile;
	}

	public float getTransmissionWavelength() {
		return transmissionWavelength;
	}

	public void setTransmissionWavelength(float transmissionWavelength) {
		this.transmissionWavelength = transmissionWavelength;
	}

	public float getScatteringWavelength() {
		return scatteringWavelength;
	}

	public void setScatteringWavelength(float scatteringWavelength) {
		this.scatteringWavelength = scatteringWavelength;
	}

	public float getTransmissionAttenuation() {
		return transmissionAttenuation;
	}

	public void setTransmissionAttenuation(float transmissionAttenuation) {
		this.transmissionAttenuation = transmissionAttenuation;
	}

	public float getScatteringAttenuation() {
		return scatteringAttenuation;
	}

	public void setScatteringAttenuation(float scatteringAttenuation) {
		this.scatteringAttenuation = scatteringAttenuation;
	}

	public float getTransmissionL1() {
		return transmissionL1;
	}

	public void setTransmissionL1(float transmissionL1) {
		this.transmissionL1 = transmissionL1;
	}

	public float getScatteringL1() {
		return scatteringL1;
	}

	public void setScatteringL1(float scatteringL1) {
		this.scatteringL1 = scatteringL1;
	}

	public float getTransmissionL2() {
		return transmissionL2;
	}

	public void setTransmissionL2(float transmissionL2) {
		this.transmissionL2 = transmissionL2;
	}

	public float getScatteringL2() {
		return scatteringL2;
	}

	public void setScatteringL2(float scatteringL2) {
		this.scatteringL2 = scatteringL2;
	}

	/*************************************************************************
	 * Helper methods
	 *************************************************************************/
	
	public AcquisitionSetting copy() {
		AcquisitionSetting newCopy = new AcquisitionSetting(getConfig());
		newCopy.setRunningTransmission(isRunningTransmission());
		newCopy.setRunScattering(isRunScattering());
		newCopy.setPreset(getPreset());
		newCopy.setRunningTransmission(isRunningTransmission());
		newCopy.setRunningScattering(isRunningScattering());
		newCopy.setTransmissionDataFile(getTransmissionDataFile());
		newCopy.setScatteringDataFile(getScatteringDataFile());
		newCopy.setTransmissionWavelength(getTransmissionWavelength());
		newCopy.setScatteringWavelength(getScatteringWavelength());
		newCopy.setTransmissionAttenuation(getTransmissionAttenuation());
		newCopy.setScatteringAttenuation(getScatteringAttenuation());
		newCopy.setTransmissionL1(getTransmissionL1());
		newCopy.setScatteringL1(getScatteringL1());
		newCopy.setTransmissionL2(getTransmissionL2());
		newCopy.setScatteringL2(getScatteringL2());
		return newCopy;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AcquisitionSetting [config=");
		builder.append(config);
		builder.append(", isRunningScattering=");
		builder.append(isRunningScattering);
		builder.append(", isRunningTransmission=");
		builder.append(isRunningTransmission);
		builder.append(", preset=");
		builder.append(preset);
		builder.append(", runScattering=");
		builder.append(runScattering);
		builder.append(", runTransmission=");
		builder.append(runTransmission);
		builder.append(", scatteringAttenuation=");
		builder.append(scatteringAttenuation);
		builder.append(", scatteringDataFile=");
		builder.append(scatteringDataFile);
		builder.append(", scatteringL1=");
		builder.append(scatteringL1);
		builder.append(", scatteringL2=");
		builder.append(scatteringL2);
		builder.append(", scatteringWavelength=");
		builder.append(scatteringWavelength);
		builder.append(", transmissionAttenuation=");
		builder.append(transmissionAttenuation);
		builder.append(", transmissionDataFile=");
		builder.append(transmissionDataFile);
		builder.append(", transmissionL1=");
		builder.append(transmissionL1);
		builder.append(", transmissionL2=");
		builder.append(transmissionL2);
		builder.append(", transmissionWavelength=");
		builder.append(transmissionWavelength);
		builder.append("]");
		return builder.toString();
	}
	
}
