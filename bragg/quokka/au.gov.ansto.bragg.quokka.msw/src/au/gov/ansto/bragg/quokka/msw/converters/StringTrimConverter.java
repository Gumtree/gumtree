package au.gov.ansto.bragg.quokka.msw.converters;

import org.gumtree.msw.ui.ModelValueConverterAdapter;

public class StringTrimConverter extends ModelValueConverterAdapter<String, String> {
	// finals
	public static final StringTrimConverter DEFAULT = new StringTrimConverter();
	public static final String DEFAULT_VALUE = "";
	
	// construction
	public StringTrimConverter() {
		super(String.class, String.class);
	}
	
	// methods
	@Override
	public String fromModelValue(String value) {
		if (value == null)
			return DEFAULT_VALUE;
		
		return value;
	}
	@Override
	public String toModelValue(String value) {
		if (value == null)
			return DEFAULT_VALUE;
		
		return value.trim();
	}
}
