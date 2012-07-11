/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lindsay Winkler (Bragg Institute) - initial implementation
 *******************************************************************************/

package au.gov.ansto.bragg.quokka.dra.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.quokka.dra.algolib.core.InputParameterWrapper;

/**
 * Processor which wraps all of the output parameters into a <code>Group</code>.
 * Each of the output parameters becomes an attribute in the group.
 */
public class OutputWrapper implements ConcreteProcessor {

	private Double wrap_o_qmin;
	private Double wrap_o_qmax;
	private Double wrap_o_sigmaqmin;
	private Double wrap_o_sigmaqmax;
	private Double wrap_o_qmaxv;
	private Double wrap_o_qmaxh;
	private Double wrap_o_intensity;
	private Double wrap_o_umbra;
	private Double wrap_o_bv;
	
	private InputParameterWrapper wrap_o_inputparameters;
	
	private Map<String, Double> inputValues = null;
	private Map<String, Double> outputValues = null;
	private Map<String, String> inputUnits = null;
	private Map<String, String> outputUnits = null;
	
	// These are all the input variables.
	
	private IGroup wrap_o_output;

	public OutputWrapper(){
		
	}
	
	private void initialiseValues() {
		if (inputValues == null) {
			inputValues = new HashMap<String, Double>();
			
			inputValues.put("Source to Sample Distance (L1)", wrap_o_inputparameters.getL1());
			inputValues.put("Sample to Detector Distance (L2)", wrap_o_inputparameters.getL2());
			inputValues.put("qminres", wrap_o_inputparameters.getQminRes());
			inputValues.put("qmaxres", wrap_o_inputparameters.getQmaxRes());
			inputValues.put("Lambda", wrap_o_inputparameters.getLambda());
			inputValues.put("Beam Stop Size", wrap_o_inputparameters.getBs());
			inputValues.put("Detector Size", wrap_o_inputparameters.getDet_width());
			inputValues.put("Detector Resolution", wrap_o_inputparameters.getDDet());
			inputValues.put("Detector Pixel Size", wrap_o_inputparameters.getAPixel());
			inputValues.put("Detector Offset", wrap_o_inputparameters.getOffset());
			inputValues.put("Source Aperture (S1)", wrap_o_inputparameters.getS1());
			inputValues.put("Sample Aperture (S2)", wrap_o_inputparameters.getS2());
			inputValues.put("l_gap", wrap_o_inputparameters.getL_gap());
			inputValues.put("Guide Width", wrap_o_inputparameters.getGuide_width());
			inputValues.put("Max SSD", wrap_o_inputparameters.getS1max());
			inputValues.put("T1", wrap_o_inputparameters.getT1());
			inputValues.put("T2", wrap_o_inputparameters.getT2());
			inputValues.put("T3", wrap_o_inputparameters.getT3());
			inputValues.put("Guide Trans", wrap_o_inputparameters.getGuide_trans());
			inputValues.put("Cold Source Flux (phi0)", wrap_o_inputparameters.getPhi0());
			inputValues.put("lambdaT", wrap_o_inputparameters.getLambdaT());
			inputValues.put("Lambda Width", wrap_o_inputparameters.getLambdaWidth());
		}
		
		// FIXME
		// Need to insert correct units here
		// That is, for each value of the input and output, we need to
		// insert the correct unit by hand.
		if (inputUnits == null) {
			inputUnits = new HashMap<String, String>();
			Iterator<String> iterator = inputValues.keySet().iterator();
			while(iterator.hasNext()) {
				String key = iterator.next();
				inputUnits.put(key, "");
			}
		}
		inputUnits.put("Source to Sample Distance (L1)", "m");
		inputUnits.put("Sample to Detector Distance (L2)", "m");
		inputUnits.put("Source Aperture (S1)", "m");
		inputUnits.put("Sample Aperture (S2)", "m");
		inputUnits.put("Lambda", "\u212b");
		
		if (outputValues == null) {
			outputValues = new HashMap<String, Double>();
			
			outputValues.put("Qmin", wrap_o_qmin);
			outputValues.put("Qmax", wrap_o_qmax);
			outputValues.put("sigmaQmin", wrap_o_sigmaqmin);
			outputValues.put("sigmaQmax", wrap_o_sigmaqmax);
			outputValues.put("QmaxV", wrap_o_qmaxv);
			outputValues.put("QmaxH", wrap_o_qmaxh);
			outputValues.put("Intensity", wrap_o_intensity);
			outputValues.put("Umbra", wrap_o_umbra);
			outputValues.put("Bv", wrap_o_bv);
		}
		
		// FIXME
		// Need to insert correct units here
		if (outputUnits == null) {
			outputUnits = new HashMap<String, String>();
			Iterator<String> iterator = outputValues.keySet().iterator();
			while(iterator.hasNext()) {
				String key = iterator.next();
				outputUnits.put(key, "");
			}
		}
	}
	
