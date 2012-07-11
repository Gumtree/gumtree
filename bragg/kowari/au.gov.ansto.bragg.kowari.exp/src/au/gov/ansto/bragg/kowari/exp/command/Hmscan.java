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
package au.gov.ansto.bragg.kowari.exp.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.dom.sics.SicsDOMException;

import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOMFactory;
import au.gov.ansto.bragg.kowari.exp.core.Command;
import au.gov.ansto.bragg.kowari.exp.core.KowariExperiment;
import au.gov.ansto.bragg.kowari.exp.exception.InitializeCommandException;

/**
 * @author nxi
 * Created on 10/07/2008
 */
public class Hmscan implements Command {

	public final static String HMSCAN_PATH = "/commands/scan/hmscan";
	public final static String HMSCAN_SCANVARIABLE_PATH = HMSCAN_PATH + "/scan_variable";
	public final static String HMSCAN_SCANSTART_PATH = HMSCAN_PATH + "/scan_start";
	public final static String HMSCAN_SCANINCREMENT_PATH = HMSCAN_PATH + "/scan_increment";
	public final static String HMSCAN_NUMBEROFPOINTS_PATH = HMSCAN_PATH + "/NP";
	public final static String HMSCAN_MODE_PATH = HMSCAN_PATH + "/mode";
	public final static String HMSCAN_PRESET_PATH = HMSCAN_PATH + "/preset";
	public final static String HMSCAN_CHANNEL_PATH = HMSCAN_PATH + "/channel";
	public final static String HMSCAN_STATUS_PATH = HMSCAN_PATH + "/feedback/status";
	public final static String HMSCAN_VARIABLE_VALUE_PATH = HMSCAN_PATH + "/feedback/scan_variable_value";
	public final static String HMSCAN_FILENAME_PATH = "/experiment/file_name";
	public static List<IDynamicControllerListener> statusListenerList = 
		new ArrayList<IDynamicControllerListener>(); 
	public static List<IDynamicControllerListener> variableListenerList = 
		new ArrayList<IDynamicControllerListener>(); 
	private static final int TIME_OUT = 1000;
	
	private static final int TIME_INTERVAL = 10;
		
	public final static String RUNSCAN_COMMAND = "runscan";
	private KowariExperiment experiment;
	private boolean dirtyFlag = false;
	
	String scanVar;
	double start;
	double stop;
	int numberOfPoints;
	String mode;
	double preset;
	String channel = "0";
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.core.Command#getHelp()
	 */
	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.core.Command#getShortDescription()
	 */
	public String getShortDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.core.Command#run()
	 */
	public String run() {
		// TODO Auto-generated method stub
		IDynamicController status = null;
		try {
			status = experiment.getDynamicController(HMSCAN_STATUS_PATH);
		} catch (SicsDOMException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		IDynamicControllerListener statusListener = new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				if(newValue.getStringData().equals("BUSY")) {
					dirtyFlag = true;
				}
			}
		};
		for (IDynamicControllerListener listener : statusListenerList){
			status.removeComponentListener(listener);
		}
		statusListenerList.clear();
		status.addComponentListener(statusListener);
		statusListenerList.add(statusListener);
		
		IDynamicController variableValue = null;
		try {
			variableValue = experiment.getDynamicController(HMSCAN_VARIABLE_VALUE_PATH);
		} catch (SicsDOMException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		IDynamicControllerListener variableValueListener = new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				experiment.printlnToShell("move " + scanVar + " to " + newValue.getStringData() + "\n");
				experiment.printlnToShell("Do a hmm scan with " + mode + "=" + preset + "\n");
			}
		};
		for (IDynamicControllerListener listener : variableListenerList){
			variableValue.removeComponentListener(listener);
		}
		variableListenerList.clear();
		variableValue.addComponentListener(variableValueListener);
		variableListenerList.add(variableValueListener);
		
		IDynamicController filename = null;
		try {
			filename = experiment.getDynamicController(HMSCAN_FILENAME_PATH);
		} catch (SicsDOMException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		experiment.sicsSet(HMSCAN_SCANVARIABLE_PATH, scanVar);
		experiment.sicsSet(HMSCAN_SCANSTART_PATH, String.valueOf(start));
		experiment.sicsSet(HMSCAN_SCANINCREMENT_PATH, String.valueOf((stop - start) / 
				numberOfPoints));
		experiment.sicsSet(HMSCAN_NUMBEROFPOINTS_PATH, String.valueOf(numberOfPoints));
		experiment.sicsSet(HMSCAN_MODE_PATH, mode);
		experiment.sicsSet(HMSCAN_PRESET_PATH, String.valueOf(preset));
		experiment.sicsSet(HMSCAN_CHANNEL_PATH, channel);
		try {
			Thread.sleep(100);
		} catch (Exception e) {
			// TODO: handle exception
		}
		dirtyFlag = false;
		int count = 0;
		experiment.sicsRun(HMSCAN_PATH);
		while(!dirtyFlag) {
			try {
				Thread.sleep(TIME_INTERVAL);
				count += TIME_INTERVAL;
				if(count > TIME_OUT) {
					status.removeComponentListener(statusListener);
					return "Time out on starting monitor count";
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				status.removeComponentListener(statusListener);
				return e.getMessage();
			}
		}
		while(true) {
			try {
				if(status.getValue().getStringData().equals("IDLE")) {
					status.removeComponentListener(statusListener);
					variableValue.removeComponentListener(variableValueListener);
					String resultFilename = filename.getValue().getStringData();
					experiment.printlnToShell("Save scan result in file: " + resultFilename + "\n");
					if (resultFilename.equals("UNKNOWN")) 
						resultFilename = "D:/dra/kowaridata/KWR0000019.nx.hdf";
					else {
						File newFile = new File(resultFilename);
						resultFilename = "W:/commissioning/" + newFile.getName();
					}
					if (!(new File(resultFilename)).exists())
						resultFilename = "D:/dra/kowaridata/KWR0000019.nx.hdf";
					experiment.setScanResultFilename(resultFilename);
					CicadaDOM cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
					IGroup resultGroup = cicada.loadDataFromFile(resultFilename);
					experiment.setResultEntryList(((NcGroup) resultGroup).getEntries());
//					experiment.setResultEntryList(experiment.getKakadu().addDataSourceFile(resultFilename));
					status.removeComponentListener(statusListener);
					variableValue.removeComponentListener(variableValueListener);
					return "scan command finished\n";
				}
				Thread.sleep(TIME_INTERVAL);
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				status.removeComponentListener(statusListener);
				return e.getMessage();
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.core.Command#setExperiment(au.gov.ansto.bragg.kowari.exp.core.EchidnaExperiment)
	 */
	public void setExperiment(KowariExperiment experiment) {
		// TODO Auto-generated method stub
		this.experiment = experiment;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.core.Command#setParameter(java.lang.String[])
	 */
	public void setParameter(String... params) throws InitializeCommandException {
		// TODO Auto-generated method stub
		if (params == null || params.length < 6)
			throw new InitializeCommandException("expecting at least 6 arguments");
		try{
			scanVar = params[0];
			start = Double.valueOf(params[1]);
			stop = Double.valueOf(params[2]);
			numberOfPoints = Integer.valueOf(params[3]);
			mode = params[4];
			preset = Double.valueOf(params[5]);
			if (params.length > 6)
				channel = params[6];
		}catch (Exception e) {
			// TODO: handle exception
			throw new InitializeCommandException("can not parse arguments");
		}
	}

	public KowariExperiment getExperiment(){
		return KowariExperiment.getInstance();
	}
}
