package org.gumtree.ui.scripting.viewer;

public interface ICommandHistoryList {

	public void appendCommand(String command);
	
	public void clearCommands();
	
	public String get(int index);
	
	public String[] getCommands();
	
	public int size();
	
	public boolean isEmpty();
	
}
