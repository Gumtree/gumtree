/******************************************************************************* 
* Copyright (c) 2009 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Paul Hathaway - July 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.impl.io.NcHdfWriter;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.Util;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class MultiDimExport extends ConcreteProcessor {

	private static final String processClass = "MultiDimExport"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009071401; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;
	private Boolean doStop = false;
	private Boolean flag = false;

	private Boolean doInterrupt = false;
	private Boolean doPatchTotalCounts = true;

	private IGroup inGroup; // nxData Group
	private Plot  outPlot;
		
	private static final String FILESUFFIX = ".nx.hdf";
	private static final String ATTR_FILENAME = "file_name";
	
//	private DataManager manager;
	private URI    exportFolder;
	private String exportPath = "D:\\datafile\\quokka\\rhqc\\";
	
	private Map<String,IDataItem> scan = new HashMap<String,IDataItem>();
	private Integer firstSliceIndex = 0;
	private Integer lastSliceIndex = 0;
	private Integer sliceStride = 1;
	private Integer fixedIndex = 0;
	private Integer sliceIndex = firstSliceIndex;
	private String  filePrefix = "qkk";
	private String  histogramName = "hmm_xy";
	
	public MultiDimExport() {
		this.setReprocessable(false);
		super.informVarValueChange("firstSliceIndex", firstSliceIndex);
		super.informVarValueChange("lastSliceIndex", lastSliceIndex);
		super.informVarValueChange("sliceIndex", sliceIndex);
	}
	
	//-----------------------------------------------------------------------
	public Boolean process() throws Exception {
		
		IGroup entry = inGroup;
		IGroup root = entry.getRootGroup();
		IGroup nxData = entry.getGroupWithAttribute(
					Util.NEXUS_CLASS_ATTRIBUTE_NAME, 
					Util.NEXUS_DATA_ATTRIBUTE_VALUE);
		IGroup source = root.findGroup("sourcecopy");
		IDataItem histogram = source.getDataItemWithAttribute("signal","1");
		IAttribute sliceAxes = prepareAxes(histogram);
		String filebase = setFilebase(root);

		IGroup detector = (IGroup) root.findContainerByPath("/entry1/instrument/detector");

		/* TODO: Introduce 'doInterrupt' to stop main loop */
		for (sliceIndex=firstSliceIndex;sliceIndex<=lastSliceIndex;sliceIndex+=sliceStride) {
			if (isDebugMode) { 
				System.out.println(processClass+"."+"process loop slice ["+sliceIndex+"] START"); 
			}
			
			IDataItem slice = histogram.getASlice(fixedIndex, sliceIndex);
			String slicefilename = prepareSliceFilename(filePrefix,sliceIndex);

			prepareSliceProperties(slice,sliceAxes,sliceIndex);
			
			nxData.addDataItem(slice);	
			detector.addDataItem(slice);

			prepareSliceScanParameters(root,sliceIndex);			
			if (doPatchTotalCounts) {
				int sum = patchTotalCounts(root,slice);
				if (isDebugMode) { 
					System.out.println("patch total_counts ["+sliceIndex+"] to "+sum); 
				}
			}
			exportSlice(entry,sliceIndex,slicefilename);
			
			nxData.removeDataItem(slice);
			detector.removeDataItem(slice);
			
			if (isDebugMode) { 
				System.out.println(processClass+"."+"process loop slice ["+sliceIndex+"] END"); 
			}
		}		
		if (isDebugMode) { 
			System.out.println(processClass+"."+"process END"); 
		}
		nxData = null;
		root = null;
		entry = null;
		histogram = null;
		
		return doStop;
	}
	//-----------------------------------------------------------------------
	
	private IAttribute prepareAxes(IDataItem histogram) {
		IAttribute axes = histogram.getAttribute("axes");
		String[] axesArray = axes.getStringValue().split(":");
		StringBuffer axesBuffer = new StringBuffer();
		for (int i=0;i<axesArray.length;i++) {
			if (fixedIndex!=i) {
				axesBuffer.append(axesArray[i]);
				if (1<(axesArray.length-i)) {
					axesBuffer.append(":");
				}
			}
		}
		if (isDebugMode) { 
			System.out.println(processClass+"."+"prepareStructures"+" END"); 
		}
		return Factory.createAttribute("axes",axesBuffer.toString());		
	}

	private void prepareSliceProperties(IDataItem slice,IAttribute sliceAxes,Integer index) {
		slice.addStringAttribute("signal","1");
		slice.addStringAttribute("index",index.toString());
		slice.addOneAttribute(sliceAxes);
		slice.setName(histogramName);
		if (isDebugMode) { 
			System.out.println(processClass+"."+"prepareSliceProperties ["+index+"] END"); 
		}
	}
	
	private void prepareSliceScanParameters(IGroup root,Integer index) {
		boolean isDebugMode=false;
		root.addStringAttribute("index",index.toString());		
		for(Entry<String, IDataItem> e : scan.entrySet()) {
			String path = e.getKey();
			int last = path.lastIndexOf("/");
			if (last<(path.length()-1)) { last++; }
			String p = path.substring(0, last); 
			IGroup parent = null;
			try {
				parent = (IGroup) root.findContainerByPath(p);
			} catch (Exception exception) { }
			if (null!=parent) {
				try {
					IDataItem di = e.getValue();
					if ((null!=di) && (index<di.getSize())) {
						Object existing = null;
						try {
							existing = parent.findContainerByPath(path);
						} catch (Exception exception) { }
						if (null!=existing) {
							if (existing instanceof IDataItem) { 
								parent.removeDataItem((IDataItem) existing);
							}
						}
						IDataItem dNew = di.clone();
						IArray arr;
						arr = dNew.getData(new int[] {index}, new int[] {1});
						dNew.setCachedData(arr,false);
						parent.addDataItem(dNew);
						if (isDebugMode) { 
							System.out.println(
									"> DataItem: "+dNew.getName()+" indexed at ["+index+"] <"); 
						}
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InvalidRangeException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InvalidArrayTypeException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (CloneNotSupportedException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
			}
		}

		if (isDebugMode) { 
			System.out.println(processClass+"."+"prepareSliceScanParameters ["+index+"] END"); 
		}
	}

	private boolean exportSlice(IGroup entry,Integer index,String filename)
		throws IOException {
		String filepath = exportPath+filename;
		if (isDebugMode) { 
			System.out.println(processClass+"."+"exportSlice ["+index+"] to "+filename+" START"); 
		}
		NcHdfWriter hdfCreator = null;
		try{
			hdfCreator = new NcHdfWriter(new File(filepath));   
			IDataset dataset = Factory.createEmptyDatasetInstance();
			IGroup root = dataset.getRootGroup();
			IGroup slice = entry.clone();
			root.addSubgroup(slice);
			hdfCreator.writeToRoot(root);
		}
		catch (Exception e){
			if (isDebugMode) {
				e.printStackTrace();
				System.out.println("Failed to create file: "+filename);
			}
			throw new IOException("Failed to create file: "+filename);
		}
		if (isDebugMode) { 
			System.out.println(processClass+"."+"exportSlice ["+index+"] to "+filename+" END"); 
		}
		return false;
	}
	
	private Double  integrate(IDataItem histogram) throws IOException {
		IArray  arr = histogram.getData();
		Double sum = (Double) arr.getArrayMath().sum();
		return sum;
	}
	
	private Integer patchTotalCounts(IGroup root, IDataItem slice) throws IOException, InvalidArrayTypeException {
		final String KEY_TOTAL_COUNTS = "total_counts";
		final String PATH_TOTAL_COUNTS = "/entry1/data/total_counts";
		final String PATH_TOTAL_COUNTS_DET = "/entry1/instrument/detector/total_counts";
		
		int   sum = (integrate(slice)).intValue();
		IArray arr = Factory.createArray(new int[] {sum});

		try {
			IDataItem total = (IDataItem) root.findContainerByPath(PATH_TOTAL_COUNTS);		
			total.setCachedData(arr,false);
		
			IDataItem total_detector = (IDataItem) root.findContainerByPath(PATH_TOTAL_COUNTS_DET);		
			total_detector.setCachedData(arr,false);
		} catch (Exception e) { }
		
		return sum;
	}
	
	private String setFilebase(IGroup root) {
		String filepath = root.getAttribute(ATTR_FILENAME).getStringValue();				
		int li = filepath.lastIndexOf("/");
		String filename = filepath.substring(++li);
		String filebase = filename.split("\\.")[0];
		return filebase;
	}
	
	private String prepareSliceFilename(String filebase, Integer index) {
		Integer sliceID = index+10000;
		String sliceTemp = sliceID.toString().trim();
		String sliceName = sliceTemp.substring(sliceTemp.length()-4, sliceTemp.length());
		return filebase+sliceName+FILESUFFIX; // default format "qkknnnnnnn-iiii.nx.hdf"
	}
	
	private void replaceByIndex(IGroup parent, IDataItem parameter, Integer index) {
		try {
			IDataItem di = parameter.getASlice(0,index);
			parent.removeDataItem(parameter);
			parent.addDataItem(di);
		} catch (InvalidRangeException e) {			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getGroupPath(IGroup grp) {
		String path = "/";
		IGroup parent = grp.getParentGroup();
		if (!((null==parent)||(grp.equals(parent)))) {
			path = getGroupPath(parent) + grp.getName() + path;
		}
		return path;
	}
	
	/* Client Support methods -----------------------------------------*/
	
	public static DataStructureType getDataStructureType() {
		return dataStructureType;
	}

	public static DataDimensionType getDataDimensionType() {
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

	public void setFlag(Boolean flag) {
		this.flag = flag;
	}    
	
	public void setScan(Map<String,IDataItem> scan) {
		this.scan = scan;
	}

	/* Out-Ports ----------------------------------------------------*/

	public Plot getOutPlot() {
		return this.outPlot;
	}
	
	public Boolean getOutLoop() {
		return this.doStop;
	}
	
	/* Var-Ports (options) ------------------------------------------*/

	public void setStop(Boolean doForceStop) {
		this.doStop = doForceStop;
	}

	public void setInterrupt(Boolean doInterrupt) {
		this.doInterrupt = doInterrupt;
		if (doInterrupt) {
			this.doStop = doInterrupt;
		}
	}
	
	public void setFilePrefix(String prefix)  {
		this.filePrefix = prefix;
	}
	
	public void setHistogramName(String hname) {
		this.histogramName = hname;
	}
	
    /* Var-Ports (tuners) -------------------------------------------*/
	
	public void setExportFolder(String path){
		//this.exportPath = path;
//		try {
//			URI p = new URI(path);
//			this.exportFolder = p;
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}

	public void setFirstSliceIndex(Integer firstSliceIndex) {
		this.firstSliceIndex = firstSliceIndex;
//		super.informVarValueChange("firstSliceIndex", firstSliceIndex);
	}

	public void setLastSliceIndex(Integer lastSliceIndex) {
		this.lastSliceIndex = lastSliceIndex;
	}

	public void setSliceStride(Integer sliceStride) {
		this.sliceStride = sliceStride;
	}

	public void setFixedIndex(Integer fixedIndex) {
		this.fixedIndex = fixedIndex;
	}
}
