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
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.datastructures.core.exception.IndexOutOfBoundException;


/**
 * @author nxi
 * Created on 05/08/2009
 */
public class TableScanParameter extends AbstractScanParameter {

	private SimpleTableScanCommand command;
	private boolean isSelected;
	private int length;
	private int index;
	private float preset;
	private float p0;
	private float p1;
	private float p2;
	private float p3;
	private float p4;
	private float p5;
	private float p6;
	private float p7;
	private float p8;
	private float p9;
	private List<String> pNames;
	
	public TableScanParameter() {
		super();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		float oldValue = this.index;
		this.index = index;
		firePropertyChange("index", oldValue, index);
	}

	public float getPreset() {
		return preset;
	}

	public void setPreset(float preset) {
		float oldValue = this.preset;
		this.preset = preset;
		firePropertyChange("preset", oldValue, preset);
	}

	public float getP0() {
		return p0;
	}

	public void setP0(float p0) {
		float oldValue = this.p0;
		this.p0 = p0;
		firePropertyChange("p0", oldValue, p0);
	}

	public float getP1() {
		return p1;
	}

	public void setP1(float p1) {
		float oldValue = this.p1;
		this.p1 = p1;
		firePropertyChange("p1", oldValue, p1);
	}

	public float getP2() {
		return p2;
	}

	public void setP2(float p2) {
		float oldValue = this.p2;
		this.p2 = p2;
		firePropertyChange("p2", oldValue, p2);
	}

	public float getP3() {
		return p3;
	}

	public void setP3(float p3) {
		float oldValue = this.p3;
		this.p3 = p3;
		firePropertyChange("p3", oldValue, p3);
	}

	public float getP4() {
		return p4;
	}

	public void setP4(float p4) {
		float oldValue = this.p4;
		this.p4 = p4;
		firePropertyChange("p4", oldValue, p4);
	}

	public float getP5() {
		return p5;
	}

	public void setP5(float p5) {
		float oldValue = this.p5;
		this.p5 = p5;
		firePropertyChange("p5", oldValue, p5);
	}

	public float getP6() {
		return p6;
	}

	public void setP6(float p6) {
		float oldValue = this.p6;
		this.p6 = p6;
		firePropertyChange("p6", oldValue, p6);
	}

	public float getP7() {
		return p7;
	}

	public void setP7(float p7) {
		float oldValue = this.p7;
		this.p7 = p7;
		firePropertyChange("p7", oldValue, p7);
	}

	public float getP8() {
		return p8;
	}

	public void setP8(float p8) {
		float oldValue = this.p8;
		this.p8 = p8;
		firePropertyChange("p8", oldValue, p8);
	}

	public float getP9() {
		return p9;
	}

	public void setP9(float p9) {
		float oldValue = this.p9;
		this.p9 = p9;
		firePropertyChange("p9", oldValue, p9);
	}

	public boolean getIsSelected() {
		return isSelected;
	}

