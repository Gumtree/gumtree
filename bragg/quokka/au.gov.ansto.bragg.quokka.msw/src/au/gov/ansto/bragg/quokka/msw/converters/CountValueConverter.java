package au.gov.ansto.bragg.quokka.msw.converters;

import org.gumtree.msw.ui.ModelValueConverterAdapter;

public class CountValueConverter extends ModelValueConverterAdapter<Long, String> {
	// construction
	public CountValueConverter() {
		super(Long.class, String.class);
	}
	
	// methods
	@Override
	public String fromModelValue(Long value) {
		StringBuilder sb = new StringBuilder(String.format("%e", (double)value));
		
		replace(sb, "e+", "e");
		while (replace(sb, "e0", "e"));
		while (replace(sb, "0e", "e"));
		replace(sb, ".e", "e");
		
		if (sb.charAt(sb.length() - 1) == 'e')
			return sb.substring(0, sb.length() - 1);
		else
			return sb.toString();
	}
	@Override
	public Long toModelValue(String value) {
		return Math.max(0L, (long)Math.round(Double.parseDouble(value)));
	}
	
	// helper
	private static boolean replace(StringBuilder sb, String str0, String str1) {
		int i = sb.indexOf(str0);
		if (i == -1)
			return false;
		
		sb.replace(i, i + str0.length(), str1);
		return true;
	}
}
