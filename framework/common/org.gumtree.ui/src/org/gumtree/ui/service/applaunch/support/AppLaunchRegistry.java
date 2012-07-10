package org.gumtree.ui.service.applaunch.support;

import static org.gumtree.ui.service.applaunch.support.AppLaunchRegistryConstants.EXTENTION_POINT_APP_LAUNCHES;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.gumtree.ui.service.applaunch.IAppLaunchDescriptor;
import org.gumtree.ui.service.applaunch.IAppLaunchRegistry;
import org.gumtree.util.eclipse.EclipseUtils;

public class AppLaunchRegistry implements IAppLaunchRegistry, IExtensionChangeHandler {

	private AppLaunchRegistryReader reader;
	
	private Set<org.gumtree.ui.service.applaunch.IAppLaunchDescriptor> descriptors;
	
	public AppLaunchRegistry() {
		super();
		descriptors = new HashSet<IAppLaunchDescriptor>();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENTION_POINT_APP_LAUNCHES);
		EclipseUtils.getExtensionTracker().registerHandler(this,
				ExtensionTracker.createExtensionPointFilter(extensionPoint));
	}
	
	private void checkReader() {
		// Use lazy instantiation to avoid spending time on reading
		// the registry in the constructor
		// (help to improve Spring bean creation time)
		if (reader == null) {
			synchronized (this) {
				if (reader == null) {
					reader = new AppLaunchRegistryReader(this);
					reader.readAppLaunches();
				}
			}
		}
	}
	
	protected void addAppLaunchDescriptor(IAppLaunchDescriptor descriptor) {
		descriptors.add(descriptor);
	}
	
	@Override
	public IAppLaunchDescriptor[] getAllAppLaunches() {
		checkReader();
		return descriptors.toArray(new IAppLaunchDescriptor[descriptors.size()]);
	}
	
	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		checkReader();
		synchronized (this) {
			IConfigurationElement[] addedElements = extension.getConfigurationElements();
			for(IConfigurationElement element : addedElements) {
				reader.readElement(element);
			}
		}
	}

	@Override
	public void removeExtension(IExtension extension, Object[] objects) {
		synchronized (this) {
			for(Object object : objects) {
				if(object instanceof IAppLaunchDescriptor) {
					descriptors.remove(object);
				}
			}
		}
	}

}
