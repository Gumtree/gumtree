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
package au.gov.ansto.bragg.kakadu.ui.editors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.process.port.TunerPortListener;

/**
 * @author nxi
 * Created on 16/04/2008
 */
public class OptionOperationParameterEditor extends OperationParameterEditor
		implements ModifyListener, VerifyListener {

	private static Color defaultTextColor, errorTextColor;
	private CCombo dropDownSelect;


	/**
	 * @param operationParameter
	 * @param parentComposite
	 */
	public OptionOperationParameterEditor(
			OperationParameter operationParameter, Composite parentComposite) {
		super(operationParameter, parentComposite);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kakadu.ui.editors.OperationParameterEditor#addApplyParameterListener(org.eclipse.swt.events.SelectionListener)
	 */
	@Override
	public void addApplyParameterListener(SelectionListener selectionListener) {
//		dropDownSelect.addSelectionListener(selectionListener);
	}


	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kakadu.ui.editors.OperationParameterEditor#createEditor()
	 */
	@Override
	protected Control createEditor() {
		dropDownSelect = new CCombo(parentComposite, SWT.DROP_DOWN | SWT.BORDER);
		
		if (defaultTextColor == null) {
			defaultTextColor = dropDownSelect.getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_FOREGROUND);
			errorTextColor = dropDownSelect.getDisplay().getSystemColor(
					SWT.COLOR_RED);
		}

		List<?> options = operationParameter.getOptions();
		Object defaultValue = operationParameter.getDefaultValue();
		int defaultSelectIndex = 0;
		for (Iterator<?> iterator = options.iterator(); iterator.hasNext();) {
			Object item = iterator.next();
			dropDownSelect.add(item.toString());
			if (defaultValue.toString().equals(item.toString()))
				defaultSelectIndex = options.indexOf(item);
		}
		dropDownSelect.select(defaultSelectIndex);
		dropDownSelect.addModifyListener(this);
		dropDownSelect.setEditable(false);
		dropDownSelect.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent arg0) {
				
			}

			public void keyReleased(KeyEvent arg0) {
				
			}});
		
 		return dropDownSelect;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kakadu.ui.editors.OperationParameterEditor#getEditorData()
	 */
	@Override
	protected Object getEditorData() {
		int selectionIndex = dropDownSelect.getSelectionIndex();
		String text = dropDownSelect.getText().trim();
		if (selectionIndex < 0 || !text.equals(dropDownSelect.getItems()[selectionIndex])) {
			String[] items = dropDownSelect.getItems();
			for (int i = 0; i < items.length; i++) {
				if (items[i].equals(text))
					return operationParameter.getOptions().get(i);
			}
			Object numberItem = findClosestNumberItem(text, items);
			if (numberItem != null)
				return numberItem;
			return operationParameter.getDefaultValue();
//			return createNewInstance(text);
		}
		return operationParameter.getOptions().get(selectionIndex);
	}

	private Object createNewInstance(String text) {
		Class<?> clazz = operationParameter.getParameterValueClass();
		Constructor<?> constructor = null;

		try {
			constructor = clazz.getConstructor(String.class);
		} catch (SecurityException e) {
			e.printStackTrace();
//			throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			System.out.println("Can not create signal from String: " + clazz.getName());
//			throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
		}
		try {
			return constructor.newInstance(text);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
//			throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
		} catch (InstantiationException e) {
			e.printStackTrace();
//			throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
//			throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
//			throw new SubmitDataException("Submit tuner '" + operationParameter.getName() + "' signal operation failed.", e);
		}

		return null;
	}

	private Object findClosestNumberItem(String text, String[] items) {
		int closestIndex = -1;
		try {
			double inputValue = Double.valueOf(text);
			double difference = Double.POSITIVE_INFINITY;
			for (int i = 0; i < items.length; i++) {
				double valueItem = Double.valueOf(items[i]);
				if (Math.abs(inputValue - valueItem) < difference)
					closestIndex = i;
			}
		} catch (Exception e) {
		}
		if (closestIndex >= 0)
			return operationParameter.getOptions().get(closestIndex);
		return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kakadu.ui.editors.OperationParameterEditor#loadData()
	 */
	@Override
	public void loadData() {
		if (operationParameter.getValue() != null) {
			List<?> options = operationParameter.getOptions();
			int defaultSelectIndex = 0;
			for (Iterator<?> iterator = options.iterator(); iterator.hasNext();) {
				Object item = iterator.next();
				if (operationParameter.getValue().toString().equals(item.toString()))
					defaultSelectIndex = options.indexOf(item);
			}
//			if (dropDownSelect.getSelection() == null)
//				dropDownSelect.select(defaultSelectIndex);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent arg0) {
		if (isChangeListenerEnabled)
			dataUpdated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
	 */
	public void verifyText(VerifyEvent arg0) {

	}

	@Override
	protected void initializeTunerListener() {
		listener = new TunerPortListener(operationParameter.getTuner()){

			@Override
			public void updateUIMax(final Object max) {
			}

			@Override
			public void updateUIMin(final Object min) {
			}

			@Override
			public void updateUIOptions(final List<?> options) {
					setChangeListenerEnable(false);
					DisplayManager.getDefault().asyncExec(new Runnable(){

						public void run() {
							setChangeListenerEnable(false);
							dropDownSelect.deselectAll();
							dropDownSelect.removeAll();
							if (options != null){
								int defaultSelectIndex = 0;
								for (Iterator<?> iterator = options.iterator(); iterator.hasNext();) {
									Object item = iterator.next();
									dropDownSelect.add(item.toString());
									if (operationParameter.getValue() != null && 
											operationParameter.getValue().toString().equals(
													item.toString()))
										defaultSelectIndex = options.indexOf(item);
								}
//								dropDownSelect.select(defaultSelectIndex);
							}
							operationParameter.setChanged(false);
							setChangeListenerEnable(true);
						}});									
			}

			@Override
			public void updateUIValue(final Object value) {
				if (value != null){
					setChangeListenerEnable(false);

					DisplayManager.getDefault().asyncExec(new Runnable(){

						public void run() {
							setChangeListenerEnable(false);
							List<?> options = operationParameter.getOptions();
							for (Iterator<?> iterator = options.iterator(); iterator.hasNext();) {
								Object item = iterator.next();
								if (value.toString().equals(item.toString())){
									final int selectIndex = options.indexOf(item);
//									dropDownSelect.select(selectIndex);
									dropDownSelect.setText(item.toString());
								}
							}
							operationParameter.setChanged(false);
							setChangeListenerEnable(true);
						}});				
				}
			}
		};
	}
	
	public void setSelection(Object value){
		List<?> options = operationParameter.getOptions();
		for (Iterator<?> iterator = options.iterator(); iterator.hasNext();) {
			Object item = iterator.next();
			if (value.equals(item)){
				final int selectIndex = options.indexOf(item);
				DisplayManager.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						dropDownSelect.select(selectIndex);
					}
				});
				break;
			}
		}
	}
}
