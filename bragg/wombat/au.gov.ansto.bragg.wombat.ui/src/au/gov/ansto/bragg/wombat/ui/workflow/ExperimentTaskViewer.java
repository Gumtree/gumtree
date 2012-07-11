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
package au.gov.ansto.bragg.wombat.ui.workflow;
import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.pgroup.ext.MenuBasedGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.batch.ui.CommandBlockTask;
import org.gumtree.gumnix.sics.batch.ui.SicsVisualBatchViewer;
import org.gumtree.gumnix.sics.batch.ui.commands.LineScriptCommand;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.gumnix.sics.ui.util.SicsBatchViewer;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.ITaskView;

import au.gov.ansto.bragg.wombat.exp.task.AbstractWombatTask;
import au.gov.ansto.bragg.wombat.exp.task.ChooseWavelengthTask;
import au.gov.ansto.bragg.wombat.exp.task.HeaderInformationBlockTask;
import au.gov.ansto.bragg.wombat.exp.task.RunTScanTask;
import au.gov.ansto.bragg.wombat.exp.task.AbstractWombatTask.ITaskPropertyChangeListener;
import au.gov.ansto.bragg.wombat.ui.internal.Activator;

/**
 * @author nxi
 * Created on 24/07/2009
 */
public class ExperimentTaskViewer extends SicsVisualBatchViewer {

	private Text estimatedTime;
	protected Button addSicsBlockButton;
	protected Menu addTaskMenu;
	/**
	 * 
	 */
	public ExperimentTaskViewer() {
		super();
	}

	@Override
	protected void createViewerControl(Composite parent) {
		super.createViewerControl(parent);
//		addBlockButton.dispose();
		Composite estimatedTimeComposite = getToolkit().createComposite(designArea);
		estimatedTimeComposite.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseScrolled(MouseEvent e) {
				System.out.println("********************************************************");
			}
		});
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(3).applyTo(estimatedTimeComposite);
		Group timeGroup = new Group(estimatedTimeComposite, SWT.NONE);
		timeGroup.setText("Estimated Time");
		getToolkit().adapt(timeGroup);
		GridLayoutFactory.swtDefaults().margins(1, 0).numColumns(2).applyTo(timeGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(timeGroup);
//		getToolkit().createLabel(timeGroup, "Estimated Time: ");
		estimatedTime = getToolkit().createText(timeGroup, "");
//		estimatedTime.;
		GridDataFactory.fillDefaults().grab(true, false).applyTo(estimatedTime);
		estimatedTime.setEditable(false);
//		estimatedTime.setText(String.valueOf((int) getEstimatedTime()));
		estimatedTime.setText(getTimeString(getEstimatedTime()));
//		getToolkit().createLabel(timeGroup, "Seconds");
		estimatedTimeComposite.moveAbove(saveButton);
//		addBlockButton.setParent(estimatedTimeComposite);
//		addBlockButton.moveBelow(timeGroup);
//		addBlockButton.setText("Add Scan Block");
		addBlockButton.dispose();
		addSicsBlockButton = getToolkit().createButton(estimatedTimeComposite, "Add Task >>", SWT.PUSH);
		addSicsBlockButton.moveAbove(saveButton);
		addSicsBlockButton.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
					"icons/drop_to_frame.gif").createImage());
		addSicsBlockButton.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent arg0) {
//				addSicsBlock();
				createAddTaskMenu();
			}
			
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(estimatedTimeComposite);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(addBlockButton);
		GridDataFactory.fillDefaults().grab(true, false).hint(160, SWT.DEFAULT).applyTo(addSicsBlockButton);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(saveButton);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(loadButton);
