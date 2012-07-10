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

import java.io.BufferedReader;
import java.io.IOException;

import org.gumtree.gumnix.sics.core.io.ISicsChannel;
import org.gumtree.gumnix.sics.core.io.ISicsProxy;
import org.gumtree.gumnix.sics.core.io.ISycamoreResponse;
import org.gumtree.gumnix.sics.core.io.ISicsChannel.ChannelState;

public class SicsListener implements Runnable {

	private BufferedReader input;
	private ISicsChannel channel;
	
	public SicsListener(BufferedReader input, ISicsChannel channel) {
		this.input = input;
		this.channel = channel;
	}

	private BufferedReader getInput() {
		return input;
	}

	private ISicsChannel getChannel() {
		return channel;
	}
	
	public void run() {
		try {
			String replyMessage;
			while ((replyMessage = getInput().readLine()) != null) {
				// little hack to sics telnet bug
				while (replyMessage.startsWith("ящ"))
					replyMessage = replyMessage.substring(2);
				replyMessage = replyMessage.trim();
				debug("SICS Reply: " + replyMessage);
				
				if(getChannel().getChannelState() == ChannelState.DISCONNECTED) {
					handleDisconnectedState(replyMessage);
					continue;
				}
				
				if(getChannel().getChannelState() == ChannelState.LOGIN) {
					handleLoginState(replyMessage);
					continue;
				}
				
				if(getChannel().getChannelState() == ChannelState.CONNECTED) {
					handleConnectedState(replyMessage);
					continue;
				}
				
				if(getChannel().getChannelState() == ChannelState.SYCAMORE) {
					handleSycamoreState(replyMessage);
					continue;
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void handleDisconnectedState(String replyMessage) {
		if(replyMessage.equalsIgnoreCase("OK"))
			getChannel().setChannelState(ChannelState.LOGIN);
	}
	
	private void handleLoginState(String replyMessage) {
		if(replyMessage.equalsIgnoreCase("Login OK"))
			getChannel().setChannelState(ChannelState.CONNECTED);
	}
	
	private void handleConnectedState(String replyMessage) {
		if(replyMessage.length() > 0)
			getChannel().setChannelState(ChannelState.SYCAMORE);
	}
	
	private void handleSycamoreState(String replyMessage) {
		if(replyMessage == null || replyMessage.length() == 0)
			return;
		ISycamoreResponse response = new SycamoreResponse(replyMessage);
		getChannel().handleResponse(response);
	}
	
    private void debug(String message) {
        if(System.getProperty(ISicsProxy.SYSTEM_PROPERTY_DEBUG) != null) {
            System.out.println(message);
        }
    }
	
}
