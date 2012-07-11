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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.PartInitException;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.dom.monitor.MonitorDOM;
import org.gumtree.gumnix.sics.dom.sics.SicsDOM;
import org.gumtree.gumnix.sics.dom.sics.SicsDOMException;
import org.gumtree.gumnix.sics.dom.sics.SicsDOMFactory;
import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView;
import org.gumtree.ui.internal.cli.beanshell.InstrumentCommandSpace;
import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView.ColorEnum;

import au.gov.ansto.bragg.quokka.exp.core.exception.InitializeCommandException;
import au.gov.ansto.bragg.quokka.exp.core.scanfunction.Function;
import au.gov.ansto.bragg.quokka.model.core.QuokkaModel;
import au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice;
import au.gov.ansto.bragg.quokka.model.core.instrument.QuokkaInstrument;

public class QuokkaExperiment extends InstrumentCommandSpace{
	static SicsDOM sics;
	static MonitorDOM monitor;
	private static List<Function> functionList;
	static QuokkaModel quokkaModel;
	static BeanShellCommandLineView beanShellView;
	private static QuokkaExperiment experiment;
	static List<ScanResult> scanResultList;
	private static String currentPath;

	public final static String QUOKKA_COMMANDS_PACKAGE_NAME = "au.gov.ansto.bragg.quokka.exp.core.command";
	public final static String QUOKKA_FUNCTIONS_PACKAGE_NAME = "au.gov.ansto.bragg.quokka.exp.core.scanfunction";
	public enum ScanFunction{CentroidX, CentroidZ, CentroidQ, Sum, Rms2Theta};
	public enum ScanCriteria{time, mon, roistats, count};

//	public class BeamStopper{
//	double centerZ;
//	double centerX;
//	double radius;

//	public BeamStopper(double centerZ, double centerX, double radius){
//	this.centerX = centerX;
//	this.centerZ = centerZ;
//	this.radius = radius;
//	}

//	public double getRadius() {
//	return radius;
//	}

//	public double getCenterZ() {
//	return centerZ;
//	}

//	public void setCenterZ(double centerZ) {
//	this.centerZ = centerZ;
//	}

//	public double getCenterX() {
//	return centerX;
//	}

//	public void setCenterX(double centerX) {
//	this.centerX = centerX;
//	}
//	}

	public static QuokkaExperiment getInstance(){
		if (experiment == null){
			experiment = new QuokkaExperiment();
		}
		return experiment;
	}

	protected QuokkaExperiment(){
		super();
//		if (sics == null) sics = new SicsDriver();
//		if (monitor == null) monitor = new Monitor();
		if (quokkaModel == null) quokkaModel = QuokkaModel.getInstance();
		try {
			if (sics == null) sics = (SicsDOM) SicsDOMFactory.getSicsDOM();
			QuokkaInstrument.setSics(sics);
			String syncResult = quokkaModel.syncWithSics();
//			printlnToShell(syncResult);
		} catch (Exception e) {
			e.printStackTrace();
			printlnToShell("SICS is not connected\n");
		}
		try {
			if (sics == null) sics = (SicsDOM) SicsDOMFactory.getSicsDOM();
		} catch (Exception e) {
//			e.printStackTrace();
			printlnToShell("Monitor is not connected\n");
		}
		checkScanFunctionList();
	}