//		Transfer[] transfers = new Transfer[] { FileTransfer.getInstance() };
//		DropTarget dropTarget = new DropTarget(parent, DND.DROP_MOVE);
//	    dropTarget.setTransfer(transfers);
//	    dropTarget.addDropListener(new DropTargetAdapter() {
//			public void drop(DropTargetEvent event) {
//				Object adaptable = event.data;
//				if(adaptable instanceof String[]) {
//					try {
//						String filename = ((String[]) adaptable)[0];
//						InputStream input = new FileInputStream(filename);
//						IWorkflow workflow = WorkflowFactory.createWorkflow(input);
//						input.close();
//						setWorkflow(workflow);
//						refreshUI();
//					} catch (Exception error) {
//						LoggerFactory.getLogger(this.getClass()).error("Cannot open file ", error);
//					}
//				}
//			}
//		});
	}
	
	private void createAddTaskMenu() {
		if (addTaskMenu == null){
			addTaskMenu = new Menu(addSicsBlockButton);
			MenuItem wavelenthTaskItem= new MenuItem(addTaskMenu, SWT.PUSH);
			wavelenthTaskItem.setText("Choose Wavelength");
			wavelenthTaskItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
					"icons/lambda_16x16.gif").createImage());
			wavelenthTaskItem.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					addTaskBlock(new ChooseWavelengthTask());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {

				}
			});
			MenuItem runTTaskItem = new MenuItem(addTaskMenu, SWT.PUSH);
			runTTaskItem.setText("Temperature Scan");
			runTTaskItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
			"icons/t16x16.png").createImage());
			runTTaskItem.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					addTaskBlock(new RunTScanTask());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {

				}
			});
			MenuItem sicsTaskItem = new MenuItem(addTaskMenu, SWT.PUSH);
			sicsTaskItem.setText("SICS Commands");
			sicsTaskItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
			"icons/script_wiz.gif").createImage());
			sicsTaskItem.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					addSicsBlock();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {

				}
			});			
		}
		addTaskMenu.setVisible(true);
	}

	private void addTaskBlock(AbstractWombatTask newTask) {
		getWorkflow().addTask(newTask);
		refreshUI();		
	}

	private float getEstimatedTime() {
		float time = 0;
		// TODO need work   
//		for (ITask task : getWorkflow().getTasks()){
//			if (task instanceof AbstractKowariScanTask)
//				time += ((AbstractKowariScanTask) task).getEstimatedTime();
//		}
		return time;
	}

	private String getTimeString(float seconds){
		if (seconds < 360)
			return String.valueOf((int) seconds) + " seconds";
		if (seconds < 3600 * 2)
			return String.valueOf(((int) (seconds / 60)) + 1) + " minites";
		if (seconds < 3600 * 5){
			double remainder = Math.IEEEremainder(seconds, 3600);
			if (remainder < 0)
				remainder = 3600 + remainder;
			int minites = (int) (remainder / 60);
			return String.valueOf((int) (seconds / 3600)) + " hours " + (
					minites > 1 ? String.valueOf(minites) + " minites" : "");
		}
		return String.valueOf((int) Math.ceil(seconds / 3600)) + " hours";
	}
	
	@Override
	protected void createCommandBlocksArea(final Composite parent) {
		
		
		for (Control child : parent.getChildren()) {
			child.dispose();
		}
		for (ITaskView taskView : getTaskViews()) {
			destoryTaskView(taskView);
		}
		getTaskViews().clear();
		
		GridLayoutFactory.swtDefaults().margins(1, 1).applyTo(parent);
		
		for (final ITask task : getWorkflow().getTasks()) {
			if (task instanceof HeaderInformationBlockTask) {
				
				final MenuBasedGroup group = new MenuBasedGroup(parent, SWT.SMOOTH);
				group.setClickExpandEnabled(true);
				group.setLayout(new FillLayout());
				getToolkit().adapt(group);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
				
				// Header display
//				final ISicsCommandBlock commandBlock = ((CommandBlockTask) task).getDataModel();
//				if (commandBlock.getName() != null) {
				group.setText("Header Information");
//				}
				
				createTaskView(group, task);
			}
			else if (task instanceof AbstractWombatTask) {
				
				final MenuBasedGroup group = new MenuBasedGroup(parent, SWT.SMOOTH);
				group.setClickExpandEnabled(true);
				group.setLayout(new FillLayout());
				getToolkit().adapt(group);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
				
				// Header display
//				final ISicsCommandBlock commandBlock = ((CommandBlockTask) task).getDataModel();
//				if (commandBlock.getName() != null) {
				group.setText(((AbstractWombatTask) task).getTitle());
//				}
				Menu menu = group.getMenu();
				addManuItems(group, menu, (AbstractWombatTask) task);

				createTaskView(group, task);
				
				((AbstractWombatTask) task).addPropertyChangeListener(new ITaskPropertyChangeListener() {
					
					public void propertyChanged(ISicsCommandElement command,
							PropertyChangeEvent event) {
						if (estimatedTime != null && !estimatedTime.isDisposed())
//							estimatedTime.setText(String.valueOf((int) ((AbstractKowariScanTask) task
//									).getEstimatedTime()));
//							estimatedTime.setText(String.valueOf((int) getEstimatedTime()));
							estimatedTime.setText(getTimeString(getEstimatedTime()));
						group.setText(((AbstractWombatTask) task).getTitle());
					}
				});
//				if (estimatedTime != null && !estimatedTime.isDisposed())
//					estimatedTime.setText(String.valueOf((int) ((AbstractKowariScanTask) task
//							).getEstimatedTime()));
			}
			
			else if (task instanceof CommandBlockTask) {
				
				final MenuBasedGroup group = new MenuBasedGroup(parent, SWT.SMOOTH);
				group.setClickExpandEnabled(true);
				group.setLayout(new FillLayout());
				getToolkit().adapt(group);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
				
				// Header display
//				final ISicsCommandBlock commandBlock = ((CommandBlockTask) task).getDataModel();
//				if (commandBlock.getName() != null) {
				group.setText("General SICS Command");
//				}
				Menu menu = group.getMenu();
				addManuItems(group, menu, (CommandBlockTask) task);
				createTaskView(group, task);
//				((CommandBlockTask) task).addPropertyChangeListener(new ITaskPropertyChangeListener() {
//					
//					@Override
//					public void propertyChanged(ISicsCommandElement command,
//							PropertyChangeEvent event) {
//						group.setText(((CommandBlockTask) task).getTitle());
//					}
//				});
			}
			
			
		}
		if (estimatedTime != null && !estimatedTime.isDisposed())
//			estimatedTime.setText(String.valueOf((int) getEstimatedTime()));
			estimatedTime.setText(getTimeString(getEstimatedTime()));
	}
	
	@Override
	protected void createRunArea(Composite parent) {
		parent.setLayout(new FillLayout());
		final SicsBatchViewer batchViewer = new SicsBatchViewer();
		batchViewer.createPartControl(parent);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// This tab is selected
				if (tabFolder.getSelectionIndex() == 1) {
					String script = getGeneratedScript();
					if (script.trim().length() > 0){
						script = "# estimated time is " + estimatedTime.getText() + "\n" + script;
						batchViewer.setCommandText(script);
//						saveTempWorkflow();
					}
				}
			}
		});
	}
	
	private void addManuItems(final MenuBasedGroup group, Menu menu, final CommandBlockTask avoidTask) {
		if (avoidTask instanceof AbstractWombatTask){
//			if (!(avoidTask instanceof ScanNDTask)){
//				MenuItem taskItem = new MenuItem(menu, SWT.PUSH);
//				taskItem.setText(ScanNDTask.TASK_TITLE);
//				taskItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
//				"icons/r16x16.png").createImage());
//				taskItem.addSelectionListener(new SelectionAdapter() {
//					public void widgetSelected(SelectionEvent e) {
//						switchTask(avoidTask, new ScanNDTask());
//					}
//				});
//			}
//			if (!(avoidTask instanceof AdvancedScanTask)){
//				MenuItem taskItem = new MenuItem(menu, SWT.PUSH);
//				taskItem.setText(AdvancedScanTask.TASK_TITLE);
//				taskItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
//				"icons/c16x16.png").createImage());
//				taskItem.addSelectionListener(new SelectionAdapter() {
//					public void widgetSelected(SelectionEvent e) {
//						switchTask(avoidTask, new AdvancedScanTask());
//					}
//				});
//			}
//			if (!(avoidTask instanceof HmmscanTask)){
//				MenuItem taskItem = new MenuItem(menu, SWT.PUSH);
//				taskItem.setText(HmmscanTask.TASK_TITLE);
//				taskItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
//				"icons/t16x16.png").createImage());
//				taskItem.addSelectionListener(new SelectionAdapter() {
//					public void widgetSelected(SelectionEvent e) {
//						switchTask(avoidTask, new HmmscanTask());
//					}
//				});
//			}
		}
		List<ITask> tasks = getWorkflow().getTasks();
		if (tasks.size() > 1){
			new MenuItem(menu, SWT.SEPARATOR);

			// Remove operation
			MenuItem removeItem = new MenuItem(menu, SWT.PUSH);
			removeItem.setText("Remove");
			removeItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
			"icons/rem_item.gif").createImage());
			removeItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					removeCommandBlock(group, (CommandBlockTask) avoidTask);
				}
			});
			// Separator
			new MenuItem(menu, SWT.SEPARATOR);

			if (tasks.indexOf(avoidTask) > 1){
				// Move up
				MenuItem moveUpItem = new MenuItem(menu, SWT.PUSH);
				moveUpItem.setText("Move up");
				moveUpItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
				"icons/upward_nav.gif").createImage());
				moveUpItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						moveCommandBlockUp((CommandBlockTask) avoidTask);
					}
				});
			}

			if (tasks.indexOf(avoidTask) < tasks.size() - 1){
				// Move down
				MenuItem moveDownItem = new MenuItem(menu, SWT.PUSH);
				moveDownItem.setText("Move down");
				moveDownItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
				"icons/downward_nav.gif").createImage());
				moveDownItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						moveCommandBlockDown((CommandBlockTask) avoidTask);
					}
				});
			}
		}

