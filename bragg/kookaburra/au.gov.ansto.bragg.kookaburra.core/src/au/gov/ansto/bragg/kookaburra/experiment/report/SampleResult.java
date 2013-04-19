package au.gov.ansto.bragg.kookaburra.experiment.report;

public class SampleResult {

	private int position;
	
	private String name;
	
	private float thickness;

	private String type;

	private float att;
	
	private long preset;
	
	private String runId;

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getThickness() {
		return thickness;
	}

	public void setThickness(float thickness) {
		this.thickness = thickness;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public float getAtt() {
		return att;
	}

	public void setAtt(float att) {
		this.att = att;
	}

	public long getPreset() {
		return preset;
	}

	public void setPreset(long preset) {
		this.preset = preset;
	}

	public String getRunId() {
		return runId;
	}

	public void setRunId(String runId) {
		this.runId = runId;
	}
	
}
