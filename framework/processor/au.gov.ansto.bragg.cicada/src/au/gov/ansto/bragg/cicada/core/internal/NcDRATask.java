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
package au.gov.ansto.bragg.cicada.core.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.DRATask;
import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;

/**
 * @author nxi
 * Created on 04/06/2008
 */
public class NcDRATask extends NcGroup implements DRATask {

	public final static String ALGORITHM_SET_ID_LABEL = "algorithmSetID";
	public final static String ALGORITHM_NAME_LABEL = "algorithmName";
	public final static String ALGORITHM_VERSION_LABEL = "algorithmVersion";
	public final static String ALGORITHM_CONFIGURATION_NAME_LABEL = "algorithmConfigurationName";
	public final static String ALGORITHM_GROUP_LABEL = "algorithmGroup";
	public final static String DATA_GROUP_LABEL = "dataGroup";
	public final static String DATA_SOURCE_ATTRIBUTE_NAME = "signal";
	public final static String DATA_SOURCE_ATTRIBUTE_VALUE = "dataSource";
	public final static String DATA_SOURCE_PATH_NAME = "path";
	public final static String DATA_STRUCTURE_TYPE_NAME = "dataStructureType";
	public final static String DATA_STRUCTURE_TYPE_VALUE = "draTask";
	/**
	 * @param dataset
	 * @param parent
	 * @param shortName
	 * @param init
	 */
	public NcDRATask(NcDataset dataset, NcGroup parent, String shortName,
			boolean init) {
		super(dataset, parent, shortName, init);
		addStringAttribute(DATA_STRUCTURE_TYPE_NAME, DATA_STRUCTURE_TYPE_VALUE);
	}


	public NcDRATask(IGroup parent, String shortName, String algorithmSetID, 
			Algorithm algorithm) throws ConfigurationException{
		this((NcDataset) parent.getDataset(), (NcGroup) parent, shortName, true);
		IGroup algorithmGroup = Factory.createGroup(this, ALGORITHM_GROUP_LABEL, true);
		Factory.createGroup(this, DATA_GROUP_LABEL, true);
		algorithmGroup.addStringAttribute(ALGORITHM_SET_ID_LABEL, algorithmSetID);
		algorithmGroup.addStringAttribute(ALGORITHM_NAME_LABEL, algorithm.getName());
		algorithmGroup.addStringAttribute(ALGORITHM_VERSION_LABEL, algorithm.getVersion());
		algorithmGroup.addStringAttribute(ALGORITHM_NAME_LABEL, algorithm.getName());
			IGroup configuration = algorithm.getConfiguration().toGDMGroup();
			algorithmGroup.addSubgroup(configuration);
			algorithmGroup.addStringAttribute(ALGORITHM_CONFIGURATION_NAME_LABEL, configuration.getShortName());
	}

	public NcDRATask(IGroup from){
		super(from);
	}
	
