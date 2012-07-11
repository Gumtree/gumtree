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
package au.gov.ansto.bragg.quokka.exp.core.command;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView;

import au.gov.ansto.bragg.quokka.exp.core.Command;
import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;
import au.gov.ansto.bragg.quokka.exp.core.ScanResult;
import au.gov.ansto.bragg.quokka.exp.core.exception.InitializeCommandException;
import au.gov.ansto.bragg.quokka.exp.core.scanfunction.Function.FunctionalStatistic;
import au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice;
import au.gov.ansto.bragg.quokka.model.core.instrument.QuokkaInstrument;

/**
 * @author nxi
 * Created on 10/04/2008
 */
public class Moveto implements Command {

	QuokkaExperiment experiment;
	FunctionalStatistic name = null;
	VirtualDevice device;
	boolean autoFlag = false;
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#getHelp()
	 */
	public String getHelp() {
		String help = "MOVETO: apply the most recent scan command result. It will move the " +
		"device to a statisic extremum of the scan result.\n\n";
		help += "Usage: moveto [<DeviceId>] [<StatisticExtremumName>] [<AutoFlag>='auto'/'noauto']\n\n";
		help += "This generic command will send instructions to SICS server to drive a "
			+ "device to a specific position.\n";
		help += "The position is got from the statistic extremum of the scan result.\n\n";
		help += "[<DeviceId>] \t the id of the target device to drive. It is an optional " +
				"aargument. If no device id is provided, it will use the device that is used" +
				"in the most recent scan command.\n\n";
		help += "<StatisticExtremumName> \t the name of the result in the last scan command. " +
		"The statistic extremum is calculated with the primary scan function in the " +
		"scan function list applied to the scan command. " +
		"The position is taken from the statistic extremum result. It is " + 
		"an optional argument. If no name is provided, it will use a default statistic " +
		"extremum result.\n\n";
		help += "The available StatisticExtremumNames are: ";
		FunctionalStatistic[] names = FunctionalStatistic.values();
		boolean isFirst = true;
		for (FunctionalStatistic name : names) {
			if (isFirst) {
				help += name.name();
				isFirst = false;
			} else 
				help += ", " + name.name();
		}
		help += ".\n\n";
		help += "<AutoFlag> \t the automatic performing flag. It takes a two values, 'auto' " +
		"or 'noauto'. If set to be auto this command" +
		"will perform the move action without asking for confirmation. Otherwise it " +
		"will prompt a confirm window for user to confirm this action. It is " +
		"an optional argument. If no AutoFlag is provided, by default it will " +
		"ask for user confirmation.\n";
		return help;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#getShortDescription()
	 */
	public String getShortDescription() {
		String description = "MOVETO: apply the most recent scan command result. It will move the " +
		"device to a statisic extremum of the scan result.\n";
		description += "Usage: moveto [<DeviceId>] [<StatisticExtremumName>] [<AutoFlag>='auto'/'noauto']\n";
		description += "For more information, please use 'help take'.\n";
		return description;	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#run()
	 */
	public String run() {
		// TODO Auto-generated method stub
//		Function scanFunction; 
//		try {
//			scanFunction = QuokkaExperiment.getFunctionList().get(0);	
//		} catch (Exception e) {
//			// TODO: handle exception
//			return "no scan funnction is defined";
//		}
//		double statisticResult = Double.NaN;
//		try {
//			if (name != null) statisticResult = scanFunction.getStatistic(name);
//			else statisticResult = scanFunction.getPeak();
//			if (Double.isNaN(statisticResult))
//				return "no " + name.name() + " is available";
//		} catch (Exception e) {
//			// TODO: handle exception
//			return "no " + name.name() + " is available";
//		}
//		try {
//			device = scanFunction.getDevice();
//			if (device == null)
//				return "can not find device in last scan";
//		} catch (Exception e) {
//			// TODO: handle exception
//			return "can not find device in last scan";
//		}
		try{
			if (device == null) device = QuokkaExperiment.getFunctionList().get(0).getDevice();
		}catch (Exception e) {
			// TODO: handle exception
			return "can not identify the device to move";
		}
		double statisticResult = Double.NaN;
		try {
			ScanResult result = QuokkaExperiment.findResultForSameDevice(device);
			if (name != null) statisticResult = result.getStatistic(name);
			else statisticResult = result.getPeak();
			if (Double.isNaN(statisticResult))
				return "no " + name.name() + " is available";
			} catch (Exception e) {
			// TODO: handle exception
				return "can not find the statistic result for the devic: " + device.getId();
		}
		
		boolean confirmed = false;
		try {
			if (!autoFlag){
				BeanShellCommandLineView beanShellView = BeanShellCommandLineView.getInstance();
				String message = "Move " + device.getId() + " to peak at " + statisticResult+ "? (yes/no)";
				boolean isValidReply = false;
				while (!isValidReply){
					String argument = beanShellView.dialog(message).toLowerCase();
//					experiment.printlnToShell("get " + argument + "\n");
					if (argument.equals("yes") || argument.matches("y")) {
						confirmed = true;
						isValidReply = true;
					}
					else if (argument.matches("no") || argument.matches("n")) {
						confirmed = false;
						isValidReply = true;
					}
				}
			}
			if (autoFlag || confirmed){
				List<String> positionDescription = new ArrayList<String>();
				positionDescription.add(String.valueOf(statisticResult));
				return QuokkaInstrument.move(QuokkaExperiment.getQuokkaModel(), 
						device.getId(), QuokkaExperiment.getSics(), positionDescription);
//				experiment.printlnToShell(QuokkaInstrument.move(QuokkaExperiment.getQuokkaModel(), 
//				device.getId(), statisticResult, QuokkaExperiment.getSics()));
//				isPeakFound = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			return "failed to move " + device.getId() + " to position " + statisticResult;
		}
		return "skipped moving " + device.getId() + " to peak at " + statisticResult + "\n";
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#setExperiment(au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment)
	 */
	public void setExperiment(QuokkaExperiment experiment) {
		// TODO Auto-generated method stub
		this.experiment = experiment;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#setParameter(java.lang.String[])
	 */
	public void setParameter(String... params)
	throws InitializeCommandException {
		// TODO Auto-generated method stub
		if (params == null) 
			return;
		if (params.length > 3) 
			throw new InitializeCommandException("can not match parameters (expecting 3 parameters)");
		for (int i = 0; i < params.length; i++) {
			try {
				setDevice(params[i]);
			} catch (Exception e) {
				// TODO: handle exception
				try {
					setAutoFlag(params[i]);
				} catch (Exception e1) {
					// TODO: handle exception
					setStatisticName(params[i]);
				}
			}
		}
//		if (params.length == 3){
//			setDevice(params[0]);
//			setStatisticName(params[1]);
//			setAutoFlag(params[2]);
//		}else if (params.length == 2){
//			for (int i = 0; i < params.length; i++) {
//				try {
//					setDevice(params[i]);
//				} catch (Exception e) {
//					// TODO: handle exception
//					try {
//						setStatisticName(params[i]);
//					} catch (Exception e) {
//						// TODO: handle exception
//						setAutoFlag(params[i]);
//					}
//				}
//			}
//		}else if (params.length == 1){
//			try{
//				setAutoFlag(params[0]);
//			}catch (Exception e) {
//				// TODO: handle exception
//				setStatisticName(params[0]);
//			}
//		}
	}

	private void setDevice(String deviceId) throws InitializeCommandException {
		// TODO Auto-generated method stub
		try {
			device = QuokkaInstrument.getDevice(deviceId);
		} catch (Exception e) {
			// TODO: handle exception
			throw new InitializeCommandException("can not set the device");
		}
	}

	private void setAutoFlag(String flag) throws InitializeCommandException {
		// TODO Auto-generated method stub
		if (flag.toLowerCase().equals("auto")){
			autoFlag = true;
		}else if (flag.toLowerCase().matches("noauto"))
			autoFlag = false;
		else throw new InitializeCommandException("failed to identify the parameter: " + flag);
	}

	private void setStatisticName(String deviceId) throws InitializeCommandException {
		// TODO Auto-generated method stub
		try {
			name = FunctionalStatistic.valueOf(deviceId);
		} catch (Exception e) {
			// TODO: handle exception
			throw new InitializeCommandException("failed to define the functional statistic " +
					"name: " + deviceId);
		}
	}

}
