package org.gumtree.ui.terminal;

public interface ICommandLineTerminal {

	public void selectCommunicationAdapter(String adapterId);

	public void connect() throws CommunicationAdapterException;

}
