package au.gov.ansto.bragg.quokka.msw.converters;

public final class ValueConverterUtil {
	// methods
	public static String trimScientificNotation(String str) {
		StringBuilder sb = new StringBuilder(str);
		
		if (sb.indexOf("e") != -1) {
			replace(sb, "e+", "e");
			while (replace(sb, "e0", "e"));
			while (replace(sb, "0e", "e"));
			replace(sb, ".e", "e");
			
			if (trimEnd(sb, "e1"))
				moveDecimalMark(sb, 1);
			else if (trimEnd(sb, "e2"))
				moveDecimalMark(sb, 2);
			else
				trimEnd(sb, "e"); // "12e" => "12"
		}
		else {
			// remove any '0' after '.' in case no 'e' was in string
			int p = sb.indexOf(".");
			if (p != -1) {
				while (trimEnd(sb, "0"));	// "1.000" => "1."
				trimEnd(sb, ".");			// "1."    => "1"
			}
		}
		
		return sb.toString();
	}

	// helper
	private static boolean replace(StringBuilder sb, String str0, String str1) {
		int i = sb.indexOf(str0);
		if (i == -1)
			return false;
		
		sb.replace(i, i + str0.length(), str1);
		return true;
	}
	private static boolean trimEnd(StringBuilder sb, String str) {
		if (sb.length() == 0)
			return false;
		if (sb.lastIndexOf(str) != sb.length() - str.length())
			return false;

		sb.setLength(sb.length() - str.length());
		return true;
	}
	private static void moveDecimalMark(StringBuilder sb, int n) {
		// only positive n is supported
		if (n <= 0)
			return;
		
		int i = sb.indexOf(".");
		if (i == -1) {
			while (n-- > 0)
				sb.append('0');
		}
		else {
			int j = i + n; // new position
			sb.deleteCharAt(i);
			if (j < sb.length())
				sb.insert(j, '.');
			else
				while (j > sb.length())
					sb.append('0');
		}
	}
}
