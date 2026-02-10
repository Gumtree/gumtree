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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
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
public class AdvancedParameter extends AbstractScanParameter {

	protected List<SingleADParameter> parameters;
	private int numberOfPoints;
	private boolean doCreateFile = false;
	
	private int currentPosition;

	/**
	 * @return the numberOfPositions
	 */
	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	/**
	 * @param numberOfPositions the numberOfPositions to set
	 */
	public void setNumberOfPoints(int numberOfPoints) {
		int oldValue = this.numberOfPoints;
		this.numberOfPoints = numberOfPoints;
		firePropertyChange("numberOfPoints", oldValue, numberOfPoints);
		for (SingleADParameter parameter : parameters)
			parameter.calculateStepSize();
	}

	/**
	 * @return the doCreateFile
	 */
	public boolean getDoCreateFile() {
		return doCreateFile;
	}

	/**
	 * @param doCreateFile the doCreateFile to set
	 */
	public void setDoCreateFile(boolean doCreateFile) {
		boolean oldValue = this.doCreateFile;
		this.doCreateFile = doCreateFile;
		firePropertyChange("doCreateFile", oldValue, doCreateFile);
	}

	/**
	 * 
	 */
	public AdvancedParameter() {
		super();
		parameters = new ArrayList<SingleADParameter>();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#createParameterUI(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createParameterUI(final Composite parent, 
			final AbstractScanCommandView commandView, 
			final FormToolkit toolkit) {
		final Group parameterGroup = new Group(parent, SWT.NULL);
//		toolkit.adapt(parameterGroup);
//		parameterGroup.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		parameterGroup.setBackground(parent.getBackground());
		parameterGroup.setText("Dimension-" + commandView.getCommand().indexOfParameter(this));

//		GridLayoutFactory.swtDefaults().spacing(6, 0).numColumns(5).applyTo(parameterGroup);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(parameterGroup);
		
//		Composite singleParameterComposite = toolkit.createComposite(parent);
//		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(6, 4).numColumns(5).applyTo(singleParameterComposite);
		if (parameters.size() == 0){
			SingleADParameter parameter = new SingleADParameter(this);
			parameter.setScanVariable("sx");
			parameters.add(parameter);
		}
		for (SingleADParameter parameter : parameters){
			parameter.createParameterUI(parameterGroup, commandView, toolkit);
		}
		
		final Text numberOfPointsText = toolkit.createText(parent, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).indent(0, 12).hint(WIDTH_PARAMETER_LONG, 
				SWT.DEFAULT).applyTo(numberOfPointsText);
		addValidator(numberOfPointsText, ParameterValidator.integerValidator);

		final Button multiFileButton = toolkit.createButton(parent, "multiple_files", SWT.RADIO);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).indent(0, 12).hint(80, SWT.DEFAULT).applyTo(multiFileButton);

		multiFileButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				firePropertyChange("multiple files", null, true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		Realm.runWithDefault(/*SWTObservables.getRealm(Display.getDefault())*/ DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(/*SWTObservables.observeText(numberOfPointsText, SWT.Modify)*/ WidgetProperties.text(SWT.Modify).observe(numberOfPointsText),
						/*BeansObservables.observeValue(getInstance(), "numberOfPoints")*/ BeanProperties.value("numberOfPoints").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(/*SWTObservables.observeSelection(multiFileButton)*/ WidgetProperties.buttonSelection().observe(multiFileButton),
						/*BeansObservables.observeValue(getInstance(), "doCreateFile")*/ BeanProperties.value("doCreateFile").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});

		final AbstractScanCommand command = commandView.getCommand();
		
		Button addButton = toolkit.createButton(parent, "", SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).indent(0, 9).hint(24, 24).applyTo(addButton);
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
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).indent(0, 9).hint(24, 24).applyTo(removeButton);
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
		dragSource.setTransfer(new Transfer[]{LocalSelectionTransfer.getTransfer()});
		final AdvancedParameter parameter = this;
		dragSource.addDragListener(new DragSourceAdapter(){
			
			@Override
			public void dragFinished(DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(null);
			}
			
			@Override
			public void dragSetData(DragSourceEvent event) {
				if (LocalSelectionTransfer.getTransfer(
						).isSupportedType(event.dataType)){
					DndTransferData transferData = new DndTransferData();
					transferData.setParent(command);
					transferData.setChild(parameter);
					LocalSelectionTransfer.getTransfer().setSelection(
							new StructuredSelection(transferData));
				}
			}
		});
		
		DropTarget dropTarget = new DropTarget(parameterGroup, DND.DROP_MOVE);
		final AdvancedParameter parentParameter = this;
		dropTarget.setTransfer(new Transfer[]{LocalSelectionTransfer.getTransfer()});
		dropTarget.addDropListener(new DropTargetAdapter(){
			@Override
			public void drop(DropTargetEvent event) {
				if (event.data instanceof StructuredSelection) {
					DndTransferData transferData = (DndTransferData) (
							(StructuredSelection) event.data).getFirstElement();
					Object parentData = transferData.getParent();
					Object child = transferData.getChild();
					if (parentData == parentParameter) {
						Point relativePoint = parameterGroup.toControl(
								new Point(event.x, event.y));
						int index = 0;
						for (Control control : parameterGroup.getChildren()) {
							if (control instanceof Combo) {
								if (relativePoint.y < control.getBounds().y 
										+ control.getBounds().height) {
									break;
								}
								index++;
							}
						}
						if (child instanceof SingleADParameter) {
							SingleADParameter parameter = (SingleADParameter) child;
							int currentIndex = indexOfSingleADParameter(parameter);
							if (currentIndex == index) {
								return;
							}
							removeSigleParameter(parameter);
							insertSigleADParameter(index, parameter);
							commandView.refreshParameterComposite();
						}
					} else if (parentData == command) {
						Point relativePoint = parent.toControl(
								new Point(event.x, event.y));
						int index = 0;
						for (Control control : parent.getChildren()){
							if (control instanceof Group){
								if (relativePoint.y < (control.getBounds().y 
										+ control.getBounds().height)) {
									break;
								}
								index++;
							}
						}
						if (child instanceof AdvancedParameter){
							AdvancedParameter parameter = (AdvancedParameter) child;
							int currentIndex = command.indexOfParameter(parameter);
							if (currentIndex == index) {
								return;
							}
							command.removeParameter(parameter);
							command.insertParameter(index, parameter);
							commandView.refreshParameterComposite();
						}
					}
				}
			}
		});
	}

