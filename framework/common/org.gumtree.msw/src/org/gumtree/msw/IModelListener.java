package org.gumtree.msw;

public interface IModelListener {
	// control
	public void onGainedControl(int clientId);
	public void onReleasedControl(int clientId);
	
	// content
	public void onReset();
	// properties
	public void onChangedProperty(Iterable<String> elementPath, String propertyName, Object oldValue, Object newValue);
	// list elements
	public void onAddedListElement(Iterable<String> listPath, String elementName);
	public void onDeletedListElement(Iterable<String> listPath, String elementName);
	public void onRecoveredListElement(Iterable<String> listPath, String elementName);

	// experiment
	public void onStartedExperiment();
	public void onPausedExperiment();
	public void onStoppedExperiment();
	
	// command
	public void onCommandSucceeded(ICommand command, ICommand undoCommand);
	public void onCommandFailed(ICommand command);
}
