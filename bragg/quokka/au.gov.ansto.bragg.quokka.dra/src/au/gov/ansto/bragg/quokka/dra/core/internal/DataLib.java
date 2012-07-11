/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Paul Hathaway - February 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core.internal;

import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.data.Factory;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.service.cli.ICommandLineOptions;

import au.gov.ansto.bragg.datastructures.core.Util;
import au.gov.ansto.bragg.datastructures.core.exception.DataAccessException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.region.RegionUtils;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;

public class DataLib {
	
	/* Fields for audit trail support */
	private static final String libClass = "DataLib"; 
	private static final String libClassVersion = "1.0"; 
	private static final long libClassID = 2009022701;

	private static Boolean isDebugMode; 
	private static Boolean hasCalledLibrary = false;
	
	public static final Double defaultMonitorCount = 1e8;
	public static final Double defaultWavelength = 5.0; // angstrom
	public static final Double minWavelength = 0.1; // angstrom
	private static final String monString = "bm1_counts";

    private static final String DEFAULT_DRA_PLUGIN = "au.gov.ansto.bragg.quokka.dra";
    private static final String OPTION_ALGO_SET = "algoSet";

    /* Key strings required to access dictionary values via Activator */
	public final static String KEY_WAVELENGTH = "LambdaA";
	public final static String KEY_ATTFACTOR = "AttFactor";
	public final static String KEY_ATTROTDEG = "AttRotDeg";
	public final static String KEY_MONITOR1 = "bm1_counts";
	public final static String KEY_DATA = "data";
	public final static String KEY_NOTES = "notes";
	
	public final static String KEY_SAMPLENAME = "sample.name";
	public final static String KEY_SAMPLEDEPTH = "sample.thickness";
	public final static String KEY_COUNTTIME = "acquire_duration";
	public final static String KEY_DETECTORSUM = "total_counts";
	public final static String KEY_DETECTOROFFSET  = "detector.x";	// mm, detector offset horizontal
	public final static String KEY_RUN = "experiment.run";
	public final static String KEY_CONFIG = "instrument.config";
	
	/* metadata not yet provided as standard in Quokka NBI datasets*/
	public final static String KEY_TRANSMISSIONFLAG = "txflag";
	public final static String KEY_TEMPERATURE  = "temperature";    // deg
	public final static String KEY_MAGFIELD  = "magField";	        // tesla ?, magnetic field
	public final static String KEY_SOURCEAPP  = "srcApp";		    // mm, source aperture
	public final static String KEY_SAMPLEAPP = "samApp";		    // mm, sample aperture
	public final static String KEY_BEAMSTOPDIA  = "beamstopDia";	// mm, beamstop diameter

	/* metadata not available from inPlot */
	public final static String KEY_TXDETECTORSUM = "txDetCount";	// count, total detector counts on transmission measurement
	public final static String KEY_DELTAWAVELENGTH = "deltaLambda"; // angstrom, wavelength spread

	/* tuner labels for metadata to enable update and exposure */
	public final static String LBL_WAVELENGTH = "wavelength";
	public final static String LBL_L2MM = "L2mm";	
	public final static String LBL_SAMPLENAME = "name";
	public final static String LBL_SAMPLEDEPTH = "thickness";
	public static final String LBL_ATTFACTOR = "attenuation";	
	public static final String LBL_ATTROTDEG = "attRotDeg";
	public final static String LBL_COUNTTIME = "countTime";
	public final static String LBL_MONITOR1 = "monCount";
	public final static String LBL_DETECTORSUM = "detCount";
	public final static String LBL_DETECTOROFFSET  = "detOffset";	// mm, detector offset horizontal

	/* metadata not available from inPlot */
	public final static String LBL_TRANSMISSIONFLAG = "transmission";
	public final static String LBL_TXDETECTORSUM = "txDetCount";	// count, total detector counts on transmission measurement
	public final static String LBL_DELTAWAVELENGTH = "deltaLambda"; // angstrom, wavelength spread
	public final static String LBL_TEMPERATURE  = "temperature";    // deg
	public final static String LBL_MAGFIELD  = "magField";	        // tesla ?, magnetic field
	public final static String LBL_SOURCEAPP  = "srcApp";		    // mm, source aperture
	public final static String LBL_SAMPLEAPP = "samApp";		    // mm, sample aperture
	public final static String LBL_BEAMSTOPDIA  = "beamstopDia";	// mm, beamstop diameter
	public final static String LBL_KAPPA = "kappa";

