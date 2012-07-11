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
package au.gov.ansto.bragg.echidna.exp.core;

import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.dom.monitor.MonitorDOM;
import org.gumtree.gumnix.sics.dom.monitor.MonitorDOMFactory;
import org.gumtree.gumnix.sics.dom.sics.SicsDOM;
import org.gumtree.gumnix.sics.dom.sics.SicsDOMException;
import org.gumtree.gumnix.sics.dom.sics.SicsDOMFactory;
import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView;
import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView.ColorEnum;
import org.gumtree.ui.internal.cli.beanshell.InstrumentCommandSpace;

import au.gov.ansto.bragg.echidna.exp.exception.EchidnaExperimentException;
import au.gov.ansto.bragg.kakadu.dom.KakaduDOM;
import au.gov.ansto.bragg.kakadu.dom.KakaduDOMFactory;

/**
 * @author nxi
 * Created on 30/06/2008
 */
public class EchidnaExperiment extends InstrumentCommandSpace {

	static SicsDOM sics;
	static MonitorDOM monitor;
	static BeanShellCommandLineView beanShellView;
	static KakaduDOM kakadu;
	private static EchidnaExperiment experiment;
	protected String scanResultFilename;
	protected List<IGroup> resultEntryList;

	public final static String ECHIDNA_COMMANDS_PACKAGE_NAME = "au.gov.ansto.bragg.echidna.exp.command";
//	public final static String QUOKKA_FUNCTIONS_PACKAGE_NAME = "au.gov.ansto.bragg.quokka.exp.core.scanfunction";

	public static EchidnaExperiment getInstance(){
		if (experiment == null){
			experiment = new EchidnaExperiment();
		}
		return experiment;
	}

	protected EchidnaExperiment(){
		super();
//		if (sics == null) sics = new SicsDriver();
//		if (monitor == null) monitor = new Monitor();
		try {
			if (sics == null) sics = (SicsDOM) SicsDOMFactory.getSicsDOM();
//			printlnToShell(syncResult);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			printlnToShell("SICS is not connected\n");
		}
		try {
			if (monitor == null) monitor = (MonitorDOM) MonitorDOMFactory.getMonitorDOM();
		} catch (Exception e) {
			// TODO: handle exception
//			e.printStackTrace();
			printlnToShell("Monitor is not connected\n");
		}
		kakadu = KakaduDOMFactory.getKakaduDOM();
		kakadu.setWorkbenchPage(BeanShellCommandLineView.getWorkbenchPage());
	}

	@Override
	public String runCommand(String commandName, String... params) {
		// TODO Auto-generated method stub

		String result = "";
		Command command = null;
		try {
			command = (Command) Class.forName(ECHIDNA_COMMANDS_PACKAGE_NAME + "."
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
		result += command.run();
		return result;
	}

	public void printlnToShell(String text){
//		if (beanShellView == null) beanShellView = BeanShellCommandLineView.getInstance();
		getBeanShell().appendText(text);
	}

	public void printlnToShell(String text, ColorEnum color){
		getBeanShell().appendText(text, color);
	}

	public static BeanShellCommandLineView getBeanShell(){
		if (beanShellView == null) beanShellView = BeanShellCommandLineView.getInstance();
		return beanShellView;
	}

	public static void showCommandLineView() throws EchidnaExperimentException {
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
		BeanShellCommandLineView.getInstance();
//		try {
//			BeanShellCommandLineView.showCommandLineView();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			throw new EchidnaExperimentException(e);
//		}
	}

	public static void showCommandLineView(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		BeanShellCommandLineView.getInstance(window);
	}
	
//	public static void showCommandLineView()
	public static SicsDOM getSics() {
		return sics;
	}

	public static MonitorDOM getMonitor() {
		return monitor;
	}

	/**
	 * @return the scanResultFilename
	 */
	public String getScanResultFilename() {
		return scanResultFilename;
	}

	/**
	 * @param scanResultFilename the scanResultFilename to set
	 */
	public void setScanResultFilename(String scanResultFilename) {
		this.scanResultFilename = scanResultFilename;
	}

	/**
	 * @return the kakadu
	 */
	public static KakaduDOM getKakadu() {
		return kakadu;
	}

	/**
	 * @return the resultEntryList
	 */
	public List<IGroup> getResultEntryList() {
		return resultEntryList;
	}

	/**
	 * @param resultEntryList the resultEntryList to set
	 */
	public void setResultEntryList(List<IGroup> resultEntryList) {
		this.resultEntryList = resultEntryList;
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
	
	public static void addStatusListener(String sicsDevicePath, 
			IDynamicControllerListener listener) throws SicsDOMException{
		sics.addStatusListener(sicsDevicePath, listener);
	}

	public IDynamicController getDynamicController(String sicsDevicePath) throws SicsDOMException {
		// TODO Auto-generated method stub
		return sics.getDynamicController(sicsDevicePath);
		
	}

}