	public void setIsSelected(boolean isSelected) {
		boolean oldValue = this.isSelected;
		this.isSelected = isSelected;
		firePropertyChange("isSelected", oldValue, isSelected);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#createParameterUI(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createParameterUI(final Composite parameterComposite, final AbstractScanCommandView commandView, 
			final FormToolkit toolkit) {
//			final Label dragLabel = toolkit.createLabel(parent, "\u2022");
//		parameterComposite = toolkit.createComposite(parent);
//		parameterComposite = new Composite(parent, SWT.NONE);
		final Label dragLabel = toolkit.createLabel(parameterComposite, "\u2022");
		dragLabel.setCursor(parameterComposite.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

		final Button selectBox = toolkit.createButton(parameterComposite, "", SWT.CHECK);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).indent(0, 2).applyTo(selectBox);

		selectBox.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.err.println("clicked");
				Control[] children = parameterComposite.getChildren();
				for (Control child : children) {
					if (child instanceof Text) {
						child.setEnabled(selectBox.getSelection());
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		for (int i = 0; i < getLength(); i++) {
			final String name = "p" + i;
			final Text pText = toolkit.createText(parameterComposite, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(pText);
			addValidator(pText, ParameterValidator.floatValidator);
			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(SWTObservables.observeText(pText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), name),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
		}

		final Text presetText = toolkit.createText(parameterComposite, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(presetText);
		addValidator(presetText, ParameterValidator.floatValidator);

		Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeSelection(selectBox),
						BeansObservables.observeValue(getInstance(), "isSelected"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(presetText, SWT.Modify),
						BeansObservables.observeValue(getInstance(), "preset"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});
		
		Button addButton = toolkit.createButton(parameterComposite, "", SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).indent(0, 0).applyTo(addButton);
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
		
		Button removeButton = toolkit.createButton(parameterComposite, "", SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).indent(0, 0).applyTo(removeButton);
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

		LocalSelectionTransfer transferObject = LocalSelectionTransfer.getTransfer();

		Transfer[] types = new Transfer[] {transferObject};
		dragSource.setTransfer(types);
		final TableScanParameter child = this;
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

	public float getP(int i) {
		if (i > 9) {
			i = 9;
		}
		if (i < 0) {
			i = 0;
		}
		switch (i) {
		case 0:
			return p0;
		case 1:
			return p1;
		case 2:
			return p2;
		case 3:
			return p3;
		case 4:
			return p4;
		case 5:
			return p5;
		case 6:
			return p6;
		case 7:
			return p7;
		case 8:
			return p8;
		case 9:
			return p9;
		default:
			return 0;
		}
	}
	
	@Override
	public String toString() {
		String text = "";
		for (int i = 0; i < getLength(); i++) {
			text += pNames.get(i) + " ";
		}
		text += "preset\n";
		for (int i = 0; i < getLength(); i++) {
			text += getP(i) + " ";
		}
		return text + " " + preset;
	}

	@Override
	public String getDriveScript(String indexName, String indent) {
		String script = "";
		if (length > 0 && command.getColumn0()) {
			script += indent + "drive " + pNames.get(0) + " " + p0 + "\n";
		}
		if (length > 1 && command.getColumn1()) {
			script += indent + "drive " + pNames.get(1) + " " + p1 + "\n";
		}
		if (length > 2 && command.getColumn2()) {
			script += indent + "drive " + pNames.get(2) + " " + p2 + "\n";
		}
		if (length > 3 && command.getColumn3()) {
			script += indent + "drive " + pNames.get(3) + " " + p3 + "\n";
		}
		if (length > 4 && command.getColumn4()) {
			script += indent + "drive " + pNames.get(4) + " " + p4 + "\n";
		}
		if (length > 5 && command.getColumn5()) {
			script += indent + "drive " + pNames.get(5) + " " + p5 + "\n";
		}
		if (length > 6 && command.getColumn6()) {
			script += indent + "drive " + pNames.get(6) + " " + p6 + "\n";
		}
		if (length > 7 && command.getColumn7()) {
			script += indent + "drive " + pNames.get(7) + " " + p7 + "\n";
		}
		if (length > 8 && command.getColumn8()) {
			script += indent + "drive " + pNames.get(8) + " " + p8 + "\n";
		}
		if (length > 9 && command.getColumn9()) {
			script += indent + "drive " + pNames.get(9) + " " + p9 + "\n";
		}
		script += indent + "histmem preset " + ((int) preset) + "\n";
		script += indent + "histmem start block\n";
		script += indent + "save " + indexName + "\n";
		return script;
	}
	
	@Override
	public String getBroadcastScript(String indexName, String indent) {
		String text = indent + "broadcast ";
		if (length > 0 && command.getColumn0()) {
			text += pNames.get(0) + ", ";
		}
		if (length > 1 && command.getColumn1()) {
			text += pNames.get(1) + ", ";
		}
		if (length > 2 && command.getColumn2()) {
			text += pNames.get(2) + ", ";
		}
		if (length > 3 && command.getColumn3()) {
			text += pNames.get(3) + ", ";
		}
		if (length > 4 && command.getColumn4()) {
			text += pNames.get(4) + ", ";
		}
		if (length > 5 && command.getColumn5()) {
			text += pNames.get(5) + ", ";
		}
		if (length > 6 && command.getColumn6()) {
			text += pNames.get(6) + ", ";
		}
		if (length > 7 && command.getColumn7()) {
			text += pNames.get(7) + ", ";
		}
		if (length > 8 && command.getColumn8()) {
			text += pNames.get(8) + ", ";
		}
		if (length > 9 && command.getColumn9()) {
			text += pNames.get(9) + ", ";
		}
		text += "preset\n" + indent;
		if (length > 0 && command.getColumn0()) {
			text += p0 + ", ";
		}
		if (length > 1 && command.getColumn1()) {
			text += p1 + ", ";
		}
		if (length > 2 && command.getColumn2()) {
			text += p2 + ", ";
		}
		if (length > 3 && command.getColumn3()) {
			text += p3 + ", ";
		}
		if (length > 4 && command.getColumn4()) {
			text += p4 + ", ";
		}
		if (length > 5 && command.getColumn5()) {
			text += p5 + ", ";
		}
		if (length > 6 && command.getColumn6()) {
			text += p6 + ", ";
		}
		if (length > 7 && command.getColumn7()) {
			text += p7 + ", ";
		}
		if (length > 8 && command.getColumn8()) {
			text += p8 + ", ";
		}
		if (length > 9 && command.getColumn9()) {
			text += p9 + ", ";
		}
		return text + " " + preset + "\n";
	}
	
	@Override
	public String getPritable(boolean isFirstLine) {
		String text = "";
		if (length > 0 && command.getColumn0()) {
			text += pNames.get(0) + ", \t";
		}
		if (length > 1 && command.getColumn1()) {
			text += pNames.get(1) + ", \t";
		}
		if (length > 2 && command.getColumn2()) {
			text += pNames.get(2) + ", \t";
		}
		if (length > 3 && command.getColumn3()) {
			text += pNames.get(3) + ", \t";
		}
		if (length > 4 && command.getColumn4()) {
			text += pNames.get(4) + ", \t";
		}
		if (length > 5 && command.getColumn5()) {
			text += pNames.get(5) + ", \t";
		}
		if (length > 6 && command.getColumn6()) {
			text += pNames.get(6) + ", \t";
		}
		if (length > 7 && command.getColumn7()) {
			text += pNames.get(7) + ", \t";
		}
		if (length > 8 && command.getColumn8()) {
			text += pNames.get(8) + ", \t";
		}
		if (length > 9 && command.getColumn9()) {
			text += pNames.get(9) + ", \t";
		}
		text += "preset\n";
		if (length > 0 && command.getColumn0()) {
			text += p0 + ", \t";
		}
		if (length > 1 && command.getColumn1()) {
			text += p1 + ", \t";
		}
		if (length > 2 && command.getColumn2()) {
			text += p2 + ", \t";
		}
		if (length > 3 && command.getColumn3()) {
			text += p3 + ", \t";
		}
		if (length > 4 && command.getColumn4()) {
			text += p4 + ", \t";
		}
		if (length > 5 && command.getColumn5()) {
			text += p5 + ", \t";
		}
		if (length > 6 && command.getColumn6()) {
			text += p6 + ", \t";
		}
		if (length > 7 && command.getColumn7()) {
			text += p7 + ", \t";
		}
		if (length > 8 && command.getColumn8()) {
			text += p8 + ", \t";
		}
		if (length > 9 && command.getColumn9()) {
			text += p9 + ", \t";
		}
		return text + preset + "\n";	}

	@Override
	public void startIteration() {
		// leave empty
	}

	@Override
	public boolean iterationHasNext() {
		// leave empty
		return false;
	}

	@Override
	public String iterationGetNext() {
		// leave empty
		return null;
	}

	@Override
	public int getNumberOfPoints() {
		// leave empty
		return 0;
	}

	/**
	 * @return the command
	 */
	public SimpleTableScanCommand getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(SimpleTableScanCommand command) {
		this.command = command;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) throws IndexOutOfBoundException {
		if (length > 10) {
			throw new IndexOutOfBoundException("Can not handle more than 10 items.");
		}
		this.length = length;
	}

	public List<String> getPNames() {
		return pNames;
	}

	public void setPNames(List<String> pNames) {
		this.pNames = pNames;
	}

	@Override
	protected void addNewParameter(AbstractScanCommand command){
		TableScanParameter newParameter = new TableScanParameter();
		newParameter.setCommand((SimpleTableScanCommand) command);
		newParameter.setIsSelected(true);
		try {
			newParameter.setLength(length);
		} catch (IndexOutOfBoundException e) {
		}
		newParameter.setIndex(index);
		newParameter.setPreset(preset);
		newParameter.setP0(p0);
		newParameter.setP1(p1);
		newParameter.setP2(p2);
		newParameter.setP3(p3);
		newParameter.setP4(p4);
		newParameter.setP5(p5);
		newParameter.setP6(p6);
		newParameter.setP7(p7);
		newParameter.setP8(p8);
		newParameter.setP9(p9);
		newParameter.setPNames(pNames);
		command.insertParameter(command.indexOfParameter(this) + 1, newParameter);
	}
	
	protected void removeParameter(AbstractScanCommand command){
		if (command.getParameterList().size() <= 1)
			return;
		command.removeParameter(this);
		firePropertyChange("parameter_remove", true, false);
	}

}
