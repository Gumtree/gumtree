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
 *
 */
public interface ISycamoreResponse {
	/**
	 * Sycamore output tag.
	 */
	public enum Tag {
		/**
		 * Normal output
		 */
		OUT,
		
		/**
		 * Status output
		 */
		STATUS,
		
		/**
		 * Event output
		 */
		EVENT,
		
		/**
		 * Warning output
		 */
		WARNING,
		
		/**
		 * Error output
		 */
		ERROR,
		
		/**
		 * Finish output
		 */
		FINISH
	}
	
	/**
	 * @return whole message, including prefix and message body
	 */
	public String getResponse();
	
	/**
	 * @return message body
	 */
	public String getMessage();
	
	/**
	 * @return sics connection id
	 */
	public String getConnId();
	
	/**
	 * @return device id
	 */
	public String getDeviceId();

	/**
	 * @return sycamore prefix tag
	 */
	public String getTag();

	/**
	 * @return transaction id
	 */
	public int getTransactionId();
	
	/**
	 * @return key inside the message body
	 */
	public String getMessageKey();
	
	/**
	 * @return values inside the message body
	 */
	public ISicsData[] getMessageValues();
}
