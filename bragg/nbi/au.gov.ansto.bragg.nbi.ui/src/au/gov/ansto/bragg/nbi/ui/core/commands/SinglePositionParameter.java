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
package au.gov.ansto.bragg.nbi.ui.core.commands;

import java.io.FileNotFoundException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.slf4j.LoggerFactory;


/**
 * @author nxi
 * Created on 05/08/2009
 */
public class SinglePositionParameter extends AbstractScanParameter {

	private String scanVariable;
	private float position;
	
	private HmmscanParameter parentParameter;
//	private float currentPosition;
	/**
	 * @return the scanVariable
	 */
	public String getScanVariable() {
		return scanVariable;
	}

	/**
	 * @param scanVariable the scanVariable to set
	 */
	public void setScanVariable(String scanVariable) {
		String oldValue = this.scanVariable;
		this.scanVariable = scanVariable;
		firePropertyChange("scanVariable", oldValue, scanVariable);
	}

	/**
	 * @return the startPosition
	 */
	public float getPosition() {
		return position;
	}

	/**
	 * @param startPosition the startPosition to set
	 */
	public void setPosition(float position) {
		float oldValue = this.position;
		this.position = position;
		firePropertyChange("position", oldValue, position);
		
	}

	/**
	 * 
	 */
	public SinglePositionParameter(HmmscanParameter parentParameter) {
		super();
		this.parentParameter = parentParameter;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#createParameterUI(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createParameterUI(Composite parent, final AbstractScanCommandView commandView, 
			final FormToolkit toolkit) {
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(6, 4).numColumns(4).applyTo(parent);
//		GridLayoutFactory.swtDefaults().numColumns(6).applyTo(parent);
		final ComboViewer scanVariableCombo = new ComboViewer(parent, SWT.READ_ONLY);
		scanVariableCombo.setContentProvider(new ArrayContentProvider());
		scanVariableCombo.setLabelProvider(new LabelProvider());
		scanVariableCombo.setSorter(new ViewerSorter());
		String[] ids = SicsBatchUIUtils.getSicsDrivableIds();
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] == null) {
				ids[i] = "";
			}
		}
		scanVariableCombo.setInput(ids);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).hint(WIDTH_COMBO, SWT.DEFAULT).applyTo(scanVariableCombo.getCombo());
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(scanVariableCombo.getCombo());
		
//		GridDataFactory.swtDefaults().hint(WIDTH_COMBO, SWT.DEFAULT).applyTo(scanVariableCombo.getCombo());

		final Text positionText = toolkit.createText(parent, "");
//		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER, SWT.DEFAULT).applyTo(startPositionText);
//		GridData data = new GridData();
//		data.grabExcessHorizontalSpace = true;
//		data.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
//		data.heightHint = SWT.DEFAULT;
//		startPositionText.setLayoutData(data);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(positionText);
		addValidator(positionText, ParameterValidator.floatValidator);
		
		Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(ViewerProperties.singleSelection().observe(scanVariableCombo),
						BeanProperties.value("scanVariable").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(positionText),
						BeanProperties.value("position").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});
		
		final AbstractScanCommand command = commandView.getCommand();
		
		Button addButton = toolkit.createButton(parent, "", SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(18, 18).applyTo(addButton);
		try {
			addButton.setImage(SicsBatchUIUtils.getBatchEditorImage("ADD"));
		} catch (FileNotFoundException e2) {
			LoggerFactory.getLogger(this.getClass()).error("can not find ADD image", e2);
		}
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addNewParameter(command);
				commandView.refreshParameterComposite();
//				notifyPropertyChanged(newCommand, null);
			}
		});
		
		Button removeButton = toolkit.createButton(parent, "", SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(18, 18).applyTo(removeButton);
		try {
			removeButton.setImage(SicsBatchUIUtils.getBatchEditorImage("REMOVE"));
		} catch (FileNotFoundException e1) {
			LoggerFactory.getLogger(this.getClass()).error("can not find REMOVE image", e1);
		}
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeParameter(command);
				commandView.refreshParameterComposite();
			}
		});
	}

	@Override
	protected void removeParameter(AbstractScanCommand command) {
		parentParameter.removeSigleParameter(this);
	}
	
	@Override
	protected void addNewParameter(AbstractScanCommand command){
		SinglePositionParameter newParameter = new SinglePositionParameter(parentParameter);
		parentParameter.insertSiglePositionParameter(parentParameter.indexOfSinglePositionParameter(this) + 1, 
				newParameter);
//		newParameter.setScanVariable(scanVariable);
//		newParameter.setStartPosition(startPosition);
//		newParameter.setFinishPosition(finishPosition);
//		newParameter.setNumberOfSteps(numberOfSteps);
//		command.insertParameter(command.indexOfParameter(this) + 1, newParameter);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#iterationGetNext()
	 */
	@Override
	public String iterationGetNext() {
		return "";
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#iterationHasNext()
	 */
	@Override
	public boolean iterationHasNext() {
		return false;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#startIteration()
	 */
	@Override
	public void startIteration() {
	}

	@Override
	public String toString() {
		return scanVariable + " " + position;
	}

	public String getSicsScript() {
		if (scanVariable != null && scanVariable.trim().length() > 0)
			return "drive " + scanVariable + " " + position;
		else 
			return "";
	}
	
	@Override
	public int getNumberOfPoints() {
		return 1;
	}

	@Override
	public String getDriveScript(String indexName, String indent) {
		return indent + "drive " + scanVariable + " " + ((float) position) + "\n";
	}
	
	@Override
	public String getBroadcastScript(String indexName, String indent) {
		return indent + "broadcast " + scanVariable + " = " + ((float) position) + "\n";
	}
	
	@Override
	public String getPritable(boolean isFirstLine) {
		return scanVariable + "\t" + position;
	}
}
