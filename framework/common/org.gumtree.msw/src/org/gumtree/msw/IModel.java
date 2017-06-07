package org.gumtree.msw;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.gumtree.msw.model.DataSource;

public interface IModel {
	// finals
	public static final String INDEX = "Index";		// used for list-elements
	
	// properties
	public IRefIdProvider getIdProvider();
	public DataSource getXsd();
	
	// control
	public boolean hasControl(int clientId);
	public boolean gainControl(int clientId);
	public boolean releaseControl(int clientId);
	
	// content
	public boolean reset();
	// properties
	public Object getProperty(Iterable<String> elementPath, String propertyName);
	public Map<String, Object> getProperties(Iterable<String> elementPath);
	public boolean validateProperty(Iterable<String> elementPath, String property, Object newValue);
	public boolean changeProperty(Iterable<String> elementPath, String property, Object newValue);
	// list elements
	public Iterable<String> getListElements(Iterable<String> listPath);
	public boolean addListElement(Iterable<String> listPath, String elementName, int targetIndex);
	public boolean duplicateListElement(Iterable<String> listPath, String originalElementName, String newElementName);
	public boolean deleteListElement(Iterable<String> listPath, String elementName);
	public boolean recoverListElement(Iterable<String> listPath, String elementName);
	
	// experiment
	public boolean startExperiment();
	public boolean pauseExperiment();
	public boolean stopExperiment();

	// command
	public boolean command(ICommand command);
	
	// serialization
	public boolean serializeTo(Iterable<String> elementPath, OutputStream stream);
	public boolean deserializeFrom(InputStream stream);
	
	// listeners
	public void addListener(IModelListener listener);
	public boolean removeListener(IModelListener listener);
}
