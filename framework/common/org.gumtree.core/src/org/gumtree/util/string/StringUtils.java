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

package org.gumtree.util.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Splitter;

public final class StringUtils {

	public static String EMPTY_STRING = "";
	
	public static String SPACE = " ";
	
	public static String formatTime(long timeInSecond) {
		long hour = timeInSecond / (60 * 60);
		long minute = (timeInSecond - (hour * 60 * 60)) / 60;
		long second = timeInSecond % 60;
		StringBuilder builder = new StringBuilder();
		if (hour < 10) {
			builder.append("0" + hour);
		} else {
			builder.append(hour);
		}
		builder.append(":");
		if (minute < 10) {
			builder.append("0" + minute);
		} else {
			builder.append(minute);
		}
		builder.append(":");
		if (second < 10) {
			builder.append("0" + second);
		} else {
			builder.append(second);
		}
		return builder.toString();
	}
	
	// Copied from Apache Commons Lang 3.3.0 (Apache Software Foundation)
	// Under license: http://www.apache.org/licenses/LICENSE-2.0
    public static boolean isNumber(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        int start = (chars[0] == '-') ? 1 : 0;
        if (sz > start + 1) {
            if (chars[start] == '0' && chars[start + 1] == 'x') {
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9')
                        && (chars[i] < 'a' || chars[i] > 'f')
                        && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
              // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent   
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns
                && (chars[i] == 'd'
                    || chars[i] == 'D'
                    || chars[i] == 'f'
                    || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l'
                || chars[i] == 'L') {
                // not allowing L with an exponent or decimal point
                return foundDigit && !hasExp && !hasDecPoint;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

	public static List<String> split(String string, String separator) {
		return split(string, separator, true);
	}
	
	public static List<String> split(String string, String separator, boolean trim) {
		if (string == null) {
			return new ArrayList<String>(0);
		}
		Iterator<String> iterator = Splitter.on(separator).split(string).iterator();
		List<String> list = new ArrayList<String>(2);
		while (iterator.hasNext()) {
			if (trim) {
				list.add(iterator.next().trim());
			} else {
				list.add(iterator.next());
			}
		}
		return list;
	}
	
	public static <T> String formatArray(T[] array, IStringProvider<T> formatter) {
		return formatIterable(Arrays.asList(array), formatter);
	}
	
	public static <T> String formatIterable(Iterable<T> iterable, IStringProvider<T> stringProvider) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		Iterator<T> iterator = iterable.iterator();
		while (iterator.hasNext()) {
			builder.append(stringProvider.asString(iterator.next()));
			if (iterator.hasNext()) {
				builder.append(",");	
			}
		}
		builder.append("]");
		return builder.toString();
	}
	
	public static String formatMap(Map<String, Object> map) {
		return formatMap(map, "Map");
	}

	// This method is not thread safe
	public static String formatMap(Map<String, Object> map,
			String mapLabel) {
		StringBuilder builder = new StringBuilder();
		builder.append(mapLabel + ":[");
		if (map != null) {
			int i = 0;
			for (Entry<String, Object> entry : map.entrySet()) {
				builder.append(entry.getKey() + "=" + entry.getValue().toString());
				i++;
				if (i < map.size()) {
					builder.append(", ");
				}
			}
		} else {
			builder.append("null");
		}
		builder.append("]");
		return builder.toString();
	}
	
	public static boolean isEmpty(String string) {
		return (string == null || string.length() == 0);
	}
	
	private StringUtils() {
		super();
	}
	
}
