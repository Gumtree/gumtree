/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.sics.io.support;

import org.gumtree.sics.io.ISicsConnectionContext;
import org.gumtree.sics.io.ISicsData;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.SicsCallbackAdapter;
import org.gumtree.sics.io.SicsConnectionContext;
import org.gumtree.sics.io.SicsExecutionException;
import org.gumtree.sics.io.SicsIOException;
import org.gumtree.sics.io.SicsRole;

public class ConnectSics {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ISicsProxy proxy = new SicsProxy();
		ISicsConnectionContext context = new SicsConnectionContext("localhost",
				60103, SicsRole.USER, "sydney");
		try {
			proxy.login(context);
			proxy.send("hlist /", new SicsCallbackAdapter() {
				public void receiveReply(ISicsData response) {
					// System.out.println(response.getObject().toString());
					setCallbackCompleted(true);
				}
			});
			proxy.disconnect();
		} catch (SicsIOException e) {
			e.printStackTrace();
		} catch (SicsExecutionException e) {
			e.printStackTrace();
		}
	}

}
