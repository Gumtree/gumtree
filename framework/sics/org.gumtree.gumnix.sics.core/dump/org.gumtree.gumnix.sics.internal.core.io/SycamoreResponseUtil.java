package org.gumtree.gumnix.sics.internal.core.io;

import org.gumtree.gumnix.sics.core.io.ISycamoreResponse;

public class SycamoreResponseUtil {

	public static String getValueAt(ISycamoreResponse response, int position) {
		if(response == null) {
			return null;
		}
		if(response.getMessageValues().length < position) {
			return null;
		}
		return response.getMessageValues()[position].getValue();
	}
	
	public static <T extends Number> Number getValueAt(ISycamoreResponse response,
			int position, Class<T> type) {
		String value = getValueAt(response, position);
		try {
			if (type.equals(Float.class)) {
				return Float.valueOf(value);
			} else if (type.equals(Integer.class)) {
				return Integer.valueOf(value);
			} else if (type.equals(Long.class)) {
				return Long.valueOf(value);
			} else if (type.equals(Double.class)) {
				return Double.valueOf(value);
			} else if (type.equals(Short.class)) {
				return Short.valueOf(value);
			} else if (type.equals(Byte.class)) {
				return Byte.valueOf(value);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return null;
	}
}
