package au.gov.ansto.bragg.quokka.msw.converters;

import org.gumtree.msw.ui.ModelValueConverterAdapter;

public class LongValueConverter extends ModelValueConverterAdapter<Long, String> {
	// finals
	public static final LongValueConverter DEFAULT = new LongValueConverter();
	
	// construction
	public LongValueConverter() {
		super(Long.class, String.class);
	}
	
	// methods
	@Override
	public String fromModelValue(Long value) {
		return value.toString();
	}
	@Override
	public Long toModelValue(String value) {
		return Long.parseLong(value);
	}
}
