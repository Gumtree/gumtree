package org.gumtree.workflow.ui.internal.util;

import static org.gumtree.workflow.ui.internal.util.WorkflowRegistryConstants.EXTENTION_POINT_WORKFLOWS;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.gumtree.util.eclipse.EclipseUtils;
import org.gumtree.workflow.ui.util.IWorkflowDescriptor;
import org.gumtree.workflow.ui.util.IWorkflowRegistry;

public class WorkflowRegistry implements IWorkflowRegistry, IExtensionChangeHandler {

	private volatile WorkflowRegistryReader reader;
	
	private Map<String, IWorkflowDescriptor> descriptorMap;
	
	private Map<String, String> categoryName;
	
	public WorkflowRegistry() {
		super();
		descriptorMap = new HashMap<String, IWorkflowDescriptor>();
		categoryName = new HashMap<String, String>();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENTION_POINT_WORKFLOWS);
		EclipseUtils.getExtensionTracker().registerHandler(this,ExtensionTracker.createExtensionPointFilter(extensionPoint));
	}
	
	protected void addWorkflowDescriptor(IWorkflowDescriptor descriptor) {
		descriptorMap.put(descriptor.getId(), descriptor);
	}
	
	protected void addCategory(String id, String name) {
		categoryName.put(id, name);
	}
	
	public IWorkflowDescriptor getDescriptor(String id) {
		checkReader();
		return descriptorMap.get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflowRegistry#getDescriptors()
	 */
	public IWorkflowDescriptor[] getDescriptors() {
		checkReader();
		return descriptorMap.values().toArray(new IWorkflowDescriptor[descriptorMap.size()]);
	}
	
	// To reduce delay on class construction, we do not read the extension registry in constructor.
	// However, to make sure stuff in extension register is read before using this registry,
	// some methods (mainly those get methods) need to call this before hand.
	private void checkReader() {
		if (reader == null) {
			synchronized (this) {
				if (reader == null) {
					reader = new WorkflowRegistryReader(this);
					reader.readWorkflows();
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflowRegistry#getShortcutDescriptors()
	 */
//	public IWorkflowDescriptor[] getShortcutDescriptors() {
//		checkReader();
//		Set<IWorkflowDescriptor> shortcutDescriptors = new HashSet<IWorkflowDescriptor>();
//		for (IWorkflowDescriptor descriptor : getDescriptors()) {
//			if (descriptor.isShortcut()) {
//				shortcutDescriptors.add(descriptor);
//			}
//		}
//		return shortcutDescriptors.toArray(new IWorkflowDescriptor[shortcutDescriptors.size()]);
//	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamichelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		checkReader();
		synchronized (this) {
			IConfigurationElement[] addedElements = extension.getConfigurationElements();
			for(IConfigurationElement element : addedElements) {
				reader.readElement(element);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
	 */
	public void removeExtension(IExtension extension, Object[] objects) {
		synchronized (this) {
			for(Object object : objects) {
				if(object instanceof IWorkflowDescriptor) {
					descriptorMap.remove(((IWorkflowDescriptor) object).getId());
				}
			}
		}
	}
						
}