	public QuokkaExperiment(String ...scanFunctions){
		this();
		functionList.clear();
		for (int i = 0; i < scanFunctions.length; i++) {
			Function function = null;
			try {
				function = (Function) Class.forName(Function.class.getPackage().getName() 
						+ "." + scanFunctions[i]).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			functionList.add(function);
		}
	}

	private void checkScanFunctionList() {
		if (functionList == null)
			functionList = new ArrayList<Function>();
		if (functionList.size() == 0){
			addFunction("ROISum");
			addFunction("ROICentroidX");
//			addFunction("ROICentroidZ");
		}
	}

	@Override
	public String runCommand(String commandName, String ...params){
		getInstance().printlnToShell("\n");
		String result = "";
		Command command = null;
		try {
			command = (Command) Class.forName(QUOKKA_COMMANDS_PACKAGE_NAME + "."
					+ Character.toUpperCase(commandName.charAt(0)) 
					+ commandName.substring(1).toLowerCase()).newInstance();
			command.setExperiment(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Can not find the command: " + commandName + "\nUse help to find available commands.";
		}
		try {
			command.setParameter(params);
		} catch (InitializeCommandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
		result += command.run();
		return result;
	}

//	public String scan(String ...params){
//	String result = "";
//	if (params.length != 6) return "can not match parameters (expecting 6 parameters)";
//	ScanCriteria criteria = null;
//	try{
//	criteria = ScanCriteria.valueOf(params[4]);
//	}catch (Exception e) {
//	// TODO: handle exception
//	return "failed to recognise scan type: " + params[4];
//	}
//	try {
//	scanCriteria = Double.valueOf(params[5]);
//	} catch (Exception e) {
//	// TODO: handle exception
//	return "failed to recognise scan criteria: " + params[5];
//	}
//	if (params[1] != "*"){
//	try {
//	motorName = MotorName.valueOf(params[0]);
//	} catch (Exception e) {
//	// TODO: handle exception
//	return "failed to define the motor: " + params[0];
//	}
//	try {
//	startPoint = Double.valueOf(params[1]);
//	endPoint = Double.valueOf(params[2]);
//	motorStep = Double.valueOf(params[3]);
//	numberOfEntry = (int) ((endPoint - startPoint) / motorStep) + 1;
//	} catch (Exception e) {
//	// TODO: handle exception
//	return "failed to define motor driven criteria: "
//	+ params[1] + ", " + params[2] + ", " + params[3];
//	}
//	}
//	if (criteria == ScanCriteria.time)
//	result = scanOnTime(scanCriteria);
//	if (criteria == ScanCriteria.mon)
//	result = scanOnMonitor(scanCriteria);
//	if (criteria == ScanCriteria.roistats)
//	result = scanOnAnalysis(scanCriteria);	
////	System.out.println(result);
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




	public String toString(){
		String result = "";
		for (Iterator<?> iterator = functionList.iterator(); iterator.hasNext();) {
			ScanFunction function = (ScanFunction) iterator.next();
			result += function.name() + "\n";
		}
		return result;
	}

	public void printlnToShell(String text){
//		if (beanShellView == null) beanShellView = BeanShellCommandLineView.getInstance();
		getBeanShell().appendText(text);
	}

	public void printlnToShell(String text, ColorEnum color){
		getBeanShell().appendText(text, color);
	}

	public static QuokkaModel getQuokkaModel() {
		return quokkaModel;
	}

	public static SicsDOM getSics() {
		return sics;
	}

	public static MonitorDOM getMonitor() {
		return monitor;
	}

	public static List<Function> getFunctionList() {
		return functionList;
	}

	public static void setFunctionList(List<String> functionNameList) {
		functionList = new ArrayList<Function>();
		for (Iterator<?> iterator = functionNameList.iterator(); iterator.hasNext();) {
			addFunction((String) iterator.next());
		}
	}

	public static void addFunction(String functionName){
		Function function = null;
		try {
			Class<?> functionClass = Class.forName(Function.class.getPackage().getName() 
					+ "." + functionName);
			Method newInstanceMethod = functionClass.getMethod("getInstance", new Class[0]);
			function = (Function) newInstanceMethod.invoke(functionClass, new Object[]{});
			function.clearPeak();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!functionList.contains(function))
			functionList.add(function);
	}

	public static void removeFunction(String functionName){
		Function function = null;
		try {
			Class<?> functionClass = Class.forName(Function.class.getPackage().getName() 
					+ "." + functionName);
			Method newInstanceMethod = functionClass.getMethod("getInstance", new Class[]{});
			function = (Function) newInstanceMethod.invoke(functionClass, new Object[]{});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		functionList.remove(function);
	}

	public void syncSicsModel(){

	}

	public static void updateResult(ScanResult result){
		ScanResult oldResult = findResultForSameDevice(result.getDevice());
		if (oldResult != null) 
			scanResultList.remove(oldResult);
		insertScanResult(result);
	}

	private static void insertScanResult(ScanResult result) {
		// TODO Auto-generated method stub
		if (scanResultList == null) 
			scanResultList = new ArrayList<ScanResult>();
		scanResultList.add(result);
	}

	public static ScanResult findResultForSameDevice(VirtualDevice device) {
		// TODO Auto-generated method stub
		if (scanResultList == null) return null;
		for (Iterator iterator = scanResultList.iterator(); iterator.hasNext();) {
			ScanResult oldResult = (ScanResult) iterator.next();
			if (oldResult.getDevice() == device)
				return oldResult;
		}
		return null;
	}

	public static List<String> getHistory(){
		return getBeanShell().getHistoryCommandList();
	}
//	public static String use(String instrumentName){
//	InstrumentCommand iCommand = new InstrumentCommand();
//	return iCommand.use(instrumentName); 
//	}
	public static BeanShellCommandLineView getBeanShell(){
		if (beanShellView == null) beanShellView = BeanShellCommandLineView.getInstance();
		return beanShellView;
	}

	public static void showCommandLineView() throws InitializeCommandException {
//		IWorkbench workbench;
//		IWorkbenchWindow workbenchWindow;
//		workbench = PlatformUI.getWorkbench();
////		final IWorkbenchPage activePage = workbenchWindow.getActivePage();
//		workbenchWindow = workbench.getActiveWorkbenchWindow();
//		try {
//			workbenchWindow.getActivePage().showView("org.gumtree.ui.cli.beanShellTerminalview");
//		} catch (PartInitException e) {
//			// TODO Auto-generated catch block
//			throw new InitializeCommandException(e);
//		};
		try {
			BeanShellCommandLineView.showCommandLineView();
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			throw new InitializeCommandException(e);
		}
	}

	/**
	 * @return the currentPath
	 */
	public static String getCurrentPath() {
		return currentPath;
	}

	public static void setCurrentPath(String path) {
		// TODO Auto-generated method stub
		currentPath = path;
	}

	
	public static String sicsRun(String sicsDevicePath) {
		// TODO Auto-generated method stub
		String result = "";
		try {
			sics.runCommand(sicsDevicePath);
		} catch (Exception e) {
			// TODO: handle exception
			String errorMessage = e.getMessage();
			if (errorMessage != null)
				return result + "failed to run the sics command, " + e.getMessage() + "\n";
			else return result + "failed to send command to SICS server\n";
		}
		return "running SICSCommand:" + sicsDevicePath + "\n";
	}
	
	public static String sicsSet(String sicsDevicePath, String position){
		String result = "";
		try {
			sics.setValue(sicsDevicePath, position);
		} catch (Exception e) {
			// TODO: handle exception
			String errorMessage = e.getMessage();
			if (errorMessage != null)
				return result + "failed to send command to SICS server, " + e.getMessage() + "\n";
			else return result + "failed to send command to SICS server\n";
		}
		return "moving SICSDevice:" + sicsDevicePath + " to " + position + "\n";
	}
	
	public IDynamicController getDynamicController(String sicsDevicePath) throws SicsDOMException {
		// TODO Auto-generated method stub
		return sics.getDynamicController(sicsDevicePath);
		
	}

}
