/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.workflow.ui.viewer2;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.pgroup.RectangleGroupStrategy;
import org.eclipse.nebula.widgets.pgroup.ext.MenuBasedGroup;
import org.eclipse.nebula.widgets.pgroup.ext.MenuBasedGroupStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.jface.ITreeNode;
import org.gumtree.ui.util.resource.UIResourceManager;
import org.gumtree.ui.widgets.FlatButton;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.TaskState;
import org.gumtree.workflow.ui.events.TaskEvent;
import org.gumtree.workflow.ui.events.TaskViewEvent;
import org.gumtree.workflow.ui.events.WorkflowEvent;
import org.gumtree.workflow.ui.events.WorkflowStructuralEvent;
import org.gumtree.workflow.ui.internal.Activator;
import org.gumtree.workflow.ui.internal.InternalImage;
import org.gumtree.workflow.ui.util.ITaskDescriptor;
import org.gumtree.workflow.ui.util.ITaskRegistry;
import org.gumtree.workflow.ui.util.WorkflowFactory;
import org.gumtree.workflow.ui.util.WorkflowUtils;
import org.gumtree.workflow.ui.viewer.TaskToolbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richclientgui.toolbox.progressIndicator.ImageSequencer;

public class WorkflowComposerViewer extends AbstractWorkflowViewerComponent {

	private static final Logger logger = LoggerFactory.getLogger(WorkflowComposerViewer.class);
	private static final String TITLE_CHANGER_DATA = "Title Changer Data";
	private UIContext c;
	
	private Map<ITask, TaskUIContext> taskUIContexts;
	
	private IEventHandler<WorkflowEvent> workfloEventHandler;
	
	private IEventHandler<TaskEvent> taskEventHandler;
	
	private IEventHandler<TaskViewEvent> taskViewEventHandler;
	
	private boolean isStatusVisable = true;
	private boolean keepAllExpanded = false;
	
	public WorkflowComposerViewer(Composite parent, int style) {
		super(parent, style);
		if ((style & SWT.Hide) > 0) {
			isStatusVisable = false;
		}
	}
	
	protected void componentDispose() {
		if (taskUIContexts != null) {
			for (TaskUIContext context : taskUIContexts.values()) {
				context.dispose();
			}
			taskUIContexts.clear();
			taskUIContexts = null;
		}
		if (workfloEventHandler != null) {
			getWorkflow().removeEventListener(workfloEventHandler);
			workfloEventHandler = null;
		}
		c = null;
		taskEventHandler = null;
		taskViewEventHandler = null;
	}
	
