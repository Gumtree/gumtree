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
package au.gov.ansto.bragg.quokka.dra.online;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.dam.core.DataManager;
import au.gov.ansto.bragg.cicada.dam.core.DataManagerFactory;
import au.gov.ansto.bragg.cicada.dam.core.exception.DataManagerException;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.online.util.LinkedMeasurement;
import au.gov.ansto.bragg.quokka.dra.online.util.ResultRecord;
import au.gov.ansto.bragg.quokka.experiment.result.ExperimentResult;
import au.gov.ansto.bragg.quokka.experiment.result.Measurement;

public class Selector extends ConcreteProcessor {

	private static final String processClass = "Selector"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009070801; 

	private final static DataStructureType dataStructureType = DataStructureType.undefined;
	private final static DataDimensionType dataDimensionType = DataDimensionType.undefined;
    private Boolean isDebugMode = true;

	private Boolean doForceStop = false;

	private ExperimentResult model;
	private ResultRecord result;
	
	private IGroup outGroup;
	
	private static final String FILEPREFIX = "QKK";
	private static final String FILESUFFIX = ".nx.hdf";
	
	private DataManager manager;
	private URI mURI;
	private String filename;
	
	public Boolean process() throws Exception {
		
		if (null==manager) {
			try {
				manager = DataManagerFactory.getDataManager();
			} catch (DataManagerException e) {
				if (isDebugMode) {
					e.printStackTrace();
				}
				doForceStop = true;
				throw new Exception(e);
			}	
		}

		List <LinkedMeasurement> measurementList = result.getMarkedMeasurements();
		if ((null!=measurementList) && (1<measurementList.size())) {
			
			LinkedMeasurement lm = measurementList.get(0);
			//mURI = fetchURI(lm);
			filename = fetchName(lm);

			File checkFile = new File(filename);
			String dataPath = System.getProperty("sics.data.path");
			checkFile = new File(dataPath + "/" + checkFile.getName());
			
			if (!checkFile.exists()){
				String errorMessage = "The target file :" + 
					checkFile.getAbsolutePath()+" can not be found";
				throw new FileNotFoundException(errorMessage);
			}
			URI nexusDataUri = NexusUtils.createNexusDataURI(checkFile.getPath(),null,0);

			mURI = nexusDataUri;

			super.informVarValueChange("mURI", this.mURI);

			outGroup = (IGroup) manager.getObject(mURI);
		}		
		return doForceStop;
	}
	
	private URI fetchURI(LinkedMeasurement lm) {
		URI uri;
		try {
			String filename = fetchName(lm);
			uri = new URI(filename);
		} catch (URISyntaxException us) {
			if (isDebugMode) {
				System.out.println("Malformed file URI from runID");
			}
			uri = null;
		}
		return uri;
	}
	
	private String fetchName(LinkedMeasurement lm) {
		Measurement m = lm.getMeasurement();
		String runID = m.getFilename();
		String name = FILEPREFIX+runID+FILESUFFIX;
		return name;
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
	
	public void setResult(ResultRecord result) {
		this.result = result;
	}    

	public void setModel(ExperimentResult model) {
		this.model = model;
	}
	
    /* Out-Ports ----------------------------------------------------*/

	public IGroup   getOutGroup() {
		return this.outGroup;
	}
	
	public Boolean getOutLoop() {
		return this.doForceStop;
	}
	
	/* Var-Ports (options) ------------------------------------------*/

	public void setStop(Boolean doForceStop) {
		this.doForceStop = doForceStop;
	}

    /* Var-Ports (tuners) -------------------------------------------*/
	
	/* No tunable options for Single Selector. Returns first marked sample file */
	public void setMURI(URI muri) {
		this.mURI = muri;
	}
}