	public Boolean process() throws Exception {
		
		initialiseValues();
		IDataset ncDataset = Factory.createEmptyDatasetInstance();
		String shortName = "CalculationSet";
		boolean init = true;
		IGroup rootGroup = ncDataset.getRootGroup();
		wrap_o_output = Factory.createGroup(ncDataset, rootGroup, shortName, init);

		// So now the output group is created.
		// We want to populate it with both input and output variables
		// according to our schema.
		wrap_o_output.addOneAttribute(Factory.createAttribute("algorithm", "calculateParameters"));
		wrap_o_output.addOneAttribute(Factory.createAttribute("version", "1.0"));
		wrap_o_output.addOneAttribute(Factory.createAttribute("signal", "calculation"));
		// wrap_o_output needs to have a root group with an empty name
		IGroup inputDataGroup = Factory.createGroup(ncDataset, wrap_o_output, "Input", init);
		IGroup outputDataGroup = Factory.createGroup(ncDataset, wrap_o_output, "Output", init);
		
		Iterator<String> iterator = inputValues.keySet().iterator();
		while(iterator.hasNext()) {
			String key = iterator.next();
			Double value = inputValues.get(key);
			IDataItem currentDataItem = Factory.createDataItem(ncDataset, inputDataGroup, key, Factory.createArray(new double[]{value}));
			currentDataItem.addOneAttribute(Factory.createAttribute("units", inputUnits.get(key)));
			inputDataGroup.addDataItem(currentDataItem);
			inputDataGroup.addOneAttribute(Factory.createAttribute("signal", "input"));
		}
		
		iterator = outputValues.keySet().iterator();
		while(iterator.hasNext()) {
			String key = iterator.next();
			Double value = outputValues.get(key);
			IDataItem currentDataItem = Factory.createDataItem(ncDataset, outputDataGroup, key, Factory.createArray(new double[]{value}));
			currentDataItem.addOneAttribute(Factory.createAttribute("units", outputUnits.get(key)));
			outputDataGroup.addDataItem(currentDataItem);
			outputDataGroup.addOneAttribute(Factory.createAttribute("signal", "output"));
		}
		
		return false;
	}
	
	public IGroup getWrap_o_output() {
		return wrap_o_output;
	}
	
	public void setWrap_o_qmin(Double wrap_o_qmin) {
		this.wrap_o_qmin = wrap_o_qmin;
	}
	public void setWrap_o_qmax(Double wrap_o_qmax) {
		this.wrap_o_qmax = wrap_o_qmax;
	}
	public void setWrap_o_sigmaqmin(Double wrap_o_sigmaqmin) {
		this.wrap_o_sigmaqmin = wrap_o_sigmaqmin;
	}
	public void setWrap_o_sigmaqmax(Double wrap_o_sigmaqmax) {
		this.wrap_o_sigmaqmax = wrap_o_sigmaqmax;
	}
	public void setWrap_o_qmaxv(Double wrap_o_qmaxv) {
		this.wrap_o_qmaxv = wrap_o_qmaxv;
	}
	public void setWrap_o_qmaxh(Double wrap_o_qmaxh) {
		this.wrap_o_qmaxh = wrap_o_qmaxh;
	}
	public void setWrap_o_intensity(Double wrap_o_intensity) {
		this.wrap_o_intensity = wrap_o_intensity;
	}
	public void setWrap_o_umbra(Double wrap_o_umbra) {
		this.wrap_o_umbra = wrap_o_umbra;
	}
	public void setWrap_o_bv(Double wrap_o_bv) {
		this.wrap_o_bv = wrap_o_bv;
	}
	
