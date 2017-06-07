package org.gumtree.msw.util;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModelListener;

public class ModelListenerAdapter implements IModelListener {
	// control
	@Override
	public void onGainedControl(int clientId) {
	}
	@Override
	public void onReleasedControl(int clientId) {
	}
	
	// content
	@Override
	public void onReset() {
	}
	// properties
	@Override
	public void onChangedProperty(Iterable<String> elementPath, String propertyName, Object oldValue, Object newValue) {
	}
	// list elements
	@Override
	public void onAddedListElement(Iterable<String> listPath, String elementName) {
	}
	@Override
	public void onDeletedListElement(Iterable<String> listPath, String elementName) {
	}
	@Override
	public void onRecoveredListElement(Iterable<String> listPath, String elementName) {
	}
	
	// experiment
	@Override
	public void onStartedExperiment() {
	}
	@Override
	public void onPausedExperiment() {
	}
	@Override
	public void onStoppedExperiment() {
	}

	// command
	@Override
	public void onCommandSucceeded(ICommand command, ICommand undoCommand) {
	}
	@Override
	public void onCommandFailed(ICommand command) {
	}
}
