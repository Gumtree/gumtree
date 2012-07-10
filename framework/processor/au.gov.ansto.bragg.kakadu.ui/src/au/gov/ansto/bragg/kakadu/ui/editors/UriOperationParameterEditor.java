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


import java.io.File;
import java.net.URI;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.process.port.TunerPortListener;


/**
 * Operation Parameter Editor for URI type of values.
 * Validation is applied.
 * 
 * @author Danil Klimontov (dak)
 */
public class UriOperationParameterEditor extends OperationParameterEditor implements ModifyListener {

	private static Color defaultTextColor, errorTextColor;
	private Text filenameText;
	private Button getURIButton;

	
	public UriOperationParameterEditor(OperationParameter operationParameter,
			Composite parentComposite) {
		super(operationParameter, parentComposite);
		
	}

	protected Control createEditor() {
		Composite editorComposite =	new Composite(parentComposite, SWT.NONE);
		
		if (defaultTextColor == null) {
			defaultTextColor = editorComposite.getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_FOREGROUND);
			errorTextColor = editorComposite.getDisplay().getSystemColor(
					SWT.COLOR_RED);
		}

		
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		editorComposite.setLayout(gridLayout);

		filenameText = new Text(editorComposite, SWT.BORDER);
//		filenameText.setEditable(false);

		//to register data modifications and validate
		filenameText.addModifyListener(this);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.widthHint = 55;
		filenameText.setLayoutData (data);

		getURIButton = new Button(editorComposite, SWT.PUSH);
		getURIButton.setText(">");
		getURIButton.setToolTipText("Click to select a nexus file from the file system");
		getURIButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
//				String filename = Util.saveFilenameFromShell(parentComposite.getShell(), "*.*", "All");
				String uriType = operationParameter.getProperty("uri_type");
				String filename = null;
				if (uriType == null)
					filename = Util.getFilenameFromShell(parentComposite.getShell(), "*.*", "All");
				else if (uriType.equals("folder"))
					filename = Util.selectDirectoryFromShell(parentComposite.getShell());
				else if (uriType.equals("save"))
					filename = Util.saveFilenameFromShell(parentComposite.getShell(), "*.*", "All");
				else if (uriType.equals("open"))
					filename = Util.getFilenameFromShell(parentComposite.getShell(), "*.*", "All");
				else 
					filename = Util.getFilenameFromShellNoCheck(parentComposite.getShell(), 
							new String[]{"*.*"}, new String[]{"All"});
				if (filename != null) {
					filenameText.setText(new File(filename).toURI().toString());
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		return editorComposite;
	}

	public void loadData() {
//		filenameText.setText(operationParameter.getValue() != null ? operationParameter.getValue().toString() : "");
		String currentText = filenameText.getText();
		String operationValue = operationParameter.getValue() != null ? operationParameter.getValue().toString() : "";
		if (!operationValue.equals(currentText))
			filenameText.setText(operationValue);

	}
	
	protected Object getEditorData() {
		String text = filenameText.getText();
		if (text == null || text.trim().length() == 0)
			return null;
		try {
			return new URI(text);
		} catch (Exception e) {
			return null;
		}
	}
	
	public void modifyText(ModifyEvent e) {
		if (validateValue())
			dataUpdated();
	}

	private boolean validateValue() {
		if (!isChangeListenerEnabled){
			isChangeListenerEnabled = true;
			return false;
		}
		try {
			final Object editorData = getEditorData();
			if (editorData == null)
				return true;
			File file = new File((URI) editorData);
			if (file.exists()){
				filenameText.setForeground(defaultTextColor);
				return true;
			}			
		} catch (Exception e) {
		}
		filenameText.setForeground(errorTextColor);
		return false;
	}

	public void addApplyParameterListener(SelectionListener selectionListener) {
		filenameText.addSelectionListener(selectionListener);
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
				
			}

			@Override
			public void updateUIValue(final Object value) {
				if (value == null || value instanceof URI)
					DisplayManager.getDefault().asyncExec(new Runnable(){

						public void run() {
							if (value == null && getEditorData() == null)
								return;
							if (value == null){
								setChangeListenerEnable(false);
								filenameText.setText("");
//								setChangeListenerEnable(true);
							}
							else if (getEditorData() == null){
								setChangeListenerEnable(false);
								filenameText.setText(value.toString());
//								setChangeListenerEnable(true);
							}
							else if (!value.toString().equals(getEditorData().toString())){
								setChangeListenerEnable(false);
								filenameText.setText(value.toString());
//								setChangeListenerEnable(true);
//								operationParameter.setChanged(false);
							}
						}});				
			}

		};
	}
	
	public void setValue(Object value){
		if (value == null && getEditorData() == null)
			return;
		if (value == null)
			filenameText.setText("");
		else if (getEditorData() == null || !value.toString().equals(getEditorData().toString()))
			filenameText.setText(value.toString());
	}
}
