package au.gov.ansto.bragg.kookaburra.experiment.util;

public enum SampleType {

	SAMPLE,
	EMPTY_CELL,
	EMPTY_BEAM,
	DARK_CURRENT;
	
	public String toString() {
		return name().toLowerCase();
	}
	
}
