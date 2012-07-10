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
package au.gov.ansto.bragg.datastructures.nexus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.data.Factory;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.utils.Utilities;
import org.gumtree.service.cli.ICommandLineOptions;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.Util;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;

/**
 * @author nxi
 * Created on 24/09/2008
 */
public class NexusUtils {

	public static final String OPTION_ALGO_SET = "gumtree.processor.algoSetPlugin";
	public static final String QUERY_FRAME_NAME = "frame";
	public static final String QUERY_ENTRY_NAME = "entry";
	public static final String CORRECTED_DATA_NAME = "corrected_data";

	/**
	 * Get a list of Nexus entries from a GDM root group. Assume the entry group 
	 * has an attribute with name and value: NX_class='NXentry' 
	 * @param rootGroup GDM Group object
	 * @return List of Groups
	 * @throws StructureTypeException
	 * Created on 17/12/2008
	 */
	public static List<IGroup> getNexusEntryList(IGroup rootGroup) throws StructureTypeException{
		List<IGroup> entryList = new ArrayList<IGroup>();
		if (rootGroup == null || 
			! rootGroup.isRoot() || 
			Util.getDataStructureType(rootGroup) != DataStructureType.nexusRoot)
				throw new StructureTypeException("input is not a nexus root group");
		for (Object item : rootGroup.getGroupList()){
			if (item instanceof IGroup)
				if (((IGroup) item).hasAttribute(
						Util.NEXUS_CLASS_ATTRIBUTE_NAME, 
						Util.NEXUS_ENTRY_ATTRIBUTE_VALUE))
					entryList.add((IGroup) item);
		}
		return entryList;
	}
	
	/**
	 * Get the Nexus data as a GDM Group from the Nexus entry Group. Assume the 
	 * Nexus data will have an attribute with name and value: NX_class='NXdata'
	 * @param entry GDM Group
	 * @return GDM Group
	 * @throws StructureTypeException
	 * Created on 17/12/2008
	 */
	public static IGroup getNexusData(IGroup entry) throws StructureTypeException{
		if (entry == null )
				throw new StructureTypeException("input is not a nexus entry group");
		for (Object item : entry.getGroupList()){
			if (item instanceof IGroup){
				IGroup group = (IGroup) item;
				if (group.hasAttribute(
						Util.NEXUS_CLASS_ATTRIBUTE_NAME, 
						Util.NEXUS_DATA_ATTRIBUTE_VALUE))
					return group;
			}
		}
		return null;
	}
	
	/**
	 * Get the Nexus signal as a DataItem from the Nexus data Group. Assume the Nexus signal 
	 * DataItem will have an attribute with name and value: signal='1'. 
	 * @param nexusData GDM Group
	 * @return GDM DataItem
	 * @throws StructureTypeException
	 * Created on 17/12/2008
	 */
	public static IDataItem getNexusSignal(IGroup nexusData) throws StructureTypeException {
		if (nexusData == null || 
			(Util.getDataStructureType(nexusData) != DataStructureType.nexusData
					&& Util.getDataStructureType(nexusData) != DataStructureType.plot))
				throw new StructureTypeException("input is not a nexus data group");
		for (Object item : nexusData.getDataItemList()){
			if (item instanceof IDataItem){
				IDataItem signal = (IDataItem) item;
				if (signal.hasAttribute(Util.NEXUS_SIGNAL_ATTRIBUTE_NAME, "1"))
					return signal;
			}
		}
		return null;
	}
	
