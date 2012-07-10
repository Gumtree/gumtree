package org.gumtree.ui.widgets;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.core.object.IConfigurable;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.util.forms.FormControlWidget;
import org.gumtree.util.collection.IParameters;

public abstract class DataDisplayWidget extends FormControlWidget implements IConfigurable {

	public static final String MODE_PUSH = "push";
	
	public static final String MODE_PULL = "pull";
	
	private IDataAccessManager dam;
	
	private float refreshDelay;
	
	private IParameters parameters;
	
	private String mode;
	
	private volatile Job job;
	
	public DataDisplayWidget(Composite parent, int style) {
		super(parent, style);
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dam = null;
				parameters = null;
				mode = null;
				refreshDelay = 0;
				if (job != null) {
					job.cancel();
					job = null;
				}
			}
		});
	}

	protected IDataAccessManager getDataAccessManager() {
		if (dam == null) {
			dam = ServiceUtils.getService(IDataAccessManager.class);
		}
		return dam;
	}
	
	protected void setDataAccessManager(IDataAccessManager dam) {
		this.dam = dam;
	}
	
	public float getRefreshDelay() {
		return refreshDelay;
	}
	
	public void setRefreshDelay(float second) {
		float oldValue = this.refreshDelay;
		this.refreshDelay = second;
		firePropertyChange("refreshDelay", oldValue, second);
		// Reset scheduler
		if (job != null && !isDisposed()) {
			job.cancel();
			job.schedule(Math.round(getRefreshDelay() * 1000));
		}
	}
	
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		String oldValue = this.mode;
		this.mode = mode;
		firePropertyChange("mode", oldValue, mode);
	}

	public IParameters getParameters() {
		return parameters;
	}
	
	public void setParameters(IParameters parameters) {
		this.parameters = parameters;
	}
	
	public void afterParametersSet() {		
		if (MODE_PUSH.equalsIgnoreCase(getMode())) {
			// Setup pushing
			setupPush();
		}
		// Schedule polling job (default is one off pull if refreshDelay is not set)
		schedulePull();
	}

	private void schedulePull() {
		// Cancel previous job
		if (job != null) {
			job.cancel();
		}
		job = new Job(DataDisplayWidget.class.getName()) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					// Get data
					pullData();
					// Do not want to poll too often,
					// so zero sampling rate means not to poll
					if (getRefreshDelay() > 0 && job != null) {
						synchronized (this) {
							if (job != null) {
								job.schedule(Math.round(getRefreshDelay() * 1000));
							}
						}
					}
				} catch (Exception e) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							"Failed to fetch data.", e);
				}
				return Status.OK_STATUS;
			}			
		};
		job.setSystem(true);
		job.schedule();
	}
	
	protected abstract void pullData();

	protected abstract void setupPush();
	
}
