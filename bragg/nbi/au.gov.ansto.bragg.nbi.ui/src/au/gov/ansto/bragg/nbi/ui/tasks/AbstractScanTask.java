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
package au.gov.ansto.bragg.nbi.ui.tasks;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.gumnix.sics.batch.ui.CommandBlockTask;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.TaskState;

import au.gov.ansto.bragg.nbi.ui.core.commands.AbstractScanCommand;



/**
 * @author nxi
 * Created on 11/08/2009
 */
public abstract class AbstractScanTask extends CommandBlockTask {

	private List<ITaskPropertyChangeListener> taskPropertyChangeListeners = 
		new ArrayList<ITaskPropertyChangeListener>();
	
	/**
	 * 
	 */
	public AbstractScanTask() {
		super();
	}

//	public abstract String getTitle();

	public float getEstimatedTime(){
		float estimatedTime = 0;
		for (ISicsCommandElement command : getDataModel().getCommands()){
			if (command instanceof AbstractScanCommand){
				AbstractScanCommand scanCommand = (AbstractScanCommand) command;
				estimatedTime += scanCommand.getEstimatedTime();
			}
		}
		return estimatedTime;
	}

	public String getTimeUnits(){
		ISicsCommandElement[] commands = getDataModel().getCommands();
		if (commands != null && commands.length > 0){
			try{
				return ((AbstractScanCommand) commands[0]).getEstimationUnits();
			}catch (Exception e) {
			}
		}
		return "";
	}
	
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
//		PlatformUtils.getPlatformEventBus().postEvent(new TaskEvent(this, TaskState.UPDATED));
		setState(TaskState.UPDATED);
	}
	
//	@Override
//	public String getLabel() {
//		String title = getTitle();
//		if (title == null) {
//			return super.getLabel();
//		} else {
//			return title;
//		}
//	}
	
	public void clearPropertyChangeListeners(){
		taskPropertyChangeListeners.clear();
	}

	@Override
	public void setLabel(String label) {
		super.setLabel(label);
		for (ISicsCommandElement command : getDataModel().getCommands()){
			if (command instanceof AbstractScanCommand){
				AbstractScanCommand scanCommand = (AbstractScanCommand) command;
				scanCommand.setTitle(label);
			}
		}
	}
	
	public abstract ITask newThisTask();
	
//	@Override
//	public String getLabel() {
//		return getTitle();
//	}
}
