package au.gov.ansto.bragg.quokka.msw;

public enum AttenuationAlgorithm {
	// options
	FIXED_ATTENUATION,
	ITERATIVE_ATTENUATION,
	SMART_ATTENUATION;
	
	// finals
	private static final String STR_FIXED_ATTENUATION = "fixed attenuation";
	private static final String STR_ITERATIVE_ATTENUATION = "iterative attenuation";
	private static final String STR_SMART_ATTENUATION = "smart attenuation";

	// methods
	@Override
	public String toString() {
		switch (this) {
		case FIXED_ATTENUATION:
			return STR_FIXED_ATTENUATION;
		case ITERATIVE_ATTENUATION:
			return STR_ITERATIVE_ATTENUATION;
		case SMART_ATTENUATION:
			return STR_SMART_ATTENUATION;
		}

		throw new IllegalStateException();
	}
	public static AttenuationAlgorithm from(String value) {
		if (STR_FIXED_ATTENUATION.equals(value))
			return FIXED_ATTENUATION;
		if (STR_ITERATIVE_ATTENUATION.equals(value))
			return ITERATIVE_ATTENUATION;
		if (STR_SMART_ATTENUATION.equals(value))
			return SMART_ATTENUATION;
		
		throw new IllegalArgumentException();
	}
}
