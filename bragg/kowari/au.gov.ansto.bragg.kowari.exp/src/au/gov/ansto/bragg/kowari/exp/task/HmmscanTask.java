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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.models.AbstractModelObject;

import au.gov.ansto.bragg.kowari.exp.command.AbstractScanCommand;
import au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter;
import au.gov.ansto.bragg.kowari.exp.command.HmmscanCommand;
import au.gov.ansto.bragg.kowari.exp.commandView.HmmscanCommandView;

/**
 * @author nxi
 * Created on 12/08/2009
 */
public class HmmscanTask extends AbstractKowariScanTask {

	public final static String TASK_TITLE = "Arbitrary Scan";

	/**
	 * 
	 */
	public HmmscanTask() {
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
				description = ((HmmscanCommand) commands[0]).getScanDescription();
			}catch (Exception e) {
			}
		}
		String units = getTimeUnits();
		if (description != null && description.trim().length() > 0)
			return TASK_TITLE + " {" + description + "}" + " (" + (int) getEstimatedTime() + units + ")" ;
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
		return new HmmscanTaskView();
	}

	private class HmmscanTaskView extends AbstractTaskView{

		private Composite parent;
		@Override
		public void createPartControl(Composite parent) {
			this.parent = parent;
			GridLayoutFactory.swtDefaults().numColumns(4).applyTo(parent);
			if (getDataModel().getCommands().length == 0){
				HmmscanCommand newCommand = new HmmscanCommand();
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
				if (command instanceof HmmscanCommand){
					createCommandUI(parent, (HmmscanCommand) command);
//					addCommandListener(command);
				}
			}
			fireRefresh();
		}

		private void createCommandUI(Composite parent, HmmscanCommand command) {
			HmmscanCommandView commandView = new HmmscanCommandView(command);
			commandView.setTaskView(this);
			commandView.createPartControl(parent);
			addCommandListener(command);
		}

		private void createLabelArea(Composite parent) {
//			getToolkit().createLabel(parent, "");
			Composite labelComposite = getToolkit().createComposite(parent);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(labelComposite);
			GridLayoutFactory.swtDefaults().numColumns(4).applyTo(labelComposite);
			Label scanVarLabel = getToolkit().createLabel(labelComposite, "variable");
			GridDataFactory.swtDefaults().hint(80, SWT.DEFAULT).applyTo(scanVarLabel);
			Label positionLabel = getToolkit().createLabel(labelComposite, "position");
			GridDataFactory.swtDefaults().hint(85, SWT.DEFAULT).applyTo(positionLabel);
			getToolkit().createLabel(parent, "mode");
			getToolkit().createLabel(parent, "preset");
		}
		
		private void addCommandListener(final ISicsCommandElement command) {
			if (command instanceof AbstractModelObject)
				((AbstractModelObject) command).addPropertyChangeListener(new PropertyChangeListener() {
					
					public void propertyChange(PropertyChangeEvent arg0) {
//						if (arg0.getPropertyName().equals("parameter_add") || arg0.getPropertyName().equals(
//								"parameter_remove") || arg0.getPropertyName().equals("preset"))
						notifyPropertyChanged(command, arg0);
						if (arg0.getPropertyName().equals("parameter_add")){
							Object parameter = arg0.getNewValue();
							if (parameter instanceof AbstractScanParameter)
								((AbstractScanParameter) parameter).addPropertyChangeListener(this);
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

	@Override
	public ITask newThisTask() {
		return new HmmscanTask();
	}
}
