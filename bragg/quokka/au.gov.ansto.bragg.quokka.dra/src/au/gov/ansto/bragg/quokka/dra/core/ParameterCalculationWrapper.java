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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.quokka.dra.algolib.core.InputParameterWrapper;
import au.gov.ansto.bragg.quokka.dra.algolib.core.QuokkaVectorCalculation;

public class ParameterCalculationWrapper implements ConcreteProcessor {

	// Input
	private IGroup wrap_trigger;
	
	// Variables
	private Double wrap_l1;
	private Double wrap_l2;
	private Double wrap_s1;
	private Double wrap_s2;
	private Double wrap_lambda;
	private Double wrap_lambdaWidth;
	private Double wrap_bS;
	private Double wrap_detWidth;
	private Double wrap_dDet;
	private Double wrap_aPixel;
	private Double wrap_offset;
	private Double wrap_guideWidth;
	private Double wrap_phi0;
	private Double wrap_s1Max;
	private Double wrap_t1;
	private Double wrap_t2;
	private Double wrap_t3;
	private Double wrap_lGap;
	private Double wrap_guideTrans;
	private Double wrap_lambdaT;
	private Double wrap_qMinRes;
	private Double wrap_qMaxRes;
	
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
	
	public Boolean process() throws Exception {
		wrap_o_inputparameters = new InputParameterWrapper(
				wrap_l1,
				wrap_l2,
				wrap_s1,
				wrap_s2,
				wrap_lambda,
				wrap_lambdaWidth,
				wrap_bS,
				wrap_detWidth,
				wrap_dDet,
				wrap_aPixel,
				wrap_offset,
				wrap_guideWidth,
				wrap_phi0,
				wrap_s1Max,
				wrap_t1,
				wrap_t2,
				wrap_t3,
				wrap_lGap,
				wrap_guideTrans,
				wrap_lambdaT,
				wrap_qMinRes,
				wrap_qMaxRes
			);
		
		// Now send this through the processors and wrap the output.
		/////////////////////////////////////////////////////////////
		// Each of these values needs to be calculated.
		QuokkaVectorCalculation libraryInstance = QuokkaVectorCalculation.getInstance();
		
		wrap_o_qmin = libraryInstance.getQMin(wrap_o_inputparameters);
		wrap_o_qmax = libraryInstance.getQMax(wrap_o_inputparameters);
		wrap_o_sigmaqmin = libraryInstance.getSigmaQMin(wrap_o_inputparameters);
		wrap_o_sigmaqmax = libraryInstance.getSigmaQMax(wrap_o_inputparameters);
		wrap_o_qmaxv = libraryInstance.getQMaxV(wrap_o_inputparameters);
		wrap_o_qmaxh = libraryInstance.getQMaxH(wrap_o_inputparameters);
		wrap_o_intensity = libraryInstance.getIntensity(wrap_o_inputparameters);
		wrap_o_umbra = libraryInstance.getUmbra(wrap_o_inputparameters);
		wrap_o_bv = libraryInstance.getBv(wrap_o_inputparameters);
		
		/////////////////////////////////////////////////////////////
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
	
	public IGroup getWrap_o_output() {
		return wrap_o_output;
	}
	
	public Map<String, Double> getInputValues() {
		return inputValues;
	}
	
	public Map<String, Double> getOutputValues() {
		return outputValues;
	}

	public void setWrap_l1(Double wrap_l1) {
		this.wrap_l1 = wrap_l1;
	}

	public void setWrap_l2(Double wrap_l2) {
		this.wrap_l2 = wrap_l2;
	}

	public void setWrap_s1(Double wrap_s1) {
		this.wrap_s1 = wrap_s1;
	}

	public void setWrap_s2(Double wrap_s2) {
		this.wrap_s2 = wrap_s2;
	}

	public void setWrap_lambda(Double wrap_lambda) {
		this.wrap_lambda = wrap_lambda;
	}

	public void setWrap_lambdaWidth(Double wrap_lambdaWidth) {
		this.wrap_lambdaWidth = wrap_lambdaWidth;
	}

	public void setWrap_bS(Double wrap_bS) {
		this.wrap_bS = wrap_bS;
	}

	public void setWrap_detWidth(Double wrap_detWidth) {
		this.wrap_detWidth = wrap_detWidth;
	}

	public void setWrap_dDet(Double wrap_dDet) {
		this.wrap_dDet = wrap_dDet;
	}

	public void setWrap_aPixel(Double wrap_aPixel) {
		this.wrap_aPixel = wrap_aPixel;
	}

	public void setWrap_offset(Double wrap_offset) {
		this.wrap_offset = wrap_offset;
	}

	public void setWrap_guideWidth(Double wrap_guideWidth) {
		this.wrap_guideWidth = wrap_guideWidth;
	}

	public void setWrap_phi0(Double wrap_phi0) {
		this.wrap_phi0 = wrap_phi0;
	}

	public void setWrap_s1Max(Double wrap_s1Max) {
		this.wrap_s1Max = wrap_s1Max;
	}

	public void setWrap_lGap(Double wrap_lGap) {
		this.wrap_lGap = wrap_lGap;
	}

	public void setWrap_guideTrans(Double wrap_guideTrans) {
		this.wrap_guideTrans = wrap_guideTrans;
	}

	public void setWrap_lambdaT(Double wrap_lambdaT) {
		this.wrap_lambdaT = wrap_lambdaT;
	}

	public void setWrap_qMinRes(Double wrap_qMinRes) {
		this.wrap_qMinRes = wrap_qMinRes;
	}

	public void setWrap_qMaxRes(Double wrap_qMaxRes) {
		this.wrap_qMaxRes = wrap_qMaxRes;
	}

	public void setWrap_t1(Double wrap_t1) {
		this.wrap_t1 = wrap_t1;
	}

	public void setWrap_t2(Double wrap_t2) {
		this.wrap_t2 = wrap_t2;
	}

	public void setWrap_t3(Double wrap_t3) {
		this.wrap_t3 = wrap_t3;
	}
	
	public void setWrap_trigger(IGroup wrap_control) {
		this.wrap_trigger = wrap_control;
	}

}
