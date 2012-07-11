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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.dom.sics.SicsControlListener;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.ui.internal.cli.beanshell.ActionListener;
import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView;
import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView.ColorEnum;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.AlgorithmInput;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOMFactory;
import au.gov.ansto.bragg.quokka.exp.core.Command;
import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;
import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment.ScanCriteria;
import au.gov.ansto.bragg.quokka.exp.core.exception.GetDataFailedException;
import au.gov.ansto.bragg.quokka.exp.core.exception.InitializeCommandException;
import au.gov.ansto.bragg.quokka.exp.core.interpreter.QuokkaSicsStatusListener;
import au.gov.ansto.bragg.quokka.exp.core.scanfunction.Function;
import au.gov.ansto.bragg.quokka.exp.ui.ExperimentPlotEditor;
import au.gov.ansto.bragg.quokka.model.core.QuokkaConstants;
import au.gov.ansto.bragg.quokka.model.core.device.ApxSamx;
import au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice;
import au.gov.ansto.bragg.quokka.model.core.instrument.QuokkaInstrument;

public class Scan implements Command{

	//	SicsDOM sics = null;
	//	MonitorDOM monitor = null;
	private String commandString = "scan";
	private QuokkaExperiment experiment = null;
	private double startPoint;
	private double endPoint;
	private double motorStep;
	//	private MotorName motorName;
	private VirtualDevice motorDevice;
	private double scanCriteria = 0;
	private int numberOfEntry;
	private ScanCriteria criteria = null;
	private boolean editorExists = false;
	private boolean autoFlag = false;
	private double[] entryArray;
	private Thread scanThread;
	private ActionListener stopListener = null;
	private ActionListener skipListener = null;
	private static IGroup scanResult;
	private String datatype = "HISTOGRAM_XY";
	private String savetype = "nosave";
	private boolean force = true;
	private IGroup entryGroup;
	private SicsControlListener sicsControlListener;
	private static Algorithm statisticAlgorithm;
	private static CicadaDOM amanager;
	private class Axis{
		String units;
		double origin;
		double interval;

		public Axis(String units, double origin, double interval) {
			super();
			this.units = units;
			this.origin = origin;
			this.interval = interval;
		}

	}

	private Algorithm getStatisticAlgorithm(){
		return statisticAlgorithm;
	}
	private class CommandThread extends Thread{
		Thread dependencyThread; 
		String commandLine;

		public CommandThread(){
			super();
		}

		public CommandThread(Thread dependencyThread, String commandLine){
			this.dependencyThread = dependencyThread;
			this.commandLine = commandLine;
		}

