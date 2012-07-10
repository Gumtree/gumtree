/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov 
 *     Paul Hathaway
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.editors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.process.port.TunerPortListener;

/**
 * @author Implementation: Danil Klimontov (dak)
 * @author Modifications:  Paul Hathaway (pvh)
 */
public class NumericOperationParameterEditor extends OperationParameterEditor 
									implements ModifyListener, VerifyListener {
	private static Color defaultTextColor, errorTextColor;
	private static final Integer SIG_FIGURES = 5;
	private Text parameterText;
	private Number maxValue;
	private Number minValue;


	public NumericOperationParameterEditor(
			OperationParameter operationParameter, Composite parentComposite) {
		super(operationParameter, parentComposite);
	}

	protected Control createEditor() {
		parameterText = new Text(parentComposite, SWT.BORDER);
		
		if (defaultTextColor == null) {
			defaultTextColor = parameterText.getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_FOREGROUND);
			errorTextColor = parameterText.getDisplay().getSystemColor(
					SWT.COLOR_RED);
		}
		maxValue = getNumericValue(operationParameter.getMaxValue());
		minValue = getNumericValue(operationParameter.getMinValue());
		fieldWidth = operationParameter.getUIWidth();
		if (maxValue != null || minValue != null){
			String limitText = " (";
			if (minValue != null) {
				limitText += "min:" + minValue;
			}
			if (maxValue != null) {
				if (maxValue != null) {
					limitText += ", ";
				}				
				limitText += "max:" + maxValue + "";
			}
			limitText +=")";
			parameterLabel.setText(parameterLabel.getText() + limitText);

			//add validation listener
//			parameterText.addVerifyListener(this);
		}
		//to register data modifications
		parameterText.addModifyListener(this);
		if (fieldWidth > 0)
			parameterText.setSize(fieldWidth, parameterText.getSize().y);
		
//		parameterText.addFocusListener(new FocusListener(){
//
//			public void focusGained(FocusEvent arg0) {
//			}
//
//			public void focusLost(FocusEvent arg0) {
//				if (validateValue()){
//						dataUpdated();
//				}
//			}});
		return parameterText;
	}

	public void modifyText(ModifyEvent e) {
		if (validateValue()){
//			if (!parameterText.isFocusControl())
				dataUpdated();
		}
		int i=0;
	}

	private boolean validateValue() {
		final Object editorData = getEditorData();
		if (editorData == null) {
			parameterText.setForeground(errorTextColor);
			return true;
		}
		if (minValue != null && minValue instanceof Comparable) {
			Comparable minValueComparable = (Comparable) minValue;
			if (minValueComparable.compareTo(editorData) > 0) {
				parameterText.setForeground(errorTextColor);
				return false;
			}
		}
		if (maxValue != null && maxValue instanceof Comparable) {
			Comparable maxValueComparable = (Comparable) maxValue;
			if (maxValueComparable.compareTo(editorData) < 0) {
				parameterText.setForeground(errorTextColor);
				return false;
			}
		}
		parameterText.setForeground(defaultTextColor);
		return true;
	}

	public void verifyText(VerifyEvent e) {
		
		final Object editorData = getEditorData();
		if (editorData == null) {
			return;
		}
		
		if (minValue != null && minValue instanceof Comparable) {
			Comparable minValueComparable = (Comparable) minValue;
			if (minValueComparable.compareTo(editorData) > 0) {
				e.doit = false;
				return;
			}
		}
		if (maxValue != null && maxValue instanceof Comparable) {
			Comparable maxValueComparable = (Comparable) maxValue;
			if (maxValueComparable.compareTo(editorData) < 0) {
				e.doit = false;
				return;
			}
		}
	}

	/*
	 * Modified pvhathaway sep2009
	 * @see au.gov.ansto.bragg.kakadu.ui.editors.OperationParameterEditor#loadData()
	 */
	public void loadData() {
		String currentText = parameterText.getText();
		String operationValue = operationParameter.getValue() != null ? 
				operationParameter.getValue().toString() : "";			
		try {
// dak
// (pvh note) need to check if unchanged as could get called twice for every keystroke			
			if (!Double.valueOf(currentText).equals(Double.valueOf(operationValue)))
			{
// pvh
				if(0<operationValue.length()) {
					operationValue = formatNumeric(operationValue,SIG_FIGURES);
				}			
				parameterText.setText(operationValue);
			}
		} catch (Exception e) {
			if (!operationValue.equals(currentText))
				parameterText.setText(operationValue);
		}
	}

	/**
	 * @param number Floating-point number string to be formatted
	 * @param digitLimit Maximum estimated width or significant figures
	 * @return Formatted number string
	 * 
	 * @author pvhathaway Sep 2009
	 */
	private String formatNumeric(String number,int digitLimit) {
		
		// formatting for floating point, therefore skip if no decimal point
		if (0>number.indexOf(".")) { 
			return number; 
		}
		// put in standard java number string form;
		Double value = Double.valueOf(number);
		String result = value.toString();		
		Double magnitude = Math.abs(value);
		String sMag = magnitude.toString();
		
		int ePlace = sMag.indexOf("E");
		int digits = (0<ePlace) ? ePlace-1 : result.length()-1;
		
		if (digitLimit < digits) {
			int exponent = (int) Math.round(Math.floor(Math.log10(magnitude)));
			if (digitLimit < Math.abs(exponent)) {
				// sci notation
				String formatString = "%"+(digitLimit)+"."+(digitLimit-1)+"E";
				result = String.format(formatString, value);
			} else {
				int points = digits - sMag.indexOf(".");
				int places = digitLimit-1;
				if (exponent>0) {
					places -= exponent; 
				}
				result = roundFormat(value,Math.min(places,points));
			}
		}
		return result;
	}

	/**
	 * @param val Floating-point value to format
	 * @param p Number of significant decimal places to use
	 * @return Formatted number string in #.{0} format
	 * 
	 * @author pvhathaway Sep 2009
	 */
	private String roundFormat(double val,int p) {
		String formatString = "#";
		if (0<p) {
			formatString += ".";
			for(int i=0; i<p; i++) {
				formatString += "0";
			}
		}
		DecimalFormat form = new DecimalFormat(formatString);
		return form.format(val);
	}
	
	protected Object getEditorData() {
		if (parameterText.getText() != null && parameterText.getText() != ""){
			return getNumericValue(parameterText.getText());
		}
		return null;
	}

	private Number getNumericValue(Object value) {
		if (value == null) {
			return null;
		}
		//String text = formatNumeric(value.toString(),SIG_FIGURES);
		String text = value.toString();
		Class<?> clazz = operationParameter.getParameterValueClass();
		Constructor<?> constructor = null;

		try {
			constructor = clazz.getConstructor(String.class);
		} catch (SecurityException e) {
			e.printStackTrace();
//				throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			System.out.println("Can not create signal from String: " + clazz.getName());
//				throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
		}
		if (constructor != null) {
			try {
				return (Number) constructor.newInstance(text);
			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
				//				throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
			} catch (InstantiationException e) {
//				e.printStackTrace();
				//				throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
			} catch (IllegalAccessException e) {
//				e.printStackTrace();
				//				throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
			} catch (InvocationTargetException e) {
//				e.printStackTrace();
				//				throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
			}
		}
		return null;
	}
	

	public void addApplyParameterListener(SelectionListener selectionListener) {
		parameterText.addSelectionListener(selectionListener);
	}

	@Override
	protected void initializeTunerListener() {
		listener = new TunerPortListener(operationParameter.getTuner()){

			@Override
			public void updateUIMax(final Object max) {
				if (max instanceof Number)
					DisplayManager.getDefault().asyncExec(new Runnable(){

						public void run() {
							maxValue = (Number) max;
						}});
			}

			@Override
			public void updateUIMin(final Object min) {
				if (min instanceof Number)
					DisplayManager.getDefault().asyncExec(new Runnable(){

						public void run() {
							minValue = (Number) min;
						}});
			}

			@Override
			public void updateUIOptions(final List<?> options) {
				
			}

			@Override
			public void updateUIValue(final Object value) {
				if (value instanceof Number)
					DisplayManager.getDefault().asyncExec(new Runnable(){

						public void run() {
							setChangeListenerEnable(false);
							String sVal = value.toString();
							if(0<sVal.length()) {
								sVal = formatNumeric(sVal,SIG_FIGURES);
							}
							parameterText.setText(sVal);
							operationParameter.setChanged(false);
							setChangeListenerEnable(true);
						}});
						
			}
			
		};
	}
}
