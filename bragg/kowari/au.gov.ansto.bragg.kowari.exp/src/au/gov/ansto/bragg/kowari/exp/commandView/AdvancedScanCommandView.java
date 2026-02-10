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
package au.gov.ansto.bragg.kowari.exp.commandView;

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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.kowari.exp.command.AbstractScanCommand;
import au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter;
import au.gov.ansto.bragg.kowari.exp.command.AdvancedParameter;
import au.gov.ansto.bragg.kowari.exp.command.AdvancedScanCommand;
import au.gov.ansto.bragg.kowari.exp.command.ParameterValidator;

/**
 * @author nxi
 * Created on 11/08/2009
 */
public class AdvancedScanCommandView extends AbstractScanCommandView {

	/**
	 * 
	 */

	public AdvancedScanCommandView(AdvancedScanCommand command){
		super(command);
	}
	
	@Override
	protected void createPartControl(Composite parent,
			final AbstractScanCommand command) {
		super.createPartControl(parent, command);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(parent);
//		Label titleLabel = getToolkit().createLabel(parent, command.getCommandName());
//		titleLabel.setFont(new Font(titleLabel.getFont().getDevice(), new FontData[]{new FontData("Courier New", 10, SWT.BOLD)}));
//		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).indent(0, 12).applyTo(titleLabel);
//		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.TOP).applyTo(titleLabel);
		
		parameterComposite = getToolkit().createComposite(parent);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(5).applyTo(parameterComposite);
		GridDataFactory.swtDefaults().indent(2, 0).align(SWT.BEGINNING, SWT.TOP).applyTo(parameterComposite);
		if (command.getParameterList().size() == 0){
			AdvancedParameter parameter = new AdvancedParameter();
//		parameter.setScanVariable("sx");
			command.insertParameter(parameter);
		}
//		GridLayoutFactory.swtDefaults().spacing(3, 3).applyTo(parameterComposite);
		for (AbstractScanParameter parameter : command.getParameterList()){
			parameter.createParameterUI(parameterComposite, this, getToolkit());
		}
		
		final ComboViewer modeCombo = new ComboViewer(parent, SWT.READ_ONLY);
		modeCombo.setContentProvider(new ArrayContentProvider());
		modeCombo.setLabelProvider(new LabelProvider());
		modeCombo.setSorter(new ViewerSorter());
		modeCombo.setInput(new String[]{"time", "count"});
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).indent(0, 10).hint(
				AbstractScanParameter.WIDTH_PARAMETER_SHORT, SWT.DEFAULT).applyTo(modeCombo.getCombo());

		final Text presetText = getToolkit().createText(parent, "");
//		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(presetText);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(presetText);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).indent(0, 10).hint(
				AbstractScanParameter.WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(presetText);
		addValidator(presetText, ParameterValidator.floatValidator);
		
		Realm.runWithDefault(/*SWTObservables.getRealm(Display.getDefault())*/ DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(/*ViewersObservables.observeSingleSelection(modeCombo)*/ ViewerProperties.singleSelection().observe(modeCombo),
						/*BeansObservables.observeValue(command, "scan_mode")*/ BeanProperties.value("scan_mode").observe(command),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(/*SWTObservables.observeText(presetText, SWT.Modify)*/ WidgetProperties.text(SWT.Modify).observe(presetText),
						/*BeansObservables.observeValue(command, "preset")*/ BeanProperties.value("preset").observe(command),
						new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});

//		DropTarget dropTarget = new DropTarget(parameterComposite, DND.DROP_MOVE);
//		dropTarget.setTransfer(new Transfer[]{LocalSelectionTransfer.getTransfer()});
//		dropTarget.addDropListener(new DropTargetAdapter(){
//			@Override
//			public void drop(DropTargetEvent event) {
//				if (event.data instanceof IStructuredSelection) {
//					DndTransferData transferData = (DndTransferData) (
//							(StructuredSelection) event.data).getFirstElement();
//					Object parent = transferData.getParent();
//					Object child = transferData.getChild();
//					if (parent == command){
//						Point relativePoint = parameterComposite.toControl(
//								new Point(event.x, event.y));
//						int index = 0;
//						for (Control control : parameterComposite.getChildren()){
//							if (control instanceof Group){
//								if (relativePoint.y < (control.getBounds().y 
//										+ control.getBounds().height)) {
//									break;
//								}
//								index++;
//							}
//						}
//						if (child instanceof AdvancedParameter) {
//							AdvancedParameter parameter = (AdvancedParameter) child;
//							int currentIndex = command.indexOfParameter(parameter);
//							if (currentIndex == index) {
//								return;
//							}
//							command.removeParameter(parameter);
//							command.insertParameter(index, parameter);
//							refreshParameterComposite();
//						}
//					}
//				}
//			}
//		});
	}

	
}