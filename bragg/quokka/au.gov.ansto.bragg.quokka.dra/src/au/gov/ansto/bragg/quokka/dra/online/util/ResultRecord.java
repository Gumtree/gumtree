/******************************************************************************* 
* Copyright (c) 2009 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Paul Hathaway - June 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.online.util;

import java.util.ArrayList;
import java.util.List;

import au.gov.ansto.bragg.quokka.experiment.result.*;

public class ResultRecord {
	
	private ExperimentResult eResult;
	private List<SampleResult> markedSamples;
	private List<ExperimentConfig> markedConfigs;
	private List<LinkedMeasurement> markedMeas;
	
	public ResultRecord(ExperimentResult eRes) {
		eResult = eRes;
		markedSamples = new ArrayList<SampleResult>();
		markedConfigs = new ArrayList<ExperimentConfig>();
		markedMeas    = new ArrayList<LinkedMeasurement>();
		filterModel();
	}
	
	private void filterModel() {
		List<SampleResult> sList = eResult.getSampleResults();
		for(SampleResult s : sList) {
			int sumMarked = 0;
			List<ExperimentConfig> cList = new ArrayList<ExperimentConfig>();			
			if (eResult.isControlledEnvironment()) {
				List<ControlledEnvironment> eList = s.getControlledEnvs();
				for(ControlledEnvironment e : eList) {
					cList.addAll(e.getConfigs());
				}
			} else {
				cList.addAll(s.getConfigs());
			}
			for(ExperimentConfig c : cList) {
				int numMarked = 0;
				List<ScatteringMeasurement> mList = c.getScatteringMeasurements();
				for(ScatteringMeasurement m : mList) {
					if (isMarked(m)) {
						LinkedMeasurement l = new LinkedMeasurement(m);
						l.setConfig(c);
						l.setSampleResult(s);
						l.setExperimentResult(eResult);
						markedMeas.add(l);
						numMarked++;
					}
				}
				if(0<numMarked) {
					markedConfigs.add(c);
					sumMarked += numMarked;
				}
			}
			if(0<sumMarked) {
				markedSamples.add(s);
			}
		}
	}
	
	private boolean isMarked(Measurement s) {
		boolean marked = false;
		if (s instanceof ScatteringMeasurement) {
			marked = ((ScatteringMeasurement) s).isProcessRequested();
		}
		// considered alternative mechanism: return (PROCESSMARKER == s.getMarker());
		return marked;
	}
	
	public List<SampleResult> getSampleResults() {
		return eResult.getSampleResults();
	}

	public boolean isControlledEnvironment() {
		return eResult.isControlledEnvironment();
	}

	public String getSensitivityFile() {
		return eResult.getSensitivityFile();
	}
	
	public List<SampleResult> getMarkedSamples() {
		return markedSamples;
	}

	public List<ExperimentConfig> getMarkedConfigs() {
		return markedConfigs;
	}
	
	public List<LinkedMeasurement> getMarkedMeasurements() {
		return markedMeas;
	}
}

