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
package au.gov.ansto.bragg.echidna.exp.task;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.batch.ui.CommandBlockTask;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;

/**
 * @author nxi
 * Created on 24/07/2009
 */
public abstract class AbstractEchidnaScanTask extends CommandBlockTask {

	protected abstract void createSingleCommandLine(Composite parent);
	private List<ITaskPropertyChangeListener> taskPropertyChangeListeners = 
		new ArrayList<ITaskPropertyChangeListener>();
	protected static FieldDecoration errorDec = FieldDecorationRegistry.getDefault().getFieldDecoration(
			FieldDecorationRegistry.DEC_ERROR);
	public final static Validator floatValidator = new Validator() {
		
		public boolean isValid(String text) {
			if (text == null || text.trim().length() == 0)
				return false;
			try{
				Float.valueOf(text);
				return true;
			}catch (Exception e) {
				return false;
			}
		}

		public String getErrorMessage() {
			return "please input a number";
		}
	};

	public final static Validator notEmptyValidator = new Validator() {
		
		public boolean isValid(String text) {
			if (text == null || text.trim().length() == 0)
				return false;
//			if (text.trim().contains(" "))
//				return false;
			return true;
		}

		public String getErrorMessage() {
			return "please input text";
		}
	};

	public final static Validator notEquationMarkValidator = new Validator() {
		
		public boolean isValid(String text) {
			if (text.contains("="))
				return false;
//			if (text.trim().contains(" "))
//				return false;
			return true;
		}

		public String getErrorMessage() {
			return "please input text without any equation mark";
		}
	};
	
	public final static Validator integerValidator = new Validator() {
		
		public boolean isValid(String text) {
			if (text == null || text.trim().length() == 0)
				return false;
			try{
				Integer.parseInt(text);
				return true;
			}catch (Exception e) {
				return false;
			}
		}

		public String getErrorMessage() {
			return "please input a number";
		}
	};

	public abstract String getTitle();
	public void createPartControl(Composite parent) {
		
	}
	
	public static void addValidator(final Text textBox, final Validator validator){
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
	
	public interface Validator{
		public boolean isValid(String text);

		public String getErrorMessage();
	}
	
	public abstract float getEstimatedTime();
	
	public interface ITaskPropertyChangeListener{
		public void propertyChanged(ISicsCommandElement command, PropertyChangeEvent event);
	}
	
	public void addPropertyChangeListener(ITaskPropertyChangeListener listener){
		taskPropertyChangeListeners.add(listener);
	}
	
	public void removePropertyChangeListener(ITaskPropertyChangeListener listener){
		taskPropertyChangeListeners.remove(listener);
	}
	
	public void notifyPropertyChanged(ISicsCommandElement command, PropertyChangeEvent event){
		for (ITaskPropertyChangeListener listener : taskPropertyChangeListeners){
			listener.propertyChanged(command, event);
		}
	}
	
	public void clearPropertyChangeListeners(){
		taskPropertyChangeListeners.clear();
	}
}