	/**
	 * Quokka-specific attenuation factors for attenuation disc
	 * 
	 * @param wavelength in angstroms
	 * @param attRotDeg  attenuator disc position in degrees
	 * @return AttFactor - attenuation ratio 
	 */
	@SuppressWarnings("unused")
	public static Double lookupAttFactor(Double wavelength, Double attRotDeg) {
		
		DataLib.hasCalledLibrary = true;
		
		double wTol = 0.1;    // wavelength value tolerance in angstrom
		double maxDeg = 1e3;
		int index = 0;
		
		/* Attenuation Factor currently (27/2/09) only defined for 
		 * 	wavelength = 5.0 angstrom 
		 * 	index 0 to 11 corresponding to attRotDeg 0 to 330deg in 30deg steps
		 * 
		 * When additional wavelengths determined and added to table,
		 * use as lookup if wavelength within tolerance (wTol),
		 * or linear interpolation if within range.
		 */
		final double[] attTableNick = {
				1.0,    // att0
				0.505, 
				0.181, 
				0.0717, 
				0.0366,
				0.0142,
				0.00640,
				0.00287,
				0.00103,
				7.74e-5,
				6.70e-6,
				1.97e-6, // att11
		};

		final double[] attTableBill_200109 = {
				1.0,    // att0
				0.499, 
				0.175, 
				0.0727, 
				0.0326,
				0.0124,
				0.005730,
				0.00257,
				0.000929,
				6.93e-5,
				5.46e-6,
				1.55e-6, // att11
		};

		final double[] attTable = attTableBill_200109;

		if (Math.abs(attRotDeg) > maxDeg) {
			attRotDeg = 0.0;
		}
		while (attRotDeg < 0.0) {
			attRotDeg += 360.0;
		}
		index = (int) Math.round(attRotDeg/30.0) % 12;
		return attTable[index];
	}

	public static Double normCountToDefMonitor(Double count, Double monitor, Double atten) {
		return count * defaultMonitorCount / monitor / atten;
	}
	
	public static Double normCountToRefMonitor(Double count, Double monitor, Double atten, Double refMonitor) {
		return count * refMonitor / monitor / atten;
	}

	public static Double fetchTransmission(URI txUri) throws DataAccessException {
		IGroup  entry = fetchEntry(txUri);
		double transmission = calcNormSum(entry);
		try {
			((NcGroup) entry.getRootGroup()).close();
		} catch (FileAccessException e) {
			throw new DataAccessException(e);
		}
		return transmission;
	}

	public static Double calcRoiCount(IGroup data, IGroup mask) throws DataAccessException {
		try {
			IArray roi = RegionUtils.applyRegion(data,mask);
			return roi.getArrayMath().sum();
		} catch (StructureTypeException e) {
			throw new DataAccessException(e);
		}
	}

	public static Double calcNormSum(IGroup entry) throws DataAccessException {
		double count = fetchTotalCount(entry);
		double atten = fetchAtten(entry);
		double monitor = fetchMonitor(entry);
		return DataLib.normCountToDefMonitor(count, monitor, atten);
	}

	public static Double calcNormSum(IGroup entry, Double refMonitor) throws DataAccessException {
		double count = fetchTotalCount(entry);
		double atten = fetchAtten(entry);
		double monitor = fetchMonitor(entry);
		return DataLib.normCountToRefMonitor(count, monitor, atten, refMonitor);
	}

	public static Double calcNormSumRoi(IGroup entry, IGroup roi, Double refMonitor) 
	throws DataAccessException {
		IGroup data;
		try {
			data = NexusUtils.getNexusData(entry);
		} catch (StructureTypeException e) {
			throw new DataAccessException(e);
		}
		double count = calcRoiCount(data,roi);
		double atten = fetchAtten(entry);
		double monitor = fetchMonitor(entry);
		return DataLib.normCountToRefMonitor(count, monitor, atten, refMonitor);		
	}
	
