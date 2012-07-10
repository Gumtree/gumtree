package org.gumtree.util.jmx;

import java.util.Arrays;

import org.gumtree.core.internal.Activator;
import org.gumtree.core.management.IManageableBean;
import org.gumtree.core.management.IManageableBeanProvider;
import org.gumtree.core.service.IServiceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * @author Tony Lam
 * @since 1.7
 */
public class MBeanDiscoverer implements IMBeanDiscoverer {

	private IServiceManager serviceManager;

	private ServiceListener providerServiceListener;

	private ServiceListener beanServiceListener;

	public MBeanDiscoverer() {
	}

	@Override
	public void start() {
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		discoverProviders();
		// }
		// }, "MBean Provider Discoverer").start();
	}

	private void discoverProviders() {
		ExtendedMBeanExporter exporter = new ExtendedMBeanExporter();
		exporter.setBeanProviders(getServiceManager().getServices(
				IManageableBeanProvider.class));
		exporter.setBeans(getServiceManager()
				.getServices(IManageableBean.class));
		exporter.afterPropertiesSet();
		final BundleContext context = Activator.getContext();
		providerServiceListener = new ServiceListener() {
			@Override
			public void serviceChanged(ServiceEvent event) {
				if (event.getType() == ServiceEvent.REGISTERED) {
					ServiceReference<?> ref = event.getServiceReference();
					ExtendedMBeanExporter exporter = new ExtendedMBeanExporter();
					exporter.setBeanProviders(Arrays
							.asList(new IManageableBeanProvider[] { (IManageableBeanProvider) context
									.getService(ref) }));
					exporter.afterPropertiesSet();
				}
				// TODO: remove unregistered service from JMX
			}
		};
		beanServiceListener = new ServiceListener() {
			@Override
			public void serviceChanged(ServiceEvent event) {
				if (event.getType() == ServiceEvent.REGISTERED) {
					ServiceReference<?> ref = event.getServiceReference();
					ExtendedMBeanExporter exporter = new ExtendedMBeanExporter();
					exporter.setBeans(Arrays
							.asList(new IManageableBean[] { (IManageableBean) context
									.getService(ref) }));
					exporter.afterPropertiesSet();
				}
				// TODO: remove unregistered service from JMX
			}
		};
		try {
			Activator.getContext().addServiceListener(
					providerServiceListener,
					"(" + Constants.OBJECTCLASS + "="
							+ IManageableBeanProvider.class.getName() + ')');
			Activator.getContext().addServiceListener(
					beanServiceListener,
					"(" + Constants.OBJECTCLASS + "="
							+ IManageableBean.class.getName() + ')');
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void disposeObject() {
		if (providerServiceListener != null && Activator.getContext() != null) {
			Activator.getContext().removeServiceListener(
					providerServiceListener);
			providerServiceListener = null;
		}
		if (beanServiceListener != null && Activator.getContext() != null) {
			Activator.getContext().removeServiceListener(beanServiceListener);
			beanServiceListener = null;
		}
	}

	public IServiceManager getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(IServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

}
