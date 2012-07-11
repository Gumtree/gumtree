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
package au.gov.ansto.bragg.wombat.exp.command;

import java.io.File;

import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView.ColorEnum;

import au.gov.ansto.bragg.wombat.exp.core.Command;
import au.gov.ansto.bragg.wombat.exp.core.WombatExperiment;
import au.gov.ansto.bragg.wombat.exp.exception.InitializeCommandException;

/**
 * @author nxi
 * Created on 30/06/2008
 */
public class Runscan implements Command {

	public final static String HMSCAN_PATH = "/commands/";
	public final static String HMSCAN_SCANVARIABLE_PATH = HMSCAN_PATH + "/scan_variable";
	public final static String HMSCAN_SCANSTART_PATH = HMSCAN_PATH + "/scan_start";
	public final static String HMSCAN_SCANINCREMENT_PATH = HMSCAN_PATH + "/scan_increment";
	public final static String HMSCAN_NUMBEROFPOINTS_PATH = HMSCAN_PATH + "/scan_variable";
	public final static String HMSCAN_MODE_PATH = HMSCAN_PATH + "/mode";
	public final static String HMSCAN_present_PATH = HMSCAN_PATH + "/present";
	
	public final static String RUNSCAN_COMMAND = "runscan";
	private WombatExperiment experiment;
	
	String scanVar;
	double start;
	double stop;
	int numberOfPoints;
	String mode;
	double preset;
	String saveType = "save";
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.wombat.exp.core.Command#getHelp()
	 */
	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.wombat.exp.core.Command#getShortDescription()
	 */
	public String getShortDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.wombat.exp.core.Command#run()
	 */
	public String run() {
		// TODO Auto-generated method stub
		String sicsCommand = RUNSCAN_COMMAND + " " + scanVar + " " +
		start + " " + stop + " " + numberOfPoints + " " + mode + " " +
		preset + " " + saveType;
		String resultFilename = "D:/dra/wombatdata/WBT0001166.nx.hdf";
		double step = (stop - start) / (numberOfPoints - 1);
		for (int i = 0; i < numberOfPoints; i++) {
			double position = start + i * step;
			position = Math.round(position * 100) / 100.;
			experiment.printlnToShell("move " + scanVar + " to position " + position + "\n");
			experiment.printlnToShell("do monitor scan for " + preset + " seconds\n");
			try {
				Thread.sleep((long) preset * 100);
			} catch (Exception e) {
				// TODO: handle exception
			}
			experiment.printlnToShell("append data to file: " + (new File(resultFilename)).getName() + "\n\n");
		}
		
		try {
			experiment.getSics().runRawCommand(sicsCommand);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			experiment.printlnToShell(e.getMessage(), ColorEnum.red);
		} 
		experiment.setScanResultFilename(resultFilename);
		experiment.setResultEntryList(experiment.getKakadu().addDataSourceFile(resultFilename));
		return "scan command finished\n";
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.wombat.exp.core.Command#setExperiment(au.gov.ansto.bragg.wombat.exp.core.EchidnaExperiment)
	 */
	public void setExperiment(WombatExperiment experiment) {
		// TODO Auto-generated method stub
		this.experiment = experiment;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.wombat.exp.core.Command#setParameter(java.lang.String[])
	 */
	public void setParameter(String... params) throws InitializeCommandException {
		// TODO Auto-generated method stub
		if (params.length < 6)
			throw new InitializeCommandException("expecting at least 6 arguments");
		try{
			scanVar = params[0];
			start = Double.valueOf(params[1]);
			stop = Double.valueOf(params[2]);
			numberOfPoints = Integer.valueOf(params[3]);
			mode = params[4];
			preset = Double.valueOf(params[5]);
			if (params.length > 6)
				saveType = params[6];
		}catch (Exception e) {
			// TODO: handle exception
			throw new InitializeCommandException("can not parse arguments");
		}
	}

	public WombatExperiment getExperiment(){
		return WombatExperiment.getInstance();
	}
}
