package au.gov.ansto.bragg.quokka.msw.converters;

import org.gumtree.msw.ui.ModelValueConverterAdapter;

public class IndexValueConverter extends ModelValueConverterAdapter<Integer, String> {
	// construction
	public IndexValueConverter() {
		super(Integer.class, String.class);
	}
	
	// methods
	@Override
	public String fromModelValue(Integer value) {
		return String.format("%d", (int)value + 1); // from zero-based to one-based
	}
	@Override
	public Integer toModelValue(String value) {
		return Integer.parseInt(value) - 1; // from one-based to zero-based
	}
}
