/******************************************************************************* 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.cicada.core;

import java.net.URI;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.internal.NcTunerConfiguration;
import au.gov.ansto.bragg.process.port.Tuner;

/**
 * @author nxi
 * Created on 23/04/2008
 */
public class AlgorithmConfiguration {

	private TunerConfiguration chainConfiguration;
	private URI path;
	private String comments;
	public enum AlgorithmType{analysis, experiment};
//	private Algorithm algorithm;

	public AlgorithmConfiguration(Algorithm algorithm, URI path, String name) 
	throws ConfigurationException{
		super();
		this.path = path;
//		this.algorithm = algorithm;
		try{
			chainConfiguration = new NcTunerConfiguration(Factory.createEmptyDatasetInstance().getRootGroup(), 
					name, algorithm.getClassID(), algorithm.getName());
			updateTunerConfiguration(algorithm.getTunerArray());
			updateSinkConfiguration(algorithm.getDefaultSinkName());
		}catch (Exception e) {
			throw new ConfigurationException("failed to create configuration for algorithm " + 
					algorithm.getName() + "\n" + e.getMessage(), e);
		}
	}

	public AlgorithmConfiguration(Algorithm algorithm, IDataset dataset, String name) 
	throws ConfigurationException{
		super();
		try{
			chainConfiguration = new NcTunerConfiguration(dataset.getRootGroup(), 
					name, algorithm.getClassID(), algorithm.getName());
			updateTunerConfiguration(algorithm.getTunerArray());
			updateSinkConfiguration(algorithm.getDefaultSinkName());
		}catch (Exception e) {
			throw new ConfigurationException("failed to create configuration for algorithm " + 
					algorithm.getName() + "\n" + e.getMessage(), e);
		}
	}

	public AlgorithmConfiguration(Algorithm algorithm, URI path, String name, String[] tunerNames) 
	throws ConfigurationException{
		super();
		this.path = path;
//		this.algorithm = algorithm;
		try{
			chainConfiguration = new NcTunerConfiguration(Factory.createEmptyDatasetInstance().getRootGroup(), 
					name, algorithm.getClassID(), algorithm.getName());
			List<Tuner> tunerArray = algorithm.getTunerArray();
			for (String tunerName : tunerNames){
				updateTunerConfiguration(tunerArray, tunerName);
			}
			updateSinkConfiguration(algorithm.getDefaultSinkName());
		}catch (Exception e) {
			throw new ConfigurationException("failed to create configuration for algorithm " + 
					algorithm.getName() + "\n" + e.getMessage(), e);
		}
	}
//	public AlgorithmConfiguration(URI fileURI) throws LoadAlgorithmFileFailedException {
//	this.path = fileURI;
//	Document file = null;
//	try {
//	file = Parse.readFile(new File(fileURI));
//	} catch (DocumentException e) {
////	e.printStackTrace();
//	throw new LoadAlgorithmFileFailedException(e.getMessage() + ": open file failed");
//	}
//	Element rootElement = file.getRootElement();
//	String configurationName = rootElement.attributeValue("name");
//	String recipeId = rootElement.attributeValue("recipeID");
//	String algorithmName = rootElement.attributeValue("algorithmName");
//	chainConfiguration = );
//	for (Iterator<?> iter = rootElement.elementIterator(); iter.hasNext();){
//	Element item = (Element)iter.next();
//	String tunerName = item.getName();
//	String tunerValue = item.getStringValue();
//	if (tunerName.matches("defaultSinkName"))
//	chainConfiguration.setDefaultSinkName(tunerValue);
//	else
//	chainConfiguration.addTunerConfiguration(tunerName, tunerValue);
//	}
//	}

	public AlgorithmConfiguration(IGroup configurationGroup, URI uri) throws ConfigurationException{
		path = uri;
		if (configurationGroup instanceof TunerConfiguration)
			this.chainConfiguration = (TunerConfiguration) configurationGroup;
		else 
			try {
				chainConfiguration = new NcTunerConfiguration(configurationGroup);
			} catch (Exception e) {
				throw new ConfigurationException("failed to load configuration " + 
						uri.getPath() + "\n" + e.getMessage(), e);
			}
	}

	public AlgorithmConfiguration(IGroup configurationGroup) throws ConfigurationException {
		if (configurationGroup instanceof TunerConfiguration)
			this.chainConfiguration = (TunerConfiguration) configurationGroup;
		else 
			try {
				chainConfiguration = new NcTunerConfiguration(configurationGroup);
			} catch (Exception e) {
				throw new ConfigurationException("failed to create configuration from data" + 
						configurationGroup.getName() + "\n" + e.getMessage(), e);
			}
	}
	