	protected void createUI() {
		/*********************************************************************
		 * Initialise
		 *********************************************************************/
		c = new UIContext();
		c.resourceManager = new UIResourceManager(Activator.PLUGIN_ID, this);
		c.defaultFont = JFaceResources.getDefaultFont();
		c.largeBold = c.resourceManager.createDefaultFont(10, SWT.BOLD);
		c.headerFont = c.resourceManager.createRelativeFont(c.defaultFont, 3, SWT.BOLD);
		taskUIContexts = new HashMap<ITask, TaskUIContext>();
		
		/*********************************************************************
		 * Setup listeners
		 *********************************************************************/
		workfloEventHandler = new IEventHandler<WorkflowEvent>() {
			public void handleEvent(WorkflowEvent event) {
				// Re-render on workflow structural change
				if (event instanceof WorkflowStructuralEvent) {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						public void run() throws Exception {
//							if (isDisposed()) {
//								return;
//							}
//							renderUI();
							syncWorkflowUI();
						}

					});
				}
			}
		};
		getWorkflow().addEventListener(workfloEventHandler);
		
		taskEventHandler = new IEventHandler<TaskEvent>() {
			// Update task status
			public void handleEvent(final TaskEvent event) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						if (isDisposed()) {
							return;
						}
						if (event.getState() == TaskState.UPDATED) {
							updateTitle(event.getPublisher());
						} else {
							updateTaskState(event);
						}
					}

				});
			}
		};
		
		taskViewEventHandler = new IEventHandler<TaskViewEvent>() {
			public void handleEvent(TaskViewEvent event) {
				// Refresh task
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						if (c == null || isDisposed()) {
							return;
						}
						if (c.form != null && !c.form.isDisposed()) {
							c.form.layout(true, true);
							c.form.reflow(true);
						}
					}
				});
			}
		};
		
		/*********************************************************************
		 * Draw widget
		 *********************************************************************/
		renderUI();
	}
		
	private void renderUI() {
		for (Control child : getChildren()) {
			child.dispose();
		}
		setLayout(new FillLayout());
		c.form = getToolkit().createScrolledForm(this);
		final Composite mainArea = c.form.getBody();
		GridLayoutFactory.swtDefaults().spacing(SWT.DEFAULT, 8).applyTo(mainArea);
		
		// Drop support
		DropTarget mainDropTarget = new DropTarget(mainArea, DND.DROP_MOVE);
		mainDropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
		mainDropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event){
				if (event.data instanceof IStructuredSelection) {
					Point relativePoint = mainArea.toControl(new Point(event.x, event.y));
					int index = 0;
					for (Control child : mainArea.getChildren()) {
						// The insertion index is based on the highest y coordinate
						if (relativePoint.y < (child.getBounds().y + child.getBounds().height)) {
							break;
						}
						index++;
					}
					Object selection = ((IStructuredSelection) event.data).getFirstElement();
					if (selection instanceof ITreeNode) {
						selection = ((ITreeNode) selection).getOriginalObject();
					}
					
					if (selection instanceof ITaskDescriptor) {
						try {
							WorkflowUtils.addNewTask(getWorkflow(), (ITaskDescriptor) selection, index);
							if (!keepAllExpanded) {
								for (TaskUIContext tc : taskUIContexts.values()) {
									if (tc != null && !tc.group.isDisposed()) {
										tc.group.setExpanded(false);
									}
								}
							}
//							getWorkflow().insertTask(index, ((ITaskDescriptor) selection).createNewTask());
						} catch (ObjectCreateException e) {
							logger.error("Failed to create task.", e);
							e.printStackTrace();
						}
					} else if (selection instanceof ITask) {
						getWorkflow().setTask(index, (ITask) selection);
					} else if (selection instanceof IFile) {
						try{
							File selectionFile = ((IFile) selection).getLocation().toFile();
							InputStream input = new FileInputStream(selectionFile);
							IWorkflow workflow = WorkflowFactory.createWorkflow(input);
							getWorkflow().insertTasks(index, workflow.getTasks());
						} catch (Exception e) {
							LoggerFactory.getLogger(this.getClass()).error(
									"Cannot open file " + ((File) selection).getAbsolutePath(), e);
						}
					}
					c.form.layout(true, true);
					c.form.reflow(true);
					c.form.forceFocus();
				} 
//				else {
//					Object adaptable = event.data;
//					if(adaptable instanceof String[]) {
//						try {
//							String filename = ((String[]) adaptable)[0];
//							InputStream input = new FileInputStream(filename);
//							IWorkflow workflow = WorkflowFactory.createWorkflow(input);
//							input.close();
//							setWorkflow(workflow);
//							refreshUI();
//						} catch (Exception error) {
//							LoggerFactory.getLogger(this.getClass()).error("Cannot open file ", error);
//						}
//					}
//				}
			}
		});
		
		// Render tasks
		for (final ITask task : getWorkflow().getTasks()) {
			if (!task.isVisible()) {
				continue;
			}
			// Render
			TaskUIContext tc = createTaskUI(task, c.form);
			
			// Restore
			TaskUIContext oldTC = taskUIContexts.get(task);
			if (oldTC != null) {
				if (oldTC.isDescriptionOpened) {
					GridDataFactory.fillDefaults().grab(true, false).applyTo(tc.infoArea);
					createDescriptionView(task, tc.infoArea);
					tc.descriptionButton.setSelection(true);
					tc.isDescriptionOpened = true;
				}
			}
			
			taskUIContexts.put(task, tc);
		}
		
		getParent().layout(true, true);
	}
	
	protected void refreshUI() {
		if (isDisposed()) {
			return;
		}
		for (Control child : getChildren()) {
			child.dispose();
		}
		componentDispose();
		createUI();
	}
	
	/*************************************************************************
	 * Hepler methods and classes
	 *************************************************************************/
	
	protected TaskUIContext createTaskUI(final ITask task, final ScrolledForm form) {
		final TaskUIContext tc = new TaskUIContext();
		final Composite mainArea = form.getBody();
		
		/*****************************************************************
		 * Create task holder
		 *****************************************************************/
		tc.group = new MenuBasedGroup(mainArea, SWT.SMOOTH);
		Menu menu = tc.group.getMenu();
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText("Remove");
		item.setImage(InternalImage.DELETE.getImage());
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getWorkflow().removeTask(task);
				// TODO: remove task listener
			}
		});
		final MenuItem keepAllExpandedItem = new MenuItem(menu, SWT.PUSH);
		keepAllExpandedItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setAllExpanded(!keepAllExpanded);
			}

		});
		keepAllExpandedItem.setText("Toggle Tasks Expanded/Folded");
		keepAllExpandedItem.setImage(InternalImage.LIST_SHOWED.getImage());

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(tc.group);
		tc.group.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		tc.group.setFont(c.headerFont);
		tc.group.setText(getTaskTitle(task));
		String colorString = task.getColorString();
		Color titleBGColor = null;
		if (colorString != null && colorString.trim().length() > 0) {
			titleBGColor = findColor(colorString);
			((RectangleGroupStrategy) tc.group.getStrategy()).setBackground(
				new Color[]{titleBGColor, Display.getCurrent().getSystemColor(SWT.COLOR_BLACK)}, 
				new int[]{100});
			tc.group.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		}
		tc.group.setImage(task.getIcon());
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(3, 2).applyTo(tc.group);
		