	public static IGroup fetchEntry(URI dataUri) throws DataAccessException {
		if (null==dataUri) { return null; }
		IGroup entry;
		try{
			entry = NexusUtils.getNexusEntry(dataUri);
		} catch (Exception e) {
			throw new DataAccessException("Cannot access entry from dataset", e);
		}
		return entry;
	}
	
	public static Double fetchTotalCount(IGroup entry) throws DataAccessException {
		if (null==entry) { return 1.0; }
		Double count = 1.0;
		try{
			count = entry.findGroup("instrument")
						.findGroup("detector")
						.findDataItem("total_counts").getData().getArrayMath().getMaximum();
		} catch (Exception e) {
			throw new DataAccessException("Cannot access total_count from dataset", e);
		}
		if (null==count) { count = 1.0; }
		if (1.0 > count) { // i.e. place-holder not set to valid value
			count = 1.0;
		}
		return count;
	}
	
	public static Double fetchMonitor(IGroup entry) throws DataAccessException {
		if (null==entry) { return 1.0; }
		Double count = 1.0;
		try{
			count = entry.findGroup("monitor")
						.findDataItem(monString).getData().getArrayMath().getMaximum();
		} catch (Exception e) {
			throw new DataAccessException("Cannot access monitor from dataset", e);
		}
		if (null==count) { count = 1.0;	}
		if (1.0 > count) { // i.e. place-holder not set to valid value
			count = 1.0;
		}
		return count;
	}
	
	public static Double fetchAtten(IGroup entry) throws DataAccessException {
		if (null==entry) { return 1.0; }
		
		final double atten_min = DataLib.lookupAttFactor(5.0,330.0);
		double atten = 1.0;
		Double atten_rot = 0.0;
		double wavelength_min = 0.1; // angstrom
		Double wavelength = defaultWavelength;
		
		try{
//			atten = entry.findGroup("instrument")
//						.findGroup("parameters")
//						.findGroup("derived_parameters")
//						.findDataItem("AttFactor").getData().getMaximum();
//			if (atten < atten_min) { // i.e. place-holder not set to valid value
				atten_rot = entry.findGroup("instrument")
								.findGroup("collimator")
								.findDataItem("att").getData().getArrayMath().getMaximum();
				if (null==atten_rot) {	atten_rot = 0.0; }
				wavelength = entry.findGroup("instrument")
								.findGroup("velocity_selector")
				    			.findDataItem("Lambda").getData().getArrayMath().getMaximum();
				if (null==wavelength) {	wavelength = defaultWavelength; }
				if (wavelength_min > wavelength) { // i.e. place-holder not set to valid value
					wavelength = defaultWavelength;
				}
				atten = DataLib.lookupAttFactor(wavelength,atten_rot);
//			}
			return atten;
		} catch (Exception e) {
			throw new DataAccessException("Cannot access background dataset", e);
		}
	}
	
	public static Double readInputMetaData(Plot plot, String key, Double defVal) {
		Double metadata = defVal;
		try {
			IDataItem item;
			item = plot.getDataItem(key);
			if(null==item) {
				item = plot.findDataItem(key);
			}
			if(null!=item) { 
				metadata = item.readScalarDouble(); 
			} 
		} catch (IOException ioe) {
			if(isDebugMode) System.out.print("Unable to access metadata: "+key);
		}
		return metadata;
	}
	
	public static String readInputStringMetadata(Plot plot, String key) {
		String metadata = null;
		try {
			IDataItem item;
			item = plot.getDataItem(key); 
			if(null!=item) {
				metadata = item.getData().toString(); // readScalarString(); 
			}
		} catch (IOException ioe) {
			if(isDebugMode) System.out.print("Unable to access metadata: "+key);
		}
		return metadata;
	}
	