		public void run(){
			//			Thread thread = Thread.currentThread();
			if (dependencyThread != null){
				while (dependencyThread.isAlive()) {
					try {
						//						System.out.println(commandLine + " thread sleep 100");
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (sicsControlListener != null)
					experiment.getSics().removeSicsControlListener(sicsControlListener);
				sicsControlListener = null;
			}else {
				try {
					//					System.out.println(commandLine + " thread sleep 100");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			String result = "start scanning ... \n";
//			if (criteria == ScanCriteria.time)
				result = scanOnCriteria(scanCriteria);
//			if (criteria == ScanCriteria.mon)
//				result = scanOnMonitor(scanCriteria);
//			if (criteria == ScanCriteria.roistats)
//				result = scanOnAnalysis(scanCriteria);
			experiment.printlnToShell(result);
			//			BeanShellCommandLineView beanShellView = BeanShellCommandLineView.getInstance();
			//			try {
			//				experiment.printlnToShell(">>>" + commandLine + "\n", ColorEnum.black);
			////				currentRunningThread = this;
			//				beanShellView.evaluate(commandLine);
			////				experiment.printlnToShell(">>>", ColorEnum.darkRed);
			////				beanShellView.pushOut();
			//			} catch (EvaluationFailedException e) {
			//				// TODO Auto-generated catch block
			////				e.printStackTrace();
			//				experiment.printlnToShell("failed to finish the commands -- user interrupt\n", ColorEnum.red);
			//			}

		}

		public String toString(){
			return super.toString() + " name: " + commandLine;
		}
	}

	public Scan(){
		super();
		if (amanager == null){
			amanager = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
			try {
				amanager.switchAlgorithmSet("quokka.dra");
				statisticAlgorithm = amanager.loadAlgorithm("Statistics" );
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public Scan(QuokkaExperiment experiment){
		this.experiment = experiment;
	}

	public String run(){
		String result = "";
		stopListener = new ActionListener(){
			public void run(){
				try {
					SicsCore.getSicsController().interrupt();
				} catch (SicsIOException e) {
					// TODO Auto-generated catch block
					LoggerFactory.getLogger(QuokkaExperiment.class);
				}
				stop();
			}
		};
		skipListener = new ActionListener(){
			public void run(){
				stop();
			}
		};
		if (BeanShellCommandLineView.getInstance() != null){
			BeanShellCommandLineView.getInstance().addStopActionListener(stopListener);
			BeanShellCommandLineView.getInstance().addSkipActionListener(skipListener);
		}
		scanThread = new CommandThread();
		scanThread.start();
		sicsControlListener = new SicsControlListener(){

			public void controllerInterrupted() {
				// TODO Auto-generated method stub
				stop();
			}

			public void statusChanged(ControllerStatus newStatus) {
				// TODO Auto-generated method stub
				
			}
			
		};
		experiment.getSics().addSicsControlListener(sicsControlListener);
		while (scanThread.isAlive()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (sicsControlListener != null)
			experiment.getSics().removeSicsControlListener(sicsControlListener);
		sicsControlListener = null;
		if (BeanShellCommandLineView.getInstance() != null){
			BeanShellCommandLineView.getInstance().removeStopActionListener(stopListener);
			BeanShellCommandLineView.getInstance().removeSkipActionListener(skipListener);
		}
		//		System.out.println(result);
		return result;
	}

	public void setExperiment(QuokkaExperiment experiment) {
		// TODO Auto-generated method stub
		this.experiment = experiment;
	}

	public void setParameter(String... params) throws InitializeCommandException{
		// TODO Auto-generated method stub
		if (params == null) 
			throw new InitializeCommandException("can not match parameters (expecting 6 or more parameters)");
		if (params.length < 6) 
			throw new InitializeCommandException("can not match parameters (expecting 6 or more parameters)");
		try{
			criteria = ScanCriteria.valueOf(params[4].toLowerCase());
		}catch (Exception e) {
			throw new InitializeCommandException("failed to recognise scan type: " + params[4]);
		}
		try {
			scanCriteria = Double.valueOf(params[5]);
		} catch (Exception e) {
			throw new InitializeCommandException("failed to recognise scan criteria: " + params[5]);
		}
		try {
			motorDevice = QuokkaInstrument.getDevice(params[0]);
		}catch (Exception e) {
			throw new InitializeCommandException(params[0] + " does not exist");
		}
		if (params[1] != "*"){
			try {
				startPoint = Double.valueOf(params[1]);
				endPoint = Double.valueOf(params[2]);
				motorStep = Math.abs(Double.valueOf(params[3]));
				if (startPoint > endPoint)
					motorStep = - motorStep;
				numberOfEntry = (int) ((endPoint - startPoint) / motorStep) + 1;
			} catch (Exception e) {
				throw new InitializeCommandException("failed to define motor driven criteria: "
						+ params[1] + ", " + params[2] + ", " + params[3]);
			}
		}else{
			double currentPosition = 0;
			List<String> positionDescription = motorDevice.getPosition();
			if (positionDescription.size() != 1) {
				throw new InitializeCommandException("device has multiple properties");
			}
			try {
				currentPosition = Double.valueOf(positionDescription.get(0));
			} catch (Exception e) {
				throw new InitializeCommandException("failed to recognise the device: "
						+ params[0]);
			}
			try {
				startPoint = currentPosition - Double.valueOf(params[2]);
				endPoint = currentPosition + Double.valueOf(params[2]);
				motorStep = Double.valueOf(params[3]);
				numberOfEntry = (int) ((endPoint - startPoint) / motorStep) + 1;
			} catch (Exception e) {
				throw new InitializeCommandException("failed to define motor driven criteria: "
						+ params[2] + ", " + params[3]);
			}			
		}
		entryArray = new double[numberOfEntry];
		for (int i = 0; i < entryArray.length; i++) {
			entryArray[i] = startPoint + i * motorStep;
		}
		if (params.length > 6){
			for (int i = 6; i < params.length; i++) {
				String parameter = params[i];
				if (parameter.toUpperCase().contains("HISTOGRAM_"))
					datatype = parameter.toUpperCase();
				else if (parameter.toLowerCase().contains("save"))
					savetype = parameter.toLowerCase();
				else {
					try {
						force = Boolean.valueOf(parameter.toLowerCase());
					} catch (Exception e) {
						throw new InitializeCommandException("can not identify parameter: " + parameter);
					}
				}
			}
		}
		for (int i = 0; i < params.length; i++) {
			commandString += " " + params[i];
		}
		//		if (params.length == 7){
		//			String autoFlagString = params[6];
		//			if (autoFlagString.toLowerCase().matches("auto"))
		//				autoFlag = true;
		//			else throw new InitializeCommandException("failed to recognise auto flag: " + params[6]);
		//		}
	}

	public String scanOnCriteria(double criteria){
		String result = "";
		//		dummyTest1(null);
		//		result = dummyTest2();
		//		result = dummyTest3(time);
		result = dummyTest4(criteria);
		result += "done";
		return result;
	}

	public void stop(){
		if (scanThread.isAlive())
			scanThread.stop();
		if (entryGroup != null){
			try {
				entryGroup.getDataset().close();
			} catch (IOException e) {
			}
		}
		if (sicsControlListener != null)
			experiment.getSics().removeSicsControlListener(sicsControlListener);
		sicsControlListener = null;
	}

	private String dummyTest4(double criteria) {
		String result = "";
		IDataset dataset = null;
		String filename = null;
//		CicadaDOM amanager = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
//		KakaduDOM kakadu = KakaduDOMFactory.getKakaduDOM();
//		kakadu.setWorkbenchPage(BeanShellCommandLineView.getWorkbenchPage());
		//		kakadu.setWorkbenchPage(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
//		try {
//			amanager.switchAlgorithmSet("quokka.dra");
//		} catch (LoadAlgorithmFileFailedException e1) {
//			e1.printStackTrace();
//		}
//		VisDOM kuranda = VisDOMFactory.getVisDOM();
		List<Function> scanFunctionList = QuokkaExperiment.getFunctionList();
		//		Kuranda1DPlotWidget totalSumPlot = null;
		//		Kuranda1DPlotWidget centroidXPlot = null;
		//		Kuranda1DPlotWidget centroidZPlot = null;
		double[] fitData = null;
		try {
			dataset = Factory.createEmptyDatasetInstance();
			//			beamData = createBeamData();
		} catch (Exception e) {
			experiment.printlnToShell(e.getMessage(), ColorEnum.red);
		}
		//		result += "creating dummy data at: " + dataset.getLocation() + "\n";
//		experiment.printlnToShell("creating dummy data at: " + dataset.getLocation() + "\n");
		double[] zDimension = new double[QuokkaConstants.DETECTOR_VERTICAL_PIXELS];
		for (int i = 0; i < zDimension.length; i++) {
			zDimension[i] = 5 * i;
		}
		double[] xDimension = new double[QuokkaConstants.DETECTOR_HORIZONTAL_PIXELS];
		for (int i = 0; i < xDimension.length; i++) {
			xDimension[i] = 5 * i;
		}
		double motorPosition;
		QuokkaSicsStatusListener listener = new QuokkaSicsStatusListener();
		for (int counter = 0; counter < numberOfEntry; counter++) {
			double currentMotorPosition = Double.valueOf(motorDevice.getPosition().get(0));
			motorPosition = startPoint + motorStep * counter;
			double diff = motorPosition - currentMotorPosition;
			try {
				experiment.printlnToShell("moving " + motorDevice.getId() + " to " + motorPosition + " ...\n");
				QuokkaInstrument.move(QuokkaExperiment.getQuokkaModel(), 
					motorDevice.getId(), QuokkaExperiment.getSics(), String.valueOf(motorPosition));
				if (motorDevice instanceof ApxSamx) {
					VirtualDevice samx = QuokkaInstrument.getDevice("samx");
					double currentPosition = Double.valueOf(samx.getPosition().get(0));
					double newPosition = 0;
					if (counter == 0) {
//						newPosition = currentPosition - (endPoint - startPoint) / 2;
						newPosition = currentPosition - diff;
					} else {
						newPosition = currentPosition - motorStep;
					}
					experiment.printlnToShell("moving " + samx.getId() + " to " + newPosition + " ...\n");
					QuokkaInstrument.move(QuokkaExperiment.getQuokkaModel(), 
							samx.getId(), QuokkaExperiment.getSics(), String.valueOf(newPosition));
				}
				experiment.getSics().runRawCommand("force_scan");
				filename = experiment.getSics().Runscan(motorDevice.getId(), motorPosition, motorPosition, 1, 
						this.criteria.name(), String.valueOf(criteria), datatype, savetype, force, listener);
//				filename = "D:/dra/quokkadata/QKK0010265.nx.hdf";
				experiment.printlnToShell("scan data is saved at " + filename + "\n");
				//			try{
				////				filename = QuokkaExperiment.getSics().saveScratch();
				//				experiment.printlnToShell("get files from SICS file server: filename = " + filename + "\n");
				//			}catch (Exception e){
				////				e.printStackTrace();
				//				experiment.printlnToShell("failed to get data file from SICS file server\n", ColorEnum.red);
				//			}
				File file = new File(filename);
				entryGroup = null;
//				Thread.sleep(200);
				if (file.exists()){
					entryGroup = amanager.loadDataFromFile(filename);
					if (entryGroup != null)
						entryGroup = ((NcGroup) entryGroup).getFirstEntryAccess();
					if (entryGroup != null){
						entryGroup.setShortName("entry" + counter);
						dataset.getRootGroup().addSubgroup(entryGroup);
					}
				}else{
					experiment.printlnToShell("failed to access the data file, using generated data instead.\n");
					double[][] detectorData = QuokkaExperiment.getQuokkaModel().generateDetectorData(
							criteria, QuokkaConstants.DEFAULT_WAVELENGTH);
					String entryName = "entry";
					if (counter < 10) entryName += "0";
					entryName += counter;
					entryGroup = createEntryGroup(dataset, entryName, detectorData, zDimension, 
							xDimension);
				}
//				dataset.save();
				if (entryGroup != null)
					experiment.printlnToShell("loading data into analysis module\n");
//				amanager.loadInputData(entryGroup);
				//				System.out.println( amanager.listAvailableAlgorithms() );
//				amanager.loadAlgorithm("Statistics" );
				//				System.out.println( amanager.listTuners() );
//				amanager.setTuner( 3, "statistic_result" );
//				amanager.process();
				AlgorithmInput input = new AlgorithmInput(entryGroup);
				statisticAlgorithm.setCurrentSignal(input);
				statisticAlgorithm.transfer();
				//			try {
				//				Thread.currentThread().sleep(1000);
				//			} catch (Exception e2) {
				//				e2.printStackTrace();
				//			}
				//			System.out.println( amanager.listResults() );
				IGroup algorithmResult = (IGroup) statisticAlgorithm.getSink("beam_center").getSignal();
				experiment.printlnToShell("the processing result is ready\n");
				entryGroup.getDataset().close();
				for (Iterator<?> iterator = scanFunctionList.iterator(); 
				iterator.hasNext();) {
					Function function = (Function) iterator.next();
					if (counter == 0) {
						function.clearDataHistory();
						function.setEntryArray(entryArray);
						function.setDevice(motorDevice);
					}
					try {
						function.addData(algorithmResult);
					} catch (GetDataFailedException e) {
						e.printStackTrace();
						experiment.printlnToShell("failed to retrieve scan " +
								"function data from algorithm result\n", ColorEnum.red);
					}
				}
				if (counter == 0) {
					if (scanFunctionList.size() > 0)
						ExperimentPlotEditor.loadExperimentPlot();
					//					function.plot(kuranda, numberOfEntry);
					editorExists = true;
					//				break;
				}
				boolean isPeakFound = false;
				for (Iterator<?> iterator = scanFunctionList.iterator(); 
				iterator.hasNext();) {
					Function function = (Function) iterator.next();
					function.plotLastMarker();
//					function.setTitle(function.getPlotTitle() + " (" + commandString + ")");
					double peakIndex = function.getPeak();
					if (!Double.valueOf(peakIndex).isNaN())
						experiment.printlnToShell("find " + function.getPlotTitle() + " peak at " + 
								motorDevice.getId() + " = " + peakIndex 
								+ "\n", ColorEnum.darkRed);
					if (counter == numberOfEntry -1){
						//					try {
						//						function.rePlot();
						//						double peakIndex = function.findPeak();
						//						experiment.printlnToShell("find " + function.getPlotTitle() + " peak at step " + peakIndex 
						//								+ "\n");
						//						if (!isPeakFound){
						//							motorPosition = startPoint + motorStep * peakIndex;
						//							boolean confirmed = false;
						//							if (!autoFlag) {
						////								confirmed = kakadu.confirm("Move " + 
						////								motorName + " to peak at " + motorPosition);
						//								BeanShellCommandLineView beanShellView = BeanShellCommandLineView.getInstance();
						//								String message = "Move " + motorName + " to peak at " + motorPosition + "? (yes/no)";
						//								String argument = beanShellView.dialog(message);
						////								experiment.printlnToShell("get " + argument + "\n");
						//								if (argument.matches("yes")) confirmed = true;
						//							}
						//							if (autoFlag || confirmed){
						//								experiment.printlnToShell(QuokkaInstrument.move(QuokkaExperiment.getQuokkaModel(), 
						//										motorName, motorPosition, QuokkaExperiment.getSics()));
						//								isPeakFound = true;
						//							}
						//						}
						//					} catch (Exception e) {
						////						e.printStackTrace();
						//						experiment.printlnToShell(e.getMessage() + "\n", ColorEnum.red);
						//					}
//						function.setTitle(commandString + ": " + function.getPlotTitle());

						function.clearDataHistory();
					}
				}
				isPeakFound = false;
			}catch (Exception e) {
				LoggerFactory.getLogger(Scan.class).error("scan failed", e);
				experiment.printlnToShell(e.getMessage() + "\n", ColorEnum.red);
				break;
			}
			experiment.printlnToShell("\n");
		}
		experiment.printlnToShell("finishing the scan ... ");
		//		kakadu.openKakaduPersective();
//		kakadu.addDataSourceFile(dataset.getLocation());
		//		kakadu.runAlgorithm(amanager.loadAlgorithm("Find Data"));
		//		kakadu.run(dataset.getLocation(), amanager.loadAlgorithm("Find Data"));
		//		UIAlgorithmManager.getAlgorithmManager();
		return result;
	}

	//	private String dummyTest3(double time) {
	//	// TODO Auto-generated method stub
	//	String result = "";
	//	Dataset dataset = null;
	//	String filename = null;
	//	CicadaDOM amanager = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
	//	VisDOM kuranda = VisDOMFactory.getVisDOM();
	//	Kuranda1DPlotWidget totalSumPlot = null;
	//	Kuranda1DPlotWidget centroidXPlot = null;
	//	Kuranda1DPlotWidget centroidZPlot = null;
	//	double[] fitData = null;
	//	double[] totalSumArray = new double[numberOfEntry];
	//	double[] centroidXArray = new double[numberOfEntry];
	//	double[] centroidZArray = new double[numberOfEntry];
	//	try {
	//	dataset = Factory.createTempDataset();
	////	beamData = createBeamData();
	//	} catch (Exception e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	////	result += "creating dummy data at: " + dataset.getLocation() + "\n";
	//	experiment.printlnToShell("creating dummy data at: " + dataset.getLocation() + "\n");
	////	printlnToShell(66451 - 63287 + " " + (66451 - 63287)/63287.0 + "\n");
	//	double[] zDimension = new double[QuokkaConstants.DETECTOR_VERTICAL_PIXELS];
	//	for (int i = 0; i < zDimension.length; i++) {
	//	zDimension[i] = 5 * i;
	//	}
	//	double[] xDimension = new double[QuokkaConstants.DETECTOR_HORIZONTAL_PIXELS];
	//	for (int i = 0; i < xDimension.length; i++) {
	//	xDimension[i] = 5 * i;
	//	}
	//	double motorPosition;
	//	for (int counter = 0; counter < numberOfEntry; counter++) {
	//	motorPosition = startPoint + motorStep * counter;
	//	try {
	//	experiment.printlnToShell(QuokkaInstrument.move(QuokkaExperiment.getQuokkaModel(), 
	//	motorName, motorPosition, QuokkaExperiment.getSics()));
	//	} catch (Exception e1) {
	//	// TODO Auto-generated catch block
	//	e1.printStackTrace();
	////	return e1.getMessage();
	//	}
	//	try{
	//	QuokkaExperiment.getMonitor().monitorCount( (int) time );
	//	experiment.printlnToShell("do scan for " + time + " seconds\n");
	//	}catch (Exception e){
	//	e.printStackTrace();
	//	experiment.printlnToShell("failed to do a detector scan\n");
	//	}
	//	try{
	//	filename = QuokkaExperiment.getSics().saveScratch();
	//	experiment.printlnToShell("get files from SICS file server: filename = " + filename + "\n");
	//	}catch (Exception e){
	//	e.printStackTrace();
	//	experiment.printlnToShell("failed to get data file from SICS file server\n");
	//	}
	////	if (motorName == MotorName.beamstopper_z_mm) quokkaModel.moveBeamStopperZ(motorPosition);
	////	if (motorName == MotorName.beamstopper_x_mm) quokkaModel.moveBeamStopperX(motorPosition);
	////	if (motorName == MotorName.detector_x_mm) quokkaModel.moveDetectorCenterX(motorPosition);
	////	if (motorName == MotorName.detector_y_m) quokkaModel.shrinkL2(motorPosition);
	////	if (motorName == MotorName.sample_aperture_y_m) {
	////	quokkaModel.shrinkL1(motorPosition);
	////	quokkaModel.shrinkL2(- motorPosition);
	////	}
	////	if (motorName == MotorName.XAxis) beamStopper.setCenterX(motorPosition);
	//	double[][] detectorData = QuokkaExperiment.getQuokkaModel().generateDetectorData(
	//	time, QuokkaConstants.DEFAULT_WAVELENGTH);
	//	String entryName = "entry";
	//	if (counter < 10) entryName += "0";
	//	entryName += counter;
	//	Group entryGroup = null;
	//	try {
	//	entryGroup = createEntryGroup(dataset, entryName, detectorData, zDimension, 
	//	xDimension);
	//	} catch (InvalidArrayTypeException e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	try {
	//	dataset.save();
	//	} catch (Exception e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	amanager.loadInputData(entryGroup);
	////	System.out.println( amanager.listAvailableAlgorithms() );
	//	amanager.loadAlgorithm("Statistic-Fact" );
	////	System.out.println( amanager.listTuners() );
	//	amanager.setTuner( 2, "statistic_result" );
	//	amanager.process();
	////	System.out.println( amanager.listResults() );
	//	Group algorithmResult = (Group) amanager.getResult( 2 );
	////	System.out.println( amanager.resultToString( algorithmResult ) );
	//	DataItem totalSumDataItem = algorithmResult.findDataItem( "total_sum" );
	//	DataItem centroidDataItem = algorithmResult.findDataItem( "centroid" );
	//	Array totalSum = null;
	//	Array centroid = null;
	//	try {
	//	totalSum = totalSumDataItem.getData();
	//	centroid = centroidDataItem.getData();
	//	} catch (IOException e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	System.out.println( "TotalSum data is " + totalSum );
	////	System.out.println( "TotalSum data is ");
	////	try {
	////	for (ArrayIterator iterator = entryGroup.getSignalArray().getIterator(); iterator.hasNext();) {
	////	Double item = (Double) iterator.next();
	////	System.out.println(item.toString());
	////	}
	////	} catch (SignalNotAvailableException e) {
	////	// TODO Auto-generated catch block
	////	e.printStackTrace();
	////	}
	//	System.out.println("centroid data is " + centroid);
	//	double[] centroidData = (double[]) centroid.copyTo1DJavaArray();
	//	centroidXArray[counter] = centroidData[1];
	//	centroidZArray[counter] = centroidData[0];
	//	//centroid = result.findDataItem( "centroid" ).getData();
	//	double[] totalSumData = (double[]) totalSum.copyTo1DJavaArray();
	//	totalSumArray[counter] = totalSum.getMaximum() / 10;
	//	if (counter == 0){
	//	centroidXPlot = kuranda.plot1dDouble( new double[] {
	//	centroidXArray[counter]} , 1.2* centroidXArray[counter], 
	//	0.7 * centroidXArray[counter], numberOfEntry, 1.2 * centroidXArray[counter], 
	//	"CentroidX" );
	//	centroidZPlot = kuranda.plot1dDouble( new double[] {
	//	centroidZArray[counter]} , 1.2* centroidZArray[counter], 
	//	0.7 * centroidZArray[counter], numberOfEntry, 1.2 * centroidZArray[counter], 
	//	"CentroidZ" );
	//	//view1D = kuranda.plot1dDouble( new double[] { 90 }, 100, 80, numberOfEntries, 100, "total_sum" );
	//	totalSumPlot = kuranda.plot1dDouble( new double[] {
	//	totalSumArray[counter]} , 1.2 * totalSumArray[counter], 0.7 * totalSumArray[counter], 
	//	numberOfEntry, 1.2 * totalSumArray[counter], "Total Sum" );
	//	}
	//	try {
	//	centroidXPlot.addMarker( (double) counter, centroidXArray[counter] );
	//	centroidZPlot.addMarker( (double) counter, centroidZArray[counter] );
	//	totalSumPlot.addMarker( (double) counter, totalSumArray[counter] );
	//	} catch (KurandaException e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	kuranda.rPlot();
	//	amanager.wait( 1200 );
	//	}
	//	try {
	//	centroidXPlot.setDataSource(centroidXArray, "CentroidX");
	////	centroidXPlot.setMax(totalSumArray[0] * 1.05);
	////	centroidXPlot.setMin(totalSumArray[0] * 0.9);
	//	centroidZPlot.setDataSource(centroidZArray, "CentroidZ");
	////	centroidZPlot.setMax(totalSumArray[0] * 1.2);
	////	centroidZPlot.setMin(totalSumArray[0] * 0.7);
	//	totalSumPlot.setDataSource(totalSumArray, "Total Sum");
	////	totalSumPlot.setMax(totalSumArray[0] * 1.2);
	////	totalSumPlot.setMin(totalSumArray[0] * 0.7);
	//	kuranda.rPlot();
	//	} catch (KurandaException e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}		
	//	result += "finishing the scan ... ";
	//	return result;
	//	}

	//	private String move(MotorName motorName, double motorPosition) throws Exception {
	//	// TODO Auto-generated method stub
	//	switch(motorName){
	//	case beamstopper_z_mm : quokkaModel.moveBeamStopperZ(motorPosition); break;
	//	case beamstopper_x_mm : quokkaModel.moveBeamStopperX(motorPosition); break;
	//	case detector_x_mm : quokkaModel.moveDetectorCenterX(motorPosition); break;
	//	case detector_y_m : quokkaModel.shrinkL2(motorPosition); break;
	//	case sampleaperture_y_m : quokkaModel.shrinkL1(motorPosition);
	//	quokkaModel.shrinkL2(- motorPosition); break;
	//	case entranceaperture_radius_mm : quokkaModel.setR1(motorPosition); break;
	//	case sampleaperture_radius_mm : quokkaModel.setR2(motorPosition); break;
	//	case beamstopper_radius_mm : quokkaModel.setBeamStopperRadius(motorPosition); break;
	//	default : throw new Exception("can not drive the instrument " + motorName);
	//	}
	//	return "moving " + motorName + " to " + motorPosition + "\n";
	//	}

	//	private String dummyTest2() {
	//	// TODO Auto-generated method stub
	//	String result = "";
	//	Dataset dataset = null;
	//	CicadaDOM amanager = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
	//	VisDOM kuranda = VisDOMFactory.getVisDOM();
	//	Kuranda1DPlotWidget totalSumPlot = null;
	//	Kuranda1DPlotWidget centroidXPlot = null;
	//	Kuranda1DPlotWidget centroidZPlot = null;
	//	double[] fitData = null;
	//	double[] totalSumArray = new double[numberOfEntry];
	//	double[] centroidXArray = new double[numberOfEntry];
	//	double[] centroidZArray = new double[numberOfEntry];
	//	double[][] beamData = null;
	//	try {
	//	dataset = Factory.createTempDataset();
	//	beamData = createBeamData();
	//	} catch (Exception e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	result += "creating dummy data at: " + dataset.getLocation() + "\n";
	//	double[] zDimension = new double[beamData.length];
	//	for (int i = 0; i < zDimension.length; i++) {
	//	zDimension[i] = 5 * i;
	//	}
	//	double[] xDimension = new double[beamData[0].length];
	//	for (int i = 0; i < xDimension.length; i++) {
	//	xDimension[i] = 5 * i;
	//	}
	//	double motorPosition = startPoint;
	//	for (int counter = 0; counter < numberOfEntry; counter++) {
	//	motorPosition += motorStep;
	//	if (motorName == MotorName.bsz) beamStopper.setCenterZ(motorPosition);
	//	if (motorName == MotorName.bsx) beamStopper.setCenterX(motorPosition);
	//	double[][] beamStoppedData = createStoppedData(beamData);
	//	String entryName = "entry";
	//	if (counter < 10) entryName += "0";
	//	entryName += counter;
	//	Group entryGroup = null;
	//	try {
	//	entryGroup = createEntryGroup(dataset, entryName, beamStoppedData, zDimension, xDimension);
	//	} catch (InvalidArrayTypeException e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	try {
	//	dataset.save();
	//	} catch (Exception e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	amanager.loadInputData(entryGroup);
	////	System.out.println( amanager.listAvailableAlgorithms() );
	//	amanager.loadAlgorithm("Statistic-Fact" );
	////	System.out.println( amanager.listTuners() );
	//	amanager.setTuner( 2, "statistic_result" );
	//	amanager.process();
	////	System.out.println( amanager.listResults() );
	//	Group algorithmResult = (Group) amanager.getResult( 2 );
	////	System.out.println( amanager.resultToString( algorithmResult ) );
	//	DataItem totalSumDataItem = algorithmResult.findDataItem( "total_sum" );
	//	DataItem centroidDataItem = algorithmResult.findDataItem( "centroid" );
	//	Array totalSum = null;
	//	Array centroid = null;
	//	try {
	//	totalSum = totalSumDataItem.getData();
	//	centroid = centroidDataItem.getData();
	//	} catch (IOException e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	System.out.println( "TotalSum data is " + totalSum );
	//	System.out.println("centroid data is " + centroid);
	//	double[] centroidData = (double[]) centroid.copyTo1DJavaArray();
	//	centroidXArray[counter] = centroidData[1];
	//	centroidZArray[counter] = centroidData[0];
	//	//centroid = result.findDataItem( "centroid" ).getData();
	//	double[] totalSumData = (double[]) totalSum.copyTo1DJavaArray();
	//	totalSumArray[counter] = totalSum.getMaximum() / 10;
	//	if (counter == 0){
	//	centroidXPlot = kuranda.plot1dDouble( new double[] {
	//	centroidXArray[counter]} , 1.2* centroidXArray[counter], 
	//	0.7 * centroidXArray[counter], numberOfEntry, 1.2 * centroidXArray[counter], 
	//	"CentroidX" );
	//	centroidZPlot = kuranda.plot1dDouble( new double[] {
	//	centroidZArray[counter]} , 1.2* centroidZArray[counter], 
	//	0.7 * centroidZArray[counter], numberOfEntry, 1.2 * centroidZArray[counter], 
	//	"CentroidZ" );
	//	//view1D = kuranda.plot1dDouble( new double[] { 90 }, 100, 80, numberOfEntries, 100, "total_sum" );
	//	totalSumPlot = kuranda.plot1dDouble( new double[] {
	//	totalSumArray[counter]} , 1.2 * totalSumArray[counter], 0.7 * totalSumArray[counter], 
	//	numberOfEntry, 1.2 * totalSumArray[counter], "Total Sum" );
	//	}
	//	try {
	//	centroidXPlot.addMarker( (double) counter, centroidXArray[counter] );
	//	centroidZPlot.addMarker( (double) counter, centroidZArray[counter] );
	//	totalSumPlot.addMarker( (double) counter, totalSumArray[counter] );
	//	} catch (KurandaException e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	kuranda.rPlot();
	//	amanager.wait( 1200 );
	//	}
	//	try {
	//	centroidXPlot.setDataSource(centroidXArray, "CentroidX");
	////	centroidXPlot.setMax(totalSumArray[0] * 1.05);
	////	centroidXPlot.setMin(totalSumArray[0] * 0.9);
	//	centroidZPlot.setDataSource(centroidZArray, "CentroidZ");
	////	centroidZPlot.setMax(totalSumArray[0] * 1.2);
	////	centroidZPlot.setMin(totalSumArray[0] * 0.7);
	//	totalSumPlot.setDataSource(totalSumArray, "Total Sum");
	////	totalSumPlot.setMax(totalSumArray[0] * 1.2);
	////	totalSumPlot.setMin(totalSumArray[0] * 0.7);
	//	kuranda.rPlot();
	//	} catch (KurandaException e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	return result;
	//	}

	private IGroup createEntryGroup(IDataset dataset, String name, double[][] beamStoppedData,
			double[] dimensionZ, double[] dimensionX) throws InvalidArrayTypeException {
		// TODO Auto-generated method stub
		IGroup rootGroup = dataset.getRootGroup();
		IGroup entryGroup = Factory.createGroup(dataset, rootGroup, name, true);
		entryGroup.addOneAttribute(Factory.createAttribute("NX_class", "NXentry"));
		IGroup dataGroup = Factory.createGroup(dataset, entryGroup, "data", true);
		dataGroup.addOneAttribute(Factory.createAttribute("NX_class", "NXdata"));
		IArray signalArray = Factory.createArray(beamStoppedData);
		IArray axisZArray = Factory.createArray(dimensionZ);
		IArray axisXArray = Factory.createArray(dimensionX);

		IDataItem signalDataItem = Factory.createDataItem(dataset, dataGroup, "data", signalArray);
		IDataItem axisZDataItem = 
			Factory.createDataItem(dataset, dataGroup, "vertical_pixel", axisZArray);
		axisZDataItem.addOneAttribute(Factory.createAttribute("units", "mm"));
		IDataItem axisXDataItem = 
			Factory.createDataItem(dataset, dataGroup, "horizontal_pixel", axisXArray);
		axisXDataItem.addOneAttribute(Factory.createAttribute("units", "mm"));
		((NcGroup) dataGroup).buildResultGroup(signalDataItem, axisZDataItem, axisXDataItem);
//		rootGroup.addSubgroup(entryGroup);
		return entryGroup;
	}


	//	private double[][] createStoppedData(double[][] beamData) {
	//	// TODO Auto-generated method stub

	//	double[][] stoppedData = new double[beamData.length][beamData[0].length];
	//	for (int i = 0; i < beamData.length; i++) {
	//	for (int j = 0; j < beamData[0].length; j++) {
	//	double distance = (i - beamStopper.getCenterZ()) * (i - beamStopper.getCenterZ()) 
	//	+ (j - beamStopper.getCenterX()) * (j - beamStopper.getCenterX());
	//	if (distance <= beamStopper.getRadius() * beamStopper.getRadius()) stoppedData[i][j] = 0;
	//	else stoppedData[i][j] = beamData[i][j];
	//	}
	//	}
	//	return stoppedData;
	//	}

	//	private void initialliseMotorPosition(int[] size, int radius) {
	//	// TODO Auto-generated method stub
	//	if (beamStopper == null) beamStopper = new BeamStopper(size[0] / 2, size[1] / 2, radius);
	//	}

	//	private double[][] createBeamData() {
	//	// TODO Auto-generated method stub
	//	double mean = 96;
	//	double variance = 0.04;
	//	int[] size = {192, 192};
	//	double[][] beamData = null;
	//	int beamStopperRadius = 18;
	////	private int[] block_centre = {106, 80};
	////	private int radius = 18;
	////	private int numOfEntries = 30;
	////	private int xShift = -1;
	////	private int zShift = 0;
	//	initialliseMotorPosition(size, beamStopperRadius);
	//	try {
	//	beamData = (double[][])	Formulae.generateGaussian(size, mean, variance);
	//	} catch (Exception e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	} ;

	//	return beamData;
	//	}

	//	private void dummyTest1(String filename) {
	//	// TODO Auto-generated method stub
	//	CicadaDOM amanager = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
	//	VisDOM kuranda = VisDOMFactory.getVisDOM();
	//	Kuranda1DPlotWidget totalSumPlot = null;
	//	Kuranda1DPlotWidget centroidXPlot = null;
	//	Kuranda1DPlotWidget centroidZPlot = null;
	//	String filePath = null;
	//	if (filename == null){
	//	filename = "data/quokkaSample.8.hdf";
	//	IFileStore fileStore = null;
	//	try{
	//	fileStore = GTPlatform.find(Activator.getDefault().PLUGIN_ID, filename);
	//	filePath = fileStore.toURI().getPath();
	//	}catch (Exception e1){
	////	filePath = "E:/workspace/data/quokkaSample.8.hdf";
	//	}
	//	}
	//	Group source = amanager.loadDataFromFile( filePath );
	//	int numberOfEntries = 30;
	//	int fitDefination = 10;
	//	double[] fitData = null;
	//	double[] totalSumArray = new double[numberOfEntries];
	//	double[] centroidXArray = new double[numberOfEntries];
	//	double[] centroidZArray = new double[numberOfEntries];
	//	for (int counter = 0; counter < numberOfEntries; counter++){
	//	//source = amanager.loadDataFromFile( "E:/workspace/data/echidna_2007-07-10T22-45-19_00647.nx.hdf" );
	////	Group source = amanager.loadDataFromFile( "E:/workspace/data/quokkaSample.8.hdf" );

	////	pluginXMLPath = fileStore.toLocalFile(EFS.NONE, new NullProgressMonitor()).getAbsolutePath() + "/";
	////	Group source = amanager.loadDataFromFile( "quokkaSample.8.hdf" );
	////	System.out.println( amanager.listEntry(source) );
	//	Group entry = amanager.loadEntry( source, counter );
	////	System.out.println( amanager.listAvailableAlgorithms() );
	//	amanager.loadAlgorithm("Statistic-Fact" );
	////	System.out.println( amanager.listTuners() );
	//	amanager.setTuner( 2, "statistic_result" );
	//	amanager.process();
	////	System.out.println( amanager.listResults() );
	//	Group algorithmResult = (Group) amanager.getResult( 2 );
	////	System.out.println( amanager.resultToString( algorithmResult ) );
	//	DataItem totalSumDataItem = algorithmResult.findDataItem( "total_sum" );
	//	DataItem centroidDataItem = algorithmResult.findDataItem( "centroid" );
	//	Array totalSum = null;
	//	Array centroid = null;
	//	try {
	//	totalSum = totalSumDataItem.getData();
	//	centroid = centroidDataItem.getData();
	//	} catch (IOException e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	System.out.println( "TotalSum data is " + totalSum );
	//	System.out.println("centroid data is " + centroid);
	//	double[] centroidData = (double[]) centroid.copyTo1DJavaArray();
	//	centroidXArray[counter] = centroidData[1];
	//	centroidZArray[counter] = centroidData[0];
	//	//centroid = result.findDataItem( "centroid" ).getData();
	//	double[] totalSumData = (double[]) totalSum.copyTo1DJavaArray();
	//	totalSumArray[counter] = totalSum.getMaximum() / 10;
	//	//totalSumFit = totalSumDataItem.findAttribute( "fitting" ).getValue().copyTo1DJavaArray();
	//	//totalSumFitPeak = totalSumDataItem.findAttribute( "fitting_peak" ).getValue().copyTo1DJavaArray();
	//	//centroidData = centroid.copyTo1DJavaArray();
	//	//out.println( "centroid is " + centroidData[1] + " " + centroidData[0]);
	//	//view1D = kuranda.plot1dDouble( totalSumData, totalSum.getMaximum(), totalSum.getMinimum(), totalSumData.length, 3 * totalSum.getMaximum(), "total_sum" );
	////	if (counter > 3){
	////	double[] fittingInput = new double[counter + 1];
	////	for (int i = 0; i <= counter; i ++){
	////	fittingInput[i] = totalSumArray[i];
	////	}
	////	Group totalSumGroup = amanager.createGroup( fittingInput, "data" );
	////	amanager.loadInputData( totalSumGroup );
	////	amanager.loadAlgorithm( "JAS3-Fitting" );
	////	amanager.process();
	////	Group fitResult = (Group) amanager.getResult( 2 );
	////	System.out.println( amanager.resultToString( fitResult ) );
	////	DataItem fitDataItem = fitResult.findSignal();
	////	try {
	////	fitData = (double[]) fitDataItem.getData().copyTo1DJavaArray();
	////	} catch (IOException e) {
	////	// TODO Auto-generated catch block
	////	e.printStackTrace();
	////	}
	////	}
	//	if (counter == 0){
	//	centroidXPlot = kuranda.plot1dDouble( new double[] {
	//	centroidXArray[counter]} , 1.2* centroidXArray[counter], 
	//	0.7 * centroidXArray[counter], numberOfEntries, 1.2 * centroidXArray[counter], 
	//	"CentroidX" );
	//	centroidZPlot = kuranda.plot1dDouble( new double[] {
	//	centroidZArray[counter]} , 1.2* centroidZArray[counter], 
	//	0.7 * centroidZArray[counter], numberOfEntries, 1.2 * centroidZArray[counter], 
	//	"CentroidZ" );
	//	//view1D = kuranda.plot1dDouble( new double[] { 90 }, 100, 80, numberOfEntries, 100, "total_sum" );
	//	totalSumPlot = kuranda.plot1dDouble( new double[] {
	//	totalSumArray[counter]} , 1.2 * totalSumArray[counter], 0.7 * totalSumArray[counter], 
	//	numberOfEntries, 1.2 * totalSumArray[counter], "Total Sum" );
	//	}
	//	try {
	//	centroidXPlot.addMarker( (double) counter, centroidXArray[counter] );
	//	centroidZPlot.addMarker( (double) counter, centroidZArray[counter] );
	//	totalSumPlot.addMarker( (double) counter, totalSumArray[counter] );
	//	} catch (KurandaException e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	//kuranda.refeshPlot();
	////	if (counter > 3){
	//////view1D = kuranda.getKuranda1DWidget();
	////	try {
	////	totalSumPlot.setDataSource(fitData, "fit result");
	////	} catch (KurandaException e) {
	////	// TODO Auto-generated catch block
	////	e.printStackTrace();
	////	}
	////	}
	//	kuranda.rPlot();
	//	amanager.wait( 1200 );
	//	}
	//	try {
	//	centroidXPlot.setDataSource(centroidXArray, "CentroidX");
	////	centroidXPlot.setMax(totalSumArray[0] * 1.05);
	////	centroidXPlot.setMin(totalSumArray[0] * 0.9);
	//	centroidZPlot.setDataSource(centroidZArray, "CentroidZ");
	////	centroidZPlot.setMax(totalSumArray[0] * 1.2);
	////	centroidZPlot.setMin(totalSumArray[0] * 0.7);
	//	totalSumPlot.setDataSource(totalSumArray, "Total Sum");
	////	totalSumPlot.setMax(totalSumArray[0] * 1.2);
	////	totalSumPlot.setMin(totalSumArray[0] * 0.7);
	//	kuranda.rPlot();
	//	} catch (KurandaException e) {
	//	// TODO Auto-generated catch block
	//	e.printStackTrace();
	//	}
	//	}

	public String scanOnMonitor(double criteriaValue){
		String result = null;
		result = "done";
		return result;
	}

	public String scanOnAnalysis(double criteriaValue){
		String result = null;
		result = "done";
		return result;
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		String help = "SCAN: Do experiment or commisioning task by driving SICS "
			+ "component, manage detector histogram and plot analysis result.\n\n";
		help += "Usage: scan <SICSComponentName> <StartPosition> <EndPosition> "
			+ "<Step> <ScanMode> <ScanModeCriteria> [<NoSave/Save>]\n\n";
		help += "This commond will do the following routine to perform the experment.\n" 
			+ "a. Drive the specific component to the start position.\n"
			+ "b. Do a detector scan in the mode specified by the mode type " +
			"and mode criteria.\n"
			+ "c. Drive the motor with a specific step to the next position.\n"
			+ "d. Loop to b. until reach the end position.\n\n"
			+ "All arguments in the above usage description are not optional.\n\n";
		help += "<SICSComponentName> \t exact name of the SICS component in the " +
		"instrument control tree.\n\n";
		help += "<StartPosition> \t the start position of the SICS component.\n\n";
		help += "<EndPosition> \t the end position of the SICS component.\n\n";
		help += "<step> \t the step of the SICS component moves.\n\n";
		help += "<ScanMode> \t the scan mode of the detector, e.g., time, count, " +
		"or other statistic way.\n\n";
		help += "<ScanModeCriteria> \t the argument for the scan mode. For example " +
		"1000 (sec) or 1E4 (counts).\n\n";
		help += "[<NoSave/Save>] \t the argument for save type. If 'nosave', the histogram" +
				"data will be saved in scratch files. If 'save', the histogram data" +
				"will be saved in normal files. This argument is optional.\n\n";
		//		help += "<AutoFlag>='auto' \t the argument for automatically move the SICS component " +
		//				"to the peak location of the key scan function. To use this option, put " +
		//				"string 'auto' at the argument location. This argument is " +
		//				"optional. If not provided, the command will prompt a confirm box " +
		//				"after finishing the scan.\n\n";
		return help;
	}

	public String getShortDescription() {
		// TODO Auto-generated method stub
		String description = "SCAN: Do experiment or commisioning task by driving SICS "
			+ "component, manage detector histogram and plot analysis result.\n";
		description += "Usage: scan <SICSComponentName> <StartPosition> <StopPosition> "
			+ "<Step> <ScanMode> <ScanCriteria> [<NoSave/Save>]\n";
		description += "For more information, please use 'help scan'.\n";
		return description;
	}

}
