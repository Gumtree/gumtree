package au.gov.ansto.bragg.quokka.msw.converters;

import org.gumtree.msw.ui.ModelValueConverterAdapter;

public class GroupTrimConverter extends ModelValueConverterAdapter<String, String> {
	// finals
	public static final GroupTrimConverter DEFAULT = new GroupTrimConverter();
	public static final String DEFAULT_VALUE = "";

	// construction
	public GroupTrimConverter() {
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
		
		value = value.trim().replace('\\', '/');

		int n = value.length();
		if ((n > 0) && (value.charAt(n - 1) == '/'))
			return value.substring(0, n - 1);

		return value;
	}
}
