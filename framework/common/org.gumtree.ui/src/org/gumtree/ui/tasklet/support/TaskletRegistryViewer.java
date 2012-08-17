package org.gumtree.ui.tasklet.support;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
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
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.eclipse.ui.part.FileEditorInput;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.ui.tasklet.ITaskletLauncher;
import org.gumtree.ui.tasklet.ITaskletRegistry;
import org.gumtree.ui.util.jface.ITreeNode;
import org.gumtree.ui.util.jface.TreeContentProvider;
import org.gumtree.ui.util.jface.TreeLabelProvider;
import org.gumtree.ui.util.jface.TreeNode;
import org.gumtree.ui.util.workbench.WorkbenchUtils;
import org.gumtree.ui.widgets.ExtendedComposite;

import ch.lambdaj.collection.LambdaCollections;

@SuppressWarnings("restriction")
public class TaskletRegistryViewer extends ExtendedComposite {

	private ITaskletRegistry taskletRegistry;

	private ITaskletLauncher taskletLauncher;

	private UIContext context;

	private MWindow mWindow;

	@Inject
	public TaskletRegistryViewer(Composite parent, @Optional int style) {
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
				launchAddTaskletDialog(null);
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
				if (getTaskletLauncher() != null) {
					// Run tasklet on double click
					TaskletTreeNode node = (TaskletTreeNode) ((IStructuredSelection) treeViewer
							.getSelection()).getFirstElement();
					ITasklet tasklet = node.getTasklet();
					runTasklet(tasklet);
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
				MPerspectiveStack mPerspectiveStack = TaskletUtilities
						.getActiveMPerspectiveStack();
				MPerspective mPerspective = TaskletUtilities
						.getActivePerspective();
				mPerspectiveStack.getChildren().remove(mPerspective);
			}
		});
		context.closeTaskButton.setEnabled(false);

		// Launch console button
		Button showConsoleButton = getWidgetFactory().createButton(this,
				"Show Console", SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(3, 1).applyTo(showConsoleButton);
		showConsoleButton.setEnabled(false);

		// DnD
		Transfer[] transfers = new Transfer[] {
				LocalSelectionTransfer.getTransfer(),
				FileTransfer.getInstance(), EditorInputTransfer.getInstance() };
		treeViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, transfers,
				new DropTargetAdapter() {
					public void drop(DropTargetEvent event) {
						if (FileTransfer.getInstance().isSupportedType(
								event.currentDataType)) {
							// 1. Handle dropping from file system
							String[] filenames = (String[]) event.data;
							if (filenames.length == 1) {
								File file = new File(filenames[0]);
								launchAddTaskletDialog(file.toURI().toString());
							}
						} else if (EditorInputTransfer.getInstance()
								.isSupportedType(event.currentDataType)
								&& event.data instanceof EditorInputData[]) {
							// 2. Handle dropping from remote system explorer
							EditorInputData[] inputDatas = ((EditorInputData[]) event.data);
							if (inputDatas.length == 1) {
								IEditorInput input = inputDatas[0].input;
								if (input instanceof FileEditorInput) {
									IFile file = ((FileEditorInput) input)
											.getFile();
									launchAddTaskletDialog(file
											.getLocationURI().toString());
								}
							}
						} else if (LocalSelectionTransfer.getTransfer()
								.isSupportedType(event.currentDataType)
								&& event.data instanceof IStructuredSelection) {
							// 3. Handle dropping from project explorer
							List<?> files = ((IStructuredSelection) event.data)
									.toList();
							if (files != null && files.size() == 1) {
								if (files.get(0) instanceof IFile) {
									IFile file = (IFile) files.get(0);
									launchAddTaskletDialog(file
											.getLocationURI().toString());
								}
							}
						}
					}
				});

		// Listening to perspective change
		if (PlatformUI.isWorkbenchRunning()) {
			context.window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			context.perspectiveListener = new PerspectiveAdapter() {
				public void perspectiveActivated(IWorkbenchPage page,
						IPerspectiveDescriptor perspective) {
					MPerspective mPerspective = TaskletUtilities
							.getActivePerspective();
					if (mPerspective.getProperties().containsKey("tasklet")) {
						context.closeTaskButton.setEnabled(true);
					} else {
						context.closeTaskButton.setEnabled(false);
					}
				}
			};
			context.window.addPerspectiveListener(context.perspectiveListener);
		}
	}

	@Override
	protected void disposeWidget() {
		if (context != null) {
			if (context.perspectiveListener != null) {
				context.window
						.removePerspectiveListener(context.perspectiveListener);
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

	public ITaskletRegistry getTaskletRegistry() {
		return taskletRegistry;
	}

	@Inject
	public void setTaskletRegistry(ITaskletRegistry taskletRegistry) {
		this.taskletRegistry = taskletRegistry;
	}

	public ITaskletLauncher getTaskletLauncher() {
		return taskletLauncher;
	}

	@Inject
	public void setTaskletLauncher(ITaskletLauncher taskletLauncher) {
		this.taskletLauncher = taskletLauncher;
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

	private void launchAddTaskletDialog(String contributionUri) {
		AddTaskletDialog dialog = new AddTaskletDialog(getShell());
		dialog.setContributionUri(contributionUri);
		dialog.setTaskletRegistry(getTaskletRegistry());
		dialog.open();
	}

	private void runTasklet(ITasklet tasklet) {
		// Create new perspective
		MWindow mWindow = WorkbenchUtils.getActiveMWindow();
		MPerspectiveStack stack = TaskletUtilities
				.getMPerspectiveStack(mWindow);
		MPerspective mPerspective = TaskletUtilities.createMPerspective(stack,
				tasklet.getLabel());
		mPerspective.getProperties().put("tasklet", tasklet.getLabel());
		stack.getChildren().add(mPerspective);
		// Switch to new perspective
		stack.setSelectedElement(mPerspective);
		// Run script
		getTaskletLauncher().launchTasklet(tasklet, mPerspective);
	}

	protected ITreeNode[] createTreeNode(String filter, boolean isHierarchical) {
		List<ITreeNode> treeNodes = new ArrayList<ITreeNode>(2);
		for (ITasklet tasklet : getTaskletRegistry().getTasklets()) {
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

	private class UIContext {
		Button closeTaskButton;
		Button showConsoleButton;
		IWorkbenchWindow window;
		IPerspectiveListener perspectiveListener;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		TaskletRegistryViewer viewer = new TaskletRegistryViewer(shell,
				SWT.NONE);

		ITaskletRegistry registry = new TaskletRegistry();
		viewer.setTaskletRegistry(registry);
		ITasklet tasklet = new Tasklet();
		tasklet.setLabel("1D Scan");
		tasklet.setTags("experiment");
		registry.addTasklet(tasklet);
		tasklet = new Tasklet();
		tasklet.setLabel("Histgram Memory");
		tasklet.setTags("control, status");
		registry.addTasklet(tasklet);

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
