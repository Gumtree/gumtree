package org.gumtree.ui.tasklet.support;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.eclipse.ui.part.FileEditorInput;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.ui.scripting.IScriptConsole;
import org.gumtree.ui.scripting.support.ScriptConsole;
import org.gumtree.ui.tasklet.IActivatedTasklet;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.ui.tasklet.ITaskletManager;
import org.gumtree.ui.util.ParameterizedSafeRunnable;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.jface.ITreeNode;
import org.gumtree.ui.util.jface.TreeContentProvider;
import org.gumtree.ui.util.jface.TreeLabelProvider;
import org.gumtree.ui.util.jface.TreeNode;
import org.gumtree.ui.util.workbench.WorkbenchUtils;
import org.gumtree.ui.widgets.ExtendedComposite;
import org.gumtree.util.collection.IMapFilter;
import org.gumtree.util.messaging.EventHandler;
import org.osgi.service.event.Event;

import ch.lambdaj.collection.LambdaCollections;

@SuppressWarnings("restriction")
public class TaskletManagerViewer extends ExtendedComposite {

	private ITaskletManager taskletManager;

	private UIContext context;

	private MWindow mWindow;

	@Inject
	public TaskletManagerViewer(Composite parent, @Optional int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		context = new UIContext();
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(this);

		// Create tool area
		Button addButton = getWidgetFactory().createButton(this, "", SWT.PUSH);
		addButton.setToolTipText("Add new tasklet");
		addButton.setImage(InternalImage.ADD_TASKLET_16.getImage());
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleLaunchAddTaskletDialog(null);
			}
		});

		Button hierarchyDisplayButton = getWidgetFactory().createButton(this,
				"", SWT.TOGGLE);
		hierarchyDisplayButton.setImage(InternalImage.HIERARCHY_16.getImage());

		Text searchText = getWidgetFactory().createText(this, "", SWT.SEARCH);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(searchText);

		// Create tree
		final TreeViewer treeViewer = new TreeViewer(this);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).span(3, 1).applyTo(treeViewer.getControl());
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.getTree().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (getTaskletManager() != null) {
					// Run tasklet on double click
					TaskletTreeNode node = (TaskletTreeNode) ((IStructuredSelection) treeViewer
							.getSelection()).getFirstElement();
					ITasklet tasklet = node.getTasklet();
					handleTaskletActivation(tasklet);
				}
			}
		});
		treeViewer.setInput(createTreeNode("", false));

		// Close button
		context.closeTaskButton = getWidgetFactory().createButton(this,
				"Close Task", SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(3, 1).applyTo(context.closeTaskButton);
		context.closeTaskButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				IPerspectiveDescriptor perspective = page.getPerspective();
				page.closePerspective(perspective, false, false);
			}
		});
		context.closeTaskButton.setEnabled(false);

		// Launch console button
		context.showConsoleButton = getWidgetFactory().createButton(this,
				"Toggle Console", SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(3, 1)
				.applyTo(context.showConsoleButton);
		context.showConsoleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MPerspective perspective = WorkbenchUtils
						.getActivePerspective();
				MPart mPart = WorkbenchUtils.getFirstChildWithProperty(
						perspective, MPart.class,
						new IMapFilter<String, String>() {
							@Override
							public boolean accept(String key, String value) {
								return "partType".equals(key)
										&& "taskletConsole".equals(value);
							}
						});
				if (mPart != null) {
					mPart.setVisible(!mPart.isVisible());
				} else {
					handleCreateConsole();
				}
			}
		});
		context.showConsoleButton.setEnabled(false);

		// DnD
		Transfer[] transfers = new Transfer[] {
				LocalSelectionTransfer.getTransfer(),
				FileTransfer.getInstance(), EditorInputTransfer.getInstance() };
		treeViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, transfers,
				new DropTargetListener());

		// Listening to perspective change
		if (PlatformUI.isWorkbenchRunning()) {
			context.window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			context.perspectiveListener = new PerspectiveAdapter() {
				public void perspectiveActivated(IWorkbenchPage page,
						IPerspectiveDescriptor perspective) {
					MPerspective mPerspective = WorkbenchUtils
							.getActivePerspective();
					if (mPerspective.getProperties().containsKey("id")) {
						context.closeTaskButton.setEnabled(true);
						context.showConsoleButton.setEnabled(true);
					} else {
						context.closeTaskButton.setEnabled(false);
						context.showConsoleButton.setEnabled(false);
					}
				}

				public void perspectiveChanged(IWorkbenchPage page,
						IPerspectiveDescriptor perspective,
						IWorkbenchPartReference partRef, String changeId) {
					System.out.println("Closed");
				}
			};
			context.window.addPerspectiveListener(context.perspectiveListener);
		}

		// Event handler
		context.eventHandler = new EventHandler(
				ITaskletManager.EVENT_TASKLET_REGISTRAION_ALL) {
			@Override
			public void handleEvent(Event event) {
				// Update tree viewer
				SafeUIRunner.asyncExec(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						treeViewer.setInput(createTreeNode("", false));
					}
				});
			}
		}.activate();
	}

	@Override
	protected void disposeWidget() {
		if (context != null) {
			if (context.perspectiveListener != null) {
				context.window
						.removePerspectiveListener(context.perspectiveListener);
			}
			if (context.eventHandler != null) {
				context.eventHandler.deactivate();
			}
		}
		context = null;
	}

	/*************************************************************************
	 * Event handlers
	 *************************************************************************/

	@Inject
	public void handlePerspectiveChange(
			@EventTopic(UIEvents.UILifeCycle.PERSPECTIVE_OPENED) Object data) {
		System.out.println(data);
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public ITaskletManager getTaskletManager() {
		return taskletManager;
	}

	@Inject
	public void setTaskletManager(ITaskletManager taskletManager) {
		this.taskletManager = taskletManager;
	}

	public MWindow getMWindow() {
		return mWindow;
	}

	public void setMWindow(MWindow mWindow) {
		this.mWindow = mWindow;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	private void handleLaunchAddTaskletDialog(String contributionUri) {
		AddTaskletDialog dialog = new AddTaskletDialog(getShell());
		dialog.setContributionUri(contributionUri);
		dialog.setTaskletRegistry(getTaskletManager());
		dialog.open();
	}

	private void handleTaskletActivation(ITasklet tasklet) {
		// Activate tasklet
		IActivatedTasklet activatedTasklet = getTaskletManager()
				.activatedTasklet(tasklet);
	}

	private void handleCreateConsole() {
		MPerspective mPerspective = WorkbenchUtils.getActivePerspective();
		if (mPerspective.getProperties().containsKey("id")) {
			String id = mPerspective.getProperties().get("id");
			IActivatedTasklet activatedTasklet = getTaskletManager()
					.getActivatedTasklet(id);
			final IScriptExecutor executor = (IScriptExecutor) activatedTasklet
					.getContext().get(IScriptExecutor.class);
			MPerspective perspective = WorkbenchUtils.getActivePerspective();

			MPartSashContainerElement partSashContainerElement = perspective
					.getChildren().get(0);
			perspective.getChildren().remove(partSashContainerElement);

			MPartSashContainer partSashContainer = MBasicFactory.INSTANCE
					.createPartSashContainer();
			perspective.getChildren().add(partSashContainer);
			partSashContainer.getChildren().add(partSashContainerElement);

			final MPart mPart = MBasicFactory.INSTANCE.createPart();
			mPart.setLabel("Console");
			mPart.setContributionURI("bundleclass://org.gumtree.ui/org.gumtree.ui.tasklet.support.DefaultPart");
			mPart.getProperties().put("partType", "taskletConsole");
			partSashContainer.getChildren().add(mPart);

			// Wait until widget is ready and then create console
			Job job = new Job("Find widget") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					if (mPart.getWidget() == null) {
						schedule(100);
					} else {
						// Create console
						SafeUIRunner
								.asyncExec(new ParameterizedSafeRunnable<MPart>(
										mPart) {
									@Override
									public void run(MPart mPart)
											throws Exception {
										Composite parent = (Composite) ((Composite) mPart
												.getWidget()).getChildren()[0];
										IScriptConsole console = new ScriptConsole(
												parent, SWT.NONE);
										IEclipseContext eclipseContext = Activator
												.getDefault()
												.getEclipseContext()
												.createChild();
										eclipseContext
												.set(IScriptExecutor.class,
														executor);
										ContextInjectionFactory.inject(console,
												eclipseContext);
										parent.getParent().layout(true, true);
									}
								});
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}

	}

	protected ITreeNode[] createTreeNode(String filter, boolean isHierarchical) {
		List<ITreeNode> treeNodes = new ArrayList<ITreeNode>(2);
		for (ITasklet tasklet : getTaskletManager().getTasklets()) {
			treeNodes.add(new TaskletTreeNode(tasklet));
		}
		return LambdaCollections.with(treeNodes).toArray(ITreeNode.class);
	}

	public class TaskletTreeNode extends TreeNode {
		private ITasklet tasklet;

		public TaskletTreeNode(ITasklet tasklet) {
			this.tasklet = tasklet;
		}

		public String getText() {
			return getTasklet().getLabel();
		}

		public Image getImage() {
			return InternalImage.TASKLET_16.getImage();
		}

		public ITasklet getTasklet() {
			return tasklet;
		}
	}

	private class DropTargetListener extends DropTargetAdapter {
		@Override
		public void drop(DropTargetEvent event) {
			if (FileTransfer.getInstance().isSupportedType(
					event.currentDataType)) {
				// 1. Handle dropping from file system
				String[] filenames = (String[]) event.data;
				if (filenames.length == 1) {
					File file = new File(filenames[0]);
					handleLaunchAddTaskletDialog(file.toURI().toString());
				}
			} else if (EditorInputTransfer.getInstance().isSupportedType(
					event.currentDataType)
					&& event.data instanceof EditorInputData[]) {
				// 2. Handle dropping from remote system explorer
				EditorInputData[] inputDatas = ((EditorInputData[]) event.data);
				if (inputDatas.length == 1) {
					IEditorInput input = inputDatas[0].input;
					if (input instanceof FileEditorInput) {
						IFile file = ((FileEditorInput) input).getFile();
						handleLaunchAddTaskletDialog(file.getLocationURI()
								.toString());
					}
				}
			} else if (LocalSelectionTransfer.getTransfer().isSupportedType(
					event.currentDataType)
					&& event.data instanceof IStructuredSelection) {
				// 3. Handle dropping from project explorer
				List<?> files = ((IStructuredSelection) event.data).toList();
				if (files != null && files.size() == 1) {
					if (files.get(0) instanceof IFile) {
						IFile file = (IFile) files.get(0);
						handleLaunchAddTaskletDialog(file.getLocationURI()
								.toString());
					}
				}
			}
		}
	}

	private class UIContext {
		Button closeTaskButton;
		Button showConsoleButton;
		IWorkbenchWindow window;
		IPerspectiveListener perspectiveListener;
		EventHandler eventHandler;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		TaskletManagerViewer viewer = new TaskletManagerViewer(shell, SWT.NONE);

		ITaskletManager manager = new TaskletManager();
		viewer.setTaskletManager(manager);
		ITasklet tasklet = new Tasklet();
		tasklet.setLabel("1D Scan");
		tasklet.setTags("experiment");
		manager.addTasklet(tasklet);
		tasklet = new Tasklet();
		tasklet.setLabel("Histgram Memory");
		tasklet.setTags("control, status");
		manager.addTasklet(tasklet);

		viewer.render();

		shell.setSize(500, 500);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
