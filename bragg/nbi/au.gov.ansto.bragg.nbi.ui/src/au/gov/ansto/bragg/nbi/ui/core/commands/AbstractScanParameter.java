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

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.workflow.ui.models.AbstractModelObject;


/**
 * @author nxi
 * Created on 05/08/2009
 */
public abstract class AbstractScanParameter extends AbstractModelObject {

//	private FormToolkit toolkit;
	private boolean isDisposed = false;
	protected static FieldDecoration errorDec = FieldDecorationRegistry.getDefault().getFieldDecoration(
			FieldDecorationRegistry.DEC_ERROR);
	
	/**
	 * 
	 */
	public AbstractScanParameter() {
		super();
	}

	public abstract void startIteration();
	
	public abstract boolean iterationHasNext();
	
	public abstract String iterationGetNext();
	
	public abstract void createParameterUI(Composite parent, AbstractScanCommandView commandView, 
			FormToolkit toolkit);
	
	public static final int WIDTH_PARAMETER_LONG = 48;
	public static final int WIDTH_PARAMETER_SHORT = 32;
	public static final int WIDTH_COMBO = 52;

	/**
	 * Returns an instance of form toolkit for convenience reason.
	 * 
	 * @return
	 */
//	protected FormToolkit getToolkit() {
//		if (toolkit == null) {
//			toolkit = new FormToolkit(Display.getDefault());
//		}
//		return toolkit;
//	}
	
	private boolean isDisposed(){
		return isDisposed;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.IPartControlProvider#dispose()
	 */
	public void dispose() {
		if (isDisposed()) {
			return;
		}
		isDisposed = true;
//		if (toolkit != null) {
//			toolkit.dispose();
//			toolkit = null;
//		}
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

	protected void addSelectionValidator(final Combo combo, final ParameterValidator validator){
		final ControlDecoration startangDec = new ControlDecoration(combo, SWT.LEFT | SWT.BOTTOM);
		combo.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				if (validator.isValid(combo.getText()))
					startangDec.hide();
				else{
					startangDec.setImage(errorDec.getImage());
					startangDec.setDescriptionText(validator.getErrorMessage());
					startangDec.show();
				}
			}
		});
	}
	
	protected void addNewParameter(AbstractScanCommand command){
		SimpleNDParameter newParameter = new SimpleNDParameter();
		command.insertParameter(command.indexOfParameter(this) + 1, newParameter);
	}
	
	protected void removeParameter(AbstractScanCommand command){
		if (command.getParameterList().size() <= 1)
			return;
		command.removeParameter(this);
		firePropertyChange("parameter_remove", true, false);
	}
	
	protected AbstractScanParameter getInstance(){
		return this;
	}

	public abstract int getNumberOfPoints();

	public String getForLoopHead(String indexName, String indent) {
		return "";
	}

	public abstract String getDriveScript(String indexName, String indent);

	public abstract String getBroadcastScript(String indexName, String indent);

	public abstract String getPritable(boolean isFirstLine);
}