	@Override
	protected void addNewParameter(AbstractScanCommand command){
		AdvancedParameter newParameter = new AdvancedParameter();
//		SingleADParameter singleParameter = new SingleADParameter(this);
//		newParameter.insertSigleADParameter(0, singleParameter);
//		newParameter.setScanVariable(scanVariable);
//		newParameter.setStartPosition(startPosition);
//		newParameter.setFinishPosition(finishPosition);
//		newParameter.setNumberOfSteps(numberOfSteps);
		command.insertParameter(command.indexOfParameter(this) + 1, newParameter);
	}

	public void insertSigleADParameter(int index, SingleADParameter parameter){
		parameters.add(index, parameter);
		firePropertyChange("parameter_add", null, parameter);
	}
	
	public int indexOfSingleADParameter(SingleADParameter parameter){
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
		if (parameters.size() == 0 || numberOfPoints == 0)
			return "";
		if (currentPosition >= numberOfPoints)
			return "";
		String script = "";
		for (SingleADParameter parameter : parameters){
			script += parameter.iterationGetPoint(currentPosition, numberOfPoints) + "\n";
		}
		if (doCreateFile)
			script += AbstractScanCommand.NEW_FILE_TEXT;
		currentPosition ++;
		return script;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#iterationHasNext()
	 */
	@Override
	public boolean iterationHasNext() {
		if (numberOfPoints <= 0)
			return false;
		if (numberOfPoints == 1)
			return currentPosition == 0;
		if (currentPosition >= numberOfPoints)
			return false;
		else
			return true;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#startIteration()
	 */
	@Override
	public void startIteration() {
		currentPosition = 0;
	}

	@Override
	public String toString() {
		String string = "";
		if (parameters.size() == 0)
			return string;
		if (parameters.size() == 1)
			return parameters.get(0).toString() + " " + numberOfPoints;
		String variables = "<";
		String starts = "<";
		String finishes = "<";
		for (SingleADParameter parameter : parameters){
//			string += "<" + parameter.toString() + ">";
			variables += parameter.getScanVariable() + ",";
			starts += parameter.getStartPosition() + ",";
			finishes += parameter.getFinishPosition() + ",";
		}
		variables = variables.substring(0, variables.length() - 1) + ">";
		starts = starts.substring(0, starts.length() - 1) + ">";
		finishes = finishes.substring(0, finishes.length() - 1) + ">";
		return string + variables + " " + starts + " " + finishes + " " + numberOfPoints;
	}

	public void removeSigleParameter(SingleADParameter singleADParameter) {
		if (parameters != null && parameters.size() > 1) {
			parameters.remove(singleADParameter);
			firePropertyChange("parameter_remove", null, singleADParameter);
		}
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		super.addPropertyChangeListener(listener);
		if (parameters != null)
			for (SingleADParameter parameter : parameters)
				parameter.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		super.removePropertyChangeListener(listener);
		if (parameters != null)
			for (SingleADParameter parameter : parameters)
				parameter.removePropertyChangeListener(listener);
	}
	
	@Override
	public String getForLoopHead(String indexName, String indent) {
		String script = indent + "for {set " + indexName + " 0} {$"+ indexName +" < " + numberOfPoints 
				+ "} {incr " + indexName + "} {\n";
//		indent += "\t";
//		if (doCreateFile) {
//			script += indent + AbstractScanCommand.NEW_FILE_TEXT;
//			script += indent + "set savenumber 0\n";
//		}
		return script;
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
		SingleADParameter p1 = parameters.get(0);
		if (isFirstLine) {
			text += p1.getPritable(isFirstLine) + "\t" + numberOfPoints + "\t" 
					+ (doCreateFile ? "multiple_files" : "");
		} else {
			if (parameters.size() > 1) {
				for (int i = 1; i < parameters.size(); i++) {
					text += parameters.get(i).getPritable(isFirstLine) + "\n";
				}
			}
		}
		return text;
	}
	
	public List<SingleADParameter> getADParameters() {
		return parameters;
	}
}