	/**
	 * Get the Nexus axes as as List of DataItem objects from the Nexus data Group. 
	 * Assume the axes names are listed in an attribute of the Nexus data object. 
	 * @param nexusData GDM Group object
	 * @return List of GDM DataItem objects
	 * @throws StructureTypeException
	 * Created on 17/12/2008
	 */
	public static List<IDataItem> getNexusAxis(IGroup nexusData) throws StructureTypeException{
		IDataItem signal = getNexusSignal(nexusData);
		List<IDataItem> axisList = new ArrayList<IDataItem>();
		if (signal != null){
			IAttribute axesAttribute = signal.getAttribute("axes");
			if (axesAttribute == null)
				return axisList;
			String[] axisNames = axesAttribute.getStringValue().split(":");
			for (int i = 0; i < axisNames.length; i++) {
				IDataItem axisItem = nexusData.findDataItem(axisNames[i].trim());
				if (axisItem == null){
					axisItem = findApproximateAxisItem(nexusData, axisNames[i].trim());
					if (axisItem != null)
						axisItem.setName(axisNames[i].trim());
				}
				if (axisItem != null){
					axisList.add(axisItem);
				}
			}
		}
		return axisList;
	}

	/**
	 * There are cases that the Nexus axis name are labelled partially. For example, the 
	 * real name of the axis object is called 'primary_horizontal_offset' but is referenced
	 * in the Nexus Data with the name 'horizontal_offset' for Kowari. This method is 
	 * designed only for this purpose and thus deprecated for other purpose. 
	 * @param nexusData
	 * @param axisName
	 * @return
	 * @deprecated 
	 * Created on 17/12/2008
	 */
	public static IDataItem findApproximateAxisItem(IGroup nexusData, String axisName) {
		for (Object item : nexusData.getDataItemList()){
			if (item instanceof IDataItem){
				IDataItem axis = (IDataItem) item;
				if (axis.getShortName().contains(axisName))
					return axis;
			}
		}		
		return null;
	}

	/**
	 * Get the first Nexus entry from the location of a URI. 
	 * @param fileUri URI object
	 * @return GDM Group object
	 * @throws StructureTypeException
	 * Created on 17/12/2008
	 */
	public static IGroup getNexusEntry(URI fileUri) throws StructureTypeException{
		File file = null;
		try {
			file = new File(fileUri);				
		} catch (Exception e) {
			file = null;
		}
		if (file == null || ! file.exists()){
			throw new StructureTypeException("the target URI does not exist");
		}
		try{
			IGroup rootGroup = Factory.createDatasetInstance(fileUri).getRootGroup();
			List<IGroup> entryList = getNexusEntryList(rootGroup);
			if (entryList.size() == 0)
				throw new StructureTypeException("can not find data entry in the target URI");
			return entryList.get(0);
		}catch (Exception e) {
			throw new StructureTypeException(e);
		}
	}

	public static IGroup getNexusEntry(String filePath) throws StructureTypeException{
		File file = null;
		try {
			file = new File(filePath);				
		} catch (Exception e) {
			file = null;
		}
		if (file == null || ! file.exists()){
			throw new StructureTypeException("the target URI does not exist");
		}
		try{
			IGroup rootGroup = Factory.createDatasetInstance(ConverterLib.path2URI(filePath)).getRootGroup();
			List<IGroup> entryList = getNexusEntryList(rootGroup);
			if (entryList.size() == 0)
				throw new StructureTypeException("can not find data entry in the target URI");
			return entryList.get(0);
		}catch (Exception e) {
			throw new StructureTypeException(e);
		}
	}
	
	/**
	 * Get the Nexus data as GDM Group in the first entry of the file from a location specified with a URI. 
	 * @param fileUri URI object
	 * @return GDM Group object
	 * @throws StructureTypeException
	 * Created on 17/12/2008
	 */
	public static IGroup getNexusData(URI fileUri) throws StructureTypeException{
		File file = null;
		try {
			file = new File(fileUri);				
		} catch (Exception e) {
			file = null;
		}
		if (file == null || ! file.exists()){
			throw new StructureTypeException("the target URI does not exist");
		}
		try{
			IGroup rootGroup = Factory.createDatasetInstance(fileUri).getRootGroup();
			List<IGroup> entryList = getNexusEntryList(rootGroup);
			if (entryList.size() == 0)
				throw new StructureTypeException("can not find data entry in the target URI");
			return getNexusData(entryList.get(0));
		}catch (Exception e) {
			throw new StructureTypeException(e);
		}
	}
	
