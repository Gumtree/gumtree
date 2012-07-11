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
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.IOException;
import java.net.URI;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.dam.core.DataManagerFactory;
import au.gov.ansto.bragg.quokka.dra.core.internal.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.core.internal.ConverterLib;

/**
 * This class is the concrete processor of the background correction algorithm for echidna algorithm group.
 * It uses the databag of the echidna input data object. 
 *  
 * @author nxi
 * @version 1.0
 * @since V2.2
 */
public class BackgroundCorrection implements ConcreteProcessor {
	IGroup backgroundCorrection_scanData = null;
	Boolean backgroundCorrection_skip = false;
	Boolean backgroundCorrection_stop = false; 
	Double backgroundCorrection_ratio = 1.;
	String backgroundCorrection_backgroundFilename = null;
	IGroup backgroundCorrection_output = null;
//	public BackgroundCorrection(){}		
	private IGroup backgroundCorrection_backgroundData = null;

	/**
	 * This method returns the souce input for the square integration processor.
	 * It is usually called by the source processor of the square integration processor chain.
	 * @param data in echidna data object
	 * @param signal a control signal which will specific the behavior of the processor.
	 * @param filename  path + filename in String type
	 * @return List of input source objects for the processor chain.
	 * @throws IOException 
	 */
//	public List<Object> getSource(GroupData data, String dataName, String backgroundFileName) 
//	throws Exception{
//	List<Object> result = new LinkedList<Object>();
//	CachedVariable dataVariable = null;
//	try {
//	dataVariable = data.getVariable(dataName);	
//	} catch (Exception e) {
//	// TODO: handle exception
//	dataVariable = data.getGroup(dataName).findSignal();
//	}
//	GroupData dataGroup = (GroupData) dataVariable.getParentGroup();
//	Array dataArray = dataVariable.read();
//	result.add(dataGroup);
//	result.add(dataArray);
//	double[] backgroundData = null;
//	int[] dataShape = dataArray.getShape();
//	if (dataShape.length < 2 || dataShape.length > 3) 
//	throw new Exception("wron echidna data");
//	else{
//	int row, column;
//	if (dataShape.length == 3){
//	row = dataShape[1];
//	column = dataShape[2];
//	}else{
//	row = dataShape[0];
//	column = dataShape[1];
//	}
//	backgroundData = new double[row * column];
//	for (int i = 0; i < backgroundData.length; i ++) backgroundData[i] = 5.;
//	Array backgroundArray = Array.factory(double.class, new int[]{row, column},
//	backgroundData);
//	result.add(backgroundArray);
//	}
//	return result;
//	}

