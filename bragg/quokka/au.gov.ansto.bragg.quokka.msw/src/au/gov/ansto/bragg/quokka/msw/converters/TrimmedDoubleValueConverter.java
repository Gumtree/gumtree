package au.gov.ansto.bragg.quokka.msw.converters;

import org.gumtree.msw.ui.ModelValueConverterAdapter;

public class TrimmedDoubleValueConverter extends ModelValueConverterAdapter<Double, String> {
	// finals
	public static final TrimmedDoubleValueConverter DEFAULT = new TrimmedDoubleValueConverter();
	
	// construction
	public TrimmedDoubleValueConverter() {
		super(Double.class, String.class);
	}
	
	// methods
	@Override
	public String fromModelValue(Double value) {
		return ValueConverterUtil.trimScientificNotation(
				value.toString());
	}
	@Override
	public Double toModelValue(String value) {
		return Double.parseDouble(value);
	}
}
