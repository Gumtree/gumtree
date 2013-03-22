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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * @author nxi
 * Created on 11/08/2009
 */
public class SimpleScanCommandView extends AbstractScanCommandView {

	private Map<TableScanParameter, Composite> parameterCompositeMap;
	/**
	 * 
	 */

	public SimpleScanCommandView(SimpleTableScanCommand command){
		super(command);
		parameterCompositeMap = new LinkedHashMap<TableScanParameter, Composite>();
	}
	
	@Override
	protected void createPartControl(Composite parent,
			final AbstractScanCommand command) {
		super.createPartControl(parent, command);
//		GridLayoutFactory.swtDefaults().applyTo(parent);
//		parameterComposite = getToolkit().createComposite(parent);
		parameterComposite = parent;
		parameterComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(4, 4).numColumns(((SimpleTableScanCommand) 
				command).getNumberOfMotor() + 5).applyTo(parameterComposite);
		createLabelArea(parameterComposite, (SimpleTableScanCommand) command);

//		Label titleLabel = getToolkit().createLabel(parent, command.getCommandName());
//		titleLabel.setFont(new Font(titleLabel.getFont().getDevice(), new FontData[]{new FontData("Courier New", 10, SWT.BOLD)}));
//		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).indent(0, 12).applyTo(titleLabel);
//		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.TOP).applyTo(titleLabel);
//		parameterComposite = getToolkit().createComposite(parent);
		int numberOfMotors = ((SimpleTableScanCommand) command).getNumberOfMotor();
		Label spaceLabel = getToolkit().createLabel(parameterComposite, "");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(spaceLabel);
		for (int i = 0; i < numberOfMotors; i++) {
			final Button check = getToolkit().createButton(parameterComposite, "", SWT.CHECK);
			final String columnName = "column" + i;
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check);
			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(SWTObservables.observeSelection(check),
							BeansObservables.observeValue(command, columnName),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
		}
		spaceLabel = getToolkit().createLabel(parameterComposite, "");
		GridDataFactory.swtDefaults().span(3, 1).applyTo(spaceLabel);
		for (AbstractScanParameter parameter : command.getParameterList()) {
			Composite parComposite = createParameterComposite((TableScanParameter) parameter);
			parComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		}
		
		final Button selectAll = getToolkit().createButton(parameterComposite, "Select/Deselect All", SWT.CHECK);
		GridDataFactory.swtDefaults().span(numberOfMotors + 3, 1).indent(9, 2).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(selectAll);
		boolean isAllSelected = true;
		for (AbstractScanParameter parameter : command.getParameterList()) {
			if (!((TableScanParameter) parameter).getIsSelected()) {
				isAllSelected = false;
				break;
			}
		}
		selectAll.setSelection(isAllSelected);
		for (AbstractScanParameter parameter : command.getParameterList()) {
			parameter.addPropertyChangeListener(new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					Object parameter = evt.getSource();
					if (parameter instanceof TableScanParameter)
						if (evt.getPropertyName().equals("isSelected")) {
							if (!((Boolean) evt.getNewValue())) {
								selectAll.setSelection(false);
							} else {
								boolean isAllSelected = true;
								for (AbstractScanParameter parm : command.getParameterList()) {
									if (!((TableScanParameter) parm).getIsSelected()) {
										isAllSelected = false;
										break;
									}
								}
								selectAll.setSelection(isAllSelected);
							}
						}
				}
			});
		}
		selectAll.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isSelected = selectAll.getSelection();
				for (AbstractScanParameter parameter : command.getParameterList()) {
					((TableScanParameter) parameter).setIsSelected(isSelected);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void createLabelArea(Composite parent, SimpleTableScanCommand command) {
//		Label blankLabel = getToolkit().createLabel(parent, "");
//		GridDataFactory.swtDefaults().span(3, 1).hint(26, SWT.DEFAULT).applyTo(blankLabel);
		Label spaceLabel = getToolkit().createLabel(parent, "");
		GridDataFactory.swtDefaults().span(2, 1).hint(24, SWT.DEFAULT).applyTo(spaceLabel);
		for (int i = 0; i < command.getNumberOfMotor(); i++) {
			String pName = command.getPNames().get(i);
			Label pLabel = getToolkit().createLabel(parent, pName);
//			GridDataFactory.swtDefaults().indent(4, 0).applyTo(pLabel);
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(57, SWT.DEFAULT).applyTo(pLabel);
		}
		Label presetLabel = getToolkit().createLabel(parent, command.getScan_mode());
		GridDataFactory.swtDefaults().span(3, 1).indent(4, 0).applyTo(presetLabel);
	}

	public Composite createParameterComposite(TableScanParameter parameter) {
		final Composite singleParameterComposite = new Composite(parameterComposite, SWT.NONE);
//		singleParameterComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		singleParameterComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(4, 4).numColumns( 
				((SimpleTableScanCommand) getCommand()).getNumberOfMotor() + 5).applyTo(singleParameterComposite);
		GridDataFactory.swtDefaults().span(((SimpleTableScanCommand) getCommand()).getNumberOfMotor() 
				+ 5, 1).applyTo(singleParameterComposite);
		((TableScanParameter) parameter).createParameterUI(singleParameterComposite, this, getToolkit());
		parameterCompositeMap.put((TableScanParameter) parameter, singleParameterComposite);
		
		DropTarget dropTarget = new DropTarget(singleParameterComposite, DND.DROP_MOVE);
		dropTarget.setTransfer(new Transfer[]{LocalSelectionTransfer.getTransfer()});
		dropTarget.addDropListener(new DropTargetAdapter(){
			@Override
			public void drop(DropTargetEvent event) {
				if (event.data instanceof StructuredSelection) {
					DndTransferData transferData = (DndTransferData) (
							(StructuredSelection) event.data).getFirstElement();
					Object parentData = transferData.getParent();
					Object child = transferData.getChild();
					if (parentData == getCommand()) {
						Point relativePoint = parameterComposite.toControl(
								new Point(event.x, event.y));
						int targetIndex = 0;
						Composite targetComposite = null;
						for (AbstractScanParameter parameter : getCommand().getParameterList()) {
							targetComposite = parameterCompositeMap.get(parameter);
							if (relativePoint.y < targetComposite.getBounds().y 
									+ targetComposite.getBounds().height) {
								break;
							}
							targetIndex++;
						}
						if (child instanceof TableScanParameter) {
							TableScanParameter parameter = (TableScanParameter) child;
							int currentIndex = getCommand().getParameterList().indexOf(parameter);
							if (currentIndex == targetIndex) {
								return;
							} else {
								getCommand().getParameterList().remove(parameter);
								getCommand().getParameterList().add(targetIndex, parameter);
								if (currentIndex < targetIndex) {
									parameterCompositeMap.get(parameter).moveBelow(targetComposite);
								} else {
									parameterCompositeMap.get(parameter).moveAbove(targetComposite);
								}
								refreshParameterComposite();
							}
						}
					} 
				}
			}
		});
		
		return singleParameterComposite;
	}
	
	@Override
	public void refreshParameterComposite() {
		for (AbstractScanParameter parameter : getCommand().getParameterList()) {
			if (parameter instanceof TableScanParameter) {
				TableScanParameter tableParameter = (TableScanParameter) parameter;
				if (!parameterCompositeMap.containsKey(tableParameter)) {
					Composite curComposite = createParameterComposite(tableParameter);
					int index = getCommand().getParameterList().indexOf(parameter);
					if (index > 0) {
						TableScanParameter preParameter = (TableScanParameter) getCommand().getParameterList().get(index - 1);
						Composite preComposite = parameterCompositeMap.get(preParameter);
						if (preComposite != null) {
							curComposite.moveBelow(preComposite);
						}
					}
				} else {
					parameterCompositeMap.get(parameter).setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
				}
			}
		}
		List<TableScanParameter> toRemove = new ArrayList<TableScanParameter>();
		for (TableScanParameter parameter : parameterCompositeMap.keySet()) {
			if (!getCommand().getParameterList().contains(parameter)) {
				parameterCompositeMap.get(parameter).dispose();
				toRemove.add(parameter);
			}
		}
		for (TableScanParameter parameter : toRemove) {
			parameterCompositeMap.remove(parameter);
		}
		parent.layout(parameterComposite.getChildren());
		parameterComposite.update();
		parent.update();
		fireRefresh();
	}

}
