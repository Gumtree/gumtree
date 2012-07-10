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
 * SingleData contains the string output from the Sycamore protocol.
 *
 */
public class SingleData implements ISicsData {

	private String value;
	
	/**
	 * Constructors a sics data with single value (no key).
	 * 
	 * @param value data to be stored
	 */
	public SingleData(String value) {
		assert value != null;
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.cs.sycamore.ISicsData#isKeyValuePair()
	 */
	public boolean isKeyValuePair() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.cs.sycamore.ISicsData#isSingleValue()
	 */
	public boolean isSingleValue() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.cs.sycamore.ISicsData#getKey()
	 */
	public String getKey() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.cs.sycamore.ISicsData#getValue()
	 */
	public String getValue() {
		return value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getValue();
	}
	
}
