/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package au.gov.ansto.bragg.quokka.ui.workflow;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.core.object.ObjectConfigException;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.tasks.EmptyTaskView;

import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfigTemplate;
import au.gov.ansto.bragg.quokka.experiment.model.Sample;

public class QuokkaExperimentConfigurator extends AbstractExperimentTask {

	/*************************************************************************
	 * Workflow context keys
	 *************************************************************************/
	private static final String CXT_EXPERIMENT = Experiment.class.getName();
	
	private static final String CXT_CONFIG_TEMPLATE = "configTemplate";
	
	/*************************************************************************
	 * Task parameter keys
	 *************************************************************************/
	private static final String PARAM_SAMPLE_SIZE = "sampleSize";
	
	private static final String PARAM_SAMPLE_ENV_CONTROLLERS = "sampleEnvControllers";

	private static final String PARAM_CONFIGS = "configs";
	
	private static final String PARAM_LABEL = "label";
	
	private static final String PARAM_DET_POSITION = "detPosition";
	
	private static final String PARAM_DET_OFFSET = "detOffset";
	
	private static final String PARAM_GUIDE = "guide";
	
	private static final String PARAM_ENT_ROT_AP = "entRotAp";
	
	private static final String PARAM_BS_UP = "bsUp";
	
	private static final String PARAM_BSX = "bsx";
	
	private static final String PARAM_BSZ = "bsz";
	
	private static final String PARAM_TRANSMISSION_ATT = "transmissionAttenuation";
	
	private static final String PARAM_STARTING_ATT = "startingAttenuation";
	
	/*************************************************************************
	 * Global variables
	 *************************************************************************/
	private static final int DEFAULT_SAMPLE_SIZE = 1;
		
	@Override
	protected ITaskView createViewInstance() {
		return new EmptyTaskView();
	}

	@Override
	public void initialise() {
		
		/*********************************************************************
		 * Inject experiment model
		 *********************************************************************/
		Experiment experiment = new Experiment();
		if (!getContext().containsKey(CXT_EXPERIMENT)) {
			getContext().put(Experiment.class.getName(), experiment, true);
		}
		
		/*********************************************************************
		 * Configures available sample holder
		 *********************************************************************/
		List<Sample> samples = getExperiment().getSamples();
		if (samples.size() == 0) {
			int sampleSize = getParameters().get(PARAM_SAMPLE_SIZE, Integer.class, DEFAULT_SAMPLE_SIZE);
			// Initialise samples
			for (int i = 1; i <= sampleSize; i++) {
				Sample sample = new Sample();
				sample.setPosition(i);
				samples.add(sample);
			}
		}
		
		/*********************************************************************
		 * Configures available sample environment controllers
		 *********************************************************************/
		String controllerIds = getParameters().getString(PARAM_SAMPLE_ENV_CONTROLLERS, "");
		for (String controllerId : controllerIds.split(",")) {
			experiment.getSampleEnvControllerIds().add(controllerId.trim());
		}
		
		List<InstrumentConfigTemplate> configTemplate = new ArrayList<InstrumentConfigTemplate>();
		getContext().put(CXT_CONFIG_TEMPLATE, configTemplate, false);
		
		String configIds = getParameters().getString(PARAM_CONFIGS, "");
		for (String configId : configIds.split(",")) {
			/*****************************************************************
			 * Setup
			 *****************************************************************/
			configId = configId.trim();
			// Create template
			InstrumentConfigTemplate config = new InstrumentConfigTemplate();
			// Set label
			String configLabel = getParam(configId, PARAM_LABEL);
			config.setName(configLabel);
			config.setDescription("Standard " + configLabel + " Config");
			
			/*****************************************************************
			 * Init script
			 *****************************************************************/
			StringBuilder buffer = new StringBuilder();
			String detPosition = getParam(configId, PARAM_DET_POSITION);
			String detOffset = getParam(configId, PARAM_DET_OFFSET);
			String guide = getParam(configId, PARAM_GUIDE);
			String entRotAp = getParam(configId, PARAM_ENT_ROT_AP);
			String bsUp = getParam(configId, PARAM_BS_UP);
//			String bsDown = getParam(configId, PARAM_BS_DOWN);
			
			buffer.append("# Drive attenuator to safe value");
			buffer.append("\n");
			buffer.append("driveAtt(330)");
			buffer.append("\n");
			buffer.append("# Drive detector to " + configId + " position");
			buffer.append("\n");
			buffer.append("driveDet(" + detPosition + "," + detOffset + ")");
			buffer.append("\n");
			buffer.append("# Drive guide to " + guide);
			buffer.append("\n");
			buffer.append("driveGuide(guideConfig." + guide + ")");
			buffer.append("\n");
			buffer.append("# Drive entrance aperture to " + entRotAp + " (for guide " + guide + ")");
			buffer.append("\n");
			buffer.append("driveEntRotAp(" + entRotAp + ")");
			buffer.append("\n");
			buffer.append("# Select beam stop");
			buffer.append("\n");
			buffer.append("selBs(" + bsUp + ")");
			
			config.setInitScript(buffer.toString());
			
			/*****************************************************************
			 * Pre-transmission script
			 *****************************************************************/
			buffer = new StringBuilder();
			String bsx = getParam(configId, PARAM_BSX);
			String transAtt = getParam(configId, PARAM_TRANSMISSION_ATT);

			buffer.append("# Move the beamstop out");
			buffer.append("\n");
			buffer.append("driveBsx(" + bsx + ", 100)");
			buffer.append("\n");
			buffer.append("# Drive attenuator to " + transAtt);
			buffer.append("\n");
			buffer.append("driveAtt(" + transAtt + ")");		
			
			config.setPreTransmissionScript(buffer.toString());
			
			/*****************************************************************
			 * Pre-scattering script
			 *****************************************************************/
			buffer = new StringBuilder();
			String bsz = getParam(configId, PARAM_BSZ);
			String startingAtt = getParam(configId, PARAM_STARTING_ATT);
			
			buffer.append("# Drive attenuator to safe value");
			buffer.append("\n");
			buffer.append("driveAtt(330)");
			buffer.append("\n");
			buffer.append("# Move the beamstop in");
			buffer.append("\n");
			buffer.append("driveBsx(" + bsx + ", 0)");
			buffer.append("\n");
			buffer.append("driveBsz(" + bsz + ")");
			config.setStartingAttenuation(Integer.parseInt(startingAtt));
			config.setPreScatteringScript(buffer.toString());
			
			configTemplate.add(config);
		}

	}
	
	private String getParam(String configId, String paramKey) throws ObjectConfigException {
		String value = getParameters().getString(configId + "." + paramKey);
		if (value == null) {
			throw new ObjectConfigException("Missing parameter " + paramKey + " for config " + configId);
		}
		return value;
	}
	
}