	public static String getDictionaryPath() throws IOException, URISyntaxException{
		String pluginId = null;
//		ICommandLineOptions options = ServiceUtils.getService(ICommandLineOptions.class);
		pluginId = System.getProperty(OPTION_ALGO_SET);
		if(pluginId != null) {
//			pluginId = options.getOptionValue(OPTION_ALGO_SET);
			File dict_file = new File(ConverterLib.getDictionaryPath(pluginId));
			return dict_file.getAbsolutePath();
		}
		return null;
	}

	public static IGroup createNexusEntry(IGroup parent, String shortName) throws IOException {
		IGroup entryGroup = null;
		if (parent == null){
			entryGroup = Factory.createGroup(shortName);
		}else
			entryGroup = Factory.createGroup(parent, shortName, true);
		entryGroup.addOneAttribute(Factory.createAttribute(Util.NEXUS_CLASS_ATTRIBUTE_NAME, 
					Util.NEXUS_ENTRY_ATTRIBUTE_VALUE));
		return entryGroup;
	}
	
	public static IGroup createNexusDataGroup(IGroup parent, String shortName, IArray signal, 
			List<IDataItem> axes) 
	throws IOException, InvalidArrayTypeException {
		IGroup dataGroup = null;
		if (parent == null){
			dataGroup = Factory.createGroup(shortName);
		}else
			dataGroup = Factory.createGroup(parent, shortName, true);
		dataGroup.addOneAttribute(Factory.createAttribute(Util.NEXUS_CLASS_ATTRIBUTE_NAME, 
				Util.NEXUS_DATA_ATTRIBUTE_VALUE));
		IDataItem dataItem = Factory.createDataItem(dataGroup, "data", signal);
		dataItem.addStringAttribute(Util.NEXUS_SIGNAL_ATTRIBUTE_NAME, "1");
		dataGroup.addDataItem(dataItem);
		if (axes != null && axes.size() > 0){
			String nameString = "";
			boolean init = true;
			for (IDataItem axis : axes){
				if (init){
					nameString += axis.getShortName();
					init = false;
				} else
					nameString += ":" + axis.getShortName();
				dataGroup.addDataItem(axis);
			}
			dataGroup.addStringAttribute(Util.NEXUS_AXES_ATTRIBUTE_NAME, nameString);
		}
		return dataGroup;
	}

	public static Plot createnexusDataPlot(IGroup parent, String shortName, 
			DataDimensionType dimensionType, IArray signal, IArray variance, List<IDataItem> axes) 
	throws InvalidArrayTypeException, PlotFactoryException, IOException{
		Plot dataPlot = null;
		if (parent == null)
			dataPlot = (Plot) PlotFactory.createPlot(shortName, dimensionType);
		else
			dataPlot = (Plot) PlotFactory.createPlot(parent, shortName, dimensionType);
		dataPlot.addData("data", signal, "Data", "", variance);
		dataPlot.addStringAttribute(Util.NEXUS_CLASS_ATTRIBUTE_NAME, 
				Util.NEXUS_DATA_ATTRIBUTE_VALUE);
		if (axes != null && axes.size() > 0){
			int index = 0;
			for (IDataItem axis : axes){
				dataPlot.addAxis(axis.getShortName(), axis.getData(), axis.getShortName(), 
						axis.getUnitsString(), index ++);
			}
		}
		return dataPlot;
	}
	
	public static URI createNexusDataURI(String filePath, Integer entryIndex, Integer frameIndex) 
	throws FileAccessException {
		URI fileURI = null;
		try {
			fileURI = ConverterLib.path2URI(filePath);
		} catch (FileAccessException e) {
			throw new FileAccessException("illegal file path :" + e.getMessage(), e);
		}
		String uriString = fileURI.toString();
		if (entryIndex != null && frameIndex != null)
			uriString += "?" + QUERY_ENTRY_NAME + "=" + entryIndex + "&frame" + QUERY_FRAME_NAME + "=" + frameIndex;
		else if (entryIndex != null)
			uriString += "?" + QUERY_ENTRY_NAME + "=" + entryIndex;
		else if (frameIndex != null)
			uriString += "?" + QUERY_FRAME_NAME + "=" + frameIndex;
		return URI.create(uriString);
	}

