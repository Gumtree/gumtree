package au.gov.ansto.bragg.kookaburra.experiment.report;

import java.util.ArrayList;
import java.util.List;

public class Measurement {

	private String mode;
	
	private List<SampleResult> samples;

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public List<SampleResult> getSamples() {
		if (samples == null) {
			samples = new ArrayList<SampleResult>(2);
		}
		return samples;
	}
	
}
