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

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.batch.ui.views.AbstractSicsCommandView;

import au.gov.ansto.bragg.kowari.exp.command.AbstractScanCommand;
import au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter;
import au.gov.ansto.bragg.kowari.exp.command.ParameterValidator;

/**
 * @author nxi
 * Created on 05/08/2009
 */
public class AbstractScanCommandView extends AbstractSicsCommandView<AbstractScanCommand> {

	protected static FieldDecoration errorDec = FieldDecorationRegistry.getDefault().getFieldDecoration(
			FieldDecorationRegistry.DEC_ERROR);

	protected Composite parent;
	protected Composite parameterComposite;
	
	public AbstractScanCommandView(AbstractScanCommand command){
		setCommand(command);
	}
	
	@Override
	protected void createPartControl(Composite parent,
			AbstractScanCommand command) {
		this.parent = parent;
	}

	public void refreshUI(){
		for (Control control : parent.getChildren())
			if (!control.isDisposed())
				control.dispose();
		createPartControl(parent, getCommand());
//		parameterComposite.update();
//		parameterComposite.layout();
//		parameterComposite.redraw();
	}
	
	protected void addValidator(final Text textBox, final ParameterValidator validator){
		final ControlDecoration startangDec = new ControlDecoration(textBox, SWT.LEFT | SWT.BOTTOM);
		textBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (validator.isValid(textBox.getText())) {
					startangDec.hide();
				} else {
					startangDec.setImage(errorDec.getImage());
					startangDec.setDescriptionText(validator.getErrorMessage());
					startangDec.show();
				}
			}
		});
	}

	public void refreshParameterComposite() {
		for (Control control : parameterComposite.getChildren()){
			if (!control.isDisposed())
				control.dispose();
		}
		for (AbstractScanParameter parameter : getCommand().getParameterList()){
			parameter.createParameterUI(parameterComposite, this, getToolkit());
		}
//		parent.layout(parameterComposite.getChildren());
		parent.layout(parameterComposite.getChildren());
		parameterComposite.update();
		parent.update();
		fireRefresh();
	}


}
