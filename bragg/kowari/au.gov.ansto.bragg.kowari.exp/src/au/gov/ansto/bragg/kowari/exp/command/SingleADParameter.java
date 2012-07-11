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
package au.gov.ansto.bragg.kowari.exp.command;

import java.io.FileNotFoundException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.kowari.exp.commandView.AbstractScanCommandView;
import au.gov.ansto.bragg.kowari.exp.commandView.DndTransferData;

/**
 * @author nxi
 * Created on 05/08/2009
 */
public class SingleADParameter extends AbstractScanParameter {

	private String scanVariable;
	private float startPosition;
	private float finishPosition;
	private float stepSize;
	private boolean isLocked = false;
	
	private AdvancedParameter parentParameter;
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
	public float getStartPosition() {
		return startPosition;
	}

	/**
	 * @param startPosition the startPosition to set
	 */
	public void setStartPosition(float startPosition) {
		float oldValue = this.startPosition;
		this.startPosition = startPosition;
		firePropertyChange("startPosition", oldValue, startPosition);
		if (!isLocked)
			calculateStepSize();
	}

	/**
	 * @return the finishPosition
	 */
	public float getFinishPosition() {
		return finishPosition;
	}

	/**
	 * @param finishPosition the finishPosition to set
	 */
	public void setFinishPosition(float finishPosition) {
		float oldValue = this.finishPosition;
		this.finishPosition = finishPosition;
		firePropertyChange("finishPosition", oldValue, finishPosition);
		if (!isLocked)
			calculateStepSize();
	}

	
	public float getStepSize() {
		return stepSize;
	}

	public void setStepSize(float stepSize) {
		float oldValue = this.stepSize;
		this.stepSize = stepSize;
		firePropertyChange("stepSize", oldValue, stepSize);
		if (!isLocked)
			calculateFinishPosition();
	}

	private void calculateFinishPosition() {
		float numberOfPoints = parentParameter.getNumberOfPoints();
		isLocked = true;
		if (numberOfPoints <= 2)
			setFinishPosition(startPosition + stepSize);
		else
			setFinishPosition(startPosition + stepSize * (numberOfPoints - 1));
		isLocked = false;
	}

	public void calculateStepSize() {
		float numberOfPoints = parentParameter.getNumberOfPoints();
		isLocked = true;
		if (numberOfPoints <= 2)
			setStepSize(finishPosition - startPosition);
		else
			setStepSize((finishPosition - startPosition) / (numberOfPoints - 1));
		isLocked = false;
	}

	/**
	 * 
	 */
	public SingleADParameter(AdvancedParameter parentParameter) {
		super();
		this.parentParameter = parentParameter;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#createParameterUI(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createParameterUI(Composite parent, final AbstractScanCommandView commandView, 
			final FormToolkit toolkit) {
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(6, 4).numColumns(7).applyTo(parent);
//		GridLayoutFactory.swtDefaults().numColumns(6).applyTo(parent);
		final Label dragLabel = toolkit.createLabel(parent, "\u2022");
		dragLabel.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		final ComboViewer scanVariableCombo = new ComboViewer(parent, SWT.READ_ONLY);
		scanVariableCombo.setContentProvider(new ArrayContentProvider());
		scanVariableCombo.setLabelProvider(new LabelProvider());
		scanVariableCombo.setSorter(new ViewerSorter());
		scanVariableCombo.setInput(SicsBatchUIUtils.getSicsDrivableIds());
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).hint(WIDTH_COMBO, SWT.DEFAULT).applyTo(scanVariableCombo.getCombo());
		addSelectionValidator(scanVariableCombo.getCombo(), ParameterValidator.notEmptyValidator);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(scanVariableCombo.getCombo());
		
//		GridDataFactory.swtDefaults().hint(WIDTH_COMBO, SWT.DEFAULT).applyTo(scanVariableCombo.getCombo());

		final Text startPositionText = toolkit.createText(parent, "");
//		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER, SWT.DEFAULT).applyTo(startPositionText);
//		GridData data = new GridData();
//		data.grabExcessHorizontalSpace = true;
//		data.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
//		data.heightHint = SWT.DEFAULT;
//		startPositionText.setLayoutData(data);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(startPositionText);
		addValidator(startPositionText, ParameterValidator.floatValidator);
		
		final Text finishPositionText = toolkit.createText(parent, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(finishPositionText);
		addValidator(finishPositionText, ParameterValidator.floatValidator);

		final Text stepSizeText = toolkit.createText(parent, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(stepSizeText);
		addValidator(stepSizeText, ParameterValidator.floatValidator);

		Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(ViewersObservables.observeSingleSelection(scanVariableCombo),
						BeansObservables.observeValue(getInstance(), "scanVariable"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(startPositionText, SWT.Modify),
						BeansObservables.observeValue(getInstance(), "startPosition"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(finishPositionText, SWT.Modify),
						BeansObservables.observeValue(getInstance(), "finishPosition"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(stepSizeText, SWT.Modify),
						BeansObservables.observeValue(getInstance(), "stepSize"),
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
		
		DragSource dragSource = new DragSource(dragLabel, DND.DROP_MOVE);
		dragSource.setTransfer(new Transfer[]{LocalSelectionTransfer.getTransfer()});
		final SingleADParameter child = this;
		dragSource.addDragListener(new DragSourceAdapter(){
			@Override
			public void dragFinished(DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(null);
			}
			
			@Override
			public void dragSetData(DragSourceEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(
						event.dataType)){
					DndTransferData transferData = new DndTransferData();
					transferData.setParent(parentParameter);
					transferData.setChild(child);
					LocalSelectionTransfer.getTransfer().setSelection(
							new StructuredSelection(transferData));
				}
			}
		});
	}

	@Override
	protected void removeParameter(AbstractScanCommand command) {
		parentParameter.removeSigleParameter(this);
	}
	
	@Override
	protected void addNewParameter(AbstractScanCommand command){
		SingleADParameter newParameter = new SingleADParameter(parentParameter);
		parentParameter.insertSigleADParameter(parentParameter.indexOfSingleADParameter(this) + 1, newParameter);
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
		return scanVariable + " " + startPosition + " " + finishPosition;
	}

	public String iterationGetPoint(int pointID, int numberOfPoints) {
		if (startPosition == finishPosition)
			return "drive " + scanVariable + " " + startPosition;
		if (numberOfPoints == 0)
			return "";
		float position = startPosition;
		if (numberOfPoints == 1){
			if (pointID == 0)
				position = startPosition;
			else if (pointID == 1)
				position = finishPosition;
			else 
				return "";
		}else{
			if (pointID >= numberOfPoints)
				return "";
			position = startPosition + (finishPosition - startPosition) / (numberOfPoints - 1) * pointID;
		}
		return "drive " + scanVariable + " " + position;
	}
	
	@Override
	public int getNumberOfPoints() {
		return 0;
	}

	@Override
	public String getDriveScript(String indexName, String indent) {
		return indent + "drive " + scanVariable + " " + "[expr $" + indexName + "*"
			+ ((float) stepSize) + "+" + ((float)startPosition) + "]\n";
	}
	
	@Override
	public String getBroadcastScript(String indexName, String indent) {
		return indent + "broadcast " + scanVariable + " = " + "[expr $" + indexName + "*"
			+ ((float) stepSize) + "+" + ((float)startPosition) + "]\n";
	}
	
	@Override
	public String getPritable(boolean isFirstLine) {
		return scanVariable + "\t" + String.valueOf(startPosition) + "\t" +
				String.valueOf(finishPosition) + "\t" + stepSize;
	}
}
