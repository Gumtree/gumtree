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
package au.gov.ansto.bragg.kowari.exp.task;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.models.AbstractModelObject;

import au.gov.ansto.bragg.kowari.exp.command.AbstractScanCommand;
import au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter;
import au.gov.ansto.bragg.kowari.exp.command.AdvancedParameter;
import au.gov.ansto.bragg.kowari.exp.command.AdvancedScanCommand;
import au.gov.ansto.bragg.kowari.exp.commandView.AdvancedScanCommandView;

/**
 * @author nxi
 * Created on 12/08/2009
 */
public class AdvancedScanTask extends AbstractKowariScanTask {

	public final static String TASK_TITLE = "Advanced Multi-dimensional Scan";

	/**
	 * 
	 */
	public AdvancedScanTask() {
		super();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.task.AbstractKowariScanTask#getTitle()
	 */
	@Override
	public String getTitle() {
		String description = null;
		ISicsCommandElement[] commands = getDataModel().getCommands();
		if (commands != null && commands.length > 0){
			try{
				description = ((AbstractScanCommand) commands[0]).getScanDescription();
			}catch (Exception e) {
			}
		}
		String units = getTimeUnits();
		if (description != null && description.trim().length() > 0) {
			if (description.length() > 40) {
				return "Advanced... {" + description + "}" + " (" + (int) getEstimatedTime() + units + ")";
			}
			return TASK_TITLE + " {" + description + "}" + " (" + (int) getEstimatedTime() + units + ")" ;
		}
		return TASK_TITLE;
	}

	@Override
	public String getLabel() {
		if (super.getLabel().equals(TASK_TITLE)) {
			return getTitle();
		} else {
			return super.getLabel() + " (" + (int) getEstimatedTime() + getTimeUnits() + ")";
		}
	}
	
	protected ITaskView createViewInstance() {
		return new ScanNDTaskView();
	}

	private class ScanNDTaskView extends AbstractTaskView{

		private Composite parent;
		private Button singleFileRadio;
		@Override
		public void createPartControl(Composite parent) {
			this.parent = parent;
			GridLayoutFactory.swtDefaults().numColumns(4).applyTo(parent);
			if (getDataModel().getCommands().length == 0){
				AdvancedScanCommand newCommand = new AdvancedScanCommand();
				newCommand.setScan_mode("time");
				getDataModel().addCommand(newCommand);
			}
			createTaskUI(parent);
		}

		private void createTaskUI(Composite parent) {
			createLabelArea(parent);
//			Composite commandComposite = getToolkit().createComposite(parent);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(commandComposite);
			for (ISicsCommandElement command : getDataModel().getCommands()){
				if (command instanceof AdvancedScanCommand){
					createCommandUI(parent, (AdvancedScanCommand) command);
//					addCommandListener(command);
				}
			}
			fireRefresh();
		}

		private void createCommandUI(Composite parent, AdvancedScanCommand command) {
			AdvancedScanCommandView commandView = new AdvancedScanCommandView(command);
			commandView.setTaskView(this);
			commandView.createPartControl(parent);
			addCommandListener(command);
		}

		private void createLabelArea(Composite parent) {
//			getToolkit().createLabel(parent, "");
			Composite labelComposite = getToolkit().createComposite(parent);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(labelComposite);
			GridLayoutFactory.swtDefaults().numColumns(7).applyTo(labelComposite);
			Label scanVarLabel = getToolkit().createLabel(labelComposite, "variable");
			GridDataFactory.swtDefaults().indent(2, 0).hint(89, SWT.DEFAULT).applyTo(scanVarLabel);
			Label startLabel = getToolkit().createLabel(labelComposite, "start");
			GridDataFactory.swtDefaults().hint(60, SWT.DEFAULT).applyTo(startLabel);
			Label finishLabel = getToolkit().createLabel(labelComposite, "finish");
			GridDataFactory.swtDefaults().hint(59, SWT.DEFAULT).applyTo(finishLabel);
			Label stepSizeLabel = getToolkit().createLabel(labelComposite, "step_size");
			GridDataFactory.swtDefaults().hint(108, SWT.DEFAULT).applyTo(stepSizeLabel);
			Label nostepsLabel = getToolkit().createLabel(labelComposite, "points");
			GridDataFactory.swtDefaults().hint(55, SWT.DEFAULT).applyTo(nostepsLabel);
			singleFileRadio = getToolkit().createButton(labelComposite, "single_file", SWT.RADIO | SWT.NO_FOCUS);
			GridDataFactory.swtDefaults().hint(65, SWT.DEFAULT).applyTo(singleFileRadio);
			ISicsCommandElement[] commands = getDataModel().getCommands();
			if (commands.length > 0)
				singleFileRadio.setSelection(isSingleFile());
			
			singleFileRadio.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					for (ISicsCommandElement command : getDataModel().getCommands()){
						if (command instanceof AdvancedScanCommand){
							for (AbstractScanParameter parameter : ((AdvancedScanCommand) command).
									getParameterList()){
								((AdvancedParameter) parameter).setDoCreateFile(false);
							}
//							addCommandListener(command);
						}
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					
				}
			});
//			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
//				public void run() {
//					ISicsCommandElement[] commands = getDataModel().getCommands();
//					if (commands.length > 0){
//						DataBindingContext bindingContext = new DataBindingContext();
//						bindingContext.bindValue(SWTObservables.observeSelection(singleFileRadio),
//								BeansObservables.observeValue(commands[0], "isSingleFile"),
//								new UpdateValueStrategy(), new UpdateValueStrategy());
//					}
//				}
//			});
			getToolkit().createLabel(parent, "mode");
			getToolkit().createLabel(parent, "preset");
		}
		
		private void addCommandListener(final ISicsCommandElement command) {
			if (command instanceof AbstractModelObject)
				((AbstractModelObject) command).addPropertyChangeListener(new PropertyChangeListener() {
					
					public void propertyChange(PropertyChangeEvent arg0) {
//						if (arg0.getPropertyName().equals("parameter_add") || arg0.getPropertyName().equals(
//								"parameter_remove") || arg0.getPropertyName().equals(
//								"numberOfPoints") || arg0.getPropertyName().equals("preset"))
							notifyPropertyChanged(command, arg0);
						if (arg0.getPropertyName().equals("parameter_add")){
							Object parameter = arg0.getNewValue();
							if (parameter instanceof AbstractScanParameter)
								((AbstractScanParameter) parameter).addPropertyChangeListener(this);
						}
						if (arg0.getPropertyName().equals("parameter_remove")){
//							Object parameter = arg0.getNewValue();
							if (!singleFileRadio.isDisposed())
								singleFileRadio.setSelection(isSingleFile());
						}
						if (arg0.getPropertyName().equals("multiple files")){
							if (!singleFileRadio.isDisposed())
								singleFileRadio.setSelection(false);
						}
					}
				});
		}

		@Override
		public void fireRefresh() {
			parent.update();
			parent.getParent().layout(parent.getChildren());
			super.fireRefresh();
		}
	}
	
	private boolean isSingleFile(){
		ISicsCommandElement[] commands = getDataModel().getCommands();
		if (commands.length > 0){
			return ((AdvancedScanCommand) commands[0]).isSingleFile();
		}
		return false;
	}

	@Override
	public ITask newThisTask() {
		return new AdvancedScanTask();
	}
}
