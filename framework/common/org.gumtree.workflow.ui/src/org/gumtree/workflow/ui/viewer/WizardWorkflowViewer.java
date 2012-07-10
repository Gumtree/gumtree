package org.gumtree.workflow.ui.viewer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.resource.UIResourceManager;
import org.gumtree.ui.util.resource.UIResources;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.IWorkflowIntroView;
import org.gumtree.workflow.ui.TaskState;
import org.gumtree.workflow.ui.WorkflowState;
import org.gumtree.workflow.ui.events.WorkflowStateEvent;
import org.gumtree.workflow.ui.internal.Activator;
import org.gumtree.workflow.ui.internal.InternalImage;
import org.gumtree.workflow.ui.util.WorkflowUI;
import org.gumtree.workflow.ui.util.WorkflowUtils;
import org.gumtree.workflow.ui.viewer.FlyoutPaletteComposite.FlyoutPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WizardWorkflowViewer extends AbstractWorkflowViewer {

	private static Logger logger = LoggerFactory.getLogger(WizardWorkflowViewer.class);
	
	private List<ITask> tasks;
	
	private Composite parent;
	
	private IWorkflowIntroView introView;
	
	private StackLayout stackLayout;
	
	private StackLayout taskStackLayout;
	
	// Parent form of this viewer
	private ScrolledForm scrolledForm;
	
	private Composite workflowPage;
	
	private Composite taskPage;
	
	private Composite paletteArea;
	
	private Font taskLabelBoldFont;
	
	private Font taskLabelItalicFont;
	
	private Map<Long, TaskUIContext> taskUIContexts;
	
	private ITask currentTask;
	
	private Button runButton;
	
//	private ImageHyperlink startButton;
	
	private Label stopButton;
	
	private IEventHandler<WorkflowStateEvent> workfloEventHandler;
	
	private UIResourceManager resourceManager;
	
	public WizardWorkflowViewer() {
		super();
	}
	
	public void dispose() {
		if (introView != null) {
			introView.dispose();
			introView = null;
		}
		if (taskLabelBoldFont != null) {
			taskLabelBoldFont.dispose();
			taskLabelBoldFont = null;
		}
		if (taskLabelItalicFont != null) {
			taskLabelItalicFont.dispose();
			taskLabelItalicFont = null;
		}
		if (taskUIContexts != null) {
			for (TaskUIContext context : taskUIContexts.values()) {
				ITaskView view = context.taskView;
				if (view != null) {
					view.dispose();
				}
			}
			taskUIContexts.clear();
			taskUIContexts = null;
		}
		if (workfloEventHandler != null) {
			getWorkflow().removeEventListener(workfloEventHandler);
		}
		runButton = null;
//		startButton = null;
		stopButton = null;
		scrolledForm = null;
		paletteArea = null;
		stackLayout = null;
		taskStackLayout = null;
		taskPage = null;
		workflowPage = null;
		parent = null;
		tasks = null;
		currentTask = null;
		super.dispose();
	}
	
	private List<ITask> getTasks() {
		if (tasks == null) {
			tasks = getWorkflow().getTasks();
		}
		return tasks;
	}
	
	// Layout:
	// Given Parent
	// |-Intro Page
	//  |- Intro Area
	// |-Workflow Page
	//  |-Palette
	//   |-Palette Area
	//   |-Main Area
	//    |-Control Area
	//    |-Task Page
	//    |-Task Area (multiple)
	//     |-Task Contribution Area
	//     |-Task Button Area
	//
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.viewer.IWorkflowViewer#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createViewerControl(Composite parent) {
		// Invalid workflow
		if (getWorkflow() == null) {
			createInvalidWorkflowUI(parent, "Invalid Workflow Config");
			return;
		}

		this.parent = parent;
		resourceManager = new UIResourceManager(Activator.PLUGIN_ID, parent);
		
		// Create Intro
		if (!createWorkflowIntro(parent)) {
			createInvalidWorkflowUI(parent, "Invalid Workflow Intro Config");
			return;
		}
		
		// Create workflow page
		workflowPage = getToolkit().createComposite(parent);
		workflowPage.setLayout(new FillLayout());
		
		// Create palette
		FlyoutPaletteComposite paletteComposite = new FlyoutPaletteComposite(workflowPage, SWT.NONE, getPalettePreferences(), "Workflow Info");
		createPalette(paletteComposite.getClientComposite());
		Composite mainArea = getToolkit().createComposite(paletteComposite);
		mainArea.setLayout(new GridLayout());
		paletteComposite.setState(FlyoutPaletteComposite.STATE_COLLAPSED);
		paletteComposite.setGraphicalControl(mainArea);

		
		// Create central control area
		Composite controlArea = getToolkit().createComposite(mainArea);
		controlArea.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().applyTo(controlArea);
		
		// Stop button
		stopButton = getToolkit().createLabel(controlArea,"");
		stopButton.setImage(InternalImage.STOP.getImage());
		stopButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		stopButton.setEnabled(false);
		stopButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				WorkflowUI.getWorkflowExecutor().stop(getWorkflow());
			}
		});
		
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, false).applyTo(stopButton);
		
		// Title
		String title = WorkflowUtils.getWorkflowTitle(getWorkflow());
		Label titleLabel = getToolkit().createLabel(controlArea, title);
		Font titleFont = resourceManager.createRelativeFont(titleLabel.getFont(), 10, SWT.BOLD);
		titleLabel.setFont(titleFont);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(true, false).applyTo(titleLabel);
		
		// Create task page
		taskPage = getToolkit().createComposite(mainArea);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(taskPage);
		taskStackLayout = new StackLayout();
		taskPage.setLayout(taskStackLayout);
		taskUIContexts = new HashMap<Long, TaskUIContext>();
		for (ITask task : getTasks()) {
			if (task.isVisible()) {
				// Set first visible task
				if (currentTask == null) {
					currentTask = task;
				}
				TaskUIContext context = new TaskUIContext();
				context.task = task;
				taskUIContexts.put(task.getId(), context);
				Composite taskArea = getToolkit().createComposite(taskPage);
				context.taskArea = taskArea;
				createTaskPage(taskArea, context);
			}
		}
		
		// Show component on the task page
		if (taskUIContexts.size() > 0) {
			// Show first task
			updateUI(currentTask);
			
		} else {
			// Show invalid task config
			createInvalidWorkflowUI(taskPage, "Empty task config");
		}
		
		// Listen to workflsdow when UI is ready
