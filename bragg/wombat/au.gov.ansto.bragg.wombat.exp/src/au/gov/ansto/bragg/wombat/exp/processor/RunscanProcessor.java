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
package au.gov.ansto.bragg.wombat.exp.processor;

import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView.ColorEnum;

import au.gov.ansto.bragg.kakadu.dom.KakaduDOM;
import au.gov.ansto.bragg.wombat.exp.core.WombatExperiment;

/**
 * @author nxi
 * Created on 18/04/2008
 */
public class RunscanProcessor  {

	private String runscan_scanVar;
	private String runscan_startPosition;
	private String runscan_stopPosition;
	private String runscan_numPoints;
	private String runscan_mode;
	private String runscan_preset;
	private String runscan_channel;
	private Boolean runscan_stop = false;
	private IGroup runscan_resultEntry;
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		WombatExperiment experiment = WombatExperiment.getInstance();
		String commandName = "hmscan";
		IWorkbenchWindow window = KakaduDOM.findKakaduWindow();
		if (window != null)
			WombatExperiment.showCommandLineView(window);
		else
			WombatExperiment.showCommandLineView();
		experiment.printlnToShell(commandName + " " + runscan_scanVar + " " + runscan_startPosition + 
				" " + runscan_stopPosition + " " + runscan_numPoints + " " + runscan_mode + " " + 
				runscan_preset + " " + runscan_channel + 
				" \n", ColorEnum.black);
		String result = experiment.runCommand(commandName, runscan_scanVar, runscan_startPosition, 
				runscan_stopPosition, runscan_numPoints, runscan_mode, runscan_preset, runscan_channel);
//		Thread.sleep(10000);
		experiment.printlnToShell(result + "\n", ColorEnum.blue);
		experiment.printlnToShell(">>>", ColorEnum.darkRed);
		List<IGroup> entryList = experiment.getResultEntryList();
		if (entryList.size() > 0){
			runscan_resultEntry = entryList.get(0);
			return runscan_stop;
		}
		return true;
	}

	/**
	 * @return the runscan_finished
	 */
	public IGroup getRunscan_resultEntry() {
		return runscan_resultEntry;
	}

	/**
	 * @param runscan_scanVar the runscan_scanVar to set
	 */
	public void setRunscan_scanVar(String runscan_scanVar) {
		this.runscan_scanVar = runscan_scanVar;
	}

	/**
	 * @param runscan_startPosition the runscan_startPosition to set
	 */
	public void setRunscan_startPosition(String runscan_startPosition) {
		this.runscan_startPosition = runscan_startPosition;
	}

	/**
	 * @param runscan_stopPosition the runscan_stopPosition to set
	 */
	public void setRunscan_stopPosition(String runscan_stopPosition) {
		this.runscan_stopPosition = runscan_stopPosition;
	}

	/**
	 * @param runscan_numPoints the runscan_numPoints to set
	 */
	public void setRunscan_numPoints(String runscan_numPoints) {
		this.runscan_numPoints = runscan_numPoints;
	}

	/**
	 * @param runscan_mode the runscan_mode to set
	 */
	public void setRunscan_mode(String runscan_mode) {
		this.runscan_mode = runscan_mode;
	}

	/**
	 * @param runscan_preset the runscan_preset to set
	 */
	public void setRunscan_preset(String runscan_preset) {
		this.runscan_preset = runscan_preset;
	}

	/**
	 * @param runscan_saveType the runscan_saveType to set
	 */
	public void setRunscan_saveType(String runscan_saveType) {
		this.runscan_channel = runscan_saveType;
	}

	/**
	 * @param runscan_stop the runscan_stop to set
	 */
	public void setRunscan_stop(Boolean runscan_stop) {
		this.runscan_stop = runscan_stop;
	}


}
