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
 * Created on 23/04/2008
 */
public class MovetoProcessor implements ConcreteProcessor {

	private Boolean moveto_in;
	private String moveto_deviceId = "default";
	private String moveto_statisticExtremumName = "peak";
	private Boolean moveto_autoFlag = true;
	private Boolean moveto_stop = false;
	private Boolean moveto_skip = false;
	private Boolean moveto_finished = false;
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		if (moveto_skip) return moveto_stop;
		QuokkaExperiment experiment = QuokkaExperiment.getInstance();
		String commandName = "moveto";
		String autoFlag = moveto_autoFlag ? "auto" : "noauto";
		QuokkaExperiment.showCommandLineView();
		String result;
		if (moveto_deviceId.equals("default")){
			experiment.printlnToShell(commandName + " " + moveto_statisticExtremumName + " "
					+ autoFlag + "\n", ColorEnum.black);
			result = experiment.runCommand(commandName, moveto_statisticExtremumName, 
					autoFlag);
		}
		else{
			experiment.printlnToShell(commandName + " " + moveto_statisticExtremumName + " "
					+ moveto_statisticExtremumName + " " + autoFlag + "\n", ColorEnum.black);
			result = experiment.runCommand(commandName, moveto_deviceId, 
					moveto_statisticExtremumName, autoFlag);
		}
		experiment.printlnToShell(result + "\n", ColorEnum.blue);
		experiment.printlnToShell(">>>", ColorEnum.darkRed);
		moveto_finished = true;
		return moveto_stop;
	}

	/**
	 * @return the moveto_finished
	 */
	public Boolean getMoveto_finished() {
		return moveto_finished;
	}

	/**
	 * @param moveto_deviceId the moveto_deviceId to set
	 */
	public void setMoveto_deviceId(String moveto_deviceId) {
		this.moveto_deviceId = moveto_deviceId;
	}

	/**
	 * @param moveto_statisticExtremumName the moveto_statisticExtremumName to set
	 */
	public void setMoveto_statisticExtremumName(String moveto_statisticExtremumName) {
		this.moveto_statisticExtremumName = moveto_statisticExtremumName;
	}

	/**
	 * @param moveto_autoFlag the moveto_autoFlag to set
	 */
	public void setMoveto_autoFlag(Boolean moveto_autoFlag) {
		this.moveto_autoFlag = moveto_autoFlag;
	}

	/**
	 * @param moveto_stop the moveto_stop to set
	 */
	public void setMoveto_stop(Boolean moveto_stop) {
		this.moveto_stop = moveto_stop;
	}

	/**
	 * @param moveto_skip the moveto_skip to set
	 */
	public void setMoveto_skip(Boolean moveto_skip) {
		this.moveto_skip = moveto_skip;
	}

	/**
	 * @param moveto_in the moveto_in to set
	 */
	public void setMoveto_in(Boolean moveto_in) {
		this.moveto_in = moveto_in;
	}

}
