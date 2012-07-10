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
 * Interface for sics interest lisnter.  After a user subscribe interest to
 * a sics object, any sics object status change will notify the user with
 * prefix tag "EVENT".  Sycamore proxy deliver those "EVENT" messages to
 * sics interest lisnter via <code>receiveEvent(ISycamoreResponse response)</code>.
 */
public interface ISicsInterestListener {
	/**
	 * This method is called when event arrives.
	 * 
	 * @param response sycamore output
	 */
	public void receiveEvent(ISycamoreResponse response);
}
