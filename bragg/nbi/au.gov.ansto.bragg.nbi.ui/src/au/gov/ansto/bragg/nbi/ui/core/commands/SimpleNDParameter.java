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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
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
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.slf4j.LoggerFactory;


/**
 * @author nxi
 * Created on 05/08/2009
 */
/**
 * @author nxi
 *
 */
/**
 * @author nxi
 *
 */
public class SimpleNDParameter extends AbstractScanParameter {

	private String scanVariable;
	private float startPosition;
	private float finishPosition;
	private double stepSize;
	private int numberOfPoints;
	private boolean doCreateFile = false;
	private boolean isLocked = false;
	private List<BlockName> changeSequence = new ArrayList<BlockName>();
//	private DragSource dragSource;
//	private Label dragLabel;
	
	private enum BlockName{START, FINISH, SIZE, POINTS};
	
	private float currentPosition;
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
		if (!isLocked){
			insertToChangeSequence(BlockName.START);
			logicCalculation();
		}
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
		if (!isLocked){
			insertToChangeSequence(BlockName.FINISH);
			logicCalculation();
		}
	}

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
		if (!isLocked){
			insertToChangeSequence(BlockName.POINTS);
			logicCalculation();
		}
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
	 * @return
	 */
	public double getStepSize() {
		return stepSize;
	}

	/**
	 * @param stepSize
	 */
	public void setStepSize(double stepSize) {
		double oldValue = this.stepSize;
		this.stepSize = stepSize;
		firePropertyChange("stepSize", oldValue, stepSize);
		if (!isLocked){
			insertToChangeSequence(BlockName.SIZE);
			logicCalculation();
		}
	}

	private void insertToChangeSequence(BlockName block){
		if (changeSequence.size() > 0 && changeSequence.get(0) == block)
			return;
		changeSequence.add(0, block);
//		if (changeSequence.size() > 3)
//			changeSequence.remove(3);
	}
	
	private void logicCalculation(){
		if ((stepSize == 0 || numberOfPoints <= 0) && startPosition == finishPosition)
			return;
		if (changeSequence.size() == 0)
			return;
		BlockName block = changeSequence.get(0);
		isLocked = true;
		switch (block) {
		case START:
			if (stepSize == 0){
				calculateStepSize();
			}else if (numberOfPoints <= 0)
				calculateNumberOfPoints();
			else{
				int finishIndex = changeSequence.indexOf(BlockName.FINISH);
				int sizeIndex = changeSequence.indexOf(BlockName.SIZE);
				int pointsIndex = changeSequence.indexOf(BlockName.POINTS);
				if (sizeIndex < 0)
					calculateStepSize();
				else if (pointsIndex < 0)
					calculateNumberOfPoints();
				else if (finishIndex < 0)
					calculateFinish();
				else if (sizeIndex >= finishIndex && sizeIndex >= pointsIndex)
					calculateStepSize();
				else if (pointsIndex >= finishIndex && pointsIndex >= sizeIndex)
					calculateNumberOfPoints();
				else if (finishIndex >= sizeIndex && finishIndex >= pointsIndex)
					calculateFinish();
			}
			break;
		case FINISH:
			if (stepSize == 0){
				calculateStepSize();
			}else if (numberOfPoints <= 1)
				calculateNumberOfPoints();
			if (startPosition < finishPosition && finishPosition - startPosition < stepSize || 
					startPosition > finishPosition && finishPosition - startPosition > stepSize){
				setStepSize(finishPosition - startPosition);
				setNumberOfPoints(2);
				break;
			}
			if (finishPosition == startPosition){
				setStepSize(0);
				setNumberOfPoints(1);
			}
			int sizeIndex = changeSequence.indexOf(BlockName.SIZE);
			int pointsIndex = changeSequence.indexOf(BlockName.POINTS);
			if (sizeIndex < 0)
				calculateStepSize();
			else if (pointsIndex < 0)
				calculateNumberOfPoints();
			else if (sizeIndex >= pointsIndex)
				calculateStepSize();
			else if (pointsIndex >= sizeIndex)
				calculateNumberOfPoints();
			break;
		case SIZE:
			if (stepSize == 0){
				break;
			}
			int finishIndex = changeSequence.indexOf(BlockName.FINISH);
			pointsIndex = changeSequence.indexOf(BlockName.POINTS);
			if (pointsIndex < 0)
				calculateNumberOfPoints();
			else if (finishIndex < 0)
				calculateFinish();
			else if (pointsIndex >= finishIndex)
				calculateNumberOfPoints();
			else if (finishIndex >= pointsIndex)
				calculateFinish();
			break;
		case POINTS:
			if (numberOfPoints <= 0)
				break;
			if (numberOfPoints == 1){
				setFinishPosition(startPosition);
				setStepSize(0);
			}
			finishIndex = changeSequence.indexOf(BlockName.FINISH);
			sizeIndex = changeSequence.indexOf(BlockName.SIZE);
			if (sizeIndex < 0)
				calculateStepSize();
			else if (finishIndex < 0)
				calculateFinish();
			else if (sizeIndex >= finishIndex)
				calculateStepSize();
			else if (finishIndex >= sizeIndex)
				calculateFinish();
			break;
		default:
			break;
		}
		isLocked = false;
	}
	
	private void calculateNumberOfPoints() {
		if (stepSize == 0)
			return;
		if (finishPosition > startPosition && stepSize < 0)
			return;
		if (finishPosition < startPosition && stepSize > 0)
			return;
		if (Math.abs(finishPosition - startPosition) < Math.abs(stepSize)){
			int sizeIndex = changeSequence.indexOf(BlockName.SIZE);
			int finishIndex = changeSequence.indexOf(BlockName.FINISH);
			int startIndex = changeSequence.indexOf(BlockName.START);
			if (sizeIndex < 0){
				setStepSize(finishPosition - startPosition);
			}else if (finishIndex < 0)
				setFinishPosition(startPosition + (float) stepSize);
			else if (startIndex < 0 && sizeIndex >= finishIndex)
				setStepSize(finishPosition - startPosition);
			else if (startIndex < 0 && sizeIndex < finishIndex)
				setFinishPosition(startPosition + (float) stepSize);
			else if (sizeIndex >= finishIndex || sizeIndex >= startIndex)
				setStepSize(finishPosition - startPosition);
			else
				setFinishPosition(startPosition + (float) stepSize);
			if (Math.abs(finishPosition - startPosition) < 1E-4)
				setNumberOfPoints(1);
			else
				setNumberOfPoints(2);
			return;
		}
		double points;
		if (Math.abs(finishPosition - startPosition) < 1E-4) {
			points = 1;
		} else {
			points = ((finishPosition - startPosition) / stepSize) + 1;
		}
		setNumberOfPoints((int) points);
		if (points - numberOfPoints > 1E-4){
			for (BlockName block : changeSequence){
				if (block == BlockName.FINISH){
					calculateStepSize();
					break;
				}else if (block == BlockName.SIZE){
					calculateFinish();
					break;
				}
			}
		}
	}

	private void calculateFinish() {
		if (stepSize == 0 || numberOfPoints <= 1)
			setFinishPosition(startPosition);
		else {
			setFinishPosition((float) (startPosition + stepSize * (numberOfPoints - 1)));
		}
	}

	private void calculateStepSize() {
		if (numberOfPoints <= 0){
			setStepSize(finishPosition - startPosition);
			if (stepSize < 1E-4) {
				setNumberOfPoints(1);
			} else {
				setNumberOfPoints(2);
			}
		}
		if (Math.abs(finishPosition - startPosition) < 1E-4){
			int startIndex = changeSequence.indexOf(BlockName.START);
			int finishIndex = changeSequence.indexOf(BlockName.FINISH);
			int pointsIndex = changeSequence.indexOf(BlockName.POINTS);
			int sizeIndex = changeSequence.indexOf(BlockName.SIZE);
			if ((startIndex >= 0 && startIndex < pointsIndex && startIndex < sizeIndex) || 
					(finishIndex >= 0 && finishIndex < pointsIndex && finishIndex < sizeIndex)){
				setStepSize(0);
				setNumberOfPoints(1);
			}
		}
		if (numberOfPoints == 1){
			if (Math.abs(finishPosition - startPosition) < 1E-4) {
				setNumberOfPoints(1);
				setStepSize(0);
			} else {
				setNumberOfPoints(2);
				setStepSize(finishPosition - startPosition);
			}
		}else{
			double size = (finishPosition - startPosition) / (numberOfPoints - 1);
			double floatSize;
			if (size > 0)
				floatSize = Math.nextAfter(size, size - 1);
			else
				floatSize = Math.nextAfter(size, size + 1);
			setStepSize(floatSize);
		}
	}

	/**
	 * 
	 */
	public SimpleNDParameter() {
		super();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#createParameterUI(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createParameterUI(final Composite parent, final AbstractScanCommandView commandView, 
			final FormToolkit toolkit) {
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(6, 4).numColumns(9).applyTo(parent);
//		GridLayoutFactory.swtDefaults().numColumns(6).applyTo(parent);
		Label dragLabel = toolkit.createLabel(parent, "\u2022");
		dragLabel.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		final ComboViewer scanVariableCombo = new ComboViewer(parent, SWT.READ_ONLY);
		scanVariableCombo.setContentProvider(new ArrayContentProvider());
		scanVariableCombo.setLabelProvider(new LabelProvider());
		scanVariableCombo.setSorter(new ViewerSorter());
		scanVariableCombo.setInput(SicsBatchUIUtils.getSicsDrivableIds());
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).hint(WIDTH_COMBO, SWT.DEFAULT).applyTo(scanVariableCombo.getCombo());
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(scanVariableCombo.getCombo());
//		GridDataFactory.swtDefaults().hint(WIDTH_COMBO, SWT.DEFAULT).applyTo(scanVariableCombo.getCombo());
		addSelectionValidator(scanVariableCombo.getCombo(), ParameterValidator.notEmptyValidator);

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

		final Text stepSizeBox = toolkit.createText(parent, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(stepSizeBox);
		addValidator(stepSizeBox, ParameterValidator.floatValidator);

		final Text nostepsText = toolkit.createText(parent, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(nostepsText);
		addValidator(nostepsText, ParameterValidator.integerValidator);

		final Button multiFileButton = toolkit.createButton(parent, "multiple_files", SWT.RADIO);
		GridDataFactory.swtDefaults().hint(80, SWT.DEFAULT).applyTo(multiFileButton);

		multiFileButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				firePropertyChange("multiple files", null, true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		startPositionText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent arg0) {
			}
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.keyCode == SWT.KEYPAD_CR || arg0.keyCode == SWT.CR)
					finishPositionText.setFocus();
			}
		});
		
		finishPositionText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent arg0) {
			}
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.keyCode == SWT.KEYPAD_CR || arg0.keyCode == SWT.CR)
					stepSizeBox.setFocus();
			}
		});

		stepSizeBox.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent arg0) {
			}
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.keyCode == SWT.KEYPAD_CR || arg0.keyCode == SWT.CR){
					nostepsText.setFocus();
				}
			}
		});
		
		nostepsText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent arg0) {
			}
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.keyCode == SWT.KEYPAD_CR || arg0.keyCode == SWT.CR){
					parent.setFocus();
					nostepsText.setFocus();
				}
			}
		});
		
		Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(ViewerProperties.singleSelection().observe(scanVariableCombo),
						BeanProperties.value("scanVariable").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.FocusOut).observe(startPositionText),
						BeanProperties.value("startPosition").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.FocusOut).observe(finishPositionText),
						BeanProperties.value("finishPosition").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.FocusOut).observe(stepSizeBox),
						BeanProperties.value("stepSize").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.FocusOut).observe(nostepsText),
						BeanProperties.value("numberOfPoints").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.buttonSelection().observe(multiFileButton),
						BeanProperties.value("doCreateFile").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});
		
		final AbstractScanCommand command = commandView.getCommand();
		
		final Button addButton = toolkit.createButton(parent, "", SWT.PUSH);
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
		
		final Button removeButton = toolkit.createButton(parent, "", SWT.PUSH);
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
		
