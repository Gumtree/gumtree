/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.quokka.experiment.result;

import junit.framework.TestCase;
import au.gov.ansto.bragg.quokka.core.tests.ExperimentModelFactory;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;

public class ExperimentResultGenerationTest extends TestCase {

	public void testNormalAcquisitionResultGeneration() {
		/*********************************************************************
		 * Prepares experiment model
		 *********************************************************************/
		Experiment experiment = ExperimentModelFactory.createSimpleNormalExperiment();
		
		/*********************************************************************
		 * Creates result model
		 *********************************************************************/
		ExperimentResult result = ExperimentResultUtils.createExperimentResult(experiment);
		assertNotNull(result);
		
		String xml = ExperimentResultUtils.getXStream().toXML(result);
		System.out.println(xml);
	}
	
	public void testControlledAcquisitionResultGeneration() {
		/*********************************************************************
		 * Prepares experiment model
		 *********************************************************************/
		Experiment experiment = ExperimentModelFactory.createSimpleControlledExperiment();
		
		/*********************************************************************
		 * Creates result model
		 *********************************************************************/
		ExperimentResult result = ExperimentResultUtils.createExperimentResult(experiment);
		assertNotNull(result);
		
		String xml = ExperimentResultUtils.getXStream().toXML(result);
		System.out.println(xml);
	}
	
}