	public static void writeInputMetaData(Plot plot, String key, Double metadata, String units) {
		try {
			IDataItem item;
			item = plot.getDataItem(key);
			if(null==item) {
				item = Factory.createDataItem(null,key,
						Factory.createDoubleArray(new double[] { metadata }));
				plot.updateDataItem(key,item);
			} else {
				item.setCachedData(Factory.createArray(new double[] {metadata}),true);
			}
		} catch (InvalidArrayTypeException iate) {
			if(isDebugMode) System.out.print("Unable to write metadata: "+key+"\n");
		} catch (SignalNotAvailableException snae) {
			if(isDebugMode) System.out.print("Unable to write metadata: "+key+"\n");
			snae.printStackTrace();
		}
	}
	
	public static void writeInputStringMetaData(Plot plot, String key, String metadata) {
		try {
			IDataItem item;
			item = plot.getDataItem(key);
			IArray sarray = Factory.createArray(String.class, new int[] {1});
			IIndex ima = sarray.getIndex();
			sarray.setObject(ima, metadata);
			if(null==item) {
				item = Factory.createDataItem(null,key,sarray);
				plot.updateDataItem(key,item);
			} else {
				item.setCachedData(sarray,false);
			}
		} catch (InvalidArrayTypeException iate) {
			if(isDebugMode) System.out.print("Unable to write metadata: "+key);
		} catch (SignalNotAvailableException snae) {
			if(isDebugMode) System.out.print("Unable to write metadata: "+key);
			snae.printStackTrace();
		}
	}
	
	public static List<String> readNotes(Plot plot) {
		List<IAttribute> alist = null;
		List<String> slist = new ArrayList<String>();
		IDataItem notes = plot.getDataItem(KEY_NOTES);
		if(null!=notes) {
			alist = (List<IAttribute>) notes.getAttributeList();
			if(null!=alist) {
				for(int i=0;i<alist.size();i++) {
					String out = null;
					IAttribute att = alist.get(i);
					if(att.isString()) { 
						out = att.getName()+": "+att.getStringValue();
					} else {
						Number val=att.getNumericValue();
						if(null!=val) {
							out = att.getName()+": "+val.toString();
						}
					}
					if(null!=out) {
						slist.add(out);
					}
				}
			}
		}
		return slist;
	}
	
	public static void appendNote(Plot plot, String label, String message) throws IOException {
		try {
			IDataItem notes = plot.getDataItem(KEY_NOTES);
			if(null==notes) { 
				IArray a = Factory.createArray(String.class, new int[]{1});
				notes = Factory.createDataItem(plot,KEY_NOTES,a);
			}
			if(null==message) { //append empty line
				message = "";
				label = "";
			} else {
				if(null==label) {
					label = "note";
				}
			}
			notes.addStringAttribute(label, message);
			plot.updateDataItem(KEY_NOTES,notes);
		} catch (InvalidArrayTypeException iate) {
			if(isDebugMode) System.out.print("Unable to write note: "+message);
		} catch (SignalNotAvailableException snae) {
			if(isDebugMode) System.out.print("Unable to write note: "+message);
			snae.printStackTrace();
		}
	}

	/* Client Support methods -----------------------------------------*/
	
	/* Method for audit trail support */
	public String getLibraryStamp() {
		return "LibraryClass/Version/ID: ["
				+libClass+";"
				+libClassVersion+";"
				+libClassID+"]";		
	}
	
	public void setIsDebugMode(Boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
	}

	public Boolean getIsDebugMode() {
		return isDebugMode;
	}

	public static void resetHasCalledLibrary() {
		DataLib.hasCalledLibrary = false;
	}

	public static Boolean hasCalledLibrary() {
		return hasCalledLibrary;
	}

	public static String getLibClass() {
		return libClass;
	}

	public static String getLibClassVersion() {
		return libClassVersion;
	}

	public static long getLibClassID() {
		return libClassID;
	}	
	
	public static void debugging(String msg) {
		if(isDebugMode) {
			System.out.println(msg);
		}		
	}
	
	public static void debugging(String method, String msg) {
		if(isDebugMode) {
			System.out.println(method+": "+msg);
		}		
	}
	
	public static String generateTitle(Plot plot) {
		String title = "Histogram";
		String name = getKeyParameter(plot,KEY_SAMPLENAME);
		if(null!=name) {
			title = name;
		}		
		String runnum = getRunNum(plot);
		if (null!=runnum) {
			title = title.concat(" (" + runnum + ")");
		}
		String config = getKeyParameter(plot,KEY_CONFIG);
		if(null!=config) {
			title = title.concat(" ["+config+"]");
		}
		plot.setTitle(title);		
		return title;
	}
	