//		if (!(avoidTask instanceof OneTempScanTask)){
//			MenuItem oneTempItem = new MenuItem(menu, SWT.PUSH);
//			oneTempItem.setText(OneTempScanTask.TASK_TITLE);
//			oneTempItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
//				"icons/t16x16.png").createImage());
//			oneTempItem.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent e) {
//					switchTask(avoidTask, new OneTempScanTask());
//				}
//			});
//		}
	}

	private void switchTask(CommandBlockTask oldTask, CommandBlockTask newTask) {
		int index = getWorkflow().getTasks().indexOf(oldTask);
		if (index < 0) 
			index = getWorkflow().getTasks().size();
		getWorkflow().insertTask(index, newTask);
		// Update data model
		if (getBatchScript() != null) {
//			oldTask.clearPropertyChangeListeners();
			getBatchScript().removeCommandBlock(oldTask.getDataModel());
//			getBatchScript().addCommandBlock(newTask.getDataModel());
//			getBatchScript().insertCommandBlock(index, newTask.getDataModel());
		}
		getWorkflow().removeTask(oldTask);
		// Refresh UI
		refreshUI();
//		estimatedTime.setText(String.valueOf(0));
		
	}

	private ITaskView createTaskView(Composite parent, ITask task){
		Composite viewArea = getToolkit().createComposite(parent);
		ITaskView taskView = createTaskView(task);
		taskView.createPartControl(viewArea);
		return taskView;
	}
	
	@Override
	protected void addCommandBlock() {
		List<ITask> tasks = getWorkflow().getTasks();
		ITask lastTask = tasks.get(tasks.size() - 1);
		if (lastTask instanceof AbstractWombatTask)
			getWorkflow().addTask(((AbstractWombatTask) lastTask).newThisTask());
		else
			getWorkflow().addTask(new CommandBlockTask());
			// Refresh UI
		refreshUI();
	}
	
	protected void moveCommandBlockUp(CommandBlockTask task) {
		int currentIndex = getWorkflow().getTasks().indexOf(task);
		if (currentIndex > 1) {
			// Update workflow structure
//			getWorkflow().removeTask(task);
//			getWorkflow().insertTask(currentIndex - 1, task);
			
			getWorkflow().swapTask(task, getWorkflow().getTasks().get(currentIndex - 1));
			// Update data model
			if (getBatchScript() != null) {
				getBatchScript().removeCommandBlock(task.getDataModel());
				getBatchScript().insertCommandBlock(currentIndex - 1, task.getDataModel());
			}
			// Refresh UI
			refreshUI();
		}
	}
	
	protected void addSicsBlock(){
		CommandBlockTask newTask = new CommandBlockTask();
		newTask.getDataModel().addCommand(new LineScriptCommand());
		getWorkflow().addTask(newTask);
		refreshUI();
	}
	


}
