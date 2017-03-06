package org.gumtree.msw.ui.util;

import java.util.Arrays;
import java.util.Comparator;

public class AlphanumericComparator implements Comparator<String> {
	// finals
	public static final Comparator<String> DEFAULT = new AlphanumericComparator();
	
	// methods
	@Override
	public int compare(String a, String b) {
		StringChunk aChunk = new StringChunk(a);
		StringChunk bChunk = new StringChunk(b);
		
		while (next(aChunk, bChunk)) {
			int result = aChunk.compare(bChunk);
			if (result != 0)
				return result;
		}
		
		return 0;
	}
	// testing
	public static boolean test() {
		String[] input = {
				"eee 5 ddd jpeg2001 eee",
				"eee 123 ddd jpeg2000 eee",
				"ddd",
				"aaa 5 yy 6",
				"ccc 555",
				"bbb 3 ccc",
				"bbb 9 a",
				"",
				"eee 4 ddd jpeg2001 eee",
				"ccc 11",
				"bbb 12 ccc",
				"aaa 5 yy 22",
				"aaa",
				"eee 3 ddd jpeg2000 eee",
				"ccc 5",
				"New (10)",
				"New (2)",
				"New (11)",
		};
		String[] output = {
				"",
				"aaa",
				"aaa 5 yy 6",
				"aaa 5 yy 22",
				"bbb 3 ccc",
				"bbb 9 a",
				"bbb 12 ccc",
				"ccc 5",
				"ccc 11",
				"ccc 555",
				"ddd",
				"eee 3 ddd jpeg2000 eee",
				"eee 4 ddd jpeg2001 eee",
				"eee 5 ddd jpeg2001 eee",
				"eee 123 ddd jpeg2000 eee",
				"New (2)",
				"New (10)",
				"New (11)",
			};
		
		Arrays.sort(input, new AlphanumericComparator());
		
		for (int i = 0, n = output.length; i != n; i++)
			if (!output[i].equals(input[i]))
				return false;
		
		return true;
	}
	
	// helper
	private static boolean next(StringChunk a, StringChunk b) {
		boolean aResult = a.next();
		boolean bResult = b.next();
		return aResult || bResult;
	}
	
	// chunk type
	private static enum ChunkType {
		ALPHABETIC,
		NUMERIC,
	}
	
	// string chunk information
	private static class StringChunk {
		// fields
		private final String source;
		private final int size;
		// selected
		private int i0;
		private int i1;
		private ChunkType type;
		
		// construction
		public StringChunk(String source) {
			this.source = source;
			this.size = source == null ? 0 : source.length();

			i0 = 0;
			i1 = 0;
			type = ChunkType.ALPHABETIC;
		}
		
		// properties
		public boolean isValid() {
			return i0 != i1;
		}
		public String selected() {
			return source.substring(i0, i1);
		}
		public ChunkType type() {
			return type;
		}
		
		// methods
		public boolean next() {
			if (i1 >= size) {
				i0 = size;
				i1 = size;
				return false;
			}
			else {
				i0 = i1;
				boolean numeric = isDigit(source.charAt(i1++));
				while ((i1 < size) && (numeric == isDigit(source.charAt(i1))))
					i1++;
				
				type = numeric ? ChunkType.NUMERIC : ChunkType.ALPHABETIC;				
				return true;
			}
		}
		public int compare(StringChunk other) {
			if (other == null)
				return 1;
			
			if (!isValid())
				return other.isValid() ? -1 : 0;
			if (!other.isValid())
				return 1;
			
			if (type() == ChunkType.NUMERIC) {
				if (other.type() == ChunkType.NUMERIC)
					return Long.compare(
							Long.parseLong(selected()),
							Long.parseLong(other.selected()));
				else
					return -1;
			}
			else {
				if (other.type() == ChunkType.NUMERIC)
					return +1;
				else
					return selected().compareToIgnoreCase(other.selected());
			}
		}
		
		// helpers
	    private static boolean isDigit(char c) {
	        return (c >= 48) && (c <= 57);
	    }
	}
}
