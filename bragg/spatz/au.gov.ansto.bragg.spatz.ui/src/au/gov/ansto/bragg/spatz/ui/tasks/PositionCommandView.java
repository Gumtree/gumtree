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
package au.gov.ansto.bragg.spatz.ui.tasks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;

import au.gov.ansto.bragg.nbi.ui.core.commands.AbstractScanCommand;
import au.gov.ansto.bragg.nbi.ui.core.commands.AbstractScanCommandView;
import au.gov.ansto.bragg.nbi.ui.core.commands.AbstractScanParameter;
import au.gov.ansto.bragg.nbi.ui.core.commands.DndTransferData;

/**
 * @author nxi
 * Created on 11/08/2009
 */
public class PositionCommandView extends AbstractScanCommandView {

	private Map<PositionParameter, Composite> parameterCompositeMap;
	/**
	 * 
	 */

	public PositionCommandView(PositionCommand command){
		super(command);
		parameterCompositeMap = new LinkedHashMap<PositionParameter, Composite>();
	}
	
	@Override
	protected void createPartControl(Composite parent,
			final AbstractScanCommand command) {
		super.createPartControl(parent, command);
//		GridLayoutFactory.swtDefaults().applyTo(parent);
//		parameterComposite = getToolkit().createComposite(parent);
		parameterComposite = parent;
		parameterComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(4, 4).numColumns(9).applyTo(parameterComposite);
		createLabelArea(parameterComposite, (PositionCommand) command);

//		Label titleLabel = getToolkit().createLabel(parent, command.getCommandName());
//		titleLabel.setFont(new Font(titleLabel.getFont().getDevice(), new FontData[]{new FontData("Courier New", 10, SWT.BOLD)}));
//		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).indent(0, 12).applyTo(titleLabel);
//		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.TOP).applyTo(titleLabel);
//		parameterComposite = getToolkit().createComposite(parent);

		for (AbstractScanParameter parameter : command.getParameterList()) {
			Composite parComposite = createParameterComposite((PositionParameter) parameter);
			parComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		}
		
	}

	private void createLabelArea(Composite parent, PositionCommand command) {
//		Label blankLabel = getToolkit().createLabel(parent, "");
//		GridDataFactory.swtDefaults().span(3, 1).hint(26, SWT.DEFAULT).applyTo(blankLabel);
		Label spaceLabel = getToolkit().createLabel(parameterComposite, "");
		GridDataFactory.swtDefaults().hint(10, SWT.DEFAULT).applyTo(spaceLabel);
		
		Label angleLabel = getToolkit().createLabel(parameterComposite, "position");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(54, SWT.DEFAULT).applyTo(angleLabel);
		Label omegaLabel = getToolkit().createLabel(parameterComposite, "sx");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(60, SWT.DEFAULT).applyTo(omegaLabel);
		Label ss1vgLabel = getToolkit().createLabel(parameterComposite, "sz");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(60, SWT.DEFAULT).applyTo(ss1vgLabel);
		Label ss2vgLabel = getToolkit().createLabel(parameterComposite, "sth");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(60, SWT.DEFAULT).applyTo(ss2vgLabel);
		Label ss3vgLabel = getToolkit().createLabel(parameterComposite, "sphi");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(60, SWT.DEFAULT).applyTo(ss3vgLabel);
		Label posLabel = getToolkit().createLabel(parameterComposite, "samplename");
		GridDataFactory.swtDefaults().applyTo(posLabel);
		getToolkit().createLabel(parameterComposite, "");
		getToolkit().createLabel(parameterComposite, "");
	}

	public Composite createParameterComposite(PositionParameter parameter) {
		final Composite singleParameterComposite = new Composite(parameterComposite, SWT.NONE);
//		singleParameterComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		singleParameterComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(4, 4).numColumns(9).applyTo(singleParameterComposite);
		GridDataFactory.swtDefaults().span(9, 1).applyTo(singleParameterComposite);
		((PositionParameter) parameter).createParameterUI(singleParameterComposite, this, getToolkit());
		parameterCompositeMap.put((PositionParameter) parameter, singleParameterComposite);
		
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
						if (child instanceof PositionParameter) {
							PositionParameter parameter = (PositionParameter) child;
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
								parameterCompositeMap.get(parameter).setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
								resetBackground(parameterCompositeMap.get(parameter), Display.getCurrent().getSystemColor(SWT.COLOR_WHITE), 3000);
							}
						}
					} 
				}
			}
		});
		resetBackground(singleParameterComposite, Display.getCurrent().getSystemColor(SWT.COLOR_WHITE), 3000);

		return singleParameterComposite;
	}
	
	private void resetBackground(final Composite composite, final Color color, final int wait) {
		JobRunner.run(new ILoopExitCondition() {
			private int count = 0;
			@Override
			public boolean getExitCondition() {
				return count++ > 0;
			}
		}, new Runnable() {
			
			@Override
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						composite.setBackground(color);
					}
				});
			}
		}, wait);
		
	}
	
	@Override
	public void refreshParameterComposite() {
		for (AbstractScanParameter parameter : getCommand().getParameterList()) {
			if (parameter instanceof PositionParameter) {
				PositionParameter tableParameter = (PositionParameter) parameter;
				if (!parameterCompositeMap.containsKey(tableParameter)) {
					Composite curComposite = createParameterComposite(tableParameter);
					int index = getCommand().getParameterList().indexOf(parameter);
					if (index > 0) {
						PositionParameter preParameter = (PositionParameter) getCommand().getParameterList().get(index - 1);
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
		List<PositionParameter> toRemove = new ArrayList<PositionParameter>();
		for (PositionParameter parameter : parameterCompositeMap.keySet()) {
			if (!getCommand().getParameterList().contains(parameter)) {
				parameterCompositeMap.get(parameter).dispose();
				toRemove.add(parameter);
			}
		}
		for (PositionParameter parameter : toRemove) {
			parameterCompositeMap.remove(parameter);
		}
		int idx = 0;
		for (AbstractScanParameter par : getCommand().getParameterList()) {
			((PositionParameter) par).setPosition(++idx);
		}
		parent.layout(parameterComposite.getChildren());
		parameterComposite.update();
		parent.update();
		fireRefresh();
	}

}
