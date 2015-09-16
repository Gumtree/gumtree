package au.gov.ansto.bragg.quokka.msw;

import java.util.Set;

import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;

public class Sample extends Element {
	// finals
	public static final String BLOCKED_BEAM = "BLOCKED_BEAM";
	public static final String EMPTY_BEAM = "EMPTY_BEAM";
	public static final String EMPTY_CELL = "EMPTY_CELL";
	
	// property names
	public static final DependencyProperty<Sample, Boolean> ENABLED = new DependencyProperty<>("Enabled", Boolean.class);
	public static final DependencyProperty<Sample, String> NAME = new DependencyProperty<>("Name", String.class);
	public static final DependencyProperty<Sample, String> DESCRIPTION = new DependencyProperty<>("Description", String.class);
	public static final DependencyProperty<Sample, Double> THICKNESS = new DependencyProperty<>("Thickness", Double.class);
	public static final DependencyProperty<Sample, Double> POSITION = new DependencyProperty<>("Position", Double.class);
	// optional property
	public static final DependencyProperty<Sample, Long> MIN_TIME = new DependencyProperty<>("MinTime", Long.class); // seconds
	public static final DependencyProperty<Sample, Long> MAX_TIME = new DependencyProperty<>("MaxTime", Long.class); // seconds
	public static final DependencyProperty<Sample, Long> TARGET_MONITOR_COUNTS = new DependencyProperty<>("TargetMonitorCounts", Long.class);
	public static final DependencyProperty<Sample, Long> TARGET_DETECTOR_COUNTS = new DependencyProperty<>("TargetDetectorCounts", Long.class);
	// property set
	public static final Set<IDependencyProperty> PROPERTIES = DependencyProperty.createSet(
			INDEX, ENABLED, NAME, DESCRIPTION, THICKNESS, POSITION, MIN_TIME, MAX_TIME, TARGET_MONITOR_COUNTS, TARGET_DETECTOR_COUNTS);
	
	// construction
	Sample(SampleList parent, String elementName) {
		super(parent, elementName);
	}

	// properties
	@Override
	public Set<IDependencyProperty> getProperties() {
		return PROPERTIES;
	}
	public boolean getEnabled() {
		return (boolean)get(ENABLED);
	}
	public void setEnabled(boolean value) {
		set(ENABLED, value);
	}
	public String getName() {
		return (String)get(NAME);
	}
	public void setName(String value) {
		set(NAME, value);
	}
	public String getDescription() {
		return (String)get(DESCRIPTION);
	}
	public void setDescription(String value) {
		set(DESCRIPTION, value);
	}
	public double getThickness() {
		return (double)get(THICKNESS);
	}
	public void setThickness(double value) {
		set(THICKNESS, value);
	}
	public double getPosition() {
		return (double)get(POSITION);
	}
	public void setPosition(double value) {
		set(POSITION, value);
	}
	// optional
	public long getMinTime() {
		return (long)get(MIN_TIME);
	}
	public void setMinimalTime(long value) {
		set(MIN_TIME, value);
	}
	public long getMaxTime() {
		return (long)get(MAX_TIME);
	}
	public void setMaximalTime(long value) {
		set(MAX_TIME, value);
	}
	public long getTargetMonitorCounts() {
		return (long)get(TARGET_MONITOR_COUNTS);
	}
	public void setTargetMonitorCounts(long value) {
		set(TARGET_MONITOR_COUNTS, value);
	}
	public long getTargetDetectorCounts() {
		return (long)get(TARGET_DETECTOR_COUNTS);
	}
	public void setTargetDetectorCounts(long value) {
		set(TARGET_DETECTOR_COUNTS, value);
	}

	// methods
	@Override
	public void duplicate() {
		super.duplicate();
	}
	@Override
	public void delete() {
		super.delete();
	}
}
