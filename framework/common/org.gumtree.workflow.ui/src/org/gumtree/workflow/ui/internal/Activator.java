package org.gumtree.workflow.ui.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gumtree.core.service.IServiceRegistrationManager;
import org.gumtree.core.service.ServiceRegistrationManager;
import org.gumtree.workflow.ui.internal.util.TaskRegistry;
import org.gumtree.workflow.ui.internal.util.WorkflowExecutor;
import org.gumtree.workflow.ui.internal.util.WorkflowManager;
import org.gumtree.workflow.ui.internal.util.WorkflowRegistry;
import org.gumtree.workflow.ui.util.ITaskRegistry;
import org.gumtree.workflow.ui.util.IWorkflowExecutor;
import org.gumtree.workflow.ui.util.IWorkflowManager;
import org.gumtree.workflow.ui.util.IWorkflowRegistry;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.gumtree.workflow.ui";
	
	// The shared instance
	private static Activator plugin;
	
	// Service registry helper
	private IServiceRegistrationManager serviceRegistrationManager;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		serviceRegistrationManager = new ServiceRegistrationManager(context);
		// Register workflow manager service
		serviceRegistrationManager.registerService(IWorkflowManager.class, new WorkflowManager());
		// Register workflow register service
		serviceRegistrationManager.registerService(IWorkflowRegistry.class, new WorkflowRegistry());		
		// Register workflow executor service
		serviceRegistrationManager.registerService(IWorkflowExecutor.class, new WorkflowExecutor());
		// Register task registry service
		serviceRegistrationManager.registerService(ITaskRegistry.class, new TaskRegistry());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (InternalImage.isInstalled()) {
			InternalImage.dispose();
		}
		if (serviceRegistrationManager != null) {
			serviceRegistrationManager.disposeObject();
			serviceRegistrationManager = null;
		}
		plugin = null;
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
