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

import org.gumtree.gumnix.sics.core.io.ISicsChannel;
import org.gumtree.gumnix.sics.core.io.ISicsInterestListener;
import org.gumtree.gumnix.sics.core.io.ISicsProxy;
import org.gumtree.gumnix.sics.core.io.ISicsProxyContext;
import org.gumtree.gumnix.sics.core.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.core.io.SicsIOException;
import org.gumtree.gumnix.sics.core.io.ISicsChannel.ChannelType;

public class SicsProxy implements ISicsProxy {
	private ISicsChannel generalChannel;

	private ISicsChannel interestChannel;

	private ISicsProxyContext context;

	public SicsProxy(ISicsProxyContext context) {
		this.context = context;
	}

	public void initialise() throws SicsIOException {
		if (getGeneralChannel() != null && getGeneralChannel() != null)
			throw new SicsIOException("Connection has already been established");
		if (getGeneralChannel() == null)
			setGeneralChannel(new SicsChannel(getProxyContext().getHost(),
					getProxyContext().getPort(), getProxyContext().getLogin(),
					getProxyContext().getPassword(), ChannelType.GENERAL));
		if (getInterestChannel() == null)
			setInterestChannel(new SicsChannel(getProxyContext().getHost(),
					getProxyContext().getPort(), getProxyContext()
							.getInterestLogin(), getProxyContext()
							.getInterestPassword(), ChannelType.INTEREST));
	}

	public void send(String command, ISicsProxyListener proxyListener)
			throws SicsIOException {
		if (getGeneralChannel() != null)
			getGeneralChannel().send(command, proxyListener);
		else
			new SicsIOException("Connection has not been initialised");
	}

	public void shutdown() throws SicsIOException {
		if (getGeneralChannel() != null)
			getGeneralChannel().logout();
		if (getInterestChannel() != null)
			getInterestChannel().logout();
	}

	public void addInterestListner(String sicsObjectId,
			ISicsInterestListener interestListener) throws SicsIOException {
		getInterestChannel().addInterestListner(sicsObjectId, interestListener);
	}

	public void removeInterestListener(String sicsObjectId,
			ISicsInterestListener interestListener) throws SicsIOException {
		getInterestChannel().removeInterestListener(sicsObjectId,
				interestListener);
	}

	private ISicsChannel getGeneralChannel() {
		return generalChannel;
	}

	private ISicsChannel getInterestChannel() {
		return interestChannel;
	}

	public void setGeneralChannel(ISicsChannel generalChannel) {
		this.generalChannel = generalChannel;
	}

	public void setInterestChannel(ISicsChannel interestChannel) {
		this.interestChannel = interestChannel;
	}

	public ISicsProxyContext getProxyContext() {
		return context;
	}
}
