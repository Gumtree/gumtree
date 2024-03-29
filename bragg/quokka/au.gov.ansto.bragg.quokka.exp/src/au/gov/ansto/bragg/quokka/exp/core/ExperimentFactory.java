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
package au.gov.ansto.bragg.quokka.exp.core;

public class ExperimentFactory {

	static QuokkaExperiment experiment;
	
	public static QuokkaExperiment getExperimentInstance(){
		if (experiment == null) experiment = new QuokkaExperiment();
		return experiment;
	}
	
	public static QuokkaExperiment getExperimentInstance(String ...scanFunction){
		experiment = new QuokkaExperiment(scanFunction);
		return experiment;
	}
}
