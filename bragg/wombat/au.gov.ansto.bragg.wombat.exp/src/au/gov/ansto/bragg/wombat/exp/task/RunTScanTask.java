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
package au.gov.ansto.bragg.wombat.exp.task;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.models.AbstractModelObject;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.wombat.exp.command.RunTCommand;


/**
 * @author nxi
 * Created on 24/07/2009
 */
public class RunTScanTask extends AbstractWombatTask {

	public final static String TASK_TITLE = "Temperature Scan";
//	private DoRTCommand command;
	/**
	 * 
	 */
	public RunTScanTask() {
		super();
	}
	
	public void initialise() {
		super.initialise();
//		command = new DoRTCommand();
////		command.setSicsVariable("run sc");
////		command.setValue("0");
//		getDataModel().addCommand(command);
	}

	protected ITaskView createViewInstance() {
		return new DoRTScanTaskView();
	}
		
	private class DoRTScanTaskView extends AbstractTaskView {
		protected void createCommandUI(final Composite parent, final RunTCommand command){
			
			getToolkit().createLabel(parent, "RunT ");

			final Text temperatureText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(temperatureText);
			addValidator(temperatureText, floatValidator);
			GridDataFactory.swtDefaults().hint(60, SWT.DEFAULT).applyTo(temperatureText);
			final Text delayText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(delayText);
			addValidator(delayText, floatValidator);
			GridDataFactory.swtDefaults().hint(60, SWT.DEFAULT).applyTo(delayText);
			final Text numstepsText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(numstepsText);
			addValidator(numstepsText, integerValidator);
			GridDataFactory.swtDefaults().hint(60, SWT.DEFAULT).applyTo(numstepsText);
			final Text oscnoText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(oscnoText);
			addValidator(oscnoText, integerValidator);
			GridDataFactory.swtDefaults().hint(60, SWT.DEFAULT).applyTo(oscnoText);
			Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(temperatureText),
						BeanProperties.value("temperature").observe(command),
						new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(delayText),
						BeanProperties.value("delay").observe(command),
						new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(numstepsText),
						BeanProperties.value("numsteps").observe(command),
						new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(oscnoText),
						BeanProperties.value("oscno").observe(command),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});

			Button addButton = getToolkit().createButton(parent, "", SWT.PUSH);
			try {
				addButton.setImage(SicsBatchUIUtils.getBatchEditorImage("ADD"));
			} catch (FileNotFoundException e2) {
				LoggerFactory.getLogger(this.getClass()).error("can not find ADD image", e2);
			}
			addButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					RunTCommand newCommand = new RunTCommand();
					newCommand.setTemperature(command.getTemperature());
					newCommand.setDelay(command.getDelay());
					newCommand.setNumsteps(command.getNumsteps());
					newCommand.setOscno(command.getOscno());
					getDataModel().insertCommand(getDataModel().indexOf(command) + 1, newCommand);
					refreshUI(parent);
					notifyPropertyChanged(newCommand, null);
				}
			});
			
			Button removeButton = getToolkit().createButton(parent, "", SWT.PUSH);
			try {
				removeButton.setImage(SicsBatchUIUtils.getBatchEditorImage("REMOVE"));
			} catch (FileNotFoundException e1) {
				LoggerFactory.getLogger(this.getClass()).error("can not find REMOVE image", e1);
			}
			removeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (getDataModel().getCommands().length <= 1)
						return;
					getDataModel().removeCommand(command);
					refreshUI(parent);
				}
			});
		}
		
		public void createPartControl(Composite parent) {
			GridLayoutFactory.swtDefaults().numColumns(7).applyTo(parent);
			if (getDataModel().getCommands().length == 0){
				RunTCommand newCommand = new RunTCommand();
				getDataModel().addCommand(newCommand);
			}
			createTaskUI(parent);
//			createCommandUI(parent, command);

		}

		private void createLabelArea(Composite parent) {
			getToolkit().createLabel(parent, "");
			getToolkit().createLabel(parent, "temperature");
			getToolkit().createLabel(parent, "delay");
			getToolkit().createLabel(parent, "numsteps");
			getToolkit().createLabel(parent, "oscno");
			getToolkit().createLabel(parent, "");
			getToolkit().createLabel(parent, "");
		}

		private void createTaskUI(Composite parent) {
			createLabelArea(parent);
			for (ISicsCommandElement command : getDataModel().getCommands()){
				if (command instanceof RunTCommand){
					createCommandUI(parent, (RunTCommand) command);
					addCommandListener(command);
				}
			}
//			parent.update();
//			parent.layout();
//			parent.redraw();
			fireRefresh();
		}

		private void addCommandListener(final ISicsCommandElement command) {
			if (command instanceof AbstractModelObject)
				((AbstractModelObject) command).addPropertyChangeListener(new PropertyChangeListener() {
					
					public void propertyChange(PropertyChangeEvent arg0) {
						if (arg0.getPropertyName().equals("numsteps") || arg0.getPropertyName().equals("oscno"))
							notifyPropertyChanged(command, arg0);
					}
				});
		}

		private void refreshUI(Composite parent) {
			for (Control control : parent.getChildren())
				if (!control.isDisposed())
					control.dispose();
			createTaskUI(parent);
		}
		
	}


	@Override
	public String getTitle() {
		return TASK_TITLE;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.echidna.exp.task.AbstractEchidnaScanTask#getEstimatedTime()
	 */
	@Override
	public float getEstimatedTime() {
		float estimatedTime = 0;
//		for (ISicsCommandElement command : getDataModel().getCommands()){
//			if (command instanceof DoRTCommand){
//				DoRTCommand doRTCommand = (DoRTCommand) command;
//				estimatedTime += doRTCommand.getTot_time();
//			}
//		}
		return estimatedTime;
	}

	@Override
	public ITask newThisTask() {
		return new RunTScanTask();
	}
	
}