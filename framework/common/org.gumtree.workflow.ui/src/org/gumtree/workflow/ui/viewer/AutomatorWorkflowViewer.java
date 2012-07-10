/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.workflow.ui.viewer;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.nebula.widgets.pgroup.ext.MenuBasedGroup;
import org.eclipse.nebula.widgets.pgroup.ext.MenuBasedGroupStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.service.eventbus.IEvent;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.resource.UIResourceManager;
import org.gumtree.ui.util.resource.UIResources;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.TaskState;
import org.gumtree.workflow.ui.WorkflowState;
import org.gumtree.workflow.ui.events.TaskEvent;
import org.gumtree.workflow.ui.events.WorkflowEvent;
import org.gumtree.workflow.ui.events.WorkflowStateEvent;
import org.gumtree.workflow.ui.events.WorkflowStructuralEvent;
import org.gumtree.workflow.ui.internal.Activator;
import org.gumtree.workflow.ui.internal.InternalImage;
import org.gumtree.workflow.ui.util.ITaskDescriptor;
import org.gumtree.workflow.ui.util.WorkflowFactory;
import org.gumtree.workflow.ui.util.WorkflowUI;
import org.gumtree.workflow.ui.util.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richclientgui.toolbox.progressIndicator.ImageSequencer;

public class AutomatorWorkflowViewer extends AbstractWorkflowViewer {

	private Logger logger = LoggerFactory.getLogger(AutomatorWorkflowViewer.class);
	
	private Label startPauseButton;
	
	private Label stopButton;
	
	private Label saveButton;
	
	private Font normalFont;
	
	private Font boldFont;
	
	private Map<ITask, TaskUIContext> taskUIContexts;
	
	private IEventHandler<WorkflowEvent> workfloEventHandler;
	
	private IEventHandler<TaskEvent> taskEventHandler;
	
	private UIResourceManager resourceManager;
	
	private ScrolledForm form;
	
	private SashForm toolSashForm;
	
	private SashForm logSashForm;
	
	private CLabel logLabel;
	
	private TableViewer logViewer;
	
	private Stack<IEvent> eventLogStack;
	
	private TaskToolbar toolbar;
	
	private boolean toolShown;
	
	private boolean logShown;
	
	@Override
	protected void createViewerControl(Composite parent) {
		normalFont = parent.getFont();
		boldFont = UIResources.getDefaultFont(SWT.BOLD);
		resourceManager = new UIResourceManager(Activator.PLUGIN_ID, parent);
		eventLogStack = new Stack<IEvent>();
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(0, 0).applyTo(parent);
		parent.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		
		// Part 1
		creatControlArea(parent);
		
		// Part 2
		toolSashForm = new SashForm(parent, SWT.HORIZONTAL);
		createToolArea(toolSashForm);
		logSashForm = new SashForm(toolSashForm, SWT.VERTICAL);
		form = getToolkit().createScrolledForm(logSashForm);
		form.getBody().setLayout(new FillLayout());
		Composite composite = getToolkit().createComposite(form.getBody());
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);
		createTaskArea(composite);
		createInformationArea(logSashForm);
		logSashForm.setWeights(new int[] {5, 0});
		toolSashForm.setWeights(new int[] {3, 10});
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(toolSashForm);
		