//		getWorkflow().addWorkflowListener(getWorkflowListener());
		
		// Workflow event
		workfloEventHandler = new IEventHandler<WorkflowStateEvent>() {
			public void handleEvent(WorkflowStateEvent event) {
				if (event.getState().equals(WorkflowState.RUNNING) || 
						event.getState().equals(WorkflowState.SCHEDULED)) {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						public void run() throws Exception {
							runButton.setEnabled(false);
							stopButton.setEnabled(true);
						}						
					});
				} else if (event.getState().equals(WorkflowState.STOPPING)) {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						public void run() throws Exception {
							runButton.setEnabled(false);
							stopButton.setEnabled(false);
						}						
					});
				} else  {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						public void run() throws Exception {
							runButton.setEnabled(true);
							stopButton.setEnabled(false);
						}						
					});
				}
			}
		};
		getWorkflow().addEventListener(workfloEventHandler);
	}
	
	// Assume this is run by UI update thread
	private void updateUI(ITask currentTask) {
		this.currentTask = currentTask;
		// Update update palette
		for (Control child : paletteArea.getChildren()) {
			child.dispose();
		}
		createWorkflowPaletteItem(paletteArea);
		paletteArea.layout();
		// Update buttons
		updateButtons();
		// Update task page
		taskStackLayout.topControl = taskUIContexts.get(currentTask.getId()).taskArea;
		taskPage.layout();
	}
	
	private boolean createWorkflowIntro(Composite parent) {
		// Create intro
		stackLayout = new StackLayout();
		parent.setLayout(stackLayout);
		try {
			// Create intro page
			introView = WorkflowUtils.createIntroView(getWorkflow());
			Composite introPage = getToolkit().createComposite(parent);
			stackLayout.topControl = introPage;
			
			// Create intro area
			introPage.setLayout(new GridLayout());
			Composite introArea = getToolkit().createComposite(introPage);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(introArea);
			introArea.setLayout(new FillLayout());
			introView.createPartControl(introArea);
			
			// Create intro button area
			Label separator = getToolkit().createLabel(introPage, "", SWT.SEPARATOR | SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().applyTo(separator);
			Button startButton = getToolkit().createButton(introPage, "Begin >", SWT.PUSH);
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).hint(100, SWT.DEFAULT).applyTo(startButton);
			startButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleStartAction();
				}
			});
		} catch (ObjectCreateException e) {
			logger.error("Error in creating workflow intro view.", e);
			for (Control childControl : parent.getChildren()) {
				childControl.dispose();
			}
			return false;
		}
		return true;
	}
	
	private void createTaskPage(Composite parent, TaskUIContext context) {
		parent.setLayout(new GridLayout(2, false));
		
		// Create task area
		scrolledForm = getToolkit().createScrolledForm(parent);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(2, 1).applyTo(scrolledForm);
		// Some how this stop scrolling
//		scrolledForm.setText(context.task.getLabel());
		scrolledForm.getBody().setLayout(new GridLayout());
		ITaskView taskView = createTaskView(context.task);
		if (taskView != null) {
			context.taskView = taskView;
			taskView.createPartControl(scrolledForm.getBody());
		} else {
			createErrorTaskView(scrolledForm.getBody());
		}
		
		// Create button bar
		createButtonBar(parent, context);
	}
	
	private void createButtonBar(Composite parent, TaskUIContext context) {
		ITask task = context.task;
		ITask nextTask = WorkflowUtils.getNextTask(getWorkflow(), task);			// Note: can be null
		ITask previousTask = WorkflowUtils.getPreviousTask(getWorkflow(), task);	// Note: can be null
		boolean isFirstTask = WorkflowUtils.isFirstVisibleTask(getWorkflow(), task);
		boolean isLastTask = WorkflowUtils.isLastVisibleTask(getWorkflow(), task);
		Label separator = getToolkit().createLabel(parent, "", SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(separator);
		
		if (!isFirstTask && !isLastTask) {
			Button backButton = createBackButton(parent, task, previousTask,
					GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(true, false).hint(100, SWT.DEFAULT).create());
			context.backButton = backButton;
		
			Button nextButton = createNextButton(parent, task, nextTask,
					GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).create());
			context.nextButton = nextButton;
		} else if (isFirstTask && !isLastTask) {
//			Button finishButton = getToolkit().createButton(parent, "Finish", SWT.PUSH);
//			finishButton.setLayoutData("alignx left, width 100, gaptop 5, gapbottom 5");
//
//			Button lockButton = getToolkit().createButton(parent, "Unlock", SWT.PUSH);
////			lockButton.setEnabled(taskState.equals(TaskState.LOCKED));
//			lockButton.setLayoutData("alignx left, pushx, width 100, gaptop 5, gapbottom 5");
			
			Button nextButton = createNextButton(parent, task, nextTask,
					GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(true, false).hint(100, SWT.DEFAULT).span(2, 1).create());
			context.nextButton = nextButton;
		} else if (isFirstTask && isLastTask) {			
//			Button finishButton = getToolkit().createButton(parent, "Finish", SWT.PUSH);
//			finishButton.setLayoutData("alignx right, pushx, width 100, gaptop 5, gapbottom 5, wrap");
			
			runButton = getToolkit().createButton(parent, "Run", SWT.PUSH);
			GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).span(2, 1).applyTo(runButton);
			runButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// Schedule the workflow rather than running it directly
					WorkflowUI.getWorkflowExecutor().schedule(getWorkflow());
				}
			});
		} else if (!isFirstTask && isLastTask) {
			Button backButton = createBackButton(parent, task, previousTask, 
					GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(true, false).hint(100, SWT.DEFAULT).create());
			context.backButton = backButton;
			
			runButton = getToolkit().createButton(parent, "Run", SWT.PUSH);
			GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(runButton);
			runButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// Schedule the workflow rather than running it directly
					WorkflowUI.getWorkflowExecutor().schedule(getWorkflow());
				}
			});
		}
	}
	
	private void updateButtons() {
		for (TaskUIContext context : taskUIContexts.values()) {
			ITask task = context.task;
			ITask nextTask = WorkflowUtils.getNextTask(getWorkflow(), task);			// Note: can be null
			if (context.nextButton != null) {
				TaskState nextState = nextTask.getState();
				if (nextState.equals(TaskState.IDLE)) {
					context.nextButton.setEnabled(true);
				}
			}
		}
	}
	
	private Button createNextButton(Composite parent, ITask task, final ITask nextTask, GridData layoutData) {
		Button nextButton = getToolkit().createButton(parent, "Next >", SWT.PUSH);
		nextButton.setLayoutData(layoutData);
		nextButton.setEnabled(false);
		TaskState nextState = nextTask.getState();
		if (nextState.equals(TaskState.IDLE)) {
			nextButton.setEnabled(true);
		}
		nextButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SafeUIRunner.asyncExec(new ISafeRunnable() {
					public void handleException(Throwable exception) {
					}
					public void run() throws Exception {
						if (taskPage != null && !taskPage.isDisposed()) {
							updateUI(nextTask);
						}
					}						
				}, logger);
			}
		});
		return nextButton;
	}
	
	private Button createBackButton(Composite parent, ITask task, final ITask previousTask, GridData layoutData) {
		Button backButton = getToolkit().createButton(parent, "< Back", SWT.PUSH);
		backButton.setLayoutData(layoutData);
		backButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SafeUIRunner.asyncExec(new ISafeRunnable() {
					public void handleException(Throwable exception) {
					}
					public void run() throws Exception {
						if (taskPage != null && !taskPage.isDisposed()) {
							updateUI(previousTask);
						}
					}						
				}, logger);
			}
		});
		return backButton;
	}
	
	private void createInvalidWorkflowUI(Composite parent, String message) {
		parent.setLayout(new GridLayout());
		Label label = new Label(parent, SWT.NONE);
		label.setText(message);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(label);
	}
	
	private void createPalette(Composite parent) {
		parent.setLayout(new FillLayout());
		ExpandBar expandBar = new ExpandBar(parent, SWT.V_SCROLL);
		ExpandItem item = new ExpandItem(expandBar, SWT.NONE, 0);
		item.setText("Workflow");
		
		paletteArea = getToolkit().createComposite(expandBar);
		createWorkflowPaletteItem(paletteArea);
		item.setHeight(paletteArea.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setControl(paletteArea);
		item.setExpanded(true);
		
		item = new ExpandItem(expandBar, SWT.NONE, 1);
		item.setText("Description");
	}
	
	private void createWorkflowPaletteItem(Composite parent) {
		parent.setLayout(new GridLayout(3, false));
		for (ITask task : getTasks()) {
			if (!task.isVisible()) {
				continue;
			}
			Label pointerLabel = getToolkit().createLabel(parent, "");
			Label label = getToolkit().createLabel(parent, "");
			TaskState taskState = task.getState();
			if (taskState.equals(TaskState.IDLE)) {
				label.setImage(InternalImage.UNCHECKED.getImage());
				label = getToolkit().createLabel(parent, task.getLabel());
			} else if (taskState.equals(TaskState.COMPLETED)) {
				label.setImage(InternalImage.CHECKED.getImage());
				label = getToolkit().createLabel(parent, task.getLabel());
			} else {
				label.setImage(InternalImage.UNAVAILABLE.getImage());
				label = getToolkit().createLabel(parent, task.getLabel());
			}
			// use currentTask == null to ensure the parent has size set to it's potential maximised state
			if ((currentTask != null && task.getId() == currentTask.getId()) || currentTask == null) {
				pointerLabel.setImage(InternalImage.POINTER.getImage());
				FontData fontData = new FontData(label.getFont().getFontData()[0].toString());
				fontData.setStyle(SWT.BOLD);
				fontData.setHeight(fontData.getHeight() + 2);
				taskLabelBoldFont = new Font(Display.getDefault(),fontData);
				label.setFont(taskLabelBoldFont);
			}
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(label);
			
			// Render arrow
			if (!WorkflowUtils.isLastVisibleTask(getWorkflow(), task)) {
				label = getToolkit().createLabel(parent, "");
				label = getToolkit().createLabel(parent, "");
				label = getToolkit().createLabel(parent, "");
				label.setImage(InternalImage.NEXT_STEP.getImage());
			}
			
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(label);
		}
	}
	
	protected void handleTaskViewRefreshEvent(ITaskView taskView) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (scrolledForm != null && !scrolledForm.isDisposed()) {
					scrolledForm.getBody().layout(true, true);
					scrolledForm.reflow(true);
				}
			}			
		});
	}
	
	// Can we assume this is thread safe?
	private void handleStartAction() {
		introView.handleStartAction();
		// Register workflow into the queue
		WorkflowUI.getWorkflowManager().appendWorkflow(getWorkflow());
		// Update UI
		stackLayout.topControl = workflowPage;
		SafeUIRunner.asyncExec(new ISafeRunnable() {
			public void run() {
				if (parent != null && !parent.isDisposed())	{
					parent.layout();
				}
			}
			public void handleException(Throwable exception) {
				logger.error("Cannot update layout.", exception);
			}
		}, logger);
	}
	
	private FlyoutPreferences getPalettePreferences() {
		return FlyoutPaletteComposite.createFlyoutPreferences(Activator.getDefault().getPluginPreferences());
	}
	
	private class TaskUIContext {
		private ITask task;
		private Composite taskArea;		
		private ITaskView taskView;
		private Button nextButton;
		private Button backButton;
		private Button lockButton;
	}
	
}
