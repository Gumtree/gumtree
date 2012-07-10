package org.gumtree.ui.terminal;

public interface ICommunicationAdapterDescriptor {

	public String getId();
	
	public String getLabel();
	
	public ICommunicationAdapter createNewAdapter() throws  CommunicationAdapterException;
	
}
