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
package au.gov.ansto.bragg.datastructures.core;

public class StaticDefinition {
	public final static String DATA_STRUCTURE_TYPE = "dataStructureType";
	public final static String DATA_DIMENSION_TYPE = "dataDimensionType";
	public final static String REGION_TYPE = "regionType";
	public final static String VARIANCE_DATA_REFERENCE_NAME = "data";
	public final static String DATA_VARIANCE_REFERENCE_NAME = "variance";
	// Expose the enums for scripting tools to access inner enum class
	public static DataStructureType getDataStructureType(String value) {
		return  DataStructureType.valueOf(value);
	}
	
	public static DataDimensionType getDataDimensionType(String value) {
		return  DataDimensionType.valueOf(value);
	}
	
	public static RegionType getRegionType(String value) {
		return  RegionType.valueOf(value);
	}
	
	/**
	 * Static fields definition and enumeric type definition 
	 * @author nxi
	 * Created on 20/03/2008
	 * Comments and extensions pvhathaway 17/6/09
	 */
	public enum DataStructureType{
		plotset, 		// Group of groups containing plottable data and axes 
		plot,    		// Group with plottable data and axes 
		calculation, 	// Group containing scalar items, usually resulting from calculation
		axis,    		// Variable, scale descriptor of plottable data along (usually) one dimension 
		region,  		// Group of parameters defining a region of interest for use with another data structure
		regionset,      // Group of region groups
		log,            // Group reserved for logging information
		nexusRoot, 		// Datafile root group (Nexus schema, attribute NXClass=NXRoot) 
		nexusEntry,     // Group of structures describing a single dataset/configuration (NXClass=NXEntry) 
		nexusData,      // Group of plottable data (Nexus schema, attribute NXClass=NXData)  
		configuration,  // Group reserved for configuration information
		draTask,        // structure representing a data treatment process
		fitFunction,    // structure representing a fitting function
		combined,    	// composite data structure 
		undefined
	};
	
	public enum DataDimensionType{
		tabular, 		// set/list of variables or key-value pairs
		pattern,		// one-dimensional plottable data 
		patternset,     // set of patterns, may have two-dimensional structure
		map,            // two-dimensional plottable data
		mapset,         // set of maps, may have three dimensional structure
		volume,         // three-dimensional plottable data
		volumeset,      // set of volumes, may appear as four-dimensional
		extended,       // higher-dimensional data set
		text,           // textual data eg logs
		undefined, 
		scalar,         // single scalar variable
		logic           // single logical variable (usually boolean)
	};
		
	public enum RegionType{rectilinear, polygonal, radial, point, undefined};
	
	public enum PlotMetadataType{marker, value, mapping, undefined};
}
