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
 * Interface for sics proxy listener.  Proxy listener receives corresponding outputs
 * if sics delivers them to the client.
 * 
 */
public interface ISicsProxyListener {
	/**
	 * This method is called from client receives output with tag
	 * "OUT" and "STATUS".
	 * 
	 * @param response output from sics in sycamore format
	 */
	public void receiveReply(ISycamoreResponse response);
	
	/**
	 * This method is called from client receives output with tag "ERROR".
	 * 
	 * @param response output from sics in sycamore format
	 */
	public void receiveError(ISycamoreResponse response);
	
	/**
	 * This method is called from client receives output with tag "WARNING".
	 * 
	 * @param response output from sics in sycamore format
	 */
	public void receiveWarning(ISycamoreResponse response);
	
	/**
	 * This method is called from client receives output with tag "FINISH".
	 * 
	 * @param response output from sics in sycamore format
	 */
	public void receiveFinish(ISycamoreResponse response);
	
	/**
	 * Returns whether this listener is completed or not.  When a listener
	 * is not completed, it will continue to receive corresponding response
	 * from sics if there is any.
	 * 
	 * @return true if listener is completed; false otherwise
	 */
	public boolean isListenerCompleted();
	
	/**
	 * If listener is no longer interest in receiving response from sics,
	 * setListnerCompleted should be set to true.
	 * 
	 * @param completed
	 */
	public void setListenerCompleted(boolean completed);
}
