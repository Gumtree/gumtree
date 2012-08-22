package org.gumtree.ui.tasklet.support;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.tasklet.IActivatedTasklet;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.ui.tasklet.ITaskletManager;
import org.gumtree.ui.util.SafeUIRunner;

@SuppressWarnings("restriction")
public class ActivatedTasklet implements IActivatedTasklet {

	private String id;

	private ITasklet tasklet;

	private ITaskletManager taskletManager;

	private Composite parent;

	private MPerspective mPerspective;

	private IPerspectiveDescriptor perspective;

	private Map<Object, Object> context;

	public ActivatedTasklet(ITasklet tasklet, ITaskletManager taskletManager) {
		id = UUID.randomUUID().toString();
		this.tasklet = tasklet;
		this.taskletManager = taskletManager;
		context = new HashMap<Object, Object>(2);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public ITasklet getTasklet() {
		return tasklet;
	}

	public ITaskletManager getTaskletManager() {
		return taskletManager;
	}

	@Override
	public String getLabel() {
		return getTasklet().getLabel();
	}

	@Override
	public Composite getParentComposite() {
		return parent;
	}

	@Override
	public void setParentComposite(final Composite parent) {
		// Can only be set once
		if (this.parent != null) {
			return;
		}
		this.parent = parent;
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				parent.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						// Remove from workbench model
						PlatformUI.getWorkbench().getPerspectiveRegistry()
								.deletePerspective(perspective);
						// Buggy!!
//						mPerspective.getParent().getChildren()
//								.remove(mPerspective);
						// Deactivate tasklet
						getTaskletManager().deactivatedTasklet(
								ActivatedTasklet.this);
					}
				});
			}
		});
	}

	@Override
	public MPerspective getMPerspective() {
		return mPerspective;
	}

	public void setMPerspective(MPerspective mPerspective) {
		this.mPerspective = mPerspective;
	}

	public void setPerspective(IPerspectiveDescriptor perspective) {
		this.perspective = perspective;
	}

	@Override
	public Map<Object, Object> getContext() {
		return context;
	}

	@Override
	public void disposeObject() {
		id = null;
		tasklet = null;
		parent = null;
		mPerspective = null;
		perspective = null;
		if (context != null) {
			context.clear();
			context = null;
		}
	}

}
