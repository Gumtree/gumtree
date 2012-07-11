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
package au.gov.ansto.bragg.echidna.ui.views;

import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.pgroup.ext.MenuBasedGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.batch.ui.SicsVisualBatchViewer;
import org.gumtree.gumnix.sics.batch.ui.VisualBatchBuffer;
import org.gumtree.gumnix.sics.batch.ui.buffer.IBatchBuffer;
import org.gumtree.gumnix.sics.batch.ui.buffer.IBatchBufferManager;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsBatchScript;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.gumnix.sics.ui.util.SicsBatchViewer;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.ITaskView;

import au.gov.ansto.bragg.echidna.exp.task.AbstractEchidnaScanTask;
import au.gov.ansto.bragg.echidna.exp.task.AbstractEchidnaScanTask.ITaskPropertyChangeListener;
import au.gov.ansto.bragg.echidna.exp.task.DoRTScanTask;
import au.gov.ansto.bragg.echidna.exp.task.DoTempScanTask;
import au.gov.ansto.bragg.echidna.exp.task.HeaderInformationBlockTask;
import au.gov.ansto.bragg.echidna.exp.task.OneTempScanTask;
import au.gov.ansto.bragg.echidna.exp.task.SicsBlockTask;
import au.gov.ansto.bragg.echidna.exp.task.SicsScriptBlockTask;
import au.gov.ansto.bragg.echidna.ui.internal.Activator;

/**
 * @author nxi
 * Created on 24/07/2009
 */
public class ExperimentTaskViewer extends SicsVisualBatchViewer {

	private Text estimatedTime;
	private Button addToQueueButton;
	/**
	 * 
	 */
	public ExperimentTaskViewer() {
		super();
	}

	@Override
	protected void createViewerControl(final Composite parent) {
		super.createViewerControl(parent);
		int itemCount = tabFolder.getItemCount();
		if (itemCount == 2) {
			tabFolder.getItem(1).dispose();
			tabFolder.setSingle(true);
		}
		addBlockButton.dispose();
		Composite estimatedTimeComposite = getToolkit().createComposite(designArea, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(1, 0).numColumns(4).applyTo(estimatedTimeComposite);
		getToolkit().createLabel(estimatedTimeComposite, "Estimated Time: ");
		estimatedTime = getToolkit().createText(estimatedTimeComposite, "");
//		estimatedTime.;
		GridDataFactory.fillDefaults().grab(true, false).applyTo(estimatedTime);
		estimatedTime.setEditable(false);
		estimatedTime.setText(String.valueOf(getEstimatedTime()));
		getToolkit().createLabel(estimatedTimeComposite, "Hour(s)");
		estimatedTimeComposite.moveAbove(saveButton);
		addToQueueButton = getToolkit().createButton(estimatedTimeComposite, "Add to Queue", SWT.PUSH);
		addToQueueButton.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
					"icons/add_to_queue.gif").createImage());
		addToQueueButton.setToolTipText("Put tasks into run queue.");
		addToQueueButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Ensure the current workflow is valid
				if (getWorkflow() == null) {
					return;
				}
				ISicsBatchScript batchScript = getWorkflow().getContext()
						.getSingleValue(ISicsBatchScript.class);
				if (batchScript == null) {
					return;
				}
				// Ask for buffer name input
//				InputDialog dialog = new InputDialog(parent.getShell(),
//						"New Batch Buffer", "Enter new batch buffer name:",
//						"Batch", new IInputValidator() {
//							public String isValid(String newText) {
//								if (newText == null || newText.length() == 0) {
//									return "Buffer name is empty";
//								}
//								return null;
//							}
//						});
//				if (dialog.open() == Window.CANCEL) {
//					return;
//				}
				// Create a unique batch name
				String batchName = "batch";
				String extension = getBatchDateString();
				batchName += extension;
				// Add to the queue
				IBatchBufferManager manager = ServiceUtils.getService(IBatchBufferManager.class);
//				IBatchBuffer buffer = new VisualBatchBuffer(dialog.getValue(), getWorkflow()); 
				IBatchBuffer buffer = new VisualBatchBuffer(batchName, getWorkflow());
				manager.getBatchBufferQueue().add(buffer);
				batchName = "EXP" + extension + ".wml";
				saveTempWorkflow(batchName);
				// Clean the viewer by swapping to a new workflow 
//				getWorkflowViewer().setWorkflow(WorkflowFactory.createEmptyWorkflow());
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(estimatedTimeComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(saveButton);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(loadButton);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(addToQueueButton);
	}
	
	protected String getBatchDateString() {
		Date date = Calendar.getInstance().getTime();
		return new SimpleDateFormat("yyMMddHHmmss").format(date);
	}
	
