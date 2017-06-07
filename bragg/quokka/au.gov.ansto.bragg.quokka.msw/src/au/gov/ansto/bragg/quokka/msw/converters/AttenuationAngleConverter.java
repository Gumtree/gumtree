package au.gov.ansto.bragg.quokka.msw.converters;

import org.gumtree.msw.ui.ModelValueConverterAdapter;

public class AttenuationAngleConverter extends ModelValueConverterAdapter<Integer, String> {
	// finals
	public static final AttenuationAngleConverter DEFAULT = new AttenuationAngleConverter();
	
	// construction
	public AttenuationAngleConverter() {
		super(Integer.class, String.class);
	}
	
	// methods
	@Override
	public String fromModelValue(Integer value) {
		if (value == null)
			return "";

		return String.format("%d\u00B0", value);
	}
	@Override
	public Integer toModelValue(String value) {
		// drop trailing degree symbol
		return Integer.parseInt(value.substring(0, value.length() - 1));
	}
}
