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
package au.gov.ansto.bragg.kowari.exp.processor;

import au.gov.ansto.bragg.kowari.exp.core.KowariExperiment;

/**
 * @author nxi
 * Created on 03/09/2008
 */
public class InitialiseProcessor {

	public final static String EXPERIMENT_TITLE_PATH="/experiment/title";
	public final static String SAMPLE_NAME_PATH="/sample/name";
	public final static String SAMPLE_TITLE_PATH="/sample/short_title";
	public final static String SAMPLE_DESCRIPTION="/sample/description";
	
	private String initialise_experimentTitle;
	private String initialise_sampleName;
	private String initialise_sampleTitle;
	private String initialise_sampleDescription;
	private Boolean initialise_skip = false;
	private Boolean initialise_stop = false;
	private Boolean initialise_isDone = false;
	
	public Boolean process() throws Exception{
		if (initialise_skip)
			return initialise_stop;
		KowariExperiment experiment = KowariExperiment.getInstance();
		if (initialise_experimentTitle != null && initialise_experimentTitle.trim().length() > 0)
			experiment.sicsSet(EXPERIMENT_TITLE_PATH, initialise_experimentTitle);
		if (initialise_sampleName != null && initialise_sampleName.trim().length() > 0)
		experiment.sicsSet(SAMPLE_NAME_PATH, initialise_sampleName);
		if (initialise_sampleTitle != null && initialise_sampleTitle.trim().length() > 0)
		experiment.sicsSet(SAMPLE_TITLE_PATH, initialise_sampleTitle);
		if (initialise_sampleDescription != null && initialise_sampleDescription.trim().length() > 0)
		experiment.sicsSet(SAMPLE_DESCRIPTION, initialise_sampleDescription);
		initialise_isDone = true;
		return initialise_stop;
	}

	/**
	 * @return the initialise_isDone
	 */
	public Boolean getInitialise_isDone() {
		return initialise_isDone;
	}

	/**
	 * @param initialise_experimentTitle the initialise_experimentTitle to set
	 */
	public void setInitialise_experimentTitle(String initialise_experimentTitle) {
		this.initialise_experimentTitle = initialise_experimentTitle;
	}

	/**
	 * @param initialise_sampleName the initialise_sampleName to set
	 */
	public void setInitialise_sampleName(String initialise_sampleName) {
		this.initialise_sampleName = initialise_sampleName;
	}

	/**
	 * @param initialise_sampleTitle the initialise_sampleTitle to set
	 */
	public void setInitialise_sampleTitle(String initialise_sampleTitle) {
		this.initialise_sampleTitle = initialise_sampleTitle;
	}

	/**
	 * @param initialise_sampleDescription the initialise_sampleDescription to set
	 */
	public void setInitialise_sampleDescription(String initialise_sampleDescription) {
		this.initialise_sampleDescription = initialise_sampleDescription;
	}

	/**
	 * @param initialise_skip the initialise_skip to set
	 */
	public void setInitialise_skip(Boolean initialise_skip) {
		this.initialise_skip = initialise_skip;
	}

	/**
	 * @param initialise_stop the initialise_stop to set
	 */
	public void setInitialise_stop(Boolean initialise_stop) {
		this.initialise_stop = initialise_stop;
	}
	
}
