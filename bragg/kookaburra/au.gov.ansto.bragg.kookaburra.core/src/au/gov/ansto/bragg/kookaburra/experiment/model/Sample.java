package au.gov.ansto.bragg.kookaburra.experiment.model;

import au.gov.ansto.bragg.kookaburra.experiment.util.SampleType;

/**
 * Sample 
 *
 */
public class Sample extends AbstractModelObject {

	// Sample name
	private String name;
	
	// Sample description
	private String description;
	
	// Indicate whether this sample is globally runnable in the experiment 
	private boolean runnable;

	// Position in the sample holder
	private int position;
	
	// Sample thickness in mm
	private float thickness;
	
	// Type of sample
	private SampleType type;
	
	public String getName() {
		if (name == null) {
			name = "";
		}
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

	public boolean isRunnable() {
		return runnable;
	}

	public void setRunnable(boolean runnable) {
		boolean oldValue = this.runnable;
		this.runnable = runnable;
		firePropertyChange("runnable", oldValue, runnable);
	}

	public int getPosition() {
		return position;
	}

	public int getIndex() {
		return getPosition() - 1;
	}
	public void setPosition(int position) {
		this.position = position;
	}

	public float getThickness() {
		return thickness;
	}

	public void setThickness(float thickness) {
		float oldValue = this.thickness;
		this.thickness = thickness;
		firePropertyChange("thickness", oldValue, thickness);
	}
	
	public SampleType getType() {
		if (type == null) {
			type = SampleType.SAMPLE;
		}
		return type;
	}

	public void setType(SampleType type) {
		SampleType oldValue = this.type;
		this.type = type;
		firePropertyChange("type", oldValue, type);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Sample [description=");
		builder.append(description);
		builder.append(", name=");
		builder.append(name);
		builder.append(", position=");
		builder.append(position);
		builder.append(", runnable=");
		builder.append(runnable);
		builder.append(", thickness=");
		builder.append(thickness);
		builder.append(", type=");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}
	
}
