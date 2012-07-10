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
package au.gov.ansto.bragg.freehep.jas3.core;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.freehep.jas3.core.StaticField.FunctionType;
import au.gov.ansto.bragg.freehep.jas3.exception.FitterException;

/**
 * @author nxi
 * Created on 24/06/2008
 */
public class UserDefinedFitter extends Fitter {

	public static final String FITTING_FOLDER = "FittingFunctions";
	public static final String FUNCTION_ATTRIBUTE_NAME = "FittingFunctions";
	public static final String FITTING_PROJECT = "FittingProject";
	public static List<String> keywords;
	
	private IGroup functionGroup;
	
	/**
	 * 
	 */
	public UserDefinedFitter() {
		// TODO Auto-generated constructor stub
		setFunctionType(FunctionType.AddFunction);
		if (keywords == null)
			initKeywords();
		setInverseAllowed(true);
	}

	private void initKeywords() {
		// TODO Auto-generated method stub
		keywords = new ArrayList<String>();
		Method[] methods = Math.class.getMethods();
		for (Method method : methods){
			String methodName = method.getName();
			if (!keywords.contains(methodName))
				keywords.add(methodName);
		}
		keywords.add("x");
	}

	public UserDefinedFitter(String name, String functionText) throws FitterException{
		this();
		try {
			functionGroup = Factory.createGroup(name);
			functionGroup.addStringAttribute(FUNCTION_ATTRIBUTE_NAME, functionText);
			functionGroup.addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
					StaticDefinition.DataStructureType.fitFunction.name());
			initFitter();
			createFitFunction();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new FitterException(e);
		}
		 
	}
	
	public UserDefinedFitter(URI functionURI) throws FitterException{
		this();
		try {
			IDataset dataset = Factory.createDatasetInstance(functionURI);
			functionGroup = dataset.getRootGroup().getGroupWithAttribute(
					StaticDefinition.DATA_STRUCTURE_TYPE,
					StaticDefinition.DataStructureType.fitFunction.name());
			initFitter();
			createFitFunction();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new FitterException("failed to load function from uri");
		}
	}
	
	protected void initFitter() throws FitterException{
		setFunctionText(functionGroup.getAttribute(FUNCTION_ATTRIBUTE_NAME).getStringValue());
		parseParametersFromText(getFunctionText());
		setDimension(findDimension(getFunctionText(), -1) + 1);
	}
	
	private void parseParametersFromText(String functionText) {
		// TODO Auto-generated method stub
		List<String> parameterList = new ArrayList<String>();
		String parameter = "";
		boolean isWordBegin = false;
		for (int i = 0; i < functionText.length(); i++) {
			char character = functionText.charAt(i);
			if (Character.isLetter(character)){
					parameter += character;
					isWordBegin = true;
			} else if (Character.isDigit(character)){
					if (isWordBegin)
						parameter += character;
			} else{
				if (isWordBegin){
					isWordBegin = false;
					if (! parameterList.contains(parameter))
							parameterList.add(parameter);
					parameter = "";
				}
			}
		} 
		if (isWordBegin && ! parameterList.contains(parameter)) 
			parameterList.add(parameter);
		for (String word : keywords)
			parameterList.remove(word);
		for (String parameterName : parameterList)
			addParameter(parameterName);
	}

	private int findDimension(String functionText, int initDimension){
		if (functionText.contains("x[")){
			int patternIndex = functionText.indexOf("x[");
			functionText = functionText.substring(patternIndex + 2);
			patternIndex = functionText.indexOf("]");
			int newDimension = Integer.valueOf(functionText.substring(0, patternIndex));
			if (newDimension > initDimension)
				return findDimension(functionText, newDimension);
			else 
				return findDimension(functionText, initDimension);
		}
		return initDimension;
	}
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.freehep.jas3.core.Fitter#setParameters()
	 */
	@Override
	public void setParameters() {
		// TODO Auto-generated method stub

	}

	public IGroup toGDMGroup(){
		return functionGroup;
	}
}