	public static String findQuery(URI uri, String name){
		String query = uri.getQuery();
		String[] parts = query.split("&");
		for (int i = 0; i < parts.length; i++) {
			String pair[] = parts[i].split("=");
			try {
				if (pair[0].trim().equals(name))
					return pair[1];
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public static IGroup getCorrectedNexusData(IGroup entry) throws StructureTypeException {
		if (entry == null )
			throw new StructureTypeException("input is not a nexus entry group");
		NcGroup dataGroup = null;
		for (Object item : entry.getGroupList()){
			if (item instanceof IGroup){
				IGroup group = (IGroup) item;
				if (group.hasAttribute(
						Util.NEXUS_CLASS_ATTRIBUTE_NAME, 
						Util.NEXUS_DATA_ATTRIBUTE_VALUE)){
					dataGroup = (NcGroup) group;
					break;
				}
			}
		}
		if (dataGroup != null){
			dataGroup = (NcGroup) dataGroup.clone();
			try {
				IDataItem correctedData = dataGroup.getDataItem(CORRECTED_DATA_NAME);
				IArray correctedArray = correctedData.getData();
				if (correctedData.getRank() < dataGroup.findSignal().getRank()){
					correctedArray = Factory.createArray(correctedArray.getElementType(), 
							dataGroup.findSignal().getShape(), correctedArray.getStorage());
				}
				dataGroup.findSignal().setCachedData(correctedArray, false);
			} catch (Exception e) {
				throw new StructureTypeException("can not find corrected data");
			} 
		}
		
		return dataGroup;
	}

	/**
	 * Get the Nexus data as GDM Group in the first entry of the file from a location specified with a URI. 
	 * @param fileUri URI object
	 * @return GDM Group object
	 * @throws StructureTypeException
	 * Created on 17/12/2008
	 */
	public static IGroup getCorrectedNexusData(URI fileUri) throws StructureTypeException{
		File file = null;
		try {
			file = new File(fileUri);				
		} catch (Exception e) {
			file = null;
		}
		if (file == null || ! file.exists()){
			throw new StructureTypeException("the target URI does not exist");
		}
		try{
			IGroup rootGroup = (IGroup) Utilities.findObject(fileUri, getDictionaryPath());
			List<IGroup> entryList = getNexusEntryList(rootGroup);
			if (entryList.size() == 0)
				throw new StructureTypeException("can not find data entry in the target URI");
			return getCorrectedNexusData(entryList.get(0));
		}catch (Exception e) {
			throw new StructureTypeException(e);
		}
	}

	public static String getAlgorithmDictionaryPath() throws IOException, URISyntaxException{
		String pluginId = null;
		ICommandLineOptions options = ServiceUtils.getService(ICommandLineOptions.class);
		if(options.hasOptionValue(OPTION_ALGO_SET)) {
			pluginId = options.getOptionValue(OPTION_ALGO_SET);
			File dict_file = new File(ConverterLib.getDictionaryPath(pluginId));
			return dict_file.getAbsolutePath();
		}
		return null;
	}

	public static Plot createSlicePlot(IGroup group, int index, boolean useCorrectedData) throws StructureTypeException, 
	IOException, InvalidArrayTypeException, PlotFactoryException, InvalidRangeException {
		List<IDataItem> axes = NexusUtils.getNexusAxis(group);
		IDataItem data = null;
		if (useCorrectedData)
			data = group.getDataItem(CORRECTED_DATA_NAME);
		else
			data = ((NcGroup) group).findSignal();
		IArray dataArray;
		int[] shape = data.getShape();
		int rank = data.getRank();
		if (rank <= 2)
			if (useCorrectedData)
				dataArray = group.getDataItem(CORRECTED_DATA_NAME).getData();
			else
				dataArray = ((NcGroup) group).findSignal().getData();
		else{
			int[] origin = new int[shape.length];
			int[] newShape = new int[shape.length];
//			origin[0] = index;
			for (int i = 0; i < shape.length; i++){
				if (shape[i] > index) {
					origin[i] = index;
					break;
				}
			}
			for (int i = 0; i < newShape.length; i++){
				if (i >= newShape.length - 2)
					newShape[i] = shape[i];
				else
					newShape[i] = 1;
			}
			if (useCorrectedData)
				dataArray = group.getDataItem(CORRECTED_DATA_NAME).getData(origin, newShape).getArrayUtils().reduce().getArray();
			else
				dataArray = ((NcGroup) group).findSignal().getData(origin, newShape).getArrayUtils().reduce().getArray();
		}
//		dataArray = Factory.copyToDoubleArray(dataArray);
		Plot plot = (Plot) PlotFactory.createPlot(group, "slice" + index, DataDimensionType.map);
		plot.addData("data" + index, dataArray, "data", "", dataArray);
		IDataItem yAxis = axes.get(axes.size() - 2);
		plot.addAxis(yAxis.getShortName(), yAxis.getData(), yAxis.getShortName(), yAxis.getUnitsString(), 0);
		IDataItem xAxis = axes.get(axes.size() - 1);
		IArray xAxisArray = xAxis.getData();
		if (xAxisArray.getRank() > 1)
			xAxisArray = xAxisArray.getArrayUtils().slice(0, index).getArray();
		plot.addAxis(xAxis.getShortName(), xAxisArray.copy(), xAxis.getShortName(), xAxis.getUnitsString(), 1);
		return plot;
	}

	/**
	 * 
	 * @param entry1
	 * @param entry2
	 * @return
	 * @deprecated under construction
	 * Created on 19/06/2009
	 */
	public IGroup concatenate(IGroup entry1, IGroup entry2){
		IGroup result = null;
		
		return result;
	}

	public static int getNumberOfFrames(IGroup rootGroup) throws StructureTypeException{
		IArray nexusSignal = null;
		try{
			IGroup nexusData = getNexusData(getNexusEntryList(rootGroup).get(0));
			nexusSignal = ((NcGroup) nexusData).findSignal().getData();
		}catch (Exception e) {
			throw new StructureTypeException("can not read nexus structure from the group data");
		}
		int[] shape = nexusSignal.getShape();
		if (shape.length < 3)
			return 1;
		int numberOfFrames = 1;
		for (int i = 0; i < shape.length - 2; i ++)
			numberOfFrames *= shape[i];
		return numberOfFrames;
	}
	
	public static boolean copyfile(String srFile, String dtFile){
	    try{
	      File f1 = new File(srFile);
	      File f2 = new File(dtFile);
	      InputStream in = new FileInputStream(f1);
	      
	      //For Append the file.
//	      OutputStream out = new FileOutputStream(f2,true);

	      //For Overwrite the file.
	      OutputStream out = new FileOutputStream(f2);

	      byte[] buf = new byte[1024];
	      int len;
	      while ((len = in.read(buf)) > 0){
	        out.write(buf, 0, len);
	      }
	      in.close();
	      out.close();
	      return true;
//	      System.out.println("File copied.");
	    }
	    catch(FileNotFoundException ex){
	      System.out.println(ex.getMessage() + " in the specified directory.");
	      return false;
	    }
	    catch(IOException e){
	      System.out.println(e.getMessage());
	      return false;
	    }
	}
	
	public static IDataItem getNexusVariance(IGroup dataGroup) {
		IDataItem variance = dataGroup.getDataItemWithAttribute("signal", 
				StaticDefinition.DATA_VARIANCE_REFERENCE_NAME);
		return variance;
	}
	
}
