package au.gov.ansto.bragg.quokka.msw.converters;

import org.gumtree.msw.ui.ModelValueConverterAdapter;

public class TimeValueConverter extends ModelValueConverterAdapter<Long, String> {
	// finals
	public static final TimeValueConverter DEFAULT = new TimeValueConverter();
	
	// construction
	public TimeValueConverter() {
		super(Long.class, String.class);
	}
	
	// methods
	@Override
	public String fromModelValue(Long value) {
		return String.format("%,d", value); // include thousands separators
	}
	@Override
	public Long toModelValue(String value) {
		return Math.max(0L, Long.parseLong(value.replace(",", "")));  // remove thousands separators
	}
}
