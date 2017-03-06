package au.gov.ansto.bragg.quokka.msw.converters;

import org.gumtree.msw.ui.ModelValueConverterAdapter;

public class CountValueConverter extends ModelValueConverterAdapter<Long, String> {
	// finals
	public static final CountValueConverter DEFAULT = new CountValueConverter();
	
	// construction
	public CountValueConverter() {
		super(Long.class, String.class);
	}
	
	// methods
	@Override
	public String fromModelValue(Long value) {
		return ValueConverterUtil.trimScientificNotation(
				String.format("%e", (double)value));
	}
	@Override
	public Long toModelValue(String value) {
		return Math.max(0L, Math.round(Double.parseDouble(value)));
	}
}