	public void setWrap_o_inputparameters(
			InputParameterWrapper wrap_o_inputparameters) {
		this.wrap_o_inputparameters = wrap_o_inputparameters;
	}
	
	public Map<String, Double> getInputValues() {
		return inputValues;
	}
	
	public Map<String, Double> getOutputValues() {
		return outputValues;
	}
	
	/**
	 * Example of creating and unwrapping a group. 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		OutputWrapper wrapper = new OutputWrapper();
		
		// Set all of the inputs
		// The following nine values are the outputs of the 
		// calculation
		wrapper.setWrap_o_qmin(0.0598);
		wrapper.setWrap_o_qmax(0.9977);
		wrapper.setWrap_o_sigmaqmin(13.4);
		wrapper.setWrap_o_sigmaqmax(12.2);
		wrapper.setWrap_o_qmaxv(0.6392);
		wrapper.setWrap_o_qmaxh(0.9446);
		wrapper.setWrap_o_intensity(18d);
		wrapper.setWrap_o_umbra(0.2);
		wrapper.setWrap_o_bv(0.233562);
		
		// Last input is a wrapped set of all the calculation inputs
		// Create this data structure then send it to the OutputWrapper
		InputParameterWrapper inputWrapper = new InputParameterWrapper(
				20,
				0.7,
				0.2,
				0.2,
				6,
				0.3,
				7,
				100,
				0.5,
				0.5,
				45,
				5,
				1E14,
				20,
				1,
				1,
				0.75,
				0,
				0.97,
				3.95,
				0.05,
				0.25
				);
		wrapper.setWrap_o_inputparameters(inputWrapper);
		
		// Invoke process() to create the output group
		try {
			wrapper.process();
		} catch (Exception e) {
			System.err.println("Creation of output group failed.");
			e.printStackTrace();
		}
		
		// ---EXTRACTION OF DATA FOLLOWS---
		// Get the group and illustrate extraction of data
		IGroup mainGroup = wrapper.getWrap_o_output();
		
		mainGroup.getAttribute("signal");
		
		Iterator<String> inputAttributeNames = wrapper.getInputValues().keySet().iterator();
		Iterator<String> outputAttributeNames = wrapper.getOutputValues().keySet().iterator();
		
		IGroup inputGroup = mainGroup.findGroup("Input");
		System.out.println("\nInput Parameters:\n");
		while (inputAttributeNames.hasNext()) {
			String name = inputAttributeNames.next();
			// Get the data item with this name and print out its
			// value and units.
			IDataItem currentDataItem = inputGroup.findDataItem(name);
			IArray currentData = currentDataItem.getData();
			IIndex index = currentData.getIndex();
			index.set(0);
			
			IAttribute unitAttribute = currentDataItem.getAttribute("units");
			Double value = currentData.getDouble(index);
			System.out.println(name + " = " + value + ", units = " + unitAttribute.getStringValue());
		}
		
		IGroup outputGroup = mainGroup.findGroup("Output");
		System.out.println("\nOutput Parameters:\n");
		while (outputAttributeNames.hasNext()) {
			String name = outputAttributeNames.next();
			// Get the data item with this name and print out its
			// value and units.
			IDataItem currentDataItem = outputGroup.findDataItem(name);
			IArray currentData = currentDataItem.getData();
			IIndex index = currentData.getIndex();
			index.set(0);
			
			IAttribute unitAttribute = currentDataItem.getAttribute("units");
			Double value = currentData.getDouble(index);
			System.out.println(name + " = " + value + ", units = " + unitAttribute.getStringValue());
		}
	}
}
