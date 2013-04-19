package au.gov.ansto.bragg.kookaburra.experiment.model;

import java.util.HashMap;
import java.util.Map;

public class ControlledAcquisition extends Acquisition {

	private Map<SampleEnvironment, SampleEnvironmentPreset> envSettings;
	
	public ControlledAcquisition(Experiment experiment, Map<SampleEnvironment, SampleEnvironmentPreset> envSettings) {
		super(experiment);
		this.envSettings = envSettings;
	}
	
	public Map<SampleEnvironment, SampleEnvironmentPreset> getEnvSettings() {
		if (envSettings == null) {
			envSettings = new HashMap<SampleEnvironment, SampleEnvironmentPreset>();
		}
		return envSettings;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ControlledAcquisition [envSettings=");
		builder.append(envSettings);
		builder.append("]");
		return builder.toString();
	}
	
}
