package au.gov.ansto.bragg.nbi.ui.widgets;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.widgets.swt.ExtendedComposite;

public abstract class SicsStatusWidget extends ExtendedComposite {

	private IDataAccessManager dataAccessManager;

	private ISicsManager sicsManager;

	private ISicsProxyListener proxyListener;

	public SicsStatusWidget(Composite parent, int style) {
		super(parent, style);
		proxyListener = new SicsProxyListenerAdapter() {
			public void proxyConnected() {
				bindProxy();
			}

			public void proxyDisconnected() {
				unbindProxy();
			}
		};
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (sicsManager != null) {
					sicsManager.proxy().removeProxyListener(proxyListener);
					sicsManager = null;
				}
				dataAccessManager = null;
				proxyListener = null;
			}
		});
	}

	@PostConstruct
	public void render() {
		renderWidget();
		// Enable UI if necessary
		if (getSicsManager() != null && getSicsManager().proxy().isConnected()) {
			enableWidget();
		}
	}

	protected abstract void renderWidget();

	protected abstract void enableWidget();

	protected abstract void disableWidget();

	protected void bindProxy() {
		// Wait for SICS controller ready
		Job job = new Job("Fetch SICS data") {
			protected IStatus run(IProgressMonitor monitor) {
				// Check if SICS is ready
				if (SicsCore.getSicsController() == null) {
					schedule(500);
					return Status.OK_STATUS;
				}
				// Setup UI
				enableWidget();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	protected void unbindProxy() {
		// Clear value on unbinding
		disableWidget();
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IDataAccessManager getDataAccessManager() {
		return dataAccessManager;
	}

	@Inject
	public void setDataAccessManager(IDataAccessManager dataAccessManager) {
		this.dataAccessManager = dataAccessManager;
	}

	public ISicsManager getSicsManager() {
		return sicsManager;
	}

	@Inject
	public void setSicsManager(ISicsManager sicsManager) {
		// Detach from previous proxy
		if (this.sicsManager != null) {
			this.sicsManager.proxy().removeProxyListener(proxyListener);
		}
		this.sicsManager = sicsManager;
		// Bind proxy now if possible
		if (SicsCore.getDefaultProxy().isConnected()) {
			bindProxy();
		}
		// Setup to handle dynamic connection
		sicsManager.proxy().addProxyListener(proxyListener);
	}

}
