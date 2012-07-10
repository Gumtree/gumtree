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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.process.port.TunerPortListener;

/**
 * The class represent Operation Parameter editor.
 * It implements observer for change notifications.
 * @author Danil Klimontov (dak)
 */
public abstract class OperationParameterEditor {
	protected OperationParameter operationParameter;
	protected Composite parentComposite;
	protected Label parameterLabel;
	protected int fieldWidth = 0;
	protected TunerPortListener listener;
	protected boolean isChangeListenerEnabled = true;

	
	protected final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
	private Object data; 
	
	
	private OperationParameter.OperationParameterListener operationParameterListener = new OperationParameter.OperationParameterListener(){
		public void serverDataUpdated(OperationParameter operationParameter, Object newData) {
			loadData();
		}
	};
	
	public OperationParameterEditor(OperationParameter operationParameter,
			Composite parentComposite) {
		super();
		setOperationParameter(operationParameter);
		this.parentComposite = parentComposite;
		initialize();
	}
	
	protected void initialize() {
		parameterLabel = new Label(parentComposite, SWT.NONE);
		parameterLabel.setText(operationParameter.getUILabel());

		Control editor = createEditor();
		editor.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
		
		GridData gridLayoutData = new GridData();
		gridLayoutData.horizontalAlignment = GridData.FILL;
//		if (fieldWidth == 0)
		gridLayoutData.grabExcessHorizontalSpace = true;
		gridLayoutData.horizontalIndent = 3;
		editor.setLayoutData (gridLayoutData);
		if (fieldWidth != 0)
			editor.setSize(fieldWidth, editor.getSize().y);
		initData();
//		loadData();
		initializeTunerListener();
		addVarListenerToTuner();
		if (operationParameter.getTuner().getUsage().equals("position"))
			addPointLocatorListener();
	}

	private void addPointLocatorListener() {
	}

	protected abstract void initializeTunerListener();
	
	protected void addVarListenerToTuner(){
		if (listener != null)
			operationParameter.addVarPortListener(listener);
	}

	protected void removeVarListenerFromTuner(){
		if (listener != null)
			operationParameter.removeVarPortListener(listener);
	}
	
	protected abstract Control createEditor();
	
	public abstract void loadData();
	
	public void initData() {
		loadData();
	}
	
	protected abstract Object getEditorData();
	
	protected void dataUpdated() {
		setData(getEditorData());
	}
		
	public void setOperationParameter(OperationParameter operationParameter) {
		if (this.operationParameter != null) {
			this.operationParameter.removeOperationParameterListener(operationParameterListener);
		}
		this.operationParameter = operationParameter;
		
		operationParameter.addOperationParameterListener(operationParameterListener);
	}
	
	public void setData(Object data) {
		Object oldData = this.data;
		this.data = data;
		operationParameter.setValue(data);
		if (isChangeListenerEnabled)
			fireChangeListeners(oldData, data);
		else
			isChangeListenerEnabled = true;
	}
	
	protected void dispose() {
		if (this.operationParameter != null) {
			this.operationParameter.removeOperationParameterListener(operationParameterListener);
			removeVarListenerFromTuner();
		}
	}
	
	public abstract void addApplyParameterListener(SelectionListener selectionListener);
	
	public void addChangeListener(ChangeListener changeListener) {
		changeListeners.add(changeListener);
	}
	public void removeChangeListener(ChangeListener changeListener) {
		changeListeners.remove(changeListener);
	}
	public void removeAllChangeListeners() {
		changeListeners.clear();
	}
	public List<ChangeListener> getChangeListeners() {
		return new ArrayList<ChangeListener>(changeListeners);
	}
	
	protected void fireChangeListeners(Object oldData, Object newData) {
		for (ChangeListener changeListener : changeListeners) {
			changeListener.dataChanged(oldData, newData);
		}
	}

	public interface ChangeListener {
		void dataChanged(Object oldData, Object newData);
		
	}
	
	protected void setChangeListenerEnable(boolean isEnabled){
		isChangeListenerEnabled = isEnabled;
	}
	
	public OperationParameter getOperationParameter(){
		return operationParameter;
	}
}
