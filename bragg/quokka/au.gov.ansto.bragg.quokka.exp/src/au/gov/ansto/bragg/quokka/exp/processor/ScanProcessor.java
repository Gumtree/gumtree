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
package au.gov.ansto.bragg.quokka.exp.processor;

import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView.ColorEnum;

import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;

/**
 * @author nxi
 * Created on 18/04/2008
 */
public class ScanProcessor implements ConcreteProcessor {

	private String scan_deviceId;
	private String scan_startPosition;
	private String scan_stopPosition;
	private String scan_step;
	private String scan_mode;
	private String scan_criteria;
	private Boolean scan_stop = false;
	private Boolean scan_finished = false;
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		QuokkaExperiment experiment = QuokkaExperiment.getInstance();
		String commandName = "scan";
		QuokkaExperiment.showCommandLineView();
		experiment.printlnToShell(commandName + " " + scan_deviceId + " " + scan_startPosition + 
				" " + scan_stopPosition + " " + scan_step + " " + scan_mode + " " + scan_criteria + 
				" ", ColorEnum.black);
		String result = experiment.runCommand(commandName, scan_deviceId, scan_startPosition, 
				scan_stopPosition, scan_step, scan_mode, scan_criteria);
		experiment.printlnToShell(result + "\n", ColorEnum.blue);
		experiment.printlnToShell(">>>", ColorEnum.darkRed);
		scan_finished = true;
		return scan_stop;
	}

	public void setScan_deviceId(String scan_deviceId) {
		this.scan_deviceId = scan_deviceId;
	}

	public void setScan_startPosition(String scan_startPosition) {
		this.scan_startPosition = scan_startPosition;
	}

	public void setScan_stopPosition(String scan_stopPosition) {
		this.scan_stopPosition = scan_stopPosition;
	}

	public void setScan_step(String scan_step) {
		this.scan_step = scan_step;
	}

	public void setScan_mode(String scan_mode) {
		this.scan_mode = scan_mode;
	}

	public void setScan_criteria(String scan_criteria) {
		this.scan_criteria = scan_criteria;
	}

	public void setScan_stop(Boolean scan_stop) {
		this.scan_stop = scan_stop;
	}

	public Boolean getScan_finished() {
		return scan_finished;
	}

}
