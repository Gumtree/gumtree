package au.gov.ansto.bragg.kookaburra.experiment.report;

import java.util.ArrayList;
import java.util.List;

public class SampleEnvironmentEntry {

	private List<ExperimentConfig> configs;
	
	private String setting;
	
	public List<ExperimentConfig> getConfigs() {
		if (configs == null) {
			configs = new ArrayList<ExperimentConfig>(2);
		}
		return configs;
	}

	public String getSetting() {
		return setting;
	}

	public void setSetting(String setting) {
		this.setting = setting;
	}
	
}