	private double getEstimatedTime() {
		for (ITask task : getWorkflow().getTasks()){
			if (task instanceof AbstractEchidnaScanTask)
				return ((AbstractEchidnaScanTask) task).getEstimatedTime();
		}
		return 0;
	}

	@Override
	protected void createCommandBlocksArea(Composite parent) {
		
		
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
			
			if (task instanceof SicsScriptBlockTask) {

				final MenuBasedGroup group = new MenuBasedGroup(parent, SWT.SMOOTH);
				group.setClickExpandEnabled(true);
				group.setLayout(new FillLayout());
				getToolkit().adapt(group);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(group);

				// Header display
				//				final ISicsCommandBlock commandBlock = ((CommandBlockTask) task).getDataModel();
				//				if (commandBlock.getName() != null) {
				group.setText(SicsScriptBlockTask.TITLE);
				//				}
				createTaskView(group, task);
//				((SicsBlockTask) task).addPropertyChangeListener(new ITaskPropertyChangeListener() {
//
//					@Override
//					public void propertyChanged(ISicsCommandElement command,
//							PropertyChangeEvent event) {
//						group.setText(((SicsBlockTask) task).getTitle());
//					}
//				});
			}

			if (task instanceof AbstractEchidnaScanTask) {
				
				final MenuBasedGroup group = new MenuBasedGroup(parent, SWT.SMOOTH);
				group.setClickExpandEnabled(true);
				group.setLayout(new FillLayout());
				getToolkit().adapt(group);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
				
				// Header display
//				final ISicsCommandBlock commandBlock = ((CommandBlockTask) task).getDataModel();
//				if (commandBlock.getName() != null) {
				group.setText(((AbstractEchidnaScanTask) task).getTitle());
//				}
				Menu menu = group.getMenu();
				addManuItems(group, menu, (AbstractEchidnaScanTask) task);

				createTaskView(group, task);
				
				((AbstractEchidnaScanTask) task).addPropertyChangeListener(new ITaskPropertyChangeListener() {
					
					public void propertyChanged(ISicsCommandElement command,
							PropertyChangeEvent event) {
						if (estimatedTime != null && !estimatedTime.isDisposed())
							estimatedTime.setText(String.valueOf(((AbstractEchidnaScanTask) task
									).getEstimatedTime()));
					}
				});
				if (estimatedTime != null && !estimatedTime.isDisposed())
					estimatedTime.setText(String.valueOf(((AbstractEchidnaScanTask) task
							).getEstimatedTime()));
			}
		}
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
//						saveTempWorkflow();
						script = "# estimated time = " + estimatedTime.getText() + " hour(s)\n" + script;
						batchViewer.setCommandText(script);
					}
				}
			}
		});
	}
	
	private void addManuItems(final MenuBasedGroup group, Menu menu, final AbstractEchidnaScanTask avoidTask) {
		if (!(avoidTask instanceof DoRTScanTask)){
			MenuItem doRTItem = new MenuItem(menu, SWT.PUSH);
			doRTItem.setText(DoRTScanTask.TASK_TITLE);
			doRTItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
					"icons/r16x16.png").createImage());
			doRTItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					switchTask(avoidTask, new DoRTScanTask());
				}
			});
		}
		if (!(avoidTask instanceof DoTempScanTask)){
			MenuItem doTempItem = new MenuItem(menu, SWT.PUSH);
			doTempItem.setText(DoTempScanTask.TASK_TITLE);
			doTempItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
					"icons/c16x16.png").createImage());
			doTempItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					switchTask(avoidTask, new DoTempScanTask());
				}
			});
		}
		if (!(avoidTask instanceof OneTempScanTask)){
			MenuItem oneTempItem = new MenuItem(menu, SWT.PUSH);
			oneTempItem.setText(OneTempScanTask.TASK_TITLE);
			oneTempItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
				"icons/t16x16.png").createImage());
			oneTempItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					switchTask(avoidTask, new OneTempScanTask());
				}
			});
		}
	}

	private void switchTask(AbstractEchidnaScanTask oldTask, AbstractEchidnaScanTask newTask) {
		getWorkflow().addTask(newTask);
		// Update data model
		if (getBatchScript() != null) {
			oldTask.clearPropertyChangeListeners();
			getBatchScript().removeCommandBlock(oldTask.getDataModel());
			getBatchScript().addCommandBlock(newTask.getDataModel());
		}
		getWorkflow().removeTask(oldTask);
		// Refresh UI
		refreshUI();
//		estimatedTime.setText(String.valueOf(0));
		
	}

	private void createTaskView(Composite parent, ITask task){
		Composite viewArea = getToolkit().createComposite(parent);
		ITaskView taskView = createTaskView(task);
		taskView.createPartControl(viewArea);
	}
	

}
