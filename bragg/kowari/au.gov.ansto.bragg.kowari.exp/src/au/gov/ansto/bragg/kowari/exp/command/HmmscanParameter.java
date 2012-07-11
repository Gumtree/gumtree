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

import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.kowari.exp.commandView.AbstractScanCommandView;
import au.gov.ansto.bragg.kowari.exp.commandView.DndTransferData;

/**
 * @author nxi
 * Created on 05/08/2009
 */
public class HmmscanParameter extends AbstractScanParameter {

	protected List<SinglePositionParameter> parameters;

	/**
	 * 
	 */
	public HmmscanParameter() {
		super();
		parameters = new ArrayList<SinglePositionParameter>();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#createParameterUI(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createParameterUI(Composite parent, final AbstractScanCommandView commandView, 
			final FormToolkit toolkit) {
		Group parameterGroup = new Group(parent, SWT.NULL);
//		toolkit.adapt(parameterGroup);
//		parameterGroup.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		parameterGroup.setBackground(parent.getBackground());
		parameterGroup.setText("Position-" + commandView.getCommand().indexOfParameter(this));

//		GridLayoutFactory.swtDefaults().spacing(6, 0).numColumns(5).applyTo(parameterGroup);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(parameterGroup);
		
//		Composite singleParameterComposite = toolkit.createComposite(parent);
//		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(6, 4).numColumns(5).applyTo(singleParameterComposite);
		if (parameters.size() == 0){
			SinglePositionParameter parameter = new SinglePositionParameter(this);
			parameter.setScanVariable("sx");
			parameters.add(parameter);
		}
		
		for (SinglePositionParameter parameter : parameters){
			parameter.createParameterUI(parameterGroup, commandView, toolkit);
		}
		
		final AbstractScanCommand command = commandView.getCommand();
		
		Button addButton = toolkit.createButton(parent, "", SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).indent(0, 10).hint(24, 24).applyTo(addButton);
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
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).indent(0, 10).hint(24, 24).applyTo(removeButton);
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
		
		DragSource dragSource = new DragSource(parameterGroup, DND.DROP_MOVE);
		final HmmscanParameter child = this;
		dragSource.setTransfer(new Transfer[]{LocalSelectionTransfer.getTransfer()});
		dragSource.addDragListener(new DragSourceAdapter(){
			@Override
			public void dragFinished(DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(null);
			}
			
			@Override
			public void dragSetData(DragSourceEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(
						event.dataType)) {
					DndTransferData transferData = new DndTransferData();
					transferData.setParent(command);
					transferData.setChild(child);
					LocalSelectionTransfer.getTransfer().setSelection(
							new StructuredSelection(transferData));
				}
			}
		});
	}

	@Override
	protected void addNewParameter(AbstractScanCommand command){
		HmmscanParameter newParameter = new HmmscanParameter();
		int index = 0;
		for (SinglePositionParameter parameter : parameters){
			SinglePositionParameter newSingleParameter = new SinglePositionParameter(newParameter);
			newSingleParameter.setScanVariable(parameter.getScanVariable());
			newParameter.insertSiglePositionParameter(index, newSingleParameter);
			index ++;
		}
//		newParameter.setScanVariable(scanVariable);
//		newParameter.setStartPosition(startPosition);
//		newParameter.setFinishPosition(finishPosition);
//		newParameter.setNumberOfSteps(numberOfSteps);
		command.insertParameter(command.indexOfParameter(this) + 1, newParameter);
	}

	public void insertSiglePositionParameter(int index, SinglePositionParameter parameter){
		parameters.add(index, parameter);
		firePropertyChange("parameter_add", null, parameter);
	}
	
	public int indexOfSinglePositionParameter(SinglePositionParameter parameter){
		return parameters.indexOf(parameter);
	}
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#iterationGetNext()
	 */
	@Override
	public String iterationGetNext() {
//		String script = "drive " + scanVariable + " " + currentPosition + "\n";
//		if (finishPosition == startPosition)
//			currentPosition = Float.NaN;
//		else if (numberOfPoints == 1)
//			currentPosition += finishPosition - startPosition;
//		else
//			currentPosition += (finishPosition - startPosition) / (numberOfPoints - 1);
		String script = "";
		for (SinglePositionParameter parameter : parameters){
			script += parameter.getSicsScript() + "\n";
		}
		return script;
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
		String string = "";
		for (SinglePositionParameter parameter : parameters){
			string += "<" + parameter.toString() + ">";
		}
		return string;
	}

	public void removeSigleParameter(SinglePositionParameter singlePositionParameter) {
		if (parameters != null && parameters.size() > 1)
			parameters.remove(singlePositionParameter);
	}

	@Override
	public int getNumberOfPoints() {
		return 1;
	}

	public List<String> getScanVariables() {
		List<String> scanVariables = new ArrayList<String>();
		for (SinglePositionParameter parameter : parameters)
			scanVariables.add(parameter.getScanVariable());
		return scanVariables;
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		super.addPropertyChangeListener(listener);
		if (parameters != null)
			for (SinglePositionParameter parameter : parameters)
				parameter.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		super.removePropertyChangeListener(listener);
		if (parameters != null)
			for (SinglePositionParameter parameter : parameters)
				parameter.removePropertyChangeListener(listener);
	}

	@Override
	public String getDriveScript(String indexName, String indent) {
		String script = "";
		for (AbstractScanParameter parameter : parameters) {
			script += parameter.getDriveScript(indexName, indent);
		}
		return script;
	}
	
	@Override
	public String getBroadcastScript(String indexName, String indent) {
		String script = "";
		for (AbstractScanParameter parameter : parameters) {
			script += parameter.getBroadcastScript(indexName, indent);
		}
		return script;
	}
	
	@Override
	public String getPritable(boolean isFirstLine) {
		String text = "";
		if (isFirstLine) {
			SinglePositionParameter parameter = parameters.get(0);
			text += parameter.getPritable(isFirstLine); 
		} else {
			for (int i = 1; i < parameters.size(); i++) {
				text += parameters.get(i).getPritable(false) + "\n";
			}
		}
		return text;
	}
	
	public List<SinglePositionParameter> getPositionParameters() {
		return parameters;
	}
}
