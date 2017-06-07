package au.gov.ansto.bragg.quokka.msw;

import java.util.Set;

import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;

public class Measurement extends Element {
	// finals
	public static final String TRANSMISSION = "Transmission";
	public static final String SCATTERING = "Scattering";
	
	// property names
	public static final DependencyProperty<Measurement, Boolean> ENABLED = new DependencyProperty<>("Enabled", Boolean.class);
	public static final DependencyProperty<Measurement, String> NAME = new DependencyProperty<>("Name", String.class); // e.g. Transmission or Scattering
	public static final DependencyProperty<Measurement, String> DESCRIPTION = new DependencyProperty<>("Description", String.class);
	public static final DependencyProperty<Measurement, String> SETUP_SCRIPT = new DependencyProperty<>("SetupScript", String.class);
	public static final DependencyProperty<Measurement, String> ATTENUATION_ALGORITHM = new DependencyProperty<>("AttenuationAlgorithm", String.class);
	public static final DependencyProperty<Measurement, Integer> ATTENUATION_ANGLE = new DependencyProperty<>("AttenuationAngle", Integer.class);
	public static final DependencyProperty<Measurement, Long> MIN_TIME = new DependencyProperty<>("MinTime", Long.class); // seconds
	public static final DependencyProperty<Measurement, Long> MAX_TIME = new DependencyProperty<>("MaxTime", Long.class); // seconds
	public static final DependencyProperty<Measurement, Boolean> MIN_TIME_ENABLED = new DependencyProperty<>("MinTimeEnabled", Boolean.class);
	public static final DependencyProperty<Measurement, Boolean> MAX_TIME_ENABLED = new DependencyProperty<>("MaxTimeEnabled", Boolean.class);
	public static final DependencyProperty<Measurement, Long> TARGET_MONITOR_COUNTS = new DependencyProperty<>("TargetMonitorCounts", Long.class);
	public static final DependencyProperty<Measurement, Long> TARGET_DETECTOR_COUNTS = new DependencyProperty<>("TargetDetectorCounts", Long.class);
	public static final DependencyProperty<Measurement, Boolean> TARGET_MONITOR_COUNTS_ENABLED = new DependencyProperty<>("TargetMonitorCountsEnabled", Boolean.class);
	public static final DependencyProperty<Measurement, Boolean> TARGET_DETECTOR_COUNTS_ENABLED = new DependencyProperty<>("TargetDetectorCountsEnabled", Boolean.class);
	// property set
	public static final Set<IDependencyProperty> PROPERTIES = DependencyProperty.createSet(
			INDEX, ENABLED, NAME, DESCRIPTION, SETUP_SCRIPT, ATTENUATION_ALGORITHM, ATTENUATION_ANGLE, MIN_TIME, MAX_TIME, TARGET_MONITOR_COUNTS, TARGET_DETECTOR_COUNTS, MIN_TIME_ENABLED, MAX_TIME_ENABLED, TARGET_MONITOR_COUNTS_ENABLED, TARGET_DETECTOR_COUNTS_ENABLED);

	// construction
	Measurement(Configuration parent, String name) {
		super(parent, name);
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
	public String getSetupScript() {
		return (String)get(SETUP_SCRIPT);
	}
	public void setSetupScript(String value) {
		set(SETUP_SCRIPT, value);
	}
	public AttenuationAlgorithm getAttenuationAlgorithm() {
		return AttenuationAlgorithm.from((String)get(ATTENUATION_ALGORITHM));
	}
	public void setAttenuationAlgorithm(AttenuationAlgorithm value) {
		set(ATTENUATION_ALGORITHM, value.toString());
	}
	public int getAttenuationAngle() {
		return (int)get(ATTENUATION_ANGLE);
	}
	public void setAttenuationAngle(int value) {
		set(ATTENUATION_ANGLE, value);
	}
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
