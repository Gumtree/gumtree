package org.gumtree.gumnix.sics.core;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;

import org.gumtree.gumnix.sics.io.ISicsChannelMonitor;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.util.jmx.ExtendedMBeanExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.MBeanExportException;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.support.ObjectNameManager;

public class SicsProxyMonitor implements ISicsProxyMonitor {

	private static final Logger logger = LoggerFactory
			.getLogger(SicsProxyMonitor.class);

	private ExtendedMBeanExporter exporter;
	
	public SicsProxyMonitor() {
		SicsCore.getDefaultProxy().addProxyListener(
				new SicsProxyListenerAdapter() {
					public void proxyConnected() {
						registerConnectionMonitors();
					}

					public void proxyDisconnected() {
						unregisterConnectionMonitors();
					}
				});
	}

	public String getProxyStatus() {
		return SicsCore.getDefaultProxy().getProxyState().toString();
	}

	public String getCurrentRole() {
		return SicsCore.getDefaultProxy().getCurrentRole().toString();
	}
	
	private void registerConnectionMonitors() {
		for (ISicsChannelMonitor monitor : SicsCore.getDefaultProxy()
				.getChannelMonitors()) {
			try {
				getExporter().registerManagedResource(
						monitor,
						ObjectNameManager
								.getInstance("org.gumtree.gumnix.sics:type=SicsChannel,name="
										+ monitor.getChannelId()));
			} catch (MBeanExportException e) {
				logger.error("Failed to export mbean.", e);
			} catch (MalformedObjectNameException e) {
				logger.error("Failed to create name for mbean.", e);
			}
		}
	}

	private void unregisterConnectionMonitors() {
		MBeanExporter exporter = getExporter();
		for (ISicsChannelMonitor monitor : SicsCore.getDefaultProxy()
				.getChannelMonitors()) {
			try {
				exporter.getServer()
						.unregisterMBean(
								ObjectNameManager
										.getInstance("org.gumtree.gumnix.sics:type=SicsChannel,name="
												+ monitor.getChannelId()));
			} catch (MBeanRegistrationException e) {
				logger.warn("Failed to unregister mbean,", e);
			} catch (InstanceNotFoundException e) {
				logger.warn("Failed to unregister mbean,", e);
			} catch (MalformedObjectNameException e) {
				logger.warn("Failed to unregister mbean,", e);
			}
		}
	}
	
	private ExtendedMBeanExporter getExporter() {
		if (exporter == null) {
			exporter = new ExtendedMBeanExporter();
			exporter.afterPropertiesSet();
		}
		return exporter;
	}

	@Override
	public String getRegistrationKey() {
		return "org.gumtree.gumnix.sics:type=SicsProxyMonitor";
	}

}
