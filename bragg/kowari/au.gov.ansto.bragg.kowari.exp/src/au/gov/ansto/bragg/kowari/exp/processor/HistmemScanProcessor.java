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
package au.gov.ansto.bragg.kowari.exp.processor;

import java.io.File;

import org.gumtree.data.interfaces.IGroup;
import org.gumtree.scripting.ScriptBlock;

import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOMFactory;
import au.gov.ansto.bragg.kowari.exp.core.KowariExperiment;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 30/10/2008
 */
public class HistmemScanProcessor extends ConcreteProcessor {

	public final static String HMSCAN_FILENAME_PATH = "/experiment/file_name";
	
	private String scanVar;
	private Double startPosition;
	private Double stopPosition;
	private Integer numPoints;
	private String mode;
	private String preset;
	private Boolean histmemScan_stop = false;
	private Boolean histmemScan_skip = false;
	
	private Boolean loopIn;
	private Integer runNumber = -1;
	private IGroup resultEntry;
	private Boolean loopOut;

	private double currentPosition;
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.processor.ConcreteProcessor#process()
	 */
	@Override
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		loopOut = false;
		if (histmemScan_skip)
			return true;
		String script;
		ScriptBlock block = new ScriptBlock();
		
		// run number is not natural means the scan hasn't started yet
		if (runNumber < 0){
			runNumber = 0;
//			script = "sics.execute('newfile HISTOGRAM_XYT')";
//			KowariExperiment.getInstance().runPythonScript(script, false);
//			block.append("sics.execute('newfile HISTOGRAM_XYT')");
//			block.append("sics.execute('publish ::nexus::data user')");
//			block.append("sics.execute('::nexus::data axis 1 " + scanVar + "')");
//			KowariExperiment.getInstance().runPythonBlock(block, false);
			script = "sics.execute('newfile HISTOGRAM_XYT')";
			KowariExperiment.getInstance().runPythonScript(script, false);
			script = "sics.execute('publish ::nexus::data user')";
			KowariExperiment.getInstance().runPythonScript(script, false);
			script = "sics.execute('::nexus::data axis 1 " + scanVar + "')";
			KowariExperiment.getInstance().runPythonScript(script, false);

			Thread.sleep(900);
		}else
			runNumber ++;
		if (numPoints <= 0) 
			throw new Exception("can not do " + runNumber + " point");
		double increment = (stopPosition - startPosition) / (numPoints > 1 ? numPoints - 1 : numPoints);
		currentPosition = startPosition + increment * runNumber;
		// if current position is larger than the 
		if (currentPosition > stopPosition){
			runNumber = -1;
			return true;
		}
//		block.append("sics.drive('" + scanVar + "', " + currentPosition + ")");
//		block.append("sics.histmem('start', '" + mode + "', '" + preset + "')");
//		block.append("sics.execute('save " + runNumber + "')");
//		KowariExperiment.getInstance().runPythonBlock(block, false);
		script = "sics.drive('" + scanVar + "', " + currentPosition + ")";
		KowariExperiment.getInstance().runPythonScript(script, false);
		System.out.println("***********Moving finished*************");
		script = "sics.histmem('start', '" + mode + "', " + preset + ")";
		KowariExperiment.getInstance().runPythonScript(script, false);
		System.out.println("***********Histmem finished*************");
		script = "sics.execute('save " + runNumber + "')";
		KowariExperiment.getInstance().runPythonScript(script, false);
		System.out.println("***********Save finished*************");
		

		
		String resultFilename = KowariExperiment.getSics().getValue(
				HMSCAN_FILENAME_PATH);
		Thread.sleep(5000);
		File newFile = new File(resultFilename);
		resultFilename = "W:/commissioning/" + newFile.getName();
		System.out.println(resultFilename);
		if (!(new File(resultFilename)).exists())
			resultFilename = "D:/dra/kowaridata/KWR0000019.nx.hdf";
		CicadaDOM cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
		resultEntry = cicada.loadDataFromFile(resultFilename);
		loopOut = true;
		return histmemScan_stop;
	}

	/**
	 * @return the loopOut
	 */
	public Boolean getLoopOut() {
		return loopOut;
	}

	/**
	 * @return the resultEntry
	 */
	public IGroup getResultEntry() {
		return resultEntry;
	}

	/**
	 * @param scanVar the scanVar to set
	 */
	public void setScanVar(String scanVar) {
		this.scanVar = scanVar;
	}

	/**
	 * @param startPosition the startPosition to set
	 */
	public void setStartPosition(Double startPosition) {
		this.startPosition = startPosition;
	}

	/**
	 * @param stopPosition the stopPosition to set
	 */
	public void setStopPosition(Double stopPosition) {
		this.stopPosition = stopPosition;
	}

	/**
	 * @param numPoints the number of points to set
	 */
	public void setNumPoints(Integer numPoints) {
		this.numPoints = numPoints;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @param preset the preset to set
	 */
	public void setPreset(String preset) {
		this.preset = preset;
	}

	/**
	 * @param histmemScan_stop the histmemScan_stop to set
	 */
	public void setHistmemScan_stop(Boolean histmemScan_stop) {
		this.histmemScan_stop = histmemScan_stop;
	}

	/**
	 * @param histmemScan_skip the histmemScan_skip to set
	 */
	public void setHistmemScan_skip(Boolean histmemScan_skip) {
		this.histmemScan_skip = histmemScan_skip;
	}

	/**
	 * @param loopIn the loopIn to set
	 */
	public void setLoopIn(Boolean loopIn) {
		this.loopIn = loopIn;
	}

	/**
	 * @return the runNumber
	 */
	public Integer getRunNumber() {
		return runNumber;
	}

}
