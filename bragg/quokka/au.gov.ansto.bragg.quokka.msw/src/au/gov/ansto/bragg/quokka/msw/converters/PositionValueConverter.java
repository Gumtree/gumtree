package au.gov.ansto.bragg.quokka.msw.converters;

import org.gumtree.msw.ui.ModelValueConverterAdapter;

public class PositionValueConverter extends ModelValueConverterAdapter<Double, String> {
	// construction
	public PositionValueConverter() {
		super(Double.class, String.class);
	}
	
	// methods
	@Override
	public String fromModelValue(Double value) {
		return String.format("%.0f", (double)value);
	}
	@Override
	public Double toModelValue(String value) {
		return Double.parseDouble(value);
	}
}