	private void updateTunerConfiguration(List<Tuner> tunerArray) throws ConfigurationException {
		if (tunerArray == null)
			return;
		for (Tuner tuner : tunerArray){
			Object signal = tuner.getSignal();
			if (signal instanceof IGroup) {
				IGroup groupValue = (IGroup) signal;
				try {
					chainConfiguration.addTunerConfiguration(tuner.getName(), groupValue);
				} catch (InvalidArrayTypeException e) {
					throw new ConfigurationException("failed to load value for tuner " + tuner.getName()
							+ ": " + e.getMessage(), e);
				}
			} else{
				String tunerValue = signal == null ? "null" : signal.toString();
				try {
					chainConfiguration.addTunerConfiguration(tuner.getName(), tunerValue);
				} catch (InvalidArrayTypeException e) {
					throw new ConfigurationException("failed to load value for tuner " + tuner.getName()
							+ ": " + e.getMessage(), e);
				}
			}
		}
	}

	private void updateTunerConfiguration(List<Tuner> tunerArray, String tunerName) 
	throws ConfigurationException {
		for (Tuner tuner : tunerArray){
			if (tuner.getCoreName().equals(tunerName)){
				Object signal = tuner.getSignal();
				if (signal instanceof IGroup) {
					IGroup groupValue = (IGroup) signal;
					try {
						chainConfiguration.addTunerConfiguration(tuner.getName(), groupValue);
					} catch (InvalidArrayTypeException e) {
						throw new ConfigurationException("failed to load value for tuner " + tuner.getName()
								+ ": " + e.getMessage(), e);
					}
				} else{
					String tunerValue = signal == null ? "null" : signal.toString();
					try {
						chainConfiguration.addTunerConfiguration(tuner.getName(), tunerValue);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(tuner.getName());
						System.out.println(tunerValue);
						throw new ConfigurationException("failed to load value for tuner " + tuner.getName()
								+ ": " + e.getMessage(), e);
					}
				}
				break;
			}
		}
	}
	
	private void updateSinkConfiguration(String defaultSinkName) 
	{
		if (defaultSinkName == null || defaultSinkName.trim().length() == 0)
			return;
		try {
			chainConfiguration.setDefaultSinkName(defaultSinkName);
		} catch (InvalidArrayTypeException e) {
		}

	}

	public IGroup toGDMGroup() {
//		Group group = Factory.createGroup("AlgorithmConfiguration");
//		Group rootGroup = group.getRootGroup();
////		rootGroup.addOneAttribute(Factory.createAttribute("version", "1.0"));
////		rootGroup.addOneAttribute(Factory.createAttribute("encoding", "UTF-8"));
//		DataItem comments = Factory.createDataItem(rootGroup, "comments", 
//		Factory.createArray(("Configurations for " +
//		"Algorithm " + chainConfiguration.getAlgorithmName() + ". ").toCharArray()));
//		comments.addStringAttribute("signal", "comments");
//		rootGroup.addDataItem(comments);
//		group.addOneAttribute(Factory.createAttribute("name", chainConfiguration.getName()));
//		group.addOneAttribute(Factory.createAttribute("recipeID", 
//		chainConfiguration.getRecipeID()));
//		group.addOneAttribute(Factory.createAttribute("algorithmName", 
//		chainConfiguration.getAlgorithmName()));
//		Set<String> tunerNames = chainConfiguration.getTunerNameSet();
//		for (String tunerName : tunerNames){
//		try {
//		Object tunerValue = chainConfiguration.getConfiguration(tunerName);
//		String tunerValueString = tunerValue == null ? "null" : tunerValue.toString();
//		DataItem configuration = Factory.createDataItem(group, tunerName, 
//		Factory.createArray(tunerValueString.toCharArray()));
//		group.addDataItem(configuration);
//		} catch (Exception e) {
//		throw new InvalidArrayTypeException("can not create configuration for " + 
//		tunerName);
//		}	
//		}
//		String sinkName = chainConfiguration.getDefaultSinkName();
//		if (sinkName != null){
//		DataItem sinkConfiguration = Factory.createDataItem(group, "defaultSinkName", 
//		Factory.createArray(sinkName.toCharArray()));
//		group.addDataItem(sinkConfiguration);
//		}
//		rootGroup.addSubgroup(group);
		return chainConfiguration;
	}

	/**
	 * @return the path
	 */
	public URI getPath() {
		return path;
	}

	public String getName(){
		return chainConfiguration.getName();
	}

	public Object getConfiguration(String tunerName){
		return chainConfiguration.getTunerValue(tunerName);
	}

	public List<String> getTunerNameSet(){
		return chainConfiguration.getTunerNameList();
	}

	public String getRecipeID() {
		return chainConfiguration.getRecipeID();
	}

	public String getAlgorithmName() {
		return chainConfiguration.getAlgorithmName();
	}

	public String getDefaultSinkName() {
		String sinkName = null;
		try {
			chainConfiguration.getDefaultSinkName();
		} catch (Exception e) {
		}
		return sinkName;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	public TunerConfiguration getTunerConfiguration(){
		return chainConfiguration;
	}
	
	
}