//		tc.group.addMouseListener(new MouseAdapter() {
//			public void mouseDown(MouseEvent e) {
//				System.out.println("Down");
//			}
//		});
		
		// Drag support
		DragSource dragSource = new DragSource(tc.group, DND.DROP_MOVE);
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
		DropTarget dropTarget = new DropTarget(tc.group, DND.DROP_MOVE);
		dropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
		dropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event){
				if (event.data instanceof IStructuredSelection) {
					int index = getWorkflow().getTasks().indexOf(task);
					Point relativePoint = tc.group.toControl(new Point(event.x, event.y));
					int middlePos = tc.group.getBounds().height / 2;
					if (relativePoint.y > middlePos) {
						index = index + 1;
					}
					Object selection = ((IStructuredSelection) event.data).getFirstElement();
					if (selection instanceof ITreeNode) {
						selection = ((ITreeNode) selection).getOriginalObject();
					}
					
					if (selection instanceof ITaskDescriptor) {
						try {
							WorkflowUtils.addNewTask(getWorkflow(), (ITaskDescriptor) selection, index);
							if (!keepAllExpanded) {
								for (TaskUIContext tc : taskUIContexts.values()) {
									if (tc != null && !tc.group.isDisposed()) {
										tc.group.setExpanded(false);
									}
								}
							}
//							getWorkflow().insertTask(index, ((ITaskDescriptor) selection).createNewTask());
						} catch (ObjectCreateException e) {
							logger.error("Failed to create task.", e);
						}
					} else if (selection instanceof ITask) {
						getWorkflow().setTask(index, (ITask) selection);
					} else if (selection instanceof IFile) {
						try{
							File selectionFile = ((IFile) selection).getLocation().toFile();
							InputStream input = new FileInputStream(selectionFile);
							IWorkflow workflow = WorkflowFactory.createWorkflow(input);
							getWorkflow().insertTasks(index, workflow.getTasks());
						} catch (Exception e) {
							LoggerFactory.getLogger(this.getClass()).error(
									"Cannot open file " + ((File) selection).getAbsolutePath(), e);
						}
					}
					c.form.layout(true, true);
					c.form.reflow(true);
					c.form.forceFocus();
				}
			}
		});
		
		/*****************************************************************
		 * Create task UI
		 *****************************************************************/
		final Composite groupBody = getToolkit().createComposite(tc.group);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(groupBody);
		GridLayoutFactory.swtDefaults().margins(2, 2).applyTo(groupBody);
		ITaskView taskView = createTaskView(task);
		if (taskView != null) {
			taskView.createPartControl(groupBody);
			tc.taskView = taskView;
		} else {
			createErrorTaskView(groupBody);
		}
		
		/*****************************************************************
		 * Create task info 
		 *****************************************************************/
		if (isStatusVisable) { 
		Label separator = getToolkit().createLabel(tc.group, "", SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(separator);
		
		Composite infoArea = getToolkit().createComposite(tc.group);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(infoArea);
		GridLayoutFactory.swtDefaults().numColumns(3).margins(1, 0).spacing(8, 0).applyTo(infoArea);
		
		tc.statusArea = getToolkit().createComposite(infoArea);
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(tc.statusArea);
		GridDataFactory.swtDefaults().hint(16, 16).applyTo(tc.statusArea);
		
		final FlatButton resultButton = new FlatButton(infoArea, SWT.NONE);
		getToolkit().adapt(resultButton);
		resultButton.setText("Results");
		
		tc.descriptionButton = new FlatButton(infoArea, SWT.NONE);
		getToolkit().adapt(tc.descriptionButton);
		tc.descriptionButton.setText("Description");
		
		resultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tc.descriptionButton.setSelection(false);
				if (((FlatButton) e.widget).getSelection()) {
					GridDataFactory.fillDefaults().grab(true, false).applyTo(tc.infoArea);
					for (Control child : tc.infoArea.getChildren()) {
						child.dispose();
					}
					tc.isDescriptionOpened = false;
				} else {
					GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 0).applyTo(tc.infoArea);
				}
				mainArea.layout(true, true);
				form.reflow(true);
			}
		});

		tc.descriptionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resultButton.setSelection(false);
				if (((FlatButton) e.widget).getSelection()) {
					GridDataFactory.fillDefaults().grab(true, false).applyTo(tc.infoArea);
					createDescriptionView(task, tc.infoArea);
					tc.isDescriptionOpened = true;
				} else {
					GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 0).applyTo(tc.infoArea);
					tc.isDescriptionOpened = false;
				}
				mainArea.layout(true, true);
				form.reflow(true);
			}
		});
		
		/*****************************************************************
		 * Create task info area
		 *****************************************************************/
		tc.infoArea = getToolkit().createComposite(tc.group);
		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 0).applyTo(tc.infoArea);
		}
		/*****************************************************************
		 * Event handling
		 *****************************************************************/
		// mouse listener
		tc.group.addMenuDetectListener(new MenuDetectListener() {
			
			public void menuDetected(MenuDetectEvent e) {
				if (keepAllExpanded) {
					keepAllExpandedItem.setText("Fold All Tasks");
					keepAllExpandedItem.setImage(InternalImage.CONTEXT_SHOWN.getImage());
				} else {
					keepAllExpandedItem.setText("Keep All Tasks Expanded");
					keepAllExpandedItem.setImage(InternalImage.LIST_SHOWED.getImage());
				}
				tc.group.layout();
			}
		});
		
		tc.group.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {
				onMouseClick(event, tc);
			}

		});
		
		// mouse listener
