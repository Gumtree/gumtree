/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesHelper {

	private static Pattern propertiesPattern = Pattern.compile("\\$\\{(\\w|\\.)+\\}");
	
	public static String substitueWithProperties(String string) {
		if (string == null) {
			return string;
		}
		Matcher matcher = propertiesPattern.matcher(string);
		String result = new String(string);
		while (matcher.find()) {
			String propertyName = string.substring(matcher.start() + 2, matcher.end() - 1);
			String propertyValue = System.getProperty(propertyName);
			if (propertyValue != null) {
				result = result.replaceFirst("\\$\\{(\\w|\\.)+\\}", propertyValue);
			}
		}
		return result;
	}
	
	private PropertiesHelper() {
		super();
	}
	
}
