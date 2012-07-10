/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.editors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.process.port.TunerPortListener;

/**
 * 
 * @author Danil Klimontov (dak)
 */
public class DefaultOperationParameterEditor extends OperationParameterEditor implements ModifyListener {
	private Text parameterText;


	public DefaultOperationParameterEditor(
			OperationParameter operationParameter, Composite parentComposite) {
		super(operationParameter, parentComposite);
	}

	protected Control createEditor() {
		parameterText = new Text(parentComposite, SWT.BORDER);

		//TODO define limits
//		if (tuner.getMax() != null || tuner.getMin() != null){
//			String limitText = " (";
//			if (tuner.getMin() != null) {
//				limitText += "min:" + tuner.getMin() + ", ";
//			}
//			if (tuner.getMax() != null) {
//				if (tuner.getMin() != null) {
//					limitText += ", ";
//				}				
//				limitText += "max:" + tuner.getMax() + "";
//			}
//			limitText +=")";
//			tunerLabel.setText(tunerLabel.getText() + limitText);
//		}

		//to register data modifications
		parameterText.addModifyListener(this);
		
		return parameterText;
	}

	public void modifyText(ModifyEvent e) {
		dataUpdated();
	}

	public void loadData() {
//		if (operationParameter.getValue() != null) {
//			parameterText.setText(operationParameter.getValue().toString());
//		}
		String currentText = parameterText.getText();
		String operationValue = operationParameter.getValue() != null ? 
				operationParameter.getValue().toString() : "";
		if (!operationValue.trim().equals(currentText.trim()))
			parameterText.setText(operationValue);
	}

	protected Object getEditorData() {
		if (parameterText.getText() != null && parameterText.getText() != ""){
			//checkLimit
//			if (tuner.getMin() != null) 
//				if (Double.valueOf(parameterText.getText()) < Double.valueOf(tuner.getMin().toString()))
//					parameterText.setText(tuner.getMin().toString());
//			if (tuner.getMax() != null) 
//				if (Double.valueOf(parameterText.getText()) > Double.valueOf(tuner.getMax().toString()))
//					parameterText.setText(tuner.getMax().toString());
//
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
			try {
				return constructor.newInstance(parameterText.getText());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
//				throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
			} catch (InstantiationException e) {
				e.printStackTrace();
//				throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
//				throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
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
		// TODO Auto-generated method stub
		listener = new TunerPortListener(operationParameter.getTuner()){

			@Override
			public void updateUIMax(final Object max) {
				// TODO Auto-generated method stub
			}

			@Override
			public void updateUIMin(final Object min) {
				// TODO Auto-generated method stub
			}

			@Override
			public void updateUIOptions(final List<?> options) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void updateUIValue(final Object value) {
				// TODO Auto-generated method stub
				DisplayManager.getDefault().asyncExec(new Runnable(){

					public void run() {
						// TODO Auto-generated method stub
						setChangeListenerEnable(false);
						parameterText.setText(value.toString());	
						operationParameter.setChanged(false);
						setChangeListenerEnable(true);
					}});				
			}
			
		};
	}
}
