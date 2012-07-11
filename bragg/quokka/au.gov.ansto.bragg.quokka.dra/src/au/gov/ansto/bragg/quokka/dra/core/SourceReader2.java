/******************************************************************************* 
* Copyright (c) 2009 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Paul Hathaway (26/08/2009)
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
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Data;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.core.internal.DataLib;

public class SourceReader2 extends ConcreteProcessor{
	
	/* Fields for audit trail support */
	private static final String processClass = "SourceReader2"; 
	private static final String processClassVersion = "2.0"; 
	private static final long processClassID = 2009082600; 

	/* Fields to support client-side processing */
	private static DataStructureType dataStructureType = DataStructureType.plot;
	private static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = false;

    private static final String DEFAULT_DRA_PLUGIN = "au.gov.ansto.bragg.quokka.dra";
    private static final String OPTION_ALGO_SET = "algoSet";
    private static final String REDUCTION_DIC = "/xml/reduction.dic.txt";
    private String pluginID;

    private static final String RESULT_GROUP_NAME = "result";
    
	private IGroup inGroup;
	private IGroup outGroup;
	private Plot  result;
	
	private Map<String,IDataItem> redict = new HashMap<String,IDataItem>();
	
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
	
	public SourceReader2() {
		this.setReprocessable(false);
		pluginID = findPluginID();
	}
	
	//-----------------------------------------------------------------------
	public Boolean process() throws Exception {
		//clearAxes();
		if (this.inGroup == null) {
			throw new Exception("Input Group not detected");
		}				
		this.outGroup = this.inGroup;
		flag = checkStructure(this.inGroup);
		doStop = doStop || (!flag);
		try {
			if(flag) {
				mapDictionary(redict,this.inGroup);		
				initStructure(this.inGroup);
				IGroup nxData = getDataGroup(this.inGroup);
				/* generate result Group including copy of nxData as Plot */
				result = regeneratePlot(
						this.outGroup,
						nxData,
						RESULT_GROUP_NAME,
						SourceReader2.dataDimensionType);
				if (null!=result) {
					DataLib.generateTitle(result);
					showAxes(result);
				}
			} 
			if ((!flag) || (null==result)) {
				result = PlotFactory.createPlot(RESULT_GROUP_NAME,dataDimensionType);				
				result.setTitle("No Data");
			}
		} catch (PlotFactoryException e) {
			String msg="Unable to create result plot";
			debugging(msg);
			throw new PlotFactoryException(msg);
		}
		return doStop;
	}
	//-----------------------------------------------------------------------

	private void debugging(String msg) {
		if(isDebugMode) {
			System.out.println(processClass+": "+msg);
		}		
	}
	
	private void debugging(String method, String msg) {
		if(isDebugMode) {
			System.out.println(processClass+"."+method+": "+msg);
		}		
	}

	/**
	 * @param entry Group attached to dataset structure 
	 * @return true if "signal" data can be located in nxData structure
	 * @throws FileAccessException
	 */
	private boolean checkStructure(IGroup entry) throws FileAccessException {
		if (null==entry) { return false; }
		
		IGroup root = entry.getRootGroup();		
		if (null==root) { 
			throw new FileAccessException("Invalid file structure"); 
		}
		// Find Group with Group attribute "NX_class" having value "NXdata" 
		IGroup nxData = getDataGroup(entry); 		
		if (null==nxData) { 
			throw new FileAccessException("Invalid file structure"); 
		}
		return (null!=(nxData.getDataItemWithAttribute("signal","1")));
	}
	
	private IGroup initStructure(IGroup entry) 
		throws PlotFactoryException, FileAccessException 
	{
		/* Remove detector histogram if it exists */
		IGroup instrument = entry.getGroupWithAttribute("NX_class","NXinstrument");
		if (null!=instrument) {
			IGroup detector = instrument.getGroupWithAttribute("NX_class","NXdetector");
			if (null!=detector) {
				IDataItem hmm = detector.getDataItemWithAttribute("signal","1");
				if (null!=hmm) {
					detector.removeDataItem(hmm);
				}
			}
		}
		return entry;
	}
	
	private IDataItem getData(IGroup entry) {
		// Find Group with Group attribute "NX_class" having value "NXdata" 
		IGroup nxData = getDataGroup(entry); 
		IDataItem data = nxData.getDataItemWithAttribute(
				Util.NEXUS_SIGNAL_ATTRIBUTE_NAME,
				"1");
		return data;
	}
	
	private IGroup getDataGroup(IGroup entry) {
		// Find Group with Group attribute "NX_class" having value "NXdata" 
		IGroup nxData = entry.getGroupWithAttribute(
				Util.NEXUS_CLASS_ATTRIBUTE_NAME, 
				Util.NEXUS_DATA_ATTRIBUTE_VALUE);
		return nxData;
	}
	
	private Plot regeneratePlot(IGroup parent,IGroup src,String name,DataDimensionType ddt) 
		throws PlotFactoryException 
	{
		IGroup resGroup = parent.findGroup(name);
		if (null!=resGroup) { 
			parent.removeGroup(resGroup); 
		}
		Plot result = PlotFactory.copyToPlot(parent,src,name,ddt);
		result.setTitle(name);
		return result;
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

	/*
	 * Populate rdic (Map<String,DataItem>)   
	 */
	private int mapDictionary(Map<String,IDataItem> rdic, IGroup parent) 
		throws FileAccessException 
	{
		IGroup root = parent.getRootGroup();
		try {
//			Map<String,String> dict = initDictionary(parent);
			IDictionary dict = initDictionary(parent);
			if (isDebugMode) {
				System.out.println("Mapping DataItems from Entry: "+parent.getShortName());
			}
//			for(String path : dict.values()) {
			for (IKey key : dict.getAllKeys()) {
				IPath path = dict.getPath(key);
				Object oItem = null;
				try {
					oItem = parent.findContainerByPath(path.getValue());
				} catch (Exception e) { }
				if (oItem instanceof IDataItem) {
					IDataItem item = (IDataItem) oItem;
					rdic.put(path.getValue(),item);
					if (isDebugMode) {
						System.out.println("> Mapping DataItem: "+item.getName()+" <");
					}
				}
			}
			if (isDebugMode) {
				System.out.println("Number of DataItems mapped: "+rdic.size());
			}
		} catch (FileAccessException e) {
			String msg = processClass + ".mapDictionary: "
				+ "Unable to initialise dictionary in "
				+ parent.getShortName();
			if (isDebugMode) {
				System.out.println(msg);
			}
			throw new FileAccessException(msg);
		}
		return rdic.size();
	}
	
	/* Reads REDUCTION_DIC into Map<String,String> placed in parent Group
	 */
//	private Map<String,String> initDictionary(IGroup parent) throws FileAccessException {
//		File redFile = ConverterLib.findFile(pluginID,REDUCTION_DIC);
//		String filepath = redFile.getAbsolutePath();
//		parent.initialiseDictionary(filepath);
//		return parent.getDictionary();
//	}
	private IDictionary initDictionary(IGroup parent) throws FileAccessException {
		File redFile = ConverterLib.findFile(pluginID,REDUCTION_DIC);
		String filepath = redFile.getAbsolutePath();
		IDictionary dictionary = Factory.createDictionary();
		dictionary.readEntries(filepath);
		parent.setDictionary(dictionary);
		return dictionary;
	}
	
	private int mapDataItem(Map<String,IDataItem> rdic, IGroup parent, String pname) {
		IDataItem item = parent.findDataItem(pname);
		if(null!=item) {
			rdic.put(item.getName(),item);
			if (isDebugMode) {
				System.out.println("> Mapping DataItem: "+item.getName()+" <");
			}
		}
		return rdic.size();
	}
	
	private List<IDataItem> copyAxes(IGroup src, IGroup res) {
		
		List<IDataItem> axes = new ArrayList<IDataItem>();
		IDataItem histogram = src.getDataItemWithAttribute(
				Util.NEXUS_SIGNAL_ATTRIBUTE_NAME,"1");
		IAttribute axesAttribute = histogram.findAttributeIgnoreCase(
				Util.NEXUS_AXES_ATTRIBUTE_NAME);
		
		if (null!=axesAttribute) { 
			String[] names = axesAttribute.getStringValue().split(":");			
			for (String name : names) {
				IDataItem axis = src.findDataItem(name);
				if (null!=axis) { 
					axes.add(axis); 
					res.addDataItem(axis);
				}
			}
		}		
		return axes;
	}
	
	private void showAxes(Plot plot) {		
		
		Data histogram = plot.findSingal();
		int rank = histogram.getRank();
		int[] shape = histogram.getShape();
		
		List<Axis> axes = plot.getAxisList();
		Axis axis;		
		
		int length  = axes.size();
		if ((0<length) && (0<rank)) {
			axis = axes.get(0);
			this.axis0 = axis.getName();
			this.dim0  = axis.getShape()[0]; //hm shape[0];
			super.informVarValueChange("axis0", axis0);
			super.informVarValueChange("dim0", dim0);
		} else {
			super.informVarValueChange("axis0", "");
			super.informVarValueChange("dim0", 0);			
		}
		
		if ((1<length) && (1<rank)) {
			axis = axes.get(1);
			this.axis1 = axis.getName();
			this.dim1  = shape[1]; //axis.getShape()[0] - 1; //hm shape[1];
			super.informVarValueChange("axis1", axis1);
			super.informVarValueChange("dim1", dim1);
		} else {
			super.informVarValueChange("axis1", "");
			super.informVarValueChange("dim1", 0);			
		}

		if ((2<length) && (2<rank)) {
			axis = axes.get(2);
			this.axis2 = axis.getName();
			this.dim2  = shape[2]; //axis.getShape()[0] - 1; //hm shape[2];
			super.informVarValueChange("axis2", axis2);
			super.informVarValueChange("dim2", dim2);
		} else {
			super.informVarValueChange("axis2", "");
			super.informVarValueChange("dim2", 0);			
		}

		if ((3<length) && (3<rank)) {
			axis = axes.get(3);
			this.axis3 = axis.getName();
			this.dim3  = shape[3]; //axis.getShape()[0] - 1; //hm shape[3];
			super.informVarValueChange("axis3", axis3);
			super.informVarValueChange("dim3", dim3);
		} else {
			super.informVarValueChange("axis3", "");
			super.informVarValueChange("dim3", 0);			
		}
	}
	
	private void clearAxes() {		
			super.informVarValueChange("axis0", "");
			super.informVarValueChange("dim0", 0);		
			super.informVarValueChange("axis1", "");
			super.informVarValueChange("dim1", 0);
			super.informVarValueChange("axis2", "");
			super.informVarValueChange("dim2", 0);
			super.informVarValueChange("axis3", "");
			super.informVarValueChange("dim3", 0);
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
	/* Port get/set methods -----------------------------------------*/
	
    /* In-Ports -----------------------------------------------------*/

	public void setInGroup(IGroup inGroup) {
		this.inGroup = inGroup;
	}

	/* Out-Ports ----------------------------------------------------*/

	public IGroup getOutGroup() {
		return this.outGroup;
	}

	public Plot getResult() {
		return this.result;
	}
	
	public Plot getDisplay() {
		Plot display = null;
		try {
			if(checkStructure(this.inGroup)) {
				IGroup nxData = getDataGroup(this.inGroup);
				display = regeneratePlot(
						this.inGroup,
						nxData,
						"display",
						SourceReader2.dataDimensionType);
				if (null!=display) {
					DataLib.generateTitle(display);
				}
			}
			if (null==display) {
				display = PlotFactory.createPlot("display",dataDimensionType);				
				display.setTitle("No Data");
			}
		} catch (PlotFactoryException e) {
		} catch (FileAccessException e) {
		}
		return display;			
	}
	
	public Map<String,IDataItem> getDictionary() {
		return redict;
	}

	/* Var-Ports (options) ------------------------------------------*/

	public void setStop(Boolean doStop) {
		this.doStop = doStop;
	}
	
	public Boolean getStop() {
		return this.doStop;
	}
	
    /* Var-Ports (tuners) -------------------------------------------*/
	
	public void setAxis0(String axis0) { /* UI read only */ }
	public void setAxis1(String axis1) { /* UI read only */ }
	public void setAxis2(String axis2) { /* UI read only */ }
	public void setAxis3(String axis3) { /* UI read only */ }

	public void setDim0(Integer dim0) { /* UI read only */ }
	public void setDim1(Integer dim1) { /* UI read only */ }
	public void setDim2(Integer dim2) { /* UI read only */ }
	public void setDim3(Integer dim3) { /* UI read only */ }	

}