//		initDnD(command, this);
//	}
//
//	private void initDnD(final AbstractScanCommand command, final AbstractScanParameter parameter) {
		int operations = DND.DROP_MOVE;
		DragSource dragSource = new DragSource(dragLabel, operations);

		LocalSelectionTransfer transferObject = LocalSelectionTransfer.getTransfer();

		Transfer[] types = new Transfer[] {transferObject};
		dragSource.setTransfer(types);
		final SimpleNDParameter child = this;
		dragSource.addDragListener(new DragSourceAdapter() {
			
			@Override
			public void dragFinished(DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(null);
			}
			@Override
			public void dragSetData(DragSourceEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
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
		SimpleNDParameter newParameter = new SimpleNDParameter();
//		newParameter.setScanVariable(scanVariable);
//		newParameter.setStartPosition(startPosition);
//		newParameter.setFinishPosition(finishPosition);
//		newParameter.setNumberOfSteps(numberOfSteps);
		command.insertParameter(command.indexOfParameter(this) + 1, newParameter);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#iterationGetNext()
	 */
	@Override
	public String iterationGetNext() {
		String script = "drive " + scanVariable + " " + currentPosition + "\n";
		if (doCreateFile)
			script += AbstractScanCommand.NEW_FILE_TEXT;
		if (finishPosition == startPosition)
			currentPosition = Float.NaN;
		else if (numberOfPoints == 1)
			currentPosition += finishPosition - startPosition;
		else
			currentPosition += (finishPosition - startPosition) / (numberOfPoints - 1);
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
			return currentPosition == startPosition;
		if (startPosition <= finishPosition)
			return currentPosition <= (finishPosition * (1 + (0.00001 * (finishPosition > 0 ? 1 : -1))));
		else
			return currentPosition >= (finishPosition * (1 - (0.00001 * (finishPosition > 0 ? 1 : -1))));
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#startIteration()
	 */
	@Override
	public void startIteration() {
		currentPosition = startPosition;
	}

	@Override
	public String toString() {
		return scanVariable + " " + startPosition + " " + finishPosition + " " + numberOfPoints;
	}

	@Override
	public String getForLoopHead(String indexName, String indent) {
		String script = indent + "for {set " + indexName + " 0} {$"+ indexName +" < " + numberOfPoints 
				+ "} {incr " + indexName + "} {\n";
		return script;
	}

	@Override
	public String getDriveScript(String indexName, String indent) {
		// TODO Auto-generated method stub
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
		String res = "";
		String multiFile = doCreateFile ? "multi-files" : "";
		res += scanVariable + "\t" + startPosition + "\t" + finishPosition +
				"\t" + (float) stepSize  + "\t" + numberOfPoints + "\t" + multiFile;
		return res;
	}
}
