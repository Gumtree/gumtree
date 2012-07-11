/******************************************************************************* 
* Copyright (c) 2009 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong  (based on Class PrepareSourcePlot)
*    Paul Hathaway (13/7/2009)
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.data.Factory;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IKey;
import org.gumtree.service.cli.ICommandLineOptions;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.Util;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class MultiDimSource extends ConcreteProcessor {

	/* Fields for audit trail support */
	private static final String processClass = "MultiDimSource"; 
	private static final String processClassVersion = "2.0"; 
	private static final long processClassID = 2009072101; 

	/* Fields to support client-side processing */
	private static DataStructureType dataStructureType = DataStructureType.nexusData;
	private static DataDimensionType dataDimensionType = DataDimensionType.extended;
    private Boolean isDebugMode = true;

    private static final String DEFAULT_DRA_PLUGIN = "au.gov.ansto.bragg.quokka.dra";
    private static final String OPTION_ALGO_SET = "algoSet";
    private static final String REDUCTION_DIC = "\\xml\\reduction.dic.txt";
    private String pluginID;

    private static final String SOURCE_GROUP_NAME = "sourcecopy";
    
	private IGroup inGroup;
	private IGroup outGroup;
	
	private Map<String,IDataItem> scan = new HashMap<String,IDataItem>();
	
	private Boolean doStop = false;
	private Boolean flag = false;
	
	private String axis0;
	private String axis1;
	private String axis2;
	private String axis3;
	
	private Integer dim0 = 0;
	private Integer dim1 = 0;
	private Integer dim2 = 0;
	private Integer dim3 = 0;
	
	public MultiDimSource() {
		this.setReprocessable(false);
		pluginID = findPluginID();
	}
	
	//-----------------------------------------------------------------------
	public Boolean process() throws Exception {
		if (inGroup == null) {
			throw new Exception("Input Group not detected");
		}				
		flag = checkStructure(outGroup);
		if (!flag) {
			outGroup = prepareStructure(inGroup); //outGroup is a nexus Entry
			flag = checkStructure(outGroup);
		}
		if (flag) {
			IGroup axesGroup = outGroup.getGroupWithAttribute(
					Util.NEXUS_CLASS_ATTRIBUTE_NAME, 
					Util.NEXUS_DATA_ATTRIBUTE_VALUE);
			IGroup root = outGroup.getRootGroup();		
			IGroup source = root.findGroup(SOURCE_GROUP_NAME); 
			IDataItem histogram = source.getDataItemWithAttribute(
					Util.NEXUS_SIGNAL_ATTRIBUTE_NAME,"1");
			parseAxes(axesGroup,histogram);
		}
		return doStop;
	}
	//-----------------------------------------------------------------------

	private boolean checkStructure(IGroup entry) throws FileAccessException {
		if (null==entry) { return false; }
		
		IGroup root = entry.getRootGroup();		
		if (null==root) { throw new FileAccessException("Invalid file structure"); }
		
		// Find Group with Group attribute "NX_class" having value "NXdata" 
		IGroup nxData = entry.getGroupWithAttribute(
				Util.NEXUS_CLASS_ATTRIBUTE_NAME, 
				Util.NEXUS_DATA_ATTRIBUTE_VALUE);
		if (null==nxData) { throw new FileAccessException("Invalid file structure"); }

		IGroup source = root.findGroup(SOURCE_GROUP_NAME); 
		boolean isProcessed = (null!=source);
		if (isProcessed) {
			IDataItem histogram = source.getDataItemWithAttribute("signal","1");
			isProcessed = (null!=histogram);
		}
		return isProcessed;
	}
	
	private IGroup prepareStructure(IGroup entry) {
		IGroup root = entry.getRootGroup();
		
		// Find Group with Group attribute "NX_class" having value "NXdata" 
		IGroup nxData = entry.getGroupWithAttribute(
				Util.NEXUS_CLASS_ATTRIBUTE_NAME, 
				Util.NEXUS_DATA_ATTRIBUTE_VALUE);

		IDataItem data = nxData.getDataItemWithAttribute(Util.NEXUS_SIGNAL_ATTRIBUTE_NAME,"1");
		IGroup sourceCopy = Factory.createGroup(root,"sourcecopy",true);
		sourceCopy.addDataItem(data);
		nxData.removeDataItem(data);

		/* Remove detector histogram */
		IGroup instrument = entry.getGroupWithAttribute("NX_class","NXinstrument");
		IGroup detector = instrument.getGroupWithAttribute("NX_class","NXdetector");	
		IDataItem hmm = detector.getDataItemWithAttribute("signal","1");
		detector.removeDataItem(hmm);

		mapScanItems(scan,entry,nxData,sourceCopy);		
		return entry;
	}
	
	private String findPluginID() {
		String pluginId = DEFAULT_DRA_PLUGIN;
		ICommandLineOptions options = ServiceUtils.getService(ICommandLineOptions.class);
		if (null!=options) {
			if (options.hasOptionValue(OPTION_ALGO_SET)) {
				pluginId = options.getOptionValue(OPTION_ALGO_SET);
			}
		}
		return pluginId;
	}

	private int mapScanItems(Map<String,IDataItem> scn, IGroup entry, IGroup nxData, IGroup srcCopy) {
		IGroup root = entry.getRootGroup();
		IDataItem data = srcCopy.getDataItemWithAttribute(Util.NEXUS_SIGNAL_ATTRIBUTE_NAME,"1");
		int[] shape = data.getShape();
		int numFrames = shape[0];
		try {
			IDictionary dict = initDictionary(entry);
			for(IKey key : dict.getAllKeys()) {
				IPath path = dict.getPath(key);
				Object oItem = null;
				try {
					oItem = root.findContainerByPath(path.getValue());
				} catch (Exception e) { }
				if (oItem instanceof IDataItem) {
					IDataItem item = (IDataItem) oItem;
					if (numFrames==item.getSize()) {
						scn.put(path.getValue(),item);
						if (isDebugMode) {
							System.out.println("> Mapping DataItem: "+item.getName()+" <");
						}
					}
				}
			}
			mapDataItem(scn,nxData,"total_counts");
			mapDataItem(scn,nxData,"run_number");			
			if (isDebugMode) {
				System.out.println("Number of DataItems to be indexed: "+scan.size());
			}
		} catch (FileAccessException e) {
			if (isDebugMode) {
				e.printStackTrace();
			}
		}
		return numFrames;
	}
	
	private IDictionary initDictionary(IGroup entry) throws FileAccessException {
		File redFile = ConverterLib.findFile(pluginID,REDUCTION_DIC);
		String filepath = redFile.getAbsolutePath();
		IDictionary dictionary = Factory.createDictionary();
		dictionary.readEntries(filepath);
		entry.setDictionary(dictionary);
		return dictionary;
	}
	
	private int mapDataItem(Map<String,IDataItem> scn, IGroup parent, String pname) {
		IDataItem item = parent.findDataItem(pname);
		if(null!=item) {
			scn.put(item.getName(),item);
			if (isDebugMode) {
				System.out.println("> Mapping DataItem: "+item.getName()+" <");
			}
		}
		return scn.size();
	}
	
	private void parseAxes(IGroup axesGroup, IDataItem histogram) {
		
		int rank = histogram.getRank();
		int[] shape = histogram.getShape();
		IDataItem axis;		
		
		List<IDataItem> axes = new ArrayList<IDataItem>();
		IAttribute axesAttribute = histogram.findAttributeIgnoreCase(Util.NEXUS_AXES_ATTRIBUTE_NAME);
		if (null!=axesAttribute) { 
			String[] names = axesAttribute.getStringValue().split(":");
			for (String name : names) {
				axis = axesGroup.findDataItem(name);
				if (null!=axis) { axes.add(axis); }
			}
		}

		int length  = axes.size();
		if ((0<length) && (0<rank)) {
			axis = axes.get(0);
			this.axis0 = axis.getName();
			this.dim0  = axis.getShape()[0]; //hm shape[0];
			super.informVarValueChange("axis0", axis0);
			super.informVarValueChange("dim0", dim0);
		}
		
		if ((1<length) && (1<rank)) {
			axis = axes.get(1);
			this.axis1 = axis.getName();
			this.dim1  = shape[1]; //axis.getShape()[0] - 1; //hm shape[1];
			super.informVarValueChange("axis1", axis1);
			super.informVarValueChange("dim1", dim1);
		}

		if ((2<length) && (2<rank)) {
			axis = axes.get(2);
			this.axis2 = axis.getName();
			this.dim2  = shape[2]; //axis.getShape()[0] - 1; //hm shape[2];
			super.informVarValueChange("axis2", axis2);
			super.informVarValueChange("dim2", dim2);
		}

		if ((3<length) && (3<rank)) {
			axis = axes.get(3);
			this.axis3 = axis.getName();
			this.dim3  = shape[3]; //axis.getShape()[0] - 1; //hm shape[3];
			super.informVarValueChange("axis3", axis3);
			super.informVarValueChange("dim3", dim3);
		}
	}
	
	/**
	 * @return the source reader output Group
	 */
	public IGroup getOutGroup() {
		return outGroup;
	}

	public Boolean getFlag() {
		return flag;
	}
	
	public Map<String,IDataItem> getScan() {
		return scan;
	}

	/**
	 * @param inGroup the source reader input Group to set
	 */
	public void setInGroup(IGroup inGroup) {
		this.inGroup = inGroup;
	}

	/**
	 * @param stop  - source reader set to stop
	 */
	public void setStop(Boolean doStop) {
		this.doStop = doStop;
	}
	
	/* Client Support methods -----------------------------------------*/	

	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}
	public void setIsDebugMode(Boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
	}

	public Boolean getIsDebugMode() {
		return isDebugMode;
	}	
	

	public void setAxis0(String axis0) { /* UI read only */ }
	public void setAxis1(String axis1) { /* UI read only */ }
	public void setAxis2(String axis2) { /* UI read only */ }
	public void setAxis3(String axis3) { /* UI read only */ }

	public void setDim0(Integer dim0) { /* UI read only */ }
	public void setDim1(Integer dim1) { /* UI read only */ }
	public void setDim2(Integer dim2) { /* UI read only */ }
	public void setDim3(Integer dim3) { /* UI read only */ }
}
