package au.gov.ansto.bragg.quokka.ui.workflow;

public class ProgressData {

	// A number from 0 to 1
	public float progress;
	
	public String text;

	public ProgressData(float progress, String text) {
		this.progress = progress;
		this.text = text;
	}
	
	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}
