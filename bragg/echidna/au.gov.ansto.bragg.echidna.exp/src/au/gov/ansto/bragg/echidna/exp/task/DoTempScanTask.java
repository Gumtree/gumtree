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
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
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
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.models.AbstractModelObject;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.echidna.exp.command.DoTempCommand;

/**
 * @author nxi
 * Created on 24/07/2009
 */
public class DoTempScanTask extends AbstractEchidnaScanTask {

	public final static String TASK_TITLE = "Constant Temperature Scan";
//	private DoRTCommand command;
	/**
	 * 
	 */
	public DoTempScanTask() {
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
		return new DoTempScanTaskView();
	}
		
	private class DoTempScanTaskView extends AbstractTaskView {

		protected void createCommandUI(final Composite parent, final DoTempCommand command){
			
//			final DoRTCommand command = new DoRTCommand();
//			getDataModel().addCommand(command);

			getToolkit().createLabel(parent, "doTemp ");
			final Text sampnameText = getToolkit().createText(parent, "");
			GridDataFactory.swtDefaults().hint(18, SWT.DEFAULT).align(
					SWT.FILL, SWT.CENTER).grab(true, false).applyTo(sampnameText);
			addValidator(sampnameText, notEmptyValidator);
		
			final Text starttempText = getToolkit().createText(parent, "");
			GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
					SWT.FILL, SWT.CENTER).grab(true, false).applyTo(starttempText);
			addValidator(starttempText, floatValidator);

			final Text finishtempText = getToolkit().createText(parent, "");
			GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
					SWT.FILL, SWT.CENTER).grab(true, false).applyTo(finishtempText);
			addValidator(finishtempText, floatValidator);

			final Text notempstepsText = getToolkit().createText(parent, "");
			GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
					SWT.FILL, SWT.CENTER).grab(true, false).applyTo(notempstepsText);
			addValidator(notempstepsText, integerValidator);

			final Text startangText = getToolkit().createText(parent, "");
			GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
					SWT.FILL, SWT.CENTER).grab(true, false).applyTo(startangText);
			addValidator(startangText, floatValidator);

			final Text finishangText = getToolkit().createText(parent, "");
			GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
					SWT.FILL, SWT.CENTER).grab(true, false).applyTo(finishangText);
			addValidator(finishangText, floatValidator);

			final Text stepsizeText = getToolkit().createText(parent, "");
			GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
					SWT.FILL, SWT.CENTER).grab(true, false).applyTo(stepsizeText);
			addValidator(stepsizeText, floatValidator);

			final Text tot_timeText = getToolkit().createText(parent, "");
			GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
					SWT.FILL, SWT.CENTER).grab(true, false).applyTo(tot_timeText);
			addValidator(tot_timeText, floatValidator);

			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(SWTObservables.observeText(sampnameText, SWT.Modify),
							BeansObservables.observeValue(command, "sampname"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(starttempText, SWT.Modify),
							BeansObservables.observeValue(command, "starttemp"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(finishtempText, SWT.Modify),
							BeansObservables.observeValue(command, "finishtemp"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(notempstepsText, SWT.Modify),
							BeansObservables.observeValue(command, "notempsteps"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(startangText, SWT.Modify),
							BeansObservables.observeValue(command, "startang"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(finishangText, SWT.Modify),
							BeansObservables.observeValue(command, "finishang"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(stepsizeText, SWT.Modify),
							BeansObservables.observeValue(command, "stepsize"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(tot_timeText, SWT.Modify),
							BeansObservables.observeValue(command, "tot_time"),
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
					DoTempCommand newCommand = new DoTempCommand();
					newCommand.setSampname(command.getSampname());
					newCommand.setStarttemp(command.getStarttemp());
					newCommand.setFinishtemp(command.getFinishtemp());
					newCommand.setNotempsteps(command.getNotempsteps());
					newCommand.setStartang(command.getStartang());
					newCommand.setFinishang(command.getFinishang());
					newCommand.setStepsize(command.getStepsize());
					newCommand.setTot_time(command.getTot_time());
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
			GridLayoutFactory.swtDefaults().numColumns(11).applyTo(parent);
			
			if (getDataModel().getCommands().length == 0)
				getDataModel().addCommand(new DoTempCommand());
			createTaskUI(parent);
//			createCommandUI(parent, command);

		}

		private void createLabelArea(Composite parent) {
			getToolkit().createLabel(parent, "");
			getToolkit().createLabel(parent, "sampname");
			getToolkit().createLabel(parent, "starttemp");
			getToolkit().createLabel(parent, "finishtemp");
			getToolkit().createLabel(parent, "notempsteps");
			getToolkit().createLabel(parent, "startang");
			getToolkit().createLabel(parent, "finishang");
			getToolkit().createLabel(parent, "stepsize");
			getToolkit().createLabel(parent, "tot-time");
//			getToolkit().createLabel(parent, "sample_name");
//			getToolkit().createLabel(parent, "start_temp");
//			getToolkit().createLabel(parent, "finish_temp");
//			getToolkit().createLabel(parent, "number_of_temp");
//			getToolkit().createLabel(parent, "start_angle");
//			getToolkit().createLabel(parent, "finish_angle");
//			getToolkit().createLabel(parent, "no_of_step");
//			getToolkit().createLabel(parent, "total_time");
			getToolkit().createLabel(parent, "");
			getToolkit().createLabel(parent, "");
		}

		private void createTaskUI(Composite parent) {
			createLabelArea(parent);
			for (ISicsCommandElement command : getDataModel().getCommands()){
				if (command instanceof DoTempCommand){
					createCommandUI(parent, (DoTempCommand) command);
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
						if (arg0.getPropertyName().equals("nosteps") || arg0.getPropertyName().equals("tot_time")
								|| arg0.getPropertyName().equals("notempsteps"))
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

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.echidna.exp.task.AbstractEchidnaScanTask#createSingleCommandLine(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createSingleCommandLine(Composite parent) {
		
	}

	@Override
	public String getTitle() {
		return TASK_TITLE;
	}

	@Override
	public float getEstimatedTime() {
		float estimatedTime = 0;
		for (ISicsCommandElement command : getDataModel().getCommands()){
			if (command instanceof DoTempCommand){
				DoTempCommand doTempCommand = (DoTempCommand) command;
				estimatedTime += doTempCommand.getNotempsteps() * doTempCommand.getTot_time();
			}
		}
		return estimatedTime;
	}

}