	/**
	 * This method is called when the processor process. 
	 * @param inDat scan data in three-dimensional double array type
	 * @param stepsize  in one-dimensional double array type
	 * @param twotheta0  in Double type
	 * @param binsize  in Double type
	 * @return  List of return results objects
	 * @throws IOException 
	 */
//	public List<Object> process(double[][][] inDat, double [][] array2theta,
//	double[] stepsize, Double twotheta0, Double binsize){
	public Boolean process() throws Exception{
//		List<Object> result = new LinkedList<Object>();
		if (backgroundCorrection_skip){
//			result.add(dataGroup);
			backgroundCorrection_output = backgroundCorrection_scanData;
		}else{
//			result.add(array2theta);
//			double[][] stitchResult = DRAStaticLibHRPD.echidnaDataStitch(
//			ConverterLib.get3DDouble(inDat), ConverterLib.get2DDouble(array2theta), 
//			ConverterLib.get1DDouble(stepsize), binsize, twotheta0);

//			Array array = Array.factory(double.class, new int[]{stitchResult.length, 
//			stitchResult[0].length}, ConverterLib.get1DDoubleStorage(stitchResult));
			IArray dataArray = ((NcGroup) backgroundCorrection_scanData).findSignal().getData();
			if (backgroundCorrection_backgroundFilename != null){
				URI backgroundCorrection_uri = ConverterLib.getURIFromPath(backgroundCorrection_backgroundFilename);
				backgroundCorrection_backgroundData = 
					DataManagerFactory.getDataManager(ConverterLib.getDictionaryPath()).getGroup(backgroundCorrection_uri);
				IArray backgroundArray = ((NcGroup) backgroundCorrection_backgroundData).findSignal().getData();
				int[] dataShape = dataArray.getShape();
				IArray array = null;
				double[][] correctionResult = removeBackground(dataArray, backgroundArray, backgroundCorrection_ratio);
//				array = Array.factory(double.class, new int[]{correctionResult.length, 
//				correctionResult[0].length}, ConverterLib.get1DDoubleStorage(correctionResult));
				array = Factory.createArray(double.class, new int[]{correctionResult.length, 
					correctionResult[0].length}, ConverterLib.get1DDoubleStorage(correctionResult));
				String resultName = "backgroundCorrection_result";
				backgroundCorrection_output = Factory.createGroup(backgroundCorrection_scanData.getDataset(), backgroundCorrection_scanData, resultName, true);
				((NcGroup) backgroundCorrection_output).addLog("apply background correction algorithm to get " + resultName);
//				DataItem signal = new DataItem(null, backgroundCorrection_output, resultName, array);
				IDataItem signal = Factory.createDataItem(null, backgroundCorrection_output, resultName, array);
				IDataItem stthVector = null;
				IDataItem channelVector = null;
				IDataItem twoThetaVector = null;
//				try {
//					stthVector = backgroundCorrection_scanData.getRootGroup().getDataItem("scanStep").clone();
//					channelVector = backgroundCorrection_scanData.getRootGroup().getDataItem("vertical_channel_number").clone();
//					twoThetaVector = backgroundCorrection_scanData.getRootGroup().getDataItem("thetaVector").clone();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				backgroundCorrection_output.buildResultGroup(signal, stthVector, channelVector, twoThetaVector);
				((NcGroup) backgroundCorrection_output).buildResultGroup(signal);
//				result.add(resultGroup);
			}else 
				backgroundCorrection_output = backgroundCorrection_scanData;
		}
//		return result;
		return backgroundCorrection_stop;
	}

	private double[][] removeBackground(IArray dataArray, IArray backgroundArray, Double ratio) {
		// TODO Auto-generated method stub
		double[][] correctionData = ConverterLib.get2DDouble(dataArray);
		double[][] backgroundData = ConverterLib.get2DDouble(backgroundArray);
		for (int i = 0; i < backgroundData.length; i++) {
			for (int j = 0; j < backgroundData[0].length; j++) {
				correctionData[i][j] = correctionData[i][j] - backgroundData[i][j] * ratio;
			}
		}
		return correctionData;
	}

	public IGroup getBackgroundCorrection_output() {
		return backgroundCorrection_output;
	}

	public void setBackgroundCorrection_scanData(
			IGroup backgroundCorrection_scanData) {
		this.backgroundCorrection_scanData = backgroundCorrection_scanData;
	}

	public void setBackgroundCorrection_backgroundData(
			IGroup backgroundCorrection_backgroundData) {
		this.backgroundCorrection_backgroundData = backgroundCorrection_backgroundData;
	}

	public void setBackgroundCorrection_skip(Boolean backgroundCorrection_skip) {
		this.backgroundCorrection_skip = backgroundCorrection_skip;
	}

	public void setBackgroundCorrection_stop(Boolean backgroundCorrection_stop) {
		this.backgroundCorrection_stop = backgroundCorrection_stop;
	}

	public void setBackgroundCorrection_ratio(Double backgroundCorrection_ratio) {
		this.backgroundCorrection_ratio = backgroundCorrection_ratio;
	}

	public void setBackgroundCorrection_backgroundFilename(
			String backgroundCorrection_backgroundFilename) {
		this.backgroundCorrection_backgroundFilename = backgroundCorrection_backgroundFilename;
	}


}

