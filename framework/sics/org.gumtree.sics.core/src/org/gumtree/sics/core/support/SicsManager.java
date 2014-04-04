package org.gumtree.sics.core.support;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.gumtree.sics.batch.BatchBufferManager;
import org.gumtree.sics.batch.IBatchBufferManager;
import org.gumtree.sics.control.IServerController;
import org.gumtree.sics.core.ISicsControllerProvider;
import org.gumtree.sics.core.ISicsManager;
import org.gumtree.sics.core.ISicsModelProvider;
import org.gumtree.sics.core.ISicsMonitor;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.support.SicsProxy;
import org.gumtree.sics.util.Activator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

public class SicsManager implements ISicsManager {

	private static final Logger logger = LoggerFactory.getLogger(SicsManager.class);
	
	private volatile ISicsProxy proxy;
	
	private volatile ISicsMonitor monitor;
	
	private volatile ISicsModelProvider modelProvider;
	
	private volatile ISicsControllerProvider controllerProvider;
	
	private volatile IServerController serverController;
	
	private volatile IBatchBufferManager bufferManager;
	
	private IEclipseContext context;
	
	/*************************************************************************
	 * SICS proxy
	 *************************************************************************/
	
	public SicsManager() {
		context = Activator.getDefault().getEclipseContext().createChild("sicsManager");
	}
	
	@Override
	public ISicsProxy getProxy() {
		if (proxy == null) {
			synchronized (this) {
				if (proxy == null) {
					proxy = ContextInjectionFactory.make(SicsProxy.class,
							context);
				}
			}
		}
		return proxy;
	}

	@Override
	public void setProxy(ISicsProxy proxy) {
		this.proxy = proxy;
	}

	/*************************************************************************
	 * SICS monitor
	 *************************************************************************/
	
	@Override
	public ISicsMonitor getMonitor() {
		if (monitor == null) {
			synchronized (this) {
				if (monitor == null) {
					context.set(ISicsProxy.class, getProxy());
					monitor = ContextInjectionFactory.make(SicsMonitor.class,
							context);
				}
			}
		}
		return monitor;
	}

	@Override
	public void setMonitor(ISicsMonitor monitor) {
		this.monitor = monitor;
	}

	/*************************************************************************
	 * SICS model provider
	 *************************************************************************/
	
	@Override
	public ISicsModelProvider getModelProvider() {
		if (modelProvider == null) {
			synchronized (this) {
				if (modelProvider == null) {
					context.set(ISicsProxy.class, getProxy());
					modelProvider = ContextInjectionFactory.make(
							SicsModelProvider.class, context);
				}
			}
		}
		return modelProvider;
	}

	@Override
	public void setModelProvider(ISicsModelProvider modelProvider) {
		this.modelProvider = modelProvider;
	}

	/*************************************************************************
	 * SICS controller provider
	 *************************************************************************/
	
	@Override
	public ISicsControllerProvider getSicsControllerProvider() {
		if (controllerProvider == null) {
			synchronized (this) {
				if (controllerProvider == null) {
					context.set(ISicsProxy.class, getProxy());
					context.set(ISicsMonitor.class, getMonitor());
					context.set(ISicsModelProvider.class, getModelProvider());
					controllerProvider = ContextInjectionFactory.make(
							SicsControllerProvider.class, context);
				}
			}
		}
		return controllerProvider;
	}

	@Override
	public void setSicsControllerProvider(
			ISicsControllerProvider controllerProvider) {
		this.controllerProvider = controllerProvider;
	}

	@Override
	public IServerController getServerController() {
		if (serverController == null) {
			synchronized (this) {
				if (serverController == null) {
					serverController = getSicsControllerProvider().createServerController();
				}
			}
		}
		return serverController;
	}

	@Override
	public void disposeObject() {
		if (context != null) {
			context.dispose();
			context = null;
		}
		proxy = null;
		monitor = null;
		modelProvider = null;
		controllerProvider = null;
		serverController = null;
		logger.info("SicsManager has been disposed.");
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("proxy", getProxy()).toString();
	}

	/**
	 * @return the bufferManager
	 */
	public IBatchBufferManager getBufferManager() {
		if (bufferManager == null) {
			synchronized (this) {
				if (bufferManager == null) {
					bufferManager = new BatchBufferManager(this);
					bufferManager.setProxy(getProxy());
				}
			}
		}
		return bufferManager;
	}

	/**
	 * @param bufferManager the bufferManager to set
	 */
	public void setBufferManager(IBatchBufferManager bufferManager) {
		this.bufferManager = bufferManager;
	}
	
}
