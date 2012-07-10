package org.gumtree.gumnix.sics.internal.ui;

import org.gumtree.ui.terminal.CommunicationAdapterException;
import org.gumtree.ui.terminal.ICommunicationConfigPart;
import org.gumtree.ui.terminal.ITerminalOutputBuffer;
import org.gumtree.ui.terminal.support.telnet.TelnetAdapter;

public class SicsTelnetAdapter extends TelnetAdapter {

	private SicsTelnetConfigPart part;
	
	public ICommunicationConfigPart createConfigPart() {
		part = new SicsTelnetConfigPart();
		return part;
	}

	public SicsConnectionContext getConnectionContext() {
		if(part != null) {
			return part.getConnectionContext();
		}
		return null;
	}
	
	public void connect(final ITerminalOutputBuffer outputBuffer) throws CommunicationAdapterException {
		super.connect(outputBuffer);
		send("sicslogin " + getConnectionContext().getLogin() + " " + getConnectionContext().getPassword());
	}
	
}
