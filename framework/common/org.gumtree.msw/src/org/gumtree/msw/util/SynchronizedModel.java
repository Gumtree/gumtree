package org.gumtree.msw.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.IModelListener;
import org.gumtree.msw.IRefIdProvider;
import org.gumtree.msw.model.DataSource;

public class SynchronizedModel implements IModel {
	// fields
	private final IModel model;
	
	// construction
	public SynchronizedModel(IModel model) {
		this.model = model;
	}

	// properties
	@Override
	public synchronized IRefIdProvider getIdProvider() {
		return model.getIdProvider();
	}
	@Override
	public synchronized DataSource getXsd() {
		return model.getXsd();
	}
	
	// control
	@Override
	public synchronized boolean hasControl(int clientId) {
		return model.hasControl(clientId);
	}
	@Override
	public synchronized boolean gainControl(int clientId) {
		return model.gainControl(clientId);
	}
	@Override
	public synchronized boolean releaseControl(int clientId) {
		return model.releaseControl(clientId);
	}

	// content
	@Override
	public synchronized boolean reset() {
		return model.reset();
	}
	// properties
	@Override
	public synchronized Object getProperty(Iterable<String> elementPath, String property) {
		return model.getProperty(elementPath, property);
	}
	@Override
	public synchronized Map<String, Object> getProperties(Iterable<String> elementPath) {
		return model.getProperties(elementPath);
	}
	public synchronized boolean validateProperty(Iterable<String> elementPath, String property, Object newValue) {
		return model.validateProperty(elementPath, property, newValue);
	}
	@Override
	public synchronized boolean changeProperty(Iterable<String> elementPath, String property, Object newValue) {
		return model.changeProperty(elementPath, property, newValue);
	}
	// list elements
	@Override
	public synchronized Iterable<String> getListElements(Iterable<String> listPath) {
		return model.getListElements(listPath);
	}
	@Override
	public synchronized boolean addListElement(Iterable<String> listPath, String elementName, int targetIndex) {
		return model.addListElement(listPath, elementName, targetIndex);
	}
	@Override
	public synchronized boolean duplicateListElement(Iterable<String> listPath, String originalElementName, String newElementName) {
		return model.duplicateListElement(listPath, originalElementName, newElementName);
	}
	@Override
	public synchronized boolean deleteListElement(Iterable<String> listPath, String elementName) {
		return model.deleteListElement(listPath, elementName);
	}
	@Override
	public synchronized boolean recoverListElement(Iterable<String> listPath, String elementName) {
		return model.recoverListElement(listPath, elementName);
	}

	// experiment
	@Override
	public synchronized boolean startExperiment() {
		return model.startExperiment();
	}
	@Override
	public synchronized boolean pauseExperiment() {
		return model.pauseExperiment();
	}
	@Override
	public synchronized boolean stopExperiment() {
		return model.stopExperiment();
	}

	// command
	@Override
	public synchronized boolean command(ICommand command) {
		return model.command(command);
	}
	
	// serialization
	@Override
	public synchronized boolean serializeTo(Iterable<String> elementPath, OutputStream stream) {
		return model.serializeTo(elementPath, stream);
	}
	@Override
	public synchronized boolean deserializeFrom(InputStream stream) {
		return model.deserializeFrom(stream);
	}
	
	// listeners
	@Override
	public synchronized void addListener(IModelListener listener) {
		model.addListener(listener);
	}
	@Override
	public synchronized boolean removeListener(IModelListener listener) {
		return model.removeListener(listener);
	}
}
