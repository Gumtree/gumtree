/******************************************************************************* 
* Copyright (c) 2009 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Paul Hathaway (26/08/2009)
*    last modified 22/10/2009 
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.online;

import java.io.File;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IKey;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Data;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.core.internal.DataLib;

public class SourceReader extends ConcreteProcessor{
	
	/* Fields for audit trail support */
	private static final String processClass = "SourceReader"; 
	private static final String processClassVersion = "3.0"; 
	private static final long processClassID = 2009092901; 

	/* Fields to support client-side processing */
	private static DataStructureType dataStructureType = DataStructureType.plot;
	private static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

    private static final String REDUCTION_DIC = "\\xml\\reduction.dic.txt";
    private String pluginID;

    private static final String PARAMETER_GROUP_NAME = "parameters";
    private static final String DATA_GROUP_NAME = "data_group";
    
	private IGroup inGroup;
	private Plot  dataGroup;
	
	private Boolean doStop = false;
	
	private String axis0;
	private String axis1;
	private String axis2;
	private String axis3;
	
	private Integer dim0 = 0;
	private Integer dim1 = 0;
	private Integer dim2 = 0;
	private Integer dim3 = 0;
	
	public SourceReader() {
		this.setReprocessable(false);
		pluginID = DataLib.findPluginID();
	}
	
	//-----------------------------------------------------------------------
	public Boolean process() throws Exception {
		clearAxes();
		if (this.inGroup == null) {
			throw new Exception("Input Group not detected");
		}
		this.dataGroup = PlotFactory.createPlot(DATA_GROUP_NAME,dataDimensionType);				
		
		boolean flag = isDataGroup(this.inGroup);
		this.doStop = this.doStop || (!flag);
		if(flag) {
			IGroup root = (this.inGroup).getRootGroup();
			IGroup entry = (this.inGroup).getParentGroup();
			String dataGroupName = (this.inGroup).getShortName();

			String outEntryName = generateEntryName(entry);
			String redDataName  = generateDataName(dataGroupName);
			IGroup outEntry = Factory.createGroup(root,outEntryName,true);
			Plot redData  = makeReductionGroup(outEntry,this.inGroup,redDataName);
			IGroup parameters = makeParameterGroup(outEntry,entry,REDUCTION_DIC);

			clearInputStructure(entry);

			if (null!=redData) {
				DataLib.generateTitle(redData);
				showAxes(redData);
			}
			this.dataGroup = redData;
		} 
		if ((!flag) || (null==dataGroup)) {
			dataGroup = PlotFactory.createPlot(DATA_GROUP_NAME,dataDimensionType);				
			dataGroup.setTitle("No Data");
		}
		return doStop;
	}
	
	//-----------------------------------------------------------------------
    // For reference:
	// NexusUtils.createNexusDataGroup(parent, shortName, signal, axes);
	// NexusUtils.createnexusDataPlot(parent, shortName, dimensionType, signal, variance, axes);
	// NexusUtils.createNexusDataURI(filePath, entryIndex, frameIndex);

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
	
	private String generateEntryName(IGroup basis) {
		// Allow for customisation of name
		String suffix = ".raw";
		return (basis.getName())+suffix;
	}
	
	private String generateDataName(String dataGroupName) {
		// Allow for customisation of name, default same as input
		String name = dataGroupName;
		return name;
	}

	private Plot makeReductionGroup(IGroup parent,IGroup source,String name) {
		Plot redData = null;
		try {
			redData = PlotFactory.copyToPlot(parent,source,name,dataDimensionType);
			//redData.setTitle(name);
		} catch (PlotFactoryException e) {
			debugging("makeReductionGroup","Cannot create Plot '"+name+"'");
		}
		return redData;
	}

	/**
	 * @param group Group container for data items
	 * @return true if "signal" data item can be located in group structure
	 */
	private boolean isDataGroup(IGroup group) {
		if (null==group) { return false; }
		IDataItem data = group.getDataItemWithAttribute("signal","1");
		return (null!=data);
	}
	
	private void clearInputStructure(IGroup entry)
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
	}
	
	private IGroup makeParameterGroup(IGroup parent,IGroup source,String dictPath) {
		IGroup parameters = Factory.createGroup(parent,PARAMETER_GROUP_NAME,true);
		try {
			IDictionary dict = initDictionary(source);
			debugging("Mapping DataItems from Entry: "+source.getShortName());
			int count = 0;		
			for (IKey key : dict.getAllKeys()) {
				IPath path = dict.getPath(key);
				Object oItem = source.findContainerByPath(path.getValue());
				if (oItem instanceof IDataItem) {
					IDataItem item = (IDataItem) oItem;
					parameters.addDataItem(item);
					count++;
					debugging("> Mapping DataItem ("+count+"): "+item.getName()+" <");
				}
			}
			debugging("Number of DataItems mapped: "+count);
		} catch (Exception e) {
			String msg = "Unable to complete Parameter Group in "+parent.getShortName();
			debugging("makeParametersGroup",msg);
		}		
		return parameters;
	}
		
	/* Reads REDUCTION_DIC into Map<String,String> placed in parent Group
	 */
	private IDictionary initDictionary(IGroup parent) {
		String filepath = "";
		IDictionary dictionary = Factory.createDictionary();
		try {
			File redFile = ConverterLib.findFile(pluginID,REDUCTION_DIC);
			filepath = redFile.getAbsolutePath();
			dictionary.readEntries(filepath);
			parent.setDictionary(dictionary);
		} catch (FileAccessException e) {
			debugging("initDictionary","Cannot access dictionary: "+filepath);
		}
		return dictionary;
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

	public Plot getDataGroup() {
		return this.dataGroup;
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