		// Part 3
		createStatusArea(parent);
	}

	private void creatControlArea(Composite parent) {
		Composite controlArea = getToolkit().createComposite(parent);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(controlArea);
		controlArea.setLayout(new GridLayout(4, false));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(controlArea);
		String title = WorkflowUtils.getWorkflowTitle(getWorkflow());
		Label titleLabel = getToolkit().createLabel(controlArea, title);
		Font titleFont = resourceManager.createRelativeFont(titleLabel.getFont(), 8, SWT.BOLD);
		titleLabel.setFont(titleFont);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(true, false).applyTo(titleLabel);
		startPauseButton = getToolkit().createLabel(controlArea, "");
		startPauseButton.setImage(InternalImage.PLAY.getImage());
		startPauseButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		startPauseButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				// Clear status
				for (TaskUIContext context : taskUIContexts.values()) {
					context.stackLayout.topControl = null;
					context.statusComposite.layout();
				}
				// Schedule the workflow rather than running it directly
				WorkflowUI.getWorkflowExecutor().schedule(getWorkflow());
			}
		});
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(startPauseButton);
		stopButton = getToolkit().createLabel(controlArea, "");
		stopButton.setImage(InternalImage.STOP.getImage());
		stopButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		stopButton.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(stopButton);
		stopButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				WorkflowUI.getWorkflowExecutor().stop(getWorkflow());
			}
		});
		saveButton = getToolkit().createLabel(controlArea, "");
		saveButton.setImage(InternalImage.SAVE.getImage());
		saveButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(saveButton);
		saveButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				SaveAsDialog dialog = new SaveAsDialog(parentShell);
				dialog.open();
				IPath filePath = dialog.getResult();
				if (filePath != null) {
					// Fix file extension
					if (filePath.getFileExtension() == null) {
						filePath = filePath.addFileExtension("gwf");
					}
					IWorkspace workspace= ResourcesPlugin.getWorkspace();
					IFile file= workspace.getRoot().getFile(filePath);
					try {
						OutputStream out = new FileOutputStream(file.getLocation().toFile());
						WorkflowFactory.saveWorkflow(getWorkflow(), out);
						// Refresh UI
						file.getParent().refreshLocal(1, new NullProgressMonitor());
					} catch (Exception error) {
						logger.error("Cannot save file " + file.getName(), error);
					}
				}
			}
		});
		// Workflow event
		workfloEventHandler = new IEventHandler<WorkflowEvent>() {
			public void handleEvent(WorkflowEvent event) {
				if (event instanceof WorkflowStateEvent) {
					handleStateEvent((WorkflowStateEvent) event); 
				} else if (event instanceof WorkflowStructuralEvent) {
					handleStructuralEvent((WorkflowStructuralEvent) event);
				}
			}
			public void handleStateEvent(WorkflowStateEvent event) {
				if (event.getState().equals(WorkflowState.RUNNING) || 
						event.getState().equals(WorkflowState.SCHEDULED)) {
					eventLogStack.clear();
					SafeUIRunner.asyncExec(new SafeRunnable() {
						public void run() throws Exception {
							startPauseButton.setEnabled(false);
							stopButton.setEnabled(true);
						}						
					});
				} else if (event.getState().equals(WorkflowState.STOPPING)) {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						public void run() throws Exception {
							startPauseButton.setEnabled(false);
							stopButton.setEnabled(false);
						}						
					});
				} else  {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						public void run() throws Exception {
							startPauseButton.setEnabled(true);
							stopButton.setEnabled(false);
						}						
					});
				}
				// Store in the log
				eventLogStack.add(0, event);
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						logViewer.setInput(eventLogStack.toArray(new IEvent[eventLogStack.size()]));
					}
				});
			}
			public void handleStructuralEvent(WorkflowStructuralEvent event) {
				redrawUI();
			}
		};
		getWorkflow().addEventListener(workfloEventHandler);
		
		taskEventHandler = new IEventHandler<TaskEvent>() {
			public void handleEvent(TaskEvent event) {
				updateTaskGroup(event.getPublisher(), event.getState());
			}				
		};
	}
	
	private void createToolArea(Composite parent) {
		toolShown = true;
		toolbar = new TaskToolbar(getWorkflow());
		toolbar.createPartControl(parent);
	}
	
	private void createTaskArea(Composite parent) {
//		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(form);
		taskUIContexts = new HashMap<ITask, TaskUIContext>();
		
		// Render tasks
		for (final ITask task : getWorkflow().getTasks()) {
			if (!task.isVisible()) {
				continue;
			}
			
			/*****************************************************************
			 * Create progress indicator
			 *****************************************************************/
			Composite statusComposite = getToolkit().createComposite(parent);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).grab(false, false).applyTo(statusComposite);
			StackLayout stackLayout = new StackLayout();
			statusComposite.setLayout(stackLayout);
			
			Label tickLabel = getToolkit().createLabel(statusComposite, "");
			tickLabel.setImage(InternalImage.TICK.getImage());
			
			Label stopLabel = getToolkit().createLabel(statusComposite, "");
			stopLabel.setImage(InternalImage.ERROR.getImage());
			
			ImageSequencer imageSequencer = new ImageSequencer(statusComposite, SWT.NONE, InternalImage.getIndictorImages(), 150, true);
			getToolkit().adapt(imageSequencer);
			
			stackLayout.topControl = null;
			
			/*****************************************************************
			 * Create task holder
			 *****************************************************************/
			final MenuBasedGroup group = new MenuBasedGroup(parent, SWT.SMOOTH);
			Menu menu = group.getMenu();
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText("Remove");
			item.setImage(InternalImage.DELETE.getImage());
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					getWorkflow().removeTask(task);
					// TODO: remove task listener
				}
			});
			// Drag support
			DragSource dragSource = new DragSource(group, DND.DROP_MOVE);
			dragSource.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
			dragSource.addDragListener(new DragSourceAdapter() {
				public void dragFinished(DragSourceEvent event) {
		    		LocalSelectionTransfer.getTransfer().setSelection(null);
		    	}
				public void dragSetData(DragSourceEvent event){
					if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
						LocalSelectionTransfer.getTransfer().setSelection(new StructuredSelection(task));
					}
				}
			});
			// Drop support
			DropTarget dropTarget = new DropTarget(group, DND.DROP_MOVE);
			dropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
			dropTarget.addDropListener(new DropTargetAdapter() {
				public void drop(DropTargetEvent event){
					if (event.data instanceof IStructuredSelection) {
						int index = getWorkflow().getTasks().indexOf(task);
						Point relativePoint = group.toControl(new Point(event.x, event.y));
						int middlePos = group.getBounds().height / 2;
						if (relativePoint.y > middlePos) {
							index = index + 1;
						}
						Object selection = ((IStructuredSelection) event.data).getFirstElement();
						if (selection instanceof ITaskDescriptor) {
							addNewTask((ITaskDescriptor) selection, index);
						} else if (selection instanceof ITask) {
							getWorkflow().setTask(index, (ITask) selection);
						}
					}
				}
			});
			
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(group);
			group.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			group.setText(task.getLabel());
			group.setImage(task.getIcon());
			group.setLayout(new FillLayout());
			taskUIContexts.put(task, new TaskUIContext(group, statusComposite,
					stackLayout, imageSequencer, tickLabel, stopLabel));
			// Update group colour on task state event
			task.addEventListener(taskEventHandler);
			
			/*****************************************************************
			 * Create task UI
			 *****************************************************************/
			Composite groupBody = getToolkit().createComposite(group);
			groupBody.setLayout(new FillLayout());			
			ITaskView taskView = createTaskView(task);
			if (taskView != null) {
				taskView.createPartControl(groupBody);
			} else {
				createErrorTaskView(groupBody);
			}
			
			/*****************************************************************
			 * Draw arrow between task
			 *****************************************************************/
			if (!WorkflowUtils.isLastVisibleTask(getWorkflow(), task)) {
				// Empty space
				getToolkit().createLabel(parent, "");
				// Actual arrow
				Label imageLabel = getToolkit().createLabel(parent, "");
				imageLabel.setImage(InternalImage.NEXT_STEP.getImage());
				GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(imageLabel);
			}
		}
		
		/*********************************************************************
		 * Drag and drop support
		 *********************************************************************/
		DropTarget dropTarget = new DropTarget(parent, DND.DROP_MOVE);
		dropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
		dropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (event.data instanceof IStructuredSelection) {
					Object selection = ((IStructuredSelection) event.data).getFirstElement();
					if (selection instanceof ITaskDescriptor) {
						addNewTask((ITaskDescriptor) selection);
					}
				}
			}
		});
	}
	
	private void createInformationArea(Composite parent) {
		logViewer = new TableViewer(parent, SWT.V_SCROLL | SWT.BORDER);
		logViewer.setContentProvider(new ArrayContentProvider());
		logViewer.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				if (element instanceof WorkflowStateEvent) {
					WorkflowStateEvent event = (WorkflowStateEvent) element;
					if (event.getState() == WorkflowState.FINISHED) {
						return InternalImage.TICK.getImage();
					}
				}
				return null;
			}
			public String getText(Object element) {
				if (element instanceof WorkflowStateEvent) {
					return ((WorkflowStateEvent) element).getMessgae();
				}
				return element == null ? "" : element.toString();
			}
		});
	}
	
	private void createStatusArea(Composite parent) {
		Composite composite = getToolkit().createComposite(parent);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(5).spacing(2, 2).applyTo(composite);
		
		final Label toolButton = getToolkit().createLabel(composite, "");
		toolButton.setImage(InternalImage.TOOL_SHOWED.getImage());
		toolButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		toolButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (toolShown) {
					toolSashForm.setWeights(new int[] {0, 10});
					toolButton.setImage(InternalImage.TOOL_HIDED.getImage());
					toolShown = false;
				} else {
					toolSashForm.setWeights(new int[] {3, 10});
					toolButton.setImage(InternalImage.TOOL_SHOWED.getImage());
					toolShown = true;
				}
			}
		});
		
		Label separator = getToolkit().createLabel(composite, "", SWT.SEPARATOR);
		GridDataFactory.swtDefaults().hint(SWT.DEFAULT, 16).applyTo(separator);
		
		final Label logButton = getToolkit().createLabel(composite, "");
		logButton.setImage(InternalImage.LOG_HIDED.getImage());
		logButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		logButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (logShown) {
					logSashForm.setWeights(new int[] {5, 0});
					logButton.setImage(InternalImage.LOG_HIDED.getImage());
					logShown = false;
				} else {
					logSashForm.setWeights(new int[] {5, 1});
					logButton.setImage(InternalImage.LOG_SHOWED.getImage());
					logShown = true;
				}
			}
		});

		separator = getToolkit().createLabel(composite, "", SWT.SEPARATOR);
		GridDataFactory.swtDefaults().hint(SWT.DEFAULT, 16).applyTo(separator);
		
		logLabel = new CLabel(composite, SWT.NONE);
		getToolkit().adapt(logLabel, false, false);
		logLabel.setImage(InternalImage.CHECKED.getImage());
		logLabel.setText("Workflow completed");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(logLabel);
	}
	
	/*************************************************************************
	 * UI hepler methods
	 *************************************************************************/
	
	private void updateTaskGroup(ITask task, final TaskState state) {
		final TaskUIContext uiContext = taskUIContexts.get(task);
		if (uiContext != null) {
//			if (state.equals(TaskState.RUNNING)) {
//				System.out.println("Task " + task.getLabel() + " is running");
//			} else if (state.equals(TaskState.COMPLETED)) {
//				System.out.println("Task " + task.getLabel() + " is completed");
//			}
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					PGroup group = uiContext.group;
					ImageSequencer sequencer = uiContext.imageSequencer;
					if (state.equals(TaskState.RUNNING)) {
						// Set font
						group.setFont(boldFont);
						group.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
						// Set group
						MenuBasedGroupStrategy strategy = (MenuBasedGroupStrategy) group.getStrategy();
						strategy.setBackground(new Color[] { Display.getDefault().getSystemColor(SWT.COLOR_BLACK) }, new int[] {} );
						strategy.setBorderColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
						strategy.update();
						// Set progress image
						sequencer.startSequence();
						uiContext.stackLayout.topControl = sequencer;
						uiContext.statusComposite.layout();
					} else if (state.equals(TaskState.ERROR)) {
						group.setStrategy(new MenuBasedGroupStrategy());
						group.setFont(normalFont);
						sequencer.stopSequence();
						uiContext.stackLayout.topControl = uiContext.stopLabel;
						uiContext.statusComposite.layout();
					} else if (state.equals(TaskState.COMPLETED)) {
						group.setStrategy(new MenuBasedGroupStrategy());
						group.setFont(normalFont);
						sequencer.stopSequence();
						uiContext.stackLayout.topControl = uiContext.tickLabel;
						uiContext.statusComposite.layout();
					} else if (state.equals(TaskState.IDLE)) {
						group.setStrategy(new MenuBasedGroupStrategy());
						group.setFont(normalFont);
						sequencer.stopSequence();
						uiContext.stackLayout.topControl = null;
						uiContext.statusComposite.layout();
					}
				}
			});
		}
	}
	
	protected void redrawUI() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				for (ITask task : getWorkflow().getTasks()) {
					task.removeEventListener(taskEventHandler);
				}
				for (ITaskView taskView : getTaskViews()) {
					taskView.dispose();
				}
				getTaskViews().clear();
				for (Control child : form.getBody().getChildren()) {
					child.dispose();
				}
				taskUIContexts.clear();
				Composite composite = getToolkit().createComposite(form.getBody());
				GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);
				createTaskArea(composite);
				handleTaskViewRefreshEvent(null);
			}
		});
	}
	
	protected void handleTaskViewRefreshEvent(ITaskView taskView) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (form != null && !form.isDisposed()) {
					form.layout(true, true);
					form.reflow(true);
				}
			}
		});
	}
	
	public void setFocus() {
		toolbar.setFocus();
	}
	
	public void dispose() {
		startPauseButton = null;
		stopButton = null;
		normalFont = null;
		boldFont = null;
		form = null;
		toolSashForm = null;
		logSashForm = null;
		logLabel = null;
		logViewer = null;
		taskEventHandler = null;
		toolbar = null;
		resourceManager = null;
		if (eventLogStack != null) {
			eventLogStack.clear();
			eventLogStack = null;
		}
		if (taskUIContexts != null) {
			taskUIContexts.clear();
			taskUIContexts = null;
		}
		if (workfloEventHandler != null) {
			getWorkflow().removeEventListener(workfloEventHandler);
		}
		super.dispose();
	}

	/*************************************************************************
	 * Non-UI hepler methods
	 *************************************************************************/
	
	private void addNewTask(ITaskDescriptor taskDesc) {
		try {
			WorkflowUtils.addNewTask(getWorkflow(), taskDesc);
		} catch (ObjectCreateException e) {
			logger.error("Failed to create task " + taskDesc.getClassname(), e);
		}
	}
	
	private void addNewTask(ITaskDescriptor taskDesc, int index) {
		try {
			WorkflowUtils.addNewTask(getWorkflow(), taskDesc, index);
		} catch (ObjectCreateException e) {
			logger.error("Failed to create task " + taskDesc.getClassname(), e);
		}
	}

	/*************************************************************************
	 * Hepler classes
	 *************************************************************************/
	
	private class TaskUIContext {
		private PGroup group;
		private Composite statusComposite;
		private StackLayout stackLayout;
		private ImageSequencer imageSequencer;
		private Label tickLabel;
		private Label stopLabel;
		private TaskUIContext(PGroup group, Composite statusComposite,
				StackLayout stackLayout, ImageSequencer imageSequencer,
				Label tickLabel, Label stopLabel) {
			this.group = group;
			this.statusComposite = statusComposite;
			this.stackLayout = stackLayout;
			this.imageSequencer = imageSequencer;
			this.tickLabel = tickLabel;
			this.stopLabel = stopLabel;
		}
	}
	
}
