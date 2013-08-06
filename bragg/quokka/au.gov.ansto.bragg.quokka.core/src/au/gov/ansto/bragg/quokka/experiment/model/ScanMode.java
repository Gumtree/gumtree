package au.gov.ansto.bragg.quokka.experiment.model;

public enum ScanMode {

	TIME("Time"),
	COUNTS("Detector Counts"),
	BM1("Beam Monitor Counts");
	
	// fields
	private final String label;
	
	// properties
	public String getLabel() {
		return label;
	}
	
	// constructor
	private ScanMode(String label) {
		this.label = label;
	}
	
	// helper
	public static ScanMode fromLabel(String label) {
		if (TIME.label.equalsIgnoreCase(label))
			return TIME;
		if (COUNTS.label.equalsIgnoreCase(label))
			return COUNTS;
		if (BM1.label.equalsIgnoreCase(label))
			return BM1;

		throw new IllegalArgumentException();
	}
}