	public static IDataItem getHistogram(IGroup entry) {
		// Find Group with Group attribute "NX_class" having value "NXdata" 
		IGroup nxData = getDataGroup(entry); 
		IDataItem data = nxData.getDataItemWithAttribute(
				Util.NEXUS_SIGNAL_ATTRIBUTE_NAME,
				"1");
		return data;
	}
	
	public static IGroup getDataGroup(IGroup entry) {
		// Find Group with Group attribute "NX_class" having value "NXdata" 
		IGroup nxData = entry.getGroupWithAttribute(
				Util.NEXUS_CLASS_ATTRIBUTE_NAME, 
				Util.NEXUS_DATA_ATTRIBUTE_VALUE);
		return nxData;
	}
	
	public static String getRunNum(Plot plot) {
		String runnum = null;
		try {
			IDataItem run = plot.getDataItem(KEY_RUN);
			if (null!=run) {
				runnum = stripNum(run.getData().toString());
			}
		} catch (IOException e) {
			debugging("generateTitle","Cannot get data to set Run Number");
		}
		return runnum;
	}
	
	public static String getRunNum(IGroup group) {
		String runnum = null;
		try {
			IDataItem run = group.getDataItem(KEY_RUN);
			if (null!=run) {
				runnum = stripNum(run.getData().toString());
			}
		} catch (IOException e) {
			debugging("getRunNum","Cannot get data to set Run Number");
		}
		return runnum;
	}
	
	public static String getKeyParameter(Plot plot, String key) {
		String result = null;
		try {
			IDataItem item = plot.getDataItem(key);
			if (null!=item) {
				result = item.getData().toString();
			}
		} catch (IOException e) {
			debugging("getKeyParameter","Cannot get data to set "+key);
		}
		return result;
	}

	public static String getKeyParameter(IGroup group, String key) {
		String result = null;
		try {
			IDataItem item = group.getDataItem(key);
			if (null!=item) {
				result = item.getData().toString();
			}
		} catch (IOException e) {
			debugging("getKeyParameter","Cannot get data to set "+key);
		}
		return result;
	}

	private static String stripNum(String sValue) {
		Long n=0L;
		String formatter = "#0000";
		DecimalFormat form = new DecimalFormat(formatter);
		if(sValue.matches(".*QKK.*")) {
			StringBuffer b = new StringBuffer(sValue);
			int first = b.lastIndexOf("QKK")+3;
			int last =  b.lastIndexOf(".nx.hdf");
			String s = b.substring(first,last);
			n = Long.valueOf(s, 10);
		}
		return form.format(n);
	}
	
	private static String stripSpace(String sValue) {
		return "this_config";
	}

	public static String findPluginID() {
		String pluginId = DEFAULT_DRA_PLUGIN;
		ICommandLineOptions options = ServiceUtils.getService(ICommandLineOptions.class);
		if (null!=options) {
			if (options.hasOptionValue(OPTION_ALGO_SET)) {
				pluginId = options.getOptionValue(OPTION_ALGO_SET);
			}
		}
		return pluginId;
	}

	private List<IDataItem> copyAxes(IGroup src, IGroup tgt) {
		
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
					tgt.addDataItem(axis);
				}
			}
		}		
		return axes;
	}
	
	/**
	 * @param entry Group attached to dataset structure 
	 * @return true if "signal" data can be located in root-NxEntry-NxData structure
	 * @throws FileAccessException
	 */
	public static Boolean isNexusPlotStructure(IGroup entry) throws FileAccessException {
		if (null==entry) { return false; }
		
		IGroup root = entry.getRootGroup();		
		if (null==root) { 
			throw new FileAccessException("Invalid file structure"); 
		}
		// Find Group with Group attribute "NX_class" having value "NXdata" 
		IGroup nxData = DataLib.getDataGroup(entry); 		
		if (null==nxData) { 
			throw new FileAccessException("Invalid file structure"); 
		}
		return (null!=(nxData.getDataItemWithAttribute("signal","1")));
	}
	
}
