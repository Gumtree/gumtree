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

package org.gumtree.gumnix.sics.core.io;

/**
 * Interface for data object that represent any SICS response.
 * In the Sycamore specification, all response from SICS are
 * either in key-value pair format, or in single value format.
 *
 * @since 1.0
 *
 */
public interface ISicsData {

	/**
	 * Returns whether this data represents a key-value pair.
	 * 
	 * @return <code>true</code> if data is a key-value pair, false otherwise
	 */
	public boolean isKeyValuePair();

	/**
	 * Returns whether this data represents a single value.
	 * 
	 * @return <code>true</code> if data is a single value, false otherwise
	 */
	public boolean isSingleValue();

	/**
	 * Returns the key of the data, or null if this object is not a key-value pair.
	 * 
	 * @return key of the data object
	 */
	public String getKey();

	/**
	 * Returns the value of this data object.
	 *  
	 * @return the data value in String
	 */
	public String getValue();
}
