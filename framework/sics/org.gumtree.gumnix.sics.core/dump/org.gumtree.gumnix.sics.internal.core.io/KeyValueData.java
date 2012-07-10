/*******************************************************************************
 * Copyright (c) 2004  Australian Nuclear Science and Technology Organisation.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * GumTree Platform is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.gumnix.sics.internal.core.io;

import org.gumtree.gumnix.sics.core.io.ISicsData;

/**
 * KeyValueData contains the key-value pair output from the Sycamore protocol.
 *
 */
public class KeyValueData implements ISicsData {

	private String key;
	private String value;
	
	/**
	 * Constructs a KeyValueData with given key and value.
	 * 
	 * @param key sics data key
	 * @param value sics data value
	 */
	public KeyValueData(String key, String value) {
		assert key != null && value != null;
		this.key = key;
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see sycamoretest.io.ISicsData#isKeyValuePair()
	 */
	public boolean isKeyValuePair() {
		return true;
	}

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISicsData#isSingleValue()
	 */
	public boolean isSingleValue() {
		return false;
	}

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISicsData#getKey()
	 */
	public String getKey() {
		return key;
	}

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISicsData#getValue()
	 */
	public String getValue() {
		return value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return key + "=" + value;
	}
}
