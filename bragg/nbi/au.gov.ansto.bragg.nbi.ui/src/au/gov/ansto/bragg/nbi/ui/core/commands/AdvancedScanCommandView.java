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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;


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
		
		Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(ViewersObservables.observeSingleSelection(modeCombo),
						BeansObservables.observeValue(command, "scan_mode"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(presetText, SWT.Modify),
						BeansObservables.observeValue(command, "preset"),
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
