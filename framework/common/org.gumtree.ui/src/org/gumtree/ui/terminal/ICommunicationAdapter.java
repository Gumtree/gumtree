package org.gumtree.ui.terminal;

import org.eclipse.jface.action.IAction;

public interface ICommunicationAdapter {

	public ICommunicationConfigPart createConfigPart();

	public void connect(ITerminalOutputBuffer outputBuffer) throws CommunicationAdapterException;

	public void send(String text) throws CommunicationAdapterException;

	public IAction[] getToolActions();

	public void disconnect();

	public boolean isConnected();

}
