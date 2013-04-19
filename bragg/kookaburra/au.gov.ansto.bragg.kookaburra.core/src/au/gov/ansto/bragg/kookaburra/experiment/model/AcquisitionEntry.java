package au.gov.ansto.bragg.kookaburra.experiment.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class AcquisitionEntry extends AbstractModelObject {

	private Sample sample;
	
	private boolean runnable;
	
	private Map<InstrumentConfig, AcquisitionSetting> configSettings;	
	
	public AcquisitionEntry(Sample sample) {
		super();
		this.sample = sample;
		runnable = sample.isRunnable();
	}

	public Sample getSample() {
		return sample;
	}

	public Map<InstrumentConfig, AcquisitionSetting> getConfigSettings() {
		if (configSettings == null) {
			configSettings = new HashMap<InstrumentConfig, AcquisitionSetting>();
		}
		return configSettings;
	}
	
	public boolean isRunnable() {
		return runnable;
	}
	
	public void setRunnable(boolean runnable) {
		this.runnable = runnable;
		// Set runnable individual setting, but does not change the global runnable on sample
		for (AcquisitionSetting setting : configSettings.values()) {
			setting.setRunTransmission(runnable);
			setting.setRunScattering(runnable);
		}
	}

	/*************************************************************************
	 * Helper methods
	 *************************************************************************/
	
	public AcquisitionEntry copy() {
		AcquisitionEntry copiedEntry = new AcquisitionEntry(getSample());
		for (Entry<InstrumentConfig, AcquisitionSetting> entry : getConfigSettings().entrySet()) {
			copiedEntry.getConfigSettings().put(entry.getKey(), entry.getValue().copy());
		}
		copiedEntry.setRunnable(isRunnable());
		return copiedEntry;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AcquisitionEntry [configSettings=");
		builder.append(configSettings);
		builder.append(", runnable=");
		builder.append(runnable);
		builder.append(", sample=");
		builder.append(sample);
		builder.append("]");
		return builder.toString();
	}
	
}
