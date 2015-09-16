package au.gov.ansto.bragg.quokka.msw.converters;

import org.gumtree.msw.ui.ModelValueConverterAdapter;

public class TimeValueConverter extends ModelValueConverterAdapter<Long, String> {
	// construction
	public TimeValueConverter() {
		super(Long.class, String.class);
	}
	
	// methods
	@Override
	public String fromModelValue(Long value) {
		return String.format("%,d", (long)value);
	}
	@Override
	public Long toModelValue(String value) {
		return Math.max(0L, Long.parseLong(value.replace(",", "")));
	}
}
