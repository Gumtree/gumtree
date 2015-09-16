package au.gov.ansto.bragg.quokka.msw.converters;

import org.gumtree.msw.ui.ModelValueConverterAdapter;

public class ThicknessValueConverter extends ModelValueConverterAdapter<Double, String> {
	// construction
	public ThicknessValueConverter() {
		super(Double.class, String.class);
	}
	
	// methods
	@Override
	public String fromModelValue(Double value) {
		return value.toString();
	}
	@Override
	public Double toModelValue(String value) {
		return Double.parseDouble(value);
	}
}
