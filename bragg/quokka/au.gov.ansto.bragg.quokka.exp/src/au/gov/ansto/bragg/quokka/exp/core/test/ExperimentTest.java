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
package au.gov.ansto.bragg.quokka.exp.core.test;

import au.gov.ansto.bragg.quokka.exp.core.ExperimentFactory;
import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;

public class ExperimentTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		QuokkaExperiment experiment = ExperimentFactory.getExperimentInstance("CentroidX","CentroidZ");
		System.out.println(experiment.toString());
		
	}

}