//		tc.group.addListener(SWT.MouseDoubleClick, new Listener() {
//			public void handleEvent(Event event) {
//				if (event.button == 1) {
//					makeTitleChangerArea(tc, groupBody);
//				}
//			}
//
//		});
		MenuItem titleChangeItem = new MenuItem(menu, SWT.PUSH);
		titleChangeItem.setText("Change Title");
		titleChangeItem.setImage(InternalImage.LOG_SHOWED.getImage());
		titleChangeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				makeTitleChangerArea(tc, groupBody);
			}
		});
		
		task.addEventListener(taskEventHandler);
		return tc;
	}

	private String getTaskTitle(ITask task) {
//		String title = null;
//		if (task instanceof AbstractTask) {
//			title = ((AbstractTask) task).getTitle();
//		}
//		if (title == null) {
//			return task.getLabel();
//		} else {
//			return title;
//		}
		return task.getLabel();
	}

	private void setAllExpanded(boolean keepAllExpanded) {
		this.keepAllExpanded = keepAllExpanded;
		for (TaskUIContext tc : taskUIContexts.values()) {
			if (!tc.group.isDisposed()) {
				tc.group.setExpanded(keepAllExpanded);
			}
		}
		if (c.form != null && !c.form.isDisposed()) {
			c.form.layout(true, true);
			c.form.reflow(true);
		}
	}

	private void makeTitleChangerArea(final TaskUIContext tc, final Composite taskBody) {
		/*****************************************************************
		 * Create title changer UI
		 *****************************************************************/
		for (Control control : tc.group.getChildren()) {
			if (control instanceof Composite && !control.isDisposed() && 
					TITLE_CHANGER_DATA.equals(control.getData())) {
				return;
			}
		}
		final Composite titleChangeArea = getToolkit().createComposite(tc.group);
		titleChangeArea.setData(TITLE_CHANGER_DATA);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.CENTER).applyTo(titleChangeArea);
		GridLayoutFactory.swtDefaults().numColumns(4).margins(4, 2).applyTo(titleChangeArea);
		titleChangeArea.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		titleChangeArea.setBackgroundMode(SWT.INHERIT_FORCE);
		Label titleLabel = new Label(titleChangeArea, SWT.NONE);
		titleLabel.setText("Type in a new title for this task: ");
		GridDataFactory.swtDefaults().indent(4, 4).applyTo(titleLabel);
		final Text titleText = getToolkit().createText(titleChangeArea, "");
		GridDataFactory.swtDefaults().hint(240, SWT.DEFAULT).applyTo(titleText);
		final Button titleOK = getToolkit().createButton(titleChangeArea, "OK", SWT.PUSH);
		GridDataFactory.swtDefaults().hint(44, SWT.DEFAULT).applyTo(titleOK);
		final Button titleCancel = getToolkit().createButton(titleChangeArea, "Cancel", SWT.PUSH);
		GridDataFactory.swtDefaults().hint(44, SWT.DEFAULT).applyTo(titleCancel);
		
		titleText.addKeyListener(new KeyListener() {
			
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.KEYPAD_CR || e.character == SWT.CR) {
					changeTitle(tc, titleText.getText());
					disposeTitleChanger(tc, titleChangeArea);
				}
			}
			
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		titleOK.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				changeTitle(tc, titleText.getText());
				disposeTitleChanger(tc, titleChangeArea);
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		titleCancel.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				disposeTitleChanger(tc, titleChangeArea);
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		titleChangeArea.moveAbove(taskBody);
		tc.group.layout(true, true);
		tc.group.getParent().layout(true, true);
		tc.group.getParent().update();
		tc.group.getParent().redraw();
		
		showTaskUI(tc);
		titleText.forceFocus();
	}

	private void changeTitle(TaskUIContext tc, String title) {
		if (title != null && title.trim().length() > 0) {
			ITask task = tc.taskView.getTask();
//			((AbstractTask) task).setTitle(title);
			((AbstractTask) task).setLabel(title);
			int index = getWorkflow().getTasks().indexOf(tc.taskView.getTask());
			String titleIndex = "";
			if (index >= 0) {
				titleIndex = String.valueOf(index + 1) + " - ";
			}
//			String label = tc.taskView.getTask().getLabel();
//			String shortDescription = "";
//			if (label.contains("(") && label.contains(")")) {
//				shortDescription = label.substring(label.lastIndexOf("("), label.lastIndexOf(")") + 1);
//			}
//			tc.group.setText(titleIndex + tc.title + " " + shortDescription);
			tc.group.setText(titleIndex + getTaskTitle(tc.taskView.getTask()));
		}
	}

	private void updateTitle(ITask task) {
		TaskUIContext tc = taskUIContexts.get(task);
		if (tc != null) {
			int index = getWorkflow().getTasks().indexOf(tc.taskView.getTask());
			String titleIndex = "";
			if (index >= 0) {
				titleIndex = String.valueOf(index + 1) + " - ";
			}
//			String label = task.getLabel();
//			if (tc.title != null) {
//				String shortDescription = "";
//				if (label.contains("(") && label.contains(")")) {
//					shortDescription = label.substring(label.lastIndexOf("("), label.lastIndexOf(")") + 1);
//				}
//				tc.group.setText(titleIndex + tc.title + " " + shortDescription);
//			} else {
//				tc.group.setText(titleIndex + label);
//			}
			tc.group.setText(titleIndex + getTaskTitle(task));
		}
	}

	private void disposeTitleChanger(TaskUIContext tc, Composite titleChangeArea) {
		titleChangeArea.setMenu(null);
		for (Control control : titleChangeArea.getChildren()) {
			control.dispose();
		}
		titleChangeArea.dispose();
		tc.group.layout(true, true);
		tc.group.getParent().layout(true, true);
		tc.group.getParent().update();
		tc.group.getParent().redraw();
	}
	
	private Color findColor(String colorString) {
		Display display = Display.getCurrent();
		if (colorString.toUpperCase().equals("BLACK")) {
			return display.getSystemColor(SWT.COLOR_BLACK);
		}
		if (colorString.toUpperCase().equals("BLUE")) {
			return display.getSystemColor(SWT.COLOR_BLUE);
		}
		if (colorString.toUpperCase().equals("CYAN")) {
			return display.getSystemColor(SWT.COLOR_CYAN);
		}
		if (colorString.toUpperCase().equals("DARK_BLUE")) {
			return display.getSystemColor(SWT.COLOR_DARK_BLUE);
		}
		if (colorString.toUpperCase().equals("DARK_CYAN")) {
			return display.getSystemColor(SWT.COLOR_DARK_CYAN);
		}
		if (colorString.toUpperCase().equals("DARK_GRAY")) {
			return display.getSystemColor(SWT.COLOR_DARK_GRAY);
		}
		if (colorString.toUpperCase().equals("DARK_GREEN")) {
			return display.getSystemColor(SWT.COLOR_DARK_GREEN);
		}
		if (colorString.toUpperCase().equals("DARK_MAGENTA")) {
			return display.getSystemColor(SWT.COLOR_DARK_MAGENTA);
		}
		if (colorString.toUpperCase().equals("DARK_RED")) {
			return display.getSystemColor(SWT.COLOR_DARK_RED);
		}
		if (colorString.toUpperCase().equals("DARK_YELLOW")) {
			return display.getSystemColor(SWT.COLOR_DARK_YELLOW);
		}
		if (colorString.toUpperCase().equals("GRAY")) {
			return display.getSystemColor(SWT.COLOR_GRAY);
		}
		if (colorString.toUpperCase().equals("GREEN")) {
			return display.getSystemColor(SWT.COLOR_GREEN);
		}
		if (colorString.toUpperCase().equals("MAGENTA")) {
			return display.getSystemColor(SWT.COLOR_MAGENTA);
		}
		if (colorString.toUpperCase().equals("YELLOW")) {
			return display.getSystemColor(SWT.COLOR_YELLOW);
		}
		if (colorString.toUpperCase().equals("RED")) {
			return display.getSystemColor(SWT.COLOR_RED);
		}
		
		return null;
	}

	private ITaskView createTaskView(ITask task) {
		ITaskView taskView = task.createTaskView();
		taskView.addEventListener(taskViewEventHandler);
		return taskView;
	}
	
	private void createErrorTaskView(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		Label label = getToolkit().createLabel(parent, "");
		label.setImage(InternalImage.ERROR_TASK.getImage());
		getToolkit().createLabel(parent, "Cannot create task UI.");
	}
	
	private void createDescriptionView(ITask task, Composite parent) {
		for (Control child : parent.getChildren()) {
			child.dispose();
		}
		GridLayoutFactory.fillDefaults().margins(15, 5).applyTo(parent);
		
		ITaskRegistry reg = ServiceUtils.getService(ITaskRegistry.class);
		ITaskDescriptor desc = reg.getTaskDescriptorById(task.getClass().getName());
		if (desc != null) {
			Composite descComposite = getToolkit().createComposite(parent);
			descComposite.setMenu(new Menu(descComposite));
			GridDataFactory.fillDefaults().grab(true, true).applyTo(descComposite);
			GridLayoutFactory.swtDefaults().margins(0, 0).spacing(0, 0).applyTo(descComposite);
			CLabel taskLabel = new CLabel(descComposite, SWT.NONE);
			getToolkit().adapt(taskLabel);
			taskLabel.setFont(c.largeBold);
			GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 32).applyTo(taskLabel);
			FormText descText = getToolkit().createFormText(descComposite, false);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(descText);
			TaskToolbar.updateTaskDescription(desc, taskLabel, descText);
			// To ensure the form text wraps
			GridDataFactory.fillDefaults().grab(true, true).hint(descComposite.getSize().x, SWT.DEFAULT).applyTo(descText);
		}
	}
	
	private void updateTaskState(TaskEvent event) {
		TaskUIContext tc = taskUIContexts.get(event.getPublisher());
		for (Control child : tc.statusArea.getChildren()) {
			child.dispose();
		}
		tc.statusArea.setMenu(new Menu(tc.statusArea));
		
		if (event.getState().equals(TaskState.RUNNING)) {
			
		} else {
			
		}
		
		if (event.getState().equals(TaskState.RUNNING)) {
			// Set font
			tc.group.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			// Set group
			MenuBasedGroupStrategy strategy = (MenuBasedGroupStrategy) tc.group.getStrategy();
			strategy.setBackground(new Color[] { Display.getDefault().getSystemColor(SWT.COLOR_BLACK) }, new int[] {} );
			strategy.setBorderColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			strategy.update();
			// Set progress image
			ImageSequencer imageSequencer = new ImageSequencer(tc.statusArea, SWT.NONE, InternalImage.getSmallIndictorImages(), 150, true);
			getToolkit().adapt(imageSequencer);
			imageSequencer.startSequence();
		} else if (event.getState().equals(TaskState.ERROR)) {
			tc.group.setStrategy(new MenuBasedGroupStrategy());
			// Set error image
			Label label = getToolkit().createLabel(tc.statusArea, "");
			label.setImage(InternalImage.ERROR.getImage());
		} else if (event.getState().equals(TaskState.COMPLETED)) {
			tc.group.setStrategy(new MenuBasedGroupStrategy());
			// Set clicked image
			Label label = getToolkit().createLabel(tc.statusArea, "");
			label.setImage(InternalImage.CHECKED.getImage());
		} else if (event.getState().equals(TaskState.IDLE)) {
			tc.group.setStrategy(new MenuBasedGroupStrategy());
		} 
		
		tc.statusArea.layout(true, true);
	}
	
	private class UIContext {
		private UIResourceManager resourceManager;
		private Font defaultFont;
		private Font largeBold;
		private Font headerFont;
		private ScrolledForm form;
	}
	
	private class TaskUIContext {
		private MenuBasedGroup group;
		private ITaskView taskView;
		private Composite statusArea;
		private Composite infoArea;
		private FlatButton descriptionButton;
		private boolean isDescriptionOpened;
		
		private void dispose() {
			if (taskView != null) {
				taskView.removeEventListener(taskViewEventHandler);
				taskView = null;
			}
			group = null;
		}
	}
	
	private void onMouseClick(Event event, TaskUIContext tc) {
		if (event.button == 1) {
			showTaskUI(tc);
		}
	}

	private void showTaskUI(TaskUIContext tc) {
		if (tc == null) {
			return;
		}
		if (!keepAllExpanded) {
			for (TaskUIContext item : taskUIContexts.values()) {
				if (item != tc) {
					if (!item.group.isDisposed()) {
						item.group.setExpanded(false);
					}
				}
			}
		}
		if (!tc.group.isDisposed()) {
			tc.group.setExpanded(true);
		}
		if (c.form != null && !c.form.isDisposed()) {
			c.form.layout(true, true);
			c.form.reflow(true);
		}
		c.form.forceFocus();
	}
	
	private void syncWorkflowUI() {
		Control currentControl = null;
		for (ITask oldTask : taskUIContexts.keySet()) {
			if (!getWorkflow().getTasks().contains(oldTask)) {
				TaskUIContext oldTC = taskUIContexts.get(oldTask);
				if (oldTC != null) {
					oldTC.group.dispose();
				}
			} 
		}
		int index = 0;
		for (final ITask task : getWorkflow().getTasks()) {
			if (!task.isVisible()) {
				continue;
			}
			index++;
			// Render
			TaskUIContext tc = taskUIContexts.get(task);
			if (tc == null) {
				tc = createTaskUI(task, c.form);
				taskUIContexts.put(task, tc);
			} 
//			else {
//				if (!keepAllExpanded) {
//					tc.group.setExpanded(false);
//				}
//			}
			MenuBasedGroup tcGroup = tc.group;
//			String taskIndex = String.valueOf(index) + " - ";
//			if (tc.title != null) {
//				tcGroup.setText(taskIndex + tc.title);
//			} else {
//				tcGroup.setText(taskIndex + task.getLabel());
//			}
			if (currentControl != null) {
				tcGroup.moveBelow(currentControl);
			} else {
				tcGroup.moveAbove(null);
			}
			currentControl = tcGroup;
			updateTitle(task);
		}
		getParent().layout(true, true);
	}

}
