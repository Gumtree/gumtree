/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.control.ui.batch.taskeditor;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.gumtree.control.model.SicsModelUtils;
import org.gumtree.control.ui.batch.SicsBatchUIUtils;
import org.gumtree.control.ui.batch.command.AbstractSicsCommandView;
import org.gumtree.control.ui.batch.command.SicsVariableCommand;

public class SicsVariableView extends AbstractSicsCommandView<SicsVariableCommand> {

	private DataBindingContext bindingContext;
	
	private ComboViewer comboViewer;
	
	private Text text;
	
	@Override
	public void createPartControl(Composite parent, SicsVariableCommand command) {
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(10, SWT.DEFAULT).numColumns(2).applyTo(parent);
		
		/*********************************************************************
		 * Sics variable selection
		 *********************************************************************/
		final String[] sicsVariables = SicsModelUtils.getSicsVariables();
		comboViewer = new ComboViewer(parent, SWT.READ_ONLY);
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LabelProvider());
		comboViewer.setInput(sicsVariables);
		comboViewer.setSorter(new ViewerSorter());
		comboViewer.getCombo().setVisibleItemCount(20);
		GridDataFactory.swtDefaults().hint(WIDTH_COMBO, SWT.DEFAULT).indent(0, 2).applyTo(comboViewer.getCombo());
		
		/*********************************************************************
		 * Argument
		 *********************************************************************/
		text = getToolkit().createText(parent, "", SWT.BORDER);
		text.setToolTipText("Enter sics variable argument");
		GridDataFactory.fillDefaults().indent(0, 2).grab(true, false).applyTo(text);
		// Check empty field
		final ControlDecoration controlDec = new ControlDecoration(text, SWT.LEFT | SWT.BOTTOM);
		final FieldDecoration fieldDec = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if ((text.getText() == null) || text.getText().length() == 0) {
					controlDec.setImage(fieldDec.getImage());
					controlDec.setDescriptionText("SICS variable argument is empty");
					controlDec.show();
				} else {
					controlDec.hide();
				}
			}
		});
		
		
		
		/*********************************************************************
		 * Data binding
		 *********************************************************************/
		Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				bindingContext = new DataBindingContext();
				
				bindingContext.bindValue(
						SWTObservables.observeSelection(comboViewer.getCombo()),
						BeansObservables.observeValue(getCommand(), "sicsVariable"),
						new UpdateValueStrategy(),
						new UpdateValueStrategy()
				);
				
				bindingContext.bindValue(
						SWTObservables.observeText(text, SWT.Modify),
						BeansObservables.observeValue(getCommand(), "value"),
						new UpdateValueStrategy(),
						new UpdateValueStrategy()
				);
				
				// Set Default selection (only works after the binding is set)
				if (getCommand().getSicsVariable() == null) {
					if (comboViewer.getCombo().getItemCount() > 0) {
						comboViewer.setSelection(new StructuredSelection(
								comboViewer.getCombo().getItem(comboViewer.getCombo().getItemCount() - 1)));
					}
				}
			}
		});
	}

	@Override
	public void dispose() {
		if (bindingContext != null) {
			bindingContext.dispose();
			bindingContext = null;
		}
		comboViewer = null;
		text = null;
		super.dispose();
	}
	
}
