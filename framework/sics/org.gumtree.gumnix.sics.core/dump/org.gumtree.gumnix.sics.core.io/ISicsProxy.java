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


public interface ISicsProxy {
	public static final String SYSTEM_PROPERTY_DEBUG = "debug";
	
	public void initialise() throws SicsIOException;

	public void send(String command, ISicsProxyListener proxyListener) throws SicsIOException;

	public void shutdown() throws SicsIOException;
	
	public void addInterestListner(String sicsObjectId, ISicsInterestListener interestListener) throws SicsIOException;
	
	public void removeInterestListener(String sicsObjectId, ISicsInterestListener interestListener) throws SicsIOException;
}
