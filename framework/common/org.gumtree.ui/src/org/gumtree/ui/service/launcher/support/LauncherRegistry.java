package org.gumtree.ui.service.launcher.support;

import static org.gumtree.ui.service.launcher.support.LauncherRegistryConstants.EXTENTION_POINT_LAUNCHERS;
import static org.gumtree.ui.service.launcher.support.LauncherRegistryConstants.NAME_OTHER;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gumtree.ui.service.launcher.ILauncherDescriptor;
import org.gumtree.ui.service.launcher.ILauncherRegistry;
import org.gumtree.util.eclipse.EclipseUtils;

public class LauncherRegistry implements ILauncherRegistry, IExtensionChangeHandler {

	private LauncherRegistryReader reader;
	
	private Map<String, Set<ILauncherDescriptor>> descriptorMap;
	
	private Set<ILauncherDescriptor> quickLaunchers;
	
	private Map<String, String> categoryLabels;
	
	private Map<String, ImageDescriptor> categoryIcons;
	
	public LauncherRegistry() {
		super();
		descriptorMap = new HashMap<String, Set<ILauncherDescriptor>>();
		categoryLabels = new HashMap<String, String>(5);
		categoryIcons = new HashMap<String, ImageDescriptor>(5);
		quickLaunchers = new HashSet<ILauncherDescriptor>(5);
		// Put default category
		categoryLabels.put(ID_CATEGORY_OTHER, NAME_OTHER);
		categoryIcons.put(ID_CATEGORY_OTHER, null);
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENTION_POINT_LAUNCHERS);
		EclipseUtils.getExtensionTracker().registerHandler(this,ExtensionTracker.createExtensionPointFilter(extensionPoint));
	}
	
	private void checkReader() {
		// Use lazy instantiation to avoid spending time on reading
		// the registry in the constructor
		// (help to improve Spring bean creation time)
		if (reader == null) {
			synchronized (this) {
				if (reader == null) {
					reader = new LauncherRegistryReader(this);
					reader.readLaunchers();
				}
			}
		}
	}
	
	protected void addLauncherDescriptor(ILauncherDescriptor descriptor) {
		String categoryId = descriptor.getCategory();
		Set<ILauncherDescriptor> descriptors = descriptorMap.get(categoryId);
		if (descriptors == null) {
			descriptors = new HashSet<ILauncherDescriptor>(2);
			descriptorMap.put(categoryId, descriptors);
		}
		descriptors.add(descriptor);
		// Cache quick launcher
		if (descriptor.isQuickLaunch()) {
			quickLaunchers.add(descriptor);
		}
	}

	protected void addCategory(String id, String name, ImageDescriptor icon) {
		categoryLabels.put(id, name);
		categoryIcons.put(id, icon);
	}
	
	public ILauncherDescriptor[] getLaunchers(String categoryId) {
		checkReader();
		Set<ILauncherDescriptor> descriptors = descriptorMap.get(categoryId);
		// If no descriptor available for this category
		if (descriptors == null) {
			return new ILauncherDescriptor[0];
		}
		ILauncherDescriptor[] array = descriptors.toArray(new ILauncherDescriptor[descriptors.size()]);
		// Sort before return
		Arrays.sort(array, new Comparator<ILauncherDescriptor>() {
			public int compare(ILauncherDescriptor o1, ILauncherDescriptor o2) {
				return o1.getLabel().compareTo(o2.getLabel()); 
			}
		});
		return array;
	}
	
	public ILauncherDescriptor[] getQuickLaunchers() {
		checkReader();
		ILauncherDescriptor[] array = quickLaunchers.toArray(new ILauncherDescriptor[quickLaunchers.size()]);
		// Sort before return
		Arrays.sort(array, new Comparator<ILauncherDescriptor>() {
			public int compare(ILauncherDescriptor o1, ILauncherDescriptor o2) {
				return o1.getLabel().compareTo(o2.getLabel()); 
			}
		});
		return array;
	}
	
	public String[] getCatagoryIds() {
		checkReader();
		return categoryLabels.keySet().toArray(new String[categoryLabels.size()]);
	}
	
	public ImageDescriptor getCategoryIcon(String categoryId) {
		checkReader();
		return categoryIcons.get(categoryId);
	}
	
	public String getCategoryLabel(String categoryId) {
		checkReader();
		return categoryLabels.get(categoryId);
	}
	
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		checkReader();
		synchronized (this) {
			IConfigurationElement[] addedElements = extension.getConfigurationElements();
			for(IConfigurationElement element : addedElements) {
				reader.readElement(element);
			}
		}
	}

	public void removeExtension(IExtension extension, Object[] objects) {
		synchronized (this) {
			for(Object object : objects) {
				if(object instanceof ILauncherDescriptor) {
					// dum .... but can't do it in a better way
					for (Set<ILauncherDescriptor> descriptors : descriptorMap.values()) {
						descriptors.remove(object);
					}
				}
			}
		}
	}

}