	private IGroup getAlgorithmGroup(){
		return findGroup(ALGORITHM_GROUP_LABEL);
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.cicada.core.DRATask#getAlgorithmName()
	 */
	public String getAlgorithmName() {
		IAttribute attribute = null;
		try {
			attribute = getAlgorithmGroup().getAttribute(ALGORITHM_NAME_LABEL);
		} catch (Exception e) {
			return null;
		}
		return attribute.getStringValue();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.cicada.core.DRATask#getConfiguration()
	 */
	public IGroup getAlgorithmConfiguration() throws ConfigurationException {
		String configurationGroupName = null;
		try {
			IAttribute attribute = getAlgorithmGroup().getAttribute(
					ALGORITHM_CONFIGURATION_NAME_LABEL);
			configurationGroupName = attribute.getStringValue();
		} catch (Exception e) {
			throw new ConfigurationException("failed to get configuration for algorithm " + e.getMessage(), e);
		}
		return getAlgorithmGroup().findGroup(configurationGroupName);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.cicada.core.DRATask#getDataSourceList()
	 */
	public List<URI> getDataSourceList() throws URISyntaxException {
		IGroup dataGroup = findGroup(DATA_GROUP_LABEL);
		List<URI> uriList = new ArrayList<URI>();
		for (Object item : dataGroup.getDataItemList()){
			IDataItem dataSource = (IDataItem) item;
			if (dataSource.hasAttribute(DATA_SOURCE_ATTRIBUTE_NAME, DATA_SOURCE_ATTRIBUTE_VALUE))
				try {
					IAttribute pathAttribute = dataSource.getAttribute(DATA_SOURCE_PATH_NAME);
					if (pathAttribute != null)
						uriList.add(new URI(pathAttribute.getStringValue()));
				} catch (Exception e) {
					throw new URISyntaxException("uri not valid " + e.getMessage(), dataSource.getShortName());
				}
		}
		return uriList;
	}

	public List<String> getEntryIDs(URI uri){
		IGroup dataGroup = findGroup(DATA_GROUP_LABEL);
		IDataItem dataSource = dataGroup.findDataItem(uri.toString());
		if (dataSource == null) 
			return null;
		List<String> entryNames = new ArrayList<String>();
		try {
			String entryNameValue = (String) dataSource.getData().toString();
			String[] names = entryNameValue.split(":");
			for (String name : names) {
				entryNames.add(name);
			}
		} catch (Exception e) {
			return null;
		}
		return entryNames;
	}

	public void addDataSource(URI fileURI, String entryName) throws ConfigurationException {
		IGroup dataGroup = findGroup(DATA_GROUP_LABEL);
		IDataItem dataSource = null;
//		String entryNameValue = "";
//		for (String entryName : entryNames){
//			entryNameValue += entryName + ":";
//		}
//		entryNameValue = entryNameValue.substring(0, entryNameValue.length() - 1);
		String sourceName = fileURI.toString().replace("/", "_");
		dataSource = dataGroup.findDataItem(sourceName);
		if (dataSource == null){
			try {
				dataSource = Factory.createDataItem(dataGroup, sourceName,
						Factory.createArray(entryName.toCharArray()));
			} catch (InvalidArrayTypeException e) {
				throw new ConfigurationException("can not create input source for configuration " + 
						e.getMessage(), e);
			}		
			dataSource.addStringAttribute(DATA_SOURCE_ATTRIBUTE_NAME, DATA_SOURCE_ATTRIBUTE_VALUE);
			dataSource.addStringAttribute(DATA_SOURCE_PATH_NAME, fileURI.toString());
			dataGroup.addDataItem(dataSource);
		}else{
			try {
				String entryNameValue = dataSource.getData().toString();
				String[] entryNames = entryNameValue.split(":");
				boolean isExisting = false;
				for (String name : entryNames){
					if (name.equals(entryName)){
						isExisting = true;
						break;
					}
				}
				if (! isExisting){
					entryNameValue = entryNameValue + ":" + entryName;
					dataSource.setCachedData(Factory.createArray(entryNameValue.toCharArray()), 
							false);
				}
			} catch (Exception e) {
				throw new ConfigurationException("failed to create configuration " + e.getMessage(), e);
			}
		}
	}


	public String getAlgorithmSetId() {
		IAttribute attribute = null;
		try {
			attribute = getAlgorithmGroup().getAttribute(ALGORITHM_SET_ID_LABEL);
		} catch (Exception e) {
			return null;
		}
		return attribute.getStringValue();
	}


	public String getAlgorithmVersion() {
		IAttribute attribute = null;
		try {
			attribute = getAlgorithmGroup().getAttribute(ALGORITHM_VERSION_LABEL);
		} catch (Exception e) {
			return null;
		}
		return attribute.getStringValue();
	}


	public boolean isValid() {
		return hasAttribute(DATA_STRUCTURE_TYPE_NAME, DATA_STRUCTURE_TYPE_VALUE);
	}